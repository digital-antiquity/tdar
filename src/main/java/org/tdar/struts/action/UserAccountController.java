package org.tdar.struts.action;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.interceptor.annotation.CacheControl;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Manages web requests for CRUD-ing user accounts, providing account management
 * functionality.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@ParentPackage("secured")
@Namespace("/account")
@Component
@Scope("prototype")
/* not sure this is needed */
// @InterceptorRef("paramsPrepareParamsStack")
// @Result(name = "new", type = "redirect", location = "new")
@HttpsOnly
@CacheControl
public class UserAccountController extends AbstractRegistrationController implements Preparable {

    private static final long serialVersionUID = 1147098995283237748L;

    public static final long ONE_HOUR_IN_MS = 3_600_000;

    private Long personId;
    private String url;
    private String passwordResetURL;

    @Autowired
    private transient RecaptchaService reCaptchaService;

    @Autowired
    private EntityService entityService;

    private String reCaptchaText;

    private String reminderEmail;


    // interceptorRefs = @InterceptorRef("basicStack"),
    @Action(value = "new",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = "success", location = "edit.ftl"),
                    @Result(name = "authenticated", type = "redirect", location = URLConstants.DASHBOARD) })
    @SkipValidation
    @Override
    @HttpsOnly
    public String execute() {
        if (isAuthenticated()) {
            return "authenticated";
        }

        if (StringUtils.isNotBlank(TdarConfiguration.getInstance().getRecaptchaPrivateKey())) {
            setH(new AntiSpamHelper(reCaptchaService));
        }
        return SUCCESS;
    }

    @Action(value = "recover",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = SUCCESS, type = "redirect", location = "${passwordResetURL}") })
    @SkipValidation
    @HttpsOnly
    public String recover() {
        setPasswordResetURL(getAuthenticationAndAuthorizationService().getAuthenticationProvider().getPasswordResetURL());
        return SUCCESS;
    }

    @Action(value = "edit", results = { @Result(name = SUCCESS, type = "redirect", location = "/entity/person/${person.id}/edit") })
    @SkipValidation
    @HttpsOnly
    public String edit() {
        if (isAuthenticated()) {
            return SUCCESS;
        }
        return "new";
    }

    @Action(value = VIEW)
    @SkipValidation
    @HttpsOnly
    public String view() {
        if (!isAuthenticated()) {
            return "new";
        }
        if (getAuthenticatedUser().equals(getPerson())) {
            return SUCCESS;
        }
        getLogger().warn("User {}(id:{}) attempted to access account view page for {}(id:{})", new Object[] { getAuthenticatedUser(),
                getAuthenticatedUser().getId(), getPerson(), personId });
        return UNAUTHORIZED;
    }

    @Action(value = "welcome", results = {
            @Result(name = SUCCESS, location = "view.ftl")
    })
    @SkipValidation
    @HttpsOnly
    public String welcome() {
        if (!isAuthenticated()) {
            return "new";
        }
        setPerson(getAuthenticatedUser());
        personId = getPerson().getId();
        return SUCCESS;
    }

    // FIXME: not implemented yet.
    @Action(value = "reminder",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") }
            , results = { @Result(name = "success", location = "recover.ftl"), @Result(name = "input", location = "recover.ftl") })
    @SkipValidation
    @HttpsOnly
    public String sendNewPassword() {
        Person person = entityService.findByEmail(reminderEmail);
        if (person == null || !(person instanceof TdarUser)) {
            addActionError("Sorry, we didn't find a user with this email.");
            return INPUT;
        }

        // use crowd to handle user management? post to
        // http://dev.tdar.org/crowd/console/forgottenpassword!default.action
        // or just redirect there?
        addActionError("This isn't implemented yet.");
        return SUCCESS;
    }

    @Action(value = "register",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = "success", type = "redirect", location = "welcome"),
                    @Result(name = ADD, type = "redirect", location = "/account/add"),
                    @Result(name = INPUT, location = "edit.ftl"),
                    @Result(name = REDIRECT, type = REDIRECT, location = "${url}")
            })
    @HttpsOnly
    @PostOnly
    @WriteableSession
    @DoNotObfuscate(reason = "getPerson() may have not been set on the session before sent to obfuscator, so don't want to wipe email")
    public String create() {
        if ((getPerson() == null) || !isPostRequest()) {
            return ADD;
        }

        getPerson().setContributorReason(getContributorReason());
        getPerson().setAffiliation(getAffilliation());
        try {
            AuthenticationResult result = getAuthenticationAndAuthorizationService().addAndAuthenticateUser(getPerson(), getPassword(), getInstitutionName(),
                    getServletRequest(), getServletResponse(), getSessionData(), isRequestingContributorAccess());
            if (result.getType().isValid()) {
                setPerson(result.getPerson());
                addActionMessage(getText("userAccountController.successful_registration_message"));
                if (StringUtils.isNotBlank(url)) {
                    return REDIRECT;
                }
                return SUCCESS;
            }

            // pushing error lower for unsuccessful add to CROWD, there could be
            // mulitple reasons for this failure including the fact that the
            // user is already in CROWD
            getLogger().error("Unable to authenticate with the auth service.");
            addActionError(result.toString());
            return ERROR;
        } catch (Throwable t) {
            addActionErrorWithException(getText("userAccountController.could_not_create_account"), t);
        }
        return ERROR;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    @Override
    public void prepare() {
        if (Persistable.Base.isNullOrTransient(personId)) {
            getLogger().debug("prepare: creating new person");
            setPerson(new TdarUser());
        } else {
            getLogger().debug("prepare: loading new person with person id: " + personId);
            setPerson(getGenericService().find(TdarUser.class, personId));
            if (getPerson() == null) {
                getLogger().error("Couldn't load person with id: " + personId);
            }
        }
    }

    public String getPasswordResetURL()
    {
        return passwordResetURL;
    }

    public void setPasswordResetURL(String url)
    {
        this.passwordResetURL = url;
    }

    public boolean isEditable() {
        return getAuthenticatedUser().equals(getPerson())
                || getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_PERSONAL_ENTITES, getAuthenticatedUser());
    }

    public String getReCaptchaText() {
        return reCaptchaText;
    }

    public void setReCaptchaText(String reCaptchaText) {
        this.reCaptchaText = reCaptchaText;
    }

    public String getTosUrl() {
        return getTdarConfiguration().getTosUrl();
    }

    public String getContributorAgreementUrl() {
        return getTdarConfiguration().getContributorAgreementUrl();
    }

    public List<UserAffiliation> getUserAffiliations() {
        return Arrays.asList(UserAffiliation.values());
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReminderEmail() {
        return reminderEmail;
    }

    public void setReminderEmail(String reminderEmail) {
        this.reminderEmail = reminderEmail;
    }


}

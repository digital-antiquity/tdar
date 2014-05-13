package org.tdar.struts.action;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
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
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
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
public class UserAccountController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = 1147098995283237748L;

    public static final long ONE_HOUR_IN_MS = 3_600_000;
    private static final int MAXLENGTH_CONTRIBUTOR = FieldLength.FIELD_LENGTH_512;

    private Long personId;
    private AntiSpamHelper h = new AntiSpamHelper();
    private TdarUser person;
    private String reminderEmail;
    private String confirmEmail;
    private String password;
    private String confirmPassword;
    private boolean requestingContributorAccess;
    private String institutionName;
    private String passwordResetURL;

    @Autowired
    private transient RecaptchaService reCaptchaService;

    @Autowired
    private transient EntityService entityService;

    private String reCaptchaText;

    private String contributorReason;
    private UserAffiliation affilliation;
    
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
        if (getAuthenticatedUser().equals(person)) {
            return SUCCESS;
        }
        getLogger().warn("User {}(id:{}) attempted to access account view page for {}(id:{})", new Object[] { getAuthenticatedUser(),
                getAuthenticatedUser().getId(), person, personId });
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
        person = getAuthenticatedUser();
        personId = person.getId();
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
                    @Result(name = INPUT, location = "edit.ftl") })
    @HttpsOnly
    @PostOnly
    @WriteableSession
    @DoNotObfuscate(reason = "person may have not been set on the session before sent to obfuscator, so don't want to wipe email")
    public String create() {
        if ((person == null) || !isPostRequest()) {
            return ADD;
        }

        person.setContributorReason(contributorReason);
        person.setAffilliation(getAffilliation());
        try {
            AuthenticationResult result = getAuthenticationAndAuthorizationService().addAnAuthenticateUser(person, password, institutionName,
                    getServletRequest(), getServletResponse(), getSessionData(), isRequestingContributorAccess());
            if (result.isValid()) {
                setPerson(result.getPerson());
                getLogger().debug("Authenticated successfully with auth service.");
                entityService.registerLogin(person);

                getAuthenticationAndAuthorizationService().createAuthenticationToken(person, getSessionData());
                addActionMessage(getText("userAccountController.successful_registration_message"));
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

    public boolean isUsernameRegistered(String username) {
        getLogger().trace("testing username:", username);
        if (StringUtils.isBlank(username)) {
            addActionError(getText("userAccountController.error_missing_username"));
            return true;
        }
        TdarUser person = entityService.findByUsername(username);
        return ((person != null) && person.isRegistered());
    }

    @Override
    public void validate() {
        getLogger().trace("calling validate");

        if (person.getUsername() != null) {
            String normalizedUsername = getAuthenticationAndAuthorizationService().normalizeUsername(person.getUsername());
            if (!normalizedUsername.equals(person.getUsername())) {
                getLogger().info("normalizing username; was:{} \t now:{}", person.getUsername(), normalizedUsername);
                person.setUsername(normalizedUsername);
            }

            if (!getAuthenticationAndAuthorizationService().isValidUsername(person.getUsername())) {
                addActionError(getText("userAccountController.username_invalid"));
                return;
            }

            if (!getAuthenticationAndAuthorizationService().isValidEmail(person.getEmail())) {
                addActionError(getText("userAccountController.email_invalid"));
                return;
            }
        }

        if (StringUtils.length(getContributorReason()) > MAXLENGTH_CONTRIBUTOR) {
            // FIXME: should we really be doing this? Or just turn contributorReason into a text field instead?
            getLogger().debug("contributor reason too long");
            addActionError(String.format(getText("userAccountController.could_not_authenticate_at_this_time"), "Contributor Reason", MAXLENGTH_CONTRIBUTOR));
        }
        // FIXME: replace with visitor field validation on Person?
        if (StringUtils.isBlank(person.getFirstName())) {
            addActionError(getText("userAccountController.enter_first_name"));
        }
        if (StringUtils.isBlank(person.getLastName())) {
            addActionError(getText("userAccountController.enter_last_name"));
        }

        // validate email + confirmation
        if (isUsernameRegistered(person.getUsername())) {
            getLogger().debug("username was already registered: ", person.getUsername());
            addActionError(getText("userAccountController.error_username_already_registered"));
        } else if (StringUtils.isBlank(getConfirmEmail())) {
            addActionError(getText("userAccountController.error_confirm_email"));
        } else if (!new EqualsBuilder().append(person.getEmail(), getConfirmEmail()).isEquals()) {
            addActionError(getText("userAccountController.error_emails_dont_match"));
        }
        // validate password + confirmation
        if (StringUtils.isBlank(password)) {
            addActionError(getText("userAccountController.error_choose_password"));
        } else if (StringUtils.isBlank(confirmPassword)) {
            addActionError(getText("userAccountController.error_confirm_password"));
        } else if (!new EqualsBuilder().append(password, confirmPassword).isEquals()) {
            addActionError(getText("userAccountController.error_passwords_dont_match"));
        }

        checkForSpammers();
    }

    /**
     * 
     */
    private boolean checkForSpammers() {
        // SPAM CHECKING
        // 1 - check for whether the "bogus" comment field has data
        // 2 - check whether someone is adding characters that should not be there
        // 3 - check for known spammer - fname == lname & phone = 123456

        try {
            getH().setPerson(getPerson());
            getH().checkForSpammers();
        } catch (TdarRecoverableRuntimeException tre) {
            addActionError(tre.getMessage());
            return true;
        }
        return false;
    }

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser person) {
        this.person = person;
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(String email) {
        // matching case as CROWD & getEmail lower cases everything
        this.confirmEmail = email.toLowerCase();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    @Override
    public void prepare() {
        if (Persistable.Base.isNullOrTransient(personId)) {
            getLogger().debug("prepare: creating new person");
            person = new TdarUser();
        } else {
            getLogger().debug("prepare: loading new person with person id: " + personId);
            person = getGenericService().find(TdarUser.class, personId);
            if (person == null) {
                getLogger().error("Couldn't load person with id: " + personId);
            }
        }
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institution) {
        this.institutionName = institution;
    }

    public String getReminderEmail() {
        return reminderEmail;
    }

    public void setReminderEmail(String reminderEmail) {
        this.reminderEmail = reminderEmail;
    }

    public boolean isRequestingContributorAccess() {
        return requestingContributorAccess;
    }

    public void setRequestingContributorAccess(boolean requestingContributorAccess) {
        this.requestingContributorAccess = requestingContributorAccess;
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
        return getAuthenticatedUser().equals(person)
                || getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_PERSONAL_ENTITES, getAuthenticatedUser());
    }

    // if form submittal takes too long we assume spambot. expose the timeout value to view layer so that we can make sure
    // actual humans get a form that is never too old while still locking out spambots.
    public long getRegistrationTimeout() {
        return ONE_HOUR_IN_MS;
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

    public String getContributorReason() {
        return contributorReason;
    }

    public void setContributorReason(String contributorReason) {
        this.contributorReason = contributorReason;
    }

    public UserAffiliation getAffilliation() {
        return affilliation;
    }

    public void setAffilliation(UserAffiliation affiliation) {
        this.affilliation = affiliation;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

}

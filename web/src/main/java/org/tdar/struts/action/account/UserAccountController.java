package org.tdar.struts.action.account;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.CacheControl;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Validateable;

/**
 * $Id$
 * 
 * Manages web requests for CRUD-ing user accounts, providing account management
 * functionality.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@ParentPackage("default")
@Namespace("/account")
@Component
@Scope("prototype")
@HttpsOnly
@CacheControl
public class UserAccountController extends AbstractAuthenticatableAction implements Validateable {

    private static final long serialVersionUID = 1147098995283237748L;

    public static final long ONE_HOUR_IN_MS = 3_600_000;

    private String url;
    private String passwordResetURL;
    private Long id;
    private String email;
    
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private EntityService entityService;
    private String reminderEmail;
    private AntiSpamHelper h = new AntiSpamHelper();
    private UserRegistration registration = new UserRegistration(h);
    private List<UserAffiliation> affiliations = UserAffiliation.getUserSubmittableAffiliations();

    public boolean isUsernameRegistered(String username) {
        getLogger().debug("testing username:", username);
        if (StringUtils.isBlank(username)) {
            addActionError(getText("userAccountController.error_missing_username"));
            return true;
        }
        TdarUser person = entityService.findByUsername(username);
        return ((person != null) && person.isRegistered());
    }

    @Actions(value = {
            @Action(value = "new",
                    interceptorRefs = { @InterceptorRef("tdarDefaultStack") },
                    results = {
                            @Result(name = SUCCESS, location = "edit.ftl"),
                            @Result(name = AUTHENTICATED, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.DASHBOARD) }),
            @Action(value = "add",
                    interceptorRefs = { @InterceptorRef("tdarDefaultStack") },
                    results = {
                            @Result(name = SUCCESS, location = "edit.ftl"),
                            @Result(name = AUTHENTICATED, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.DASHBOARD) })
    })
    @SkipValidation
    @Override
    @HttpsOnly
    public String execute() {
        if (isAuthenticated()) {
            return AUTHENTICATED;
        }
        if (id != null) {
            Person person = getGenericService().find(Person.class, id);
            // if our email passed matches... then pre-fill the form.  This helps with fishing of email addresses
            if (StringUtils.equalsIgnoreCase(email, person.getEmail())) {
                getReg().setupFrom(person);
            }
        }
        return SUCCESS;
    }

    @Actions({
            @Action(value = "register",
                    interceptorRefs = { @InterceptorRef("registrationStack") },
                    results = { @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.DASHBOARD),
                            @Result(name = ADD, type = TdarActionSupport.TDAR_REDIRECT, location = "/account/add"),
                            @Result(name = INPUT, location = "edit.ftl") })
    })
    @HttpsOnly
    @PostOnly
    @WriteableSession
    @DoNotObfuscate(reason = "getPerson() may have not been set on the session before sent to obfuscator, so don't want to wipe email")
    public String create() {
        if (registration == null || registration.getPerson() == null) {
            return INPUT;
        }
        try {
            AuthenticationResult result = authenticationService.addAndAuthenticateUser(registration, getServletRequest(), getServletResponse(),
                    getSessionData());
            if (result.getType().isValid()) {
                registration.setPerson(result.getPerson());
                addActionMessage(getText("userAccountController.successful_registration_message"));
                return TdarActionSupport.SUCCESS;
            }
        } catch (Throwable e) {
            addActionError(e.getLocalizedMessage());
            return TdarActionSupport.INPUT;
        }
        return TdarActionSupport.INPUT;
    }

    public String getPasswordResetURL() {
        return passwordResetURL;
    }

    public void setPasswordResetURL(String url) {
        this.passwordResetURL = url;
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

    public UserRegistration getRegistration() {
        return registration;
    }

    public UserRegistration getReg() {
        return registration;
    }

    public void setRegistration(UserRegistration registration) {
        this.registration = registration;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    // if form submittal takes too long we assume spambot. expose the timeout value to view layer so that we can make sure
    // actual humans get a form that is never too old while still locking out spambots.
    public long getRegistrationTimeout() {
        return ONE_HOUR_IN_MS;
    }

    @Override
    public void validate() {
        getLogger().debug("validating registration request");
        ErrorTransferObject errors = registration.validate(authenticationService, getServletRequest().getRemoteHost());
        processErrorObject(errors);
    }

    public List<UserAffiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<UserAffiliation> affiliations) {
        this.affiliations = affiliations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

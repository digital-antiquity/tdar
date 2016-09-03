package org.tdar.struts.action.account;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.CacheControl;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;

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
public class AccountRemindResetController extends AbstractAuthenticatableAction  {

    private static final long serialVersionUID = 1147098995283237748L;

    public static final long ONE_HOUR_IN_MS = 3_600_000;

    private String url;
    private String passwordResetURL;

    @Autowired
    private transient RecaptchaService recaptchaService;

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

    @Action(value = "recover",
            interceptorRefs = { @InterceptorRef("registrationStack") },
            results = { @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = "${passwordResetURL}") })
    @HttpsOnly
    public String recover() {
        setPasswordResetURL(authenticationService.getAuthenticationProvider().getPasswordResetURL());
        return SUCCESS;
    }

    @Action(value = "edit", results = { @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = "/entity/person/${person.id}/edit") })
    @HttpsOnly
    public String edit() {
        if (isAuthenticated()) {
            return SUCCESS;
        }
        return "new";
    }

    // FIXME: not implemented yet.
    @Action(value = "reminder",
            interceptorRefs = { @InterceptorRef("registrationStack") },
            results = { @Result(name = SUCCESS, location = "recover.ftl"), @Result(name = "input", location = "recover.ftl") })
    @HttpsOnly
    public String sendNewPassword() {
        Person person = entityService.findByEmail(reminderEmail);
        if (person == null || !(person instanceof TdarUser)) {
            addActionError(getText("userAccountController.email_invalid"));
            return INPUT;
        }
        addActionError(getText("userAccountController.not_implemented"));
        return SUCCESS;
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

    public List<UserAffiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<UserAffiliation> affiliations) {
        this.affiliations = affiliations;
    }

}

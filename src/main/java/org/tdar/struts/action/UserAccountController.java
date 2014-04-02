package org.tdar.struts.action;

import java.util.Arrays;
import java.util.List;

import net.tanesha.recaptcha.ReCaptcha;

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
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.RecaptchaService;
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

    private static final int MAXLENGTH_CONTRIBUTOR = FieldLength.FIELD_LENGTH_512;
    public static final long ONE_HOUR_IN_MS = 3_600_000;
    public static final long FIVE_SECONDS_IN_MS = 5_000;

    private Long timeCheck;
    private Long personId;
    private Person person;
    private String reminderEmail;
    private String confirmEmail;
    private String password;
    private String confirmPassword;
    private boolean requestingContributorAccess;
    private String institutionName;
    private String comment; // for simple spam protection
    private String passwordResetURL;

    private String recaptcha_challenge_field;
    private String recaptcha_response_field;

    @Autowired
    private RecaptchaService reCaptchaService;
    private String reCaptchaText;

    // interceptorRefs = @InterceptorRef("basicStack"),
    @Action(value = "new",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = "success", location = "edit.ftl"),
                    @Result(name = "authenticated", type = "redirect", location = URLConstants.DASHBOARD) })
    @SkipValidation
    @Override
    @HttpsOnly
    /* disabling because it interacts with @HttpsOnly */
    // @Result(name = "new", type = "redirect", location = "new")
    public String execute() {
        setTimeCheck(System.currentTimeMillis());
        if (isAuthenticated()) {
            return "authenticated";
        }

        if (StringUtils.isNotBlank(TdarConfiguration.getInstance().getRecaptchaPrivateKey())) {
            ReCaptcha recaptcha = reCaptchaService.generateRecaptcha();
            reCaptchaText = recaptcha.createRecaptchaHtml(null, null);
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
        if (!isAuthenticated())
            return "new";
        if (getAuthenticatedUser().equals(person))
            return SUCCESS;
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
        Person person = getEntityService().findByEmail(reminderEmail);
        if (person == null) {
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
    @DoNotObfuscate(reason="person may have not been set on the session before sent to obfuscator, so don't want to wipe email")
    public String create() {
        if (person == null || !isPostRequest()) {
            return ADD;
        }

        checkRecaptcha();

        try {
            AuthenticationResult result = getAuthenticationAndAuthorizationService().addAnAuthenticateUser(person, password, institutionName, getServletRequest(), getServletResponse(), getSessionData(), isRequestingContributorAccess());
            if (result.isValid()) {
                setPerson(result.getPerson());
                getLogger().debug("Authenticated successfully with auth service.");
                getEntityService().registerLogin(person);
                getXmlService().logRecordXmlToFilestore(person);

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

    private void checkRecaptcha() {
        if (StringUtils.isNotBlank(TdarConfiguration.getInstance().getRecaptchaPrivateKey())) {
            final boolean reCaptchaResponse = reCaptchaService.checkResponse(getRecaptcha_challenge_field(), getRecaptcha_response_field());
            if (reCaptchaResponse == false) {
                throw new TdarRecoverableRuntimeException("userAccountController.captcha_not_valid");
            }
        }
    }


    public boolean isUsernameRegistered(String username) {
        getLogger().trace("testing username:", username);
        if (StringUtils.isBlank(username)) {
            addActionError(getText("userAccountController.error_missing_username"));
            return true;
        }
        Person person = getEntityService().findByUsername(username);
        return (person != null && person.isRegistered());
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

        if (StringUtils.length(person.getContributorReason()) > MAXLENGTH_CONTRIBUTOR) {
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
        if (StringUtils.isNotBlank(getComment())) {
            getLogger().debug(String.format("we think this user was a spammer: %s  -- %s", getConfirmEmail(), getComment()));
            addActionError(getText("userAccountController.could_not_authenticate_at_this_time"));
            return true;
        }

        if (getPerson() != null) {
            try {
                if (getPerson().getEmail().endsWith("\\r") ||
                        getPerson().getFirstName().equals(getPerson().getLastName())
                        && getPerson().getPhone().equals("123456")) {
                    getLogger().debug(String.format("we think this user was a spammer: %s  -- %s", getConfirmEmail(), getComment()));
                    addActionError(getText("userAccountController.could_not_authenticate_at_this_time"));
                    return true;
                }
            } catch (NullPointerException npe) {
                // this is ok, just doing a spam check, not validating
            }
        }
        long now = System.currentTimeMillis();

        getLogger().debug("timcheck:{}", getTimeCheck());
        if (getTimeCheck() == null) {
            getLogger().debug("internal time check was null, this should never happen for real users");
            addActionError(getText("userAccountController.could_not_authenticate_at_this_time"));
            return true;
        }

        now -= timeCheck;
        if (now < FIVE_SECONDS_IN_MS || now > ONE_HOUR_IN_MS) {
            getLogger().debug(String.format("we think this user was a spammer, due to the time taken " +
                    "to complete the form field: %s  -- %s", getConfirmEmail(), now));
            addActionError(getText("userAccountController.could_not_authenticate_at_this_time"));
            return true;
        }

        return false;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
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
            person = new Person();
        } else {
            getLogger().debug("prepare: loading new person with person id: " + personId);
            person = getEntityService().find(personId);
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

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the timeCheck
     */
    public Long getTimeCheck() {
        return timeCheck;
    }

    /**
     * @param timeCheck
     *            the timeCheck to set
     */
    public void setTimeCheck(Long timeCheck) {
        this.timeCheck = timeCheck;
    }

    public String getPasswordResetURL()
    {
        return passwordResetURL;
    }

    public void setPasswordResetURL(String url)
    {
        this.passwordResetURL = url;
    }

    public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {
        this.recaptcha_challenge_field = recaptcha_challenge_field;
    }

    public String getRecaptcha_challenge_field() {
        return recaptcha_challenge_field;
    }

    public void setRecaptcha_response_field(String recaptcha_response_field) {
        this.recaptcha_response_field = recaptcha_response_field;
    }

    public String getRecaptcha_response_field() {
        return recaptcha_response_field;
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
}

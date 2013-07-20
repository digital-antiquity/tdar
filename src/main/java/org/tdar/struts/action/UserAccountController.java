package org.tdar.struts.action;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService.AuthenticationStatus;
import org.tdar.struts.interceptor.HttpsOnly;

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

@ParentPackage("default")
@Namespace("/account")
@InterceptorRef("paramsPrepareParamsStack")
@Component
@Scope("prototype")
@Result(name = "new", type = "redirect", location = "new")
public class UserAccountController extends AuthenticationAware.Base implements Preparable {

    public static final String USERNAME_INVALID = "Username invalid, usernames must be at least 5 characters and can only have letters and numbers";
    public static final String EMAIL_INVALID = "Email invalid, usernames must be at least 5 characters and can only have letters and numbers";

    private static final String EMAIL_WELCOME_TEMPLATE = "email-welcome.ftl";

    private static final long serialVersionUID = 1147098995283237748L;

    // FIXME: localize messages
    public static final String SUCCESSFUL_REGISTRATION_MESSAGE = "Thank you for registering! Your registration was processed successfully.";
    public static final String COULD_NOT_AUTHENTICATE_AT_THIS_TIME = "Could not authenticate at this time";
    public static final String ERROR_PASSWORDS_DONT_MATCH = "Please make sure your passwords match.";
    public static final String ERROR_MISSING_EMAIL = "Please enter an email address";
    public static final String ERROR_DUPLICATE_EMAIL = "Email already registered";
    public static final String ERROR_EMAILS_DONT_MATCH = "Please make sure your emails match.";
    public static final String ERROR_CONFIRM_EMAIL = "Please confirm your email to access the site.";
    public static final String ERROR_CONFIRM_PASSWORD = "Please confirm your password to access the site.";
    public static final String ERROR_CHOOSE_PASSWORD = "Please choose a password to access the site.";
    public static final String ERROR_USERNAME_ALREADY_REGISTERED = "This username  is already registered in our system.";
    public static final String ERROR_MAXLENGTH = "The '%s' field accepts a maximum of %s characters.";
    private static final int MAXLENGTH_CONTRIBUTOR = 512;

    public static final long ONE_HOUR_IN_MS = 3600000;
    public static final long FIVE_SECONDS_IN_MS = 5000;

    private static final String ERROR_MISSING_USERNAME = "Please enter a username";

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
    private ContributorRequest contributorRequest;

    // private String recaptcha_challenge_field;
    // private String recaptcha_response_field;
    // private String recaptcha_public_key;
    //
    // @Autowired
    // private ObfuscationService obfuscationService;

    @Action(value = "new", interceptorRefs = @InterceptorRef("basicStack"),
            results = {
                    @Result(name = "success", location = "edit.ftl"),
                    @Result(name = "authenticated", type = "redirect", location = URLConstants.DASHBOARD) })
    @SkipValidation
    @Override
    public String execute() {
        setTimeCheck(System.currentTimeMillis());
        if (isAuthenticated()) {
            return "authenticated";
        }
        return SUCCESS;
    }

    @Action(value = "recover", interceptorRefs = @InterceptorRef("basicStack"),
            results = { @Result(name = SUCCESS, type = "redirect", location = "${passwordResetURL}") })
    @SkipValidation
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

    @Action("view")
    @SkipValidation
    @HttpsOnly
    public String view() {
        if (!isAuthenticated())
            return "new";
        if (getAuthenticatedUser().equals(person))
            return SUCCESS;
        logger.warn("User {}(id:{}) attempted to access account view page for {}(id:{})", new Object[] { getAuthenticatedUser(),
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
    @Action(value = "reminder", results = { @Result(name = "success", location = "recover.ftl"), @Result(name = "input", location = "recover.ftl") })
    @SkipValidation
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

    @Action(value = "register", results = { @Result(name = "success", type = "redirect", location = "welcome"),
            @Result(name = "input", location = "edit.ftl") })
    @HttpsOnly
    public String create() {
        if (person == null || !isPostRequest()) {
            return INPUT;
        }
        try {
            Person findByUsername = getEntityService().findByUsername(person.getUsername());
            // short circut the login process -- if there username and password are registered and valid -- just move on.
            if (Persistable.Base.isNotNullOrTransient(findByUsername)) {
                try {
                    AuthenticationStatus status = getAuthenticationAndAuthorizationService().authenticatePerson(findByUsername.getUsername(), password,
                            getServletRequest(), getServletResponse(),
                            getSessionData());
                    if (status == AuthenticationStatus.AUTHENTICATED) {
                        return SUCCESS;
                    }
                } catch (Exception e) {
                    logger.warn("could not authenticate", e);
                }
            }
            reconcilePersonWithTransient(findByUsername, ERROR_USERNAME_ALREADY_REGISTERED);
            reconcilePersonWithTransient(getEntityService().findByEmail(person.getEmail()), ERROR_DUPLICATE_EMAIL);
            person.setRegistered(true);
            Institution institution = getEntityService().findInstitutionByName(institutionName);
            if (institution == null && !StringUtils.isBlank(institutionName)) {
                institution = new Institution();
                institution.setName(institutionName);
                getEntityService().save(institution);
            }
            person.setInstitution(institution);

            getEntityService().saveOrUpdate(person);
            // after the person has been saved, create a contributor request for
            // them as needed.
            if (isRequestingContributorAccess()) {
                // create an account request for the administrator..
                setContributorRequest(new ContributorRequest());
                getContributorRequest().setApplicant(person);
                // FIXME: eventually, this should only happen after being approved (and giving us money)
                person.setContributor(true);
                getContributorRequest().setContributorReason(person.getContributorReason());
                getContributorRequest().setTimestamp(new Date());
                getEntityService().saveOrUpdate(getContributorRequest());
            }
            // add user to Crowd
            person.setStatus(Status.ACTIVE);
            getEntityService().saveOrUpdate(person);

            getLogger().debug("Trying to add user to auth service...");

            boolean success = getAuthenticationAndAuthorizationService().getAuthenticationProvider().addUser(person, password);
            if (success) {
                sendWelcomeEmail();
                getLogger().info("Added user to auth service successfully.");
            } else {
                // we assume that the add operation failed because user was already in crowd. Common scenario for dev/alpha, but not prod.
                getLogger().error("user {} already existed in auth service.  Not unusual unless it happens in prod context ", person);
            }
            // log person in.
            AuthenticationResult result = getAuthenticationAndAuthorizationService().getAuthenticationProvider().authenticate(getServletRequest(),
                    getServletResponse(), person.getUsername(), password);

            if (result.isValid()) {
                getLogger().debug("Authenticated successfully with auth service.");
                getEntityService().registerLogin(person);
                getAuthenticationAndAuthorizationService().createAuthenticationToken(person, getSessionData());
                addActionMessage(SUCCESSFUL_REGISTRATION_MESSAGE);
                return SUCCESS;
            }

            // pushing error lower for unsuccessful add to CROWD, there could be
            // mulitple reasons for this failure including the fact that the
            // user is already in CROWD
            if (!success) {
                addActionError("a problem occured while trying to create a user");
                return ERROR;
            }
            getLogger().error("Unable to authenticate with the auth service.");
            addActionError(result.toString());
        } catch (Throwable t) {
            logger.debug("authentication error", t);
            addActionErrorWithException("Could not create account", t);
        }
        return ERROR;
    }

    private void sendWelcomeEmail() {
        try {
            String subject = String.format("Welcome to %s", TdarConfiguration.getInstance().getSiteAcronym());
            getEmailService().sendTemplate(EMAIL_WELCOME_TEMPLATE, getWelcomeEmailValues(), subject, person);
        } catch (Exception e) {
            // we don't want to ruin the new user's experience with a nasty error message...
            logger.error("Suppressed error that occured when trying to send welcome email", e);
        }
    }

    protected Map<String, Object> getWelcomeEmailValues() {
        Map<String, Object> result = new HashMap<>();
        final TdarConfiguration config = TdarConfiguration.getInstance();
        result.put("user", person);
        result.put("config", config);
        return result;
    }

    private void reconcilePersonWithTransient(Person person_, String error) {
        if (person_ != null && Persistable.Base.isNullOrTransient(person)) {

            if (person_.isRegistered()) {
                throw new TdarRecoverableRuntimeException(error);
            }

            if (person_.getStatus() != Status.FLAGGED && person_.getStatus() != Status.DELETED && person_.getStatus() != Status.FLAGGED_ACCOUNT_BALANCE) {
                person.setStatus(Status.ACTIVE);
                person_.setStatus(Status.ACTIVE);
            } else {
                logger.error("user is not valid");
                throw new TdarRecoverableRuntimeException(error);
            }

            person.setId(person_.getId());
            person = getEntityService().merge(person);

        }
    }

    public boolean isUsernameRegistered(String username) {
        logger.trace("testing username:", username);
        if (StringUtils.isBlank(username)) {
            addActionError(ERROR_MISSING_USERNAME);
            return true;
        }
        Person person = getEntityService().findByUsername(username);
        return (person != null && person.isRegistered());
    }

    @Override
    public void validate() {
        logger.trace("calling validate");

        if (person != null && person.getUsername() != null) {
            String normalizedUsername = getAuthenticationAndAuthorizationService().normalizeUsername(person.getUsername());
            if (!normalizedUsername.equals(person.getUsername())) {
                logger.info("normalizing username; was:{} \t now:{}", person.getUsername(), normalizedUsername);
                person.setUsername(normalizedUsername);
            }

            if (!getAuthenticationAndAuthorizationService().isValidUsername(person.getUsername())) {
                addActionError(USERNAME_INVALID);
                return;
            }

            if (!getAuthenticationAndAuthorizationService().isValidEmail(person.getEmail())) {
                addActionError(EMAIL_INVALID);
                return;
            }

        }

        if (StringUtils.length(person.getContributorReason()) > MAXLENGTH_CONTRIBUTOR) {
            // FIXME: should we really be doing this? Or just turn contributorReason into a text field instead?
            logger.debug("contributor reason too long");
            addActionError(String.format(ERROR_MAXLENGTH, "Contributor Reason", MAXLENGTH_CONTRIBUTOR));
        }
        // FIXME: replace with visitor field validation on Person?
        if (StringUtils.isBlank(person.getFirstName())) {
            addActionError("Please enter your first name");
        }
        if (StringUtils.isBlank(person.getLastName())) {
            addActionError("Please enter your last name");
        }

        // validate email + confirmation
        if (isUsernameRegistered(person.getUsername())) {
            logger.debug("username was already registered: ", person.getUsername());
            addActionError(ERROR_USERNAME_ALREADY_REGISTERED);
        } else if (StringUtils.isBlank(getConfirmEmail())) {
            addActionError(ERROR_CONFIRM_EMAIL);
        } else if (!new EqualsBuilder().append(person.getEmail(), getConfirmEmail()).isEquals()) {
            addActionError(ERROR_EMAILS_DONT_MATCH);
        }
        // validate password + confirmation
        if (StringUtils.isBlank(password)) {
            addActionError(ERROR_CHOOSE_PASSWORD);
        } else if (StringUtils.isBlank(confirmPassword)) {
            addActionError(ERROR_CONFIRM_PASSWORD);
        } else if (!new EqualsBuilder().append(password, confirmPassword).isEquals()) {
            addActionError(ERROR_PASSWORDS_DONT_MATCH);
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
            logger.debug(String.format("we think this user was a spammer: %s  -- %s", getConfirmEmail(), getComment()));
            addActionError(COULD_NOT_AUTHENTICATE_AT_THIS_TIME);
            return true;
        }

        if (getPerson() != null) {
            try {
                if (getPerson().getEmail().endsWith("\\r") ||
                        getPerson().getFirstName().equals(getPerson().getLastName())
                        && getPerson().getPhone().equals("123456")) {
                    logger.debug(String.format("we think this user was a spammer: %s  -- %s", getConfirmEmail(), getComment()));
                    addActionError(COULD_NOT_AUTHENTICATE_AT_THIS_TIME);
                    return true;
                }
            } catch (NullPointerException npe) {
                // this is ok, just doing a spam check, not validating
            }
        }
        long now = System.currentTimeMillis();

        logger.debug("timcheck:{}", getTimeCheck());
        if (getTimeCheck() == null) {
            logger.debug("internal time check was null, this should never happen for real users");
            addActionError(COULD_NOT_AUTHENTICATE_AT_THIS_TIME);
            return true;
        }

        now -= timeCheck;
        if (now < FIVE_SECONDS_IN_MS || now > ONE_HOUR_IN_MS) {
            logger.debug(String.format("we think this user was a spammer, due to the time taken " +
                    "to complete the form field: %s  -- %s", getConfirmEmail(), now));
            addActionError(COULD_NOT_AUTHENTICATE_AT_THIS_TIME);
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

    public ContributorRequest getContributorRequest() {
        return contributorRequest;
    }

    public void setContributorRequest(ContributorRequest contributorRequest) {
        this.contributorRequest = contributorRequest;
    }

    // public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {
    // this.recaptcha_challenge_field = recaptcha_challenge_field;
    // }
    //
    // public String getRecaptcha_challenge_field() {
    // return recaptcha_challenge_field;
    // }
    //
    // public void setRecaptcha_response_field(String recaptcha_response_field) {
    // this.recaptcha_response_field = recaptcha_response_field;
    // }
    //
    // public String getRecaptcha_response_field() {
    // return recaptcha_response_field;
    // }
    //
    // public void setRecaptcha_public_key(String recaptcha_public_key) {
    // this.recaptcha_public_key = recaptcha_public_key;
    // }
    //
    // public String getRecaptcha_public_key() {
    // return TdarConfiguration.getInstance().getRecaptchaPublicKey();
    // }

    public boolean isEditable() {
        return getAuthenticatedUser().equals(person)
                || getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_PERSONAL_ENTITES, getAuthenticatedUser());
    }

    // if form submittal takes too long we assume spambot. expose the timeout value to view layer so that we can make sure
    // actual humans get a form that is never too old while still locking out spambots.
    public long getRegistrationTimeout() {
        return ONE_HOUR_IN_MS;
    }

}

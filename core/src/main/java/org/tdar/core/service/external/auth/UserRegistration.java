package org.tdar.core.service.external.auth;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;

/**
 * Created by jimdevos on 6/17/14.
 */
public class UserRegistration extends UserAuthData {

    private static final String WELCOME_USER_KEY = "welcome-user";
    private static final String WELCOME_CONTRIBUTOR_KEY = "welcome-user-contributor";
    private static final long serialVersionUID = -378621821868811122L;
    private static final int MAXLENGTH_CONTRIBUTOR = FieldLength.FIELD_LENGTH_512;

    private List<UserAffiliation> affiliations = UserAffiliation.getUserSubmittableAffiliations();
    private String password;
    private String confirmPassword;
    private String institutionName;
    private String contributorReason;
    private String confirmEmail;
    private boolean requestingContributorAccess;
    private boolean acceptTermsOfUse;
    private UserAffiliation affiliation;

    public UserRegistration() {
        this(new AntiSpamHelper());
    }

    public String getPrefix() {
        return "registration.";
    }

    public UserRegistration(AntiSpamHelper h_) {
        AntiSpamHelper h = h_;
        if (h == null) {
            h = new AntiSpamHelper();
        }
        setH(h);
    }

    public ErrorTransferObject validate(AuthenticationService authService, String remoteHost) {

        ErrorTransferObject errors = new ErrorTransferObject();

        if (getPerson().getUsername() != null) {
            String normalizedUsername = authService.normalizeUsername(getPerson().getUsername());
            if (!normalizedUsername.equals(getPerson().getUsername())) {
                getLogger().info("normalizing username; was:{} \t now:{}", getPerson().getUsername(), normalizedUsername);
                getPerson().setUsername(normalizedUsername);
            }

            if (!authService.isValidUsername(getPerson().getUsername())) {
                errors.addFieldError(getPrefix() + "person.username", "userAccountController.username_invalid");
            }

            logger.debug("testing valid email");
            if (!authService.isValidEmail(getPerson().getEmail())) {
                logger.debug("NOT valid email");
                errors.addFieldError(getPrefix() + "person.email", "userAccountController.email_invalid");
            }
        }

        if (!acceptTermsOfUse) {
            errors.addFieldError(getPrefix() + "acceptTermsOfUse", "userAccountController.require_tos");
        }

        // contributorReason
        if (StringUtils.length(getContributorReason()) > MAXLENGTH_CONTRIBUTOR) {
            // FIXME: should we really be doing this? Or just turn contributorReason into a text field instead?
            getLogger().debug("contributor reason too long");
            errors.addFieldError(getPrefix() + "person.contributorReason", "userAccountController.error_reason_maxlength");
        }

        // firstName required
        if (StringUtils.isBlank(getPerson().getFirstName())) {
            errors.addFieldError(getPrefix() + "person.firstName", "userAccountController.enter_first_name");
        }

        // lastName required
        if (StringUtils.isBlank(getPerson().getLastName())) {
            errors.addFieldError(getPrefix() + "person.lastName", "userAccountController.enter_last_name");
        }

        // username required
        if (StringUtils.isBlank(getPerson().getUsername())) {
            errors.addFieldError(getPrefix() + "person.username", "userAccountController.error_missing_username");
        } else {
            TdarUser existingUser = authService.findByUsername(getPerson().getUsername());
            if (existingUser != null && existingUser.isRegistered()) {
                getLogger().debug("username was already registered: {}", getPerson().getUsername());
                errors.addFieldError(getPrefix() + "person.username", "userAccountController.error_username_already_registered");
            }
        }

        // confirmation email required
        if (StringUtils.isBlank(confirmEmail)) {
            errors.addFieldError(getPrefix() + "confirmEmail", "userAccountController.error_confirm_email");

            // email + confirmation email must match
        } else if (!StringUtils.equalsIgnoreCase(getPerson().getEmail(), getConfirmEmail())) {
            errors.getActionErrors().add("userAccountController.error_emails_dont_match");
        }
        // validate password + password-confirmation
        if (StringUtils.isBlank(password)) {
            errors.addFieldError(getPrefix() + "password", "userAccountController.error_choose_password");
        } else if (StringUtils.isBlank(confirmPassword)) {
            errors.addFieldError(getPrefix() + "confirmPassword", "userAccountController.error_confirm_password");
        } else if (!StringUtils.equals(getPassword(), getConfirmPassword())) {
            errors.getActionErrors().add("userAccountController.error_passwords_dont_match");
        }

        return errors;
    }

    /**
     * Returns the message key used for the notification sent to the user upon successful registration
     */
    public final String getWelcomeNewUserMessageKey() {
        if (isRequestingContributorAccess()) {
            return WELCOME_CONTRIBUTOR_KEY;
        } else {
            return WELCOME_USER_KEY;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getContributorReason() {
        return contributorReason;
    }

    public void setContributorReason(String contributorReason) {
        this.contributorReason = contributorReason;
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        this.confirmEmail = confirmEmail;
    }

    public boolean isRequestingContributorAccess() {
        return requestingContributorAccess;
    }

    public void setRequestingContributorAccess(boolean requestingContributorAccess) {
        this.requestingContributorAccess = requestingContributorAccess;
    }

    public boolean isAcceptTermsOfUse() {
        return acceptTermsOfUse;
    }

    public void setAcceptTermsOfUse(boolean acceptTermsOfUse) {
        this.acceptTermsOfUse = acceptTermsOfUse;
    }

    public UserAffiliation getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(UserAffiliation affiliation) {
        this.affiliation = affiliation;
    }

    public List<UserAffiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<UserAffiliation> affiliations) {
        this.affiliations = affiliations;
    }

    public void trace(String header) {
        getLogger().trace("{} : {}", "      username", getPerson().getUsername());
        getLogger().trace("{} : {}", "   affiliation", getAffiliation());
        getLogger().trace("{} : {}", "contrib access", isRequestingContributorAccess());
        getLogger().trace("{} : {}", "contrib reason", getContributorReason());
        getLogger().trace("{} : {}", "       browser", header);
        
    }

    public void setupFrom(Person person) {
        logger.debug("setting from: {}", person);
        this.confirmEmail = person.getEmail();
        this.institutionName = person.getInstitutionName();
        this.getPerson().setFirstName(person.getFirstName());
        this.getPerson().setLastName(person.getLastName());
        this.getPerson().setEmail(person.getEmail());
        
    }
}

package org.tdar.struts.action.auth;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.struts.data.AntiSpamHelper;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

/**
 * Created by jimdevos on 6/17/14.
 */
public class UserRegistration {
    
    private static final int MAXLENGTH_CONTRIBUTOR = FieldLength.FIELD_LENGTH_512;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private AntiSpamHelper h;
    private TdarUser person = new TdarUser();
    private String password;
    private String confirmPassword;
    private String institutionName;
    private String contributorReason;
    private String confirmEmail;
    private boolean requestingContributorAccess;
    private boolean acceptTermsOfUse;
    private UserAffiliation affiliation;
    
    public UserRegistration(RecaptchaService recaptchaService) {
        this.h = new AntiSpamHelper(recaptchaService);
    }

    // private final TextProvider textProvider;
    // private final AuthenticationAndAuthorizationService authService;
    // private final EntityService entityService;

    // public RegistrationInfo(TextProvider textProvider, AuthenticationAndAuthorizationService authService, EntityService entityService) {
    // this.textProvider = textProvider;
    // this.authService = authService;
    // this.entityService = entityService;
    // }

    public List<String> validate(TextProvider textProvider, AuthenticationAndAuthorizationService authService, EntityService entityService) {

        List<String> errors = new ArrayList<>();

        if (person.getUsername() != null) {
            String normalizedUsername = authService.normalizeUsername(person.getUsername());
            if (!normalizedUsername.equals(person.getUsername())) {
                getLogger().info("normalizing username; was:{} \t now:{}", person.getUsername(), normalizedUsername);
                person.setUsername(normalizedUsername);
            }

            if (!authService.isValidUsername(person.getUsername())) {
                errors.add(textProvider.getText("userAccountController.username_invalid"));
            }

            if (!authService.isValidEmail(person.getEmail())) {
                errors.add(textProvider.getText("userAccountController.email_invalid"));
            }
        }

        // contributorReason
        if (StringUtils.length(getContributorReason()) > MAXLENGTH_CONTRIBUTOR) {
            // FIXME: should we really be doing this? Or just turn contributorReason into a text field instead?
            getLogger().debug("contributor reason too long");
            errors.add(String.format(textProvider.getText("userAccountController.could_not_authenticate_at_this_time"), "Contributor Reason",
                    MAXLENGTH_CONTRIBUTOR));
        }

        // firstName required
        if (isBlank(person.getFirstName())) {
            errors.add(textProvider.getText("userAccountController.enter_first_name"));
        }

        // lastName required
        if (isBlank(person.getLastName())) {
            errors.add(textProvider.getText("userAccountController.enter_last_name"));
        }

        // username required
        if (isBlank(person.getUsername())) {
            errors.add(textProvider.getText("userAccountController.error_missing_username"));

            // username must not be claimed
        } else {
            TdarUser existingUser = entityService.findByUsername(person.getUsername());
            if (existingUser != null && existingUser.isRegistered()) {
                getLogger().debug("username was already registered: ", person.getUsername());
                errors.add(textProvider.getText("userAccountController.error_username_already_registered"));
            }
        }

        // confirmation email required
        if (isBlank(confirmEmail)) {
            errors.add(textProvider.getText("userAccountController.error_confirm_email"));

            // email + confirmation email must match
        } else if (!new EqualsBuilder().append(person.getEmail(), getConfirmEmail()).isEquals()) {
            errors.add(textProvider.getText("userAccountController.error_emails_dont_match"));
        }
        // validate password + password-confirmation
        if (isBlank(confirmPassword)) {
            errors.add(textProvider.getText("userAccountController.error_choose_password"));
        } else if (isBlank(getConfirmPassword())) {
            errors.add(textProvider.getText("userAccountController.error_confirm_password"));
        } else if (!new EqualsBuilder().append(getPassword(), getConfirmPassword()).isEquals()) {
            errors.add(textProvider.getText("userAccountController.error_passwords_dont_match"));
        }

        checkForSpammers(textProvider, errors);
        return errors;
    }

    private void checkForSpammers(TextProvider textProvider, List<String> errors) {
        // SPAM CHECKING
        // 1 - check for whether the "bogus" comment field has data
        // 2 - check whether someone is adding characters that should not be there
        // 3 - check for known spammer - fname == lname & phone = 123456
        try {
            getH().setPerson(person);
            getH().checkForSpammers();
        } catch (TdarRecoverableRuntimeException tre) {
            errors.add(textProvider.getText(tre.getMessage()));
        }
    }

    private Logger getLogger() {
        return logger;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser person) {
        this.person = person;
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
}

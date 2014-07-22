package org.tdar.struts.data;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.service.external.AuthenticationService;

import com.opensymphony.xwork2.TextProvider;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Created by jimdevos on 6/17/14.
 */
public class UserRegistration extends UserAuthData {

    private static final long serialVersionUID = -378621821868811122L;
    private static final int MAXLENGTH_CONTRIBUTOR = FieldLength.FIELD_LENGTH_512;

    @SuppressWarnings("unchecked")
    private List<UserAffiliation> affiliations = Arrays.asList(UserAffiliation.values());
    private String password;
    private String confirmPassword;
    private String institutionName;
    private String contributorReason;
    private String confirmEmail;
    private boolean requestingContributorAccess;
    private boolean acceptTermsOfUse;
    private UserAffiliation affiliation;
    private String welcomeNewUserMessageKey = "welcome-user";

    public UserRegistration() {
        this(new AntiSpamHelper());
    }

    public UserRegistration(AntiSpamHelper h) {
        if (h == null) {
            h = new AntiSpamHelper();
        }
        setH(h);
    }

    public List<String> validate(TextProvider textProvider, AuthenticationService authService) {

        List<String> errors = new ArrayList<>();

        if (getPerson().getUsername() != null) {
            String normalizedUsername = authService.normalizeUsername(getPerson().getUsername());
            if (!normalizedUsername.equals(getPerson().getUsername())) {
                getLogger().info("normalizing username; was:{} \t now:{}", getPerson().getUsername(), normalizedUsername);
                getPerson().setUsername(normalizedUsername);
            }

            if (!authService.isValidUsername(getPerson().getUsername())) {
                errors.add(textProvider.getText("userAccountController.username_invalid"));
            }

            if (!authService.isValidEmail(getPerson().getEmail())) {
                errors.add(textProvider.getText("userAccountController.email_invalid"));
            }
        }

        if (!acceptTermsOfUse) {
            errors.add(textProvider.getText("userAccountController.require_tos"));
        }

        // contributorReason
        if (StringUtils.length(getContributorReason()) > MAXLENGTH_CONTRIBUTOR) {
            // FIXME: should we really be doing this? Or just turn contributorReason into a text field instead?
            getLogger().debug("contributor reason too long");
            errors.add(String.format(textProvider.getText("userAccountController.could_not_authenticate_at_this_time"), "Contributor Reason",
                    MAXLENGTH_CONTRIBUTOR));
        }

        // firstName required
        if (isBlank(getPerson().getFirstName())) {
            errors.add(textProvider.getText("userAccountController.enter_first_name"));
        }

        // lastName required
        if (isBlank(getPerson().getLastName())) {
            errors.add(textProvider.getText("userAccountController.enter_last_name"));
        }

        // username required
        if (isBlank(getPerson().getUsername())) {
            errors.add(textProvider.getText("userAccountController.error_missing_username"));

            // username must not be claimed
        } else {
            TdarUser existingUser = authService.findByUsername(getPerson().getUsername());
            if (existingUser != null && existingUser.isRegistered()) {
                getLogger().debug("username was already registered: ", getPerson().getUsername());
                errors.add(textProvider.getText("userAccountController.error_username_already_registered"));
            }
        }

        // confirmation email required
        if (isBlank(confirmEmail)) {
            errors.add(textProvider.getText("userAccountController.error_confirm_email"));

            // email + confirmation email must match
        } else if (!new EqualsBuilder().append(getPerson().getEmail(), getConfirmEmail()).isEquals()) {
            errors.add(textProvider.getText("userAccountController.error_emails_dont_match"));
        }
        // validate password + password-confirmation
        if (isBlank(password)) {
            errors.add(textProvider.getText("userAccountController.error_choose_password"));
        } else if (isBlank(confirmPassword)) {
            errors.add(textProvider.getText("userAccountController.error_confirm_password"));
        } else if (!new EqualsBuilder().append(getPassword(), getConfirmPassword()).isEquals()) {
            errors.add(textProvider.getText("userAccountController.error_passwords_dont_match"));
        }

        checkForSpammers(textProvider, errors, false);
        return errors;
    }

    /**
     * Returns the message key used for the notification sent to the user upon successful registration
     * @return
     */
    public final String getWelcomeNewUserMessageKey() {
        return welcomeNewUserMessageKey;
    }

    public final void setWelcomeNewUserMessageKey(String key) {
        welcomeNewUserMessageKey = key;
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
}

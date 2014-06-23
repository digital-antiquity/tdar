package org.tdar.struts.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.struts.data.AntiSpamHelper;

/**
 * Created by jimdevos on 6/11/14.
 */
public abstract class AbstractRegistrationController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 1712219737471831319L;

    private static final int MAXLENGTH_CONTRIBUTOR = FieldLength.FIELD_LENGTH_512;

    @Autowired
    private transient RecaptchaService recaptchaService;

    private AntiSpamHelper h = new AntiSpamHelper(recaptchaService);

    protected TdarUser person;
    private String password;
    private String confirmPassword;
    private String institutionName;
    @Autowired
    private transient EntityService entityService;
    protected String contributorReason;
    private String confirmEmail;
    private boolean requestingContributorAccess;
    private UserAffiliation affilliation;

    public boolean isUsernameRegistered(String username) {
        getLogger().debug("testing username:", username);
        if (StringUtils.isBlank(username)) {
            addActionError(getText("userAccountController.error_missing_username"));
            return true;
        }
        TdarUser person = entityService.findByUsername(username);
        return ((person != null) && person.isRegistered());
    }

    @Override
    public void validate() {
        getLogger().debug("calling validate");


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
            getH().checkForSpammers(false);
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

    protected String getPassword() {
        return password;
    }

    protected String getConfirmPassword() {
        return confirmPassword;
    }


    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institution) {
        this.institutionName = institution;
    }


    public boolean isRequestingContributorAccess() {
        return requestingContributorAccess;
    }

    public void setRequestingContributorAccess(boolean requestingContributorAccess) {
        this.requestingContributorAccess = requestingContributorAccess;
    }

    // if form submittal takes too long we assume spambot. expose the timeout value to view layer so that we can make sure
    // actual humans get a form that is never too old while still locking out spambots.
    public long getRegistrationTimeout() {
        return UserAccountController.ONE_HOUR_IN_MS;
    }

    public String getContributorReason() {
        return contributorReason;
    }
    public void setContributorReason(String contributorReason) {
        this.contributorReason = contributorReason;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public UserAffiliation getAffilliation() {
        return affilliation;
    }

    public void setAffilliation(UserAffiliation affiliation) {
        this.affilliation = affiliation;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }


}

package org.tdar.core.service.external;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.ValidationAware;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.EntityService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.auth.RegistrationInfo;
import org.tdar.struts.action.auth.RegistrationInfoProvider;

/**
 * Created by jimdevos on 6/17/14.
 */
@Service
public class RegistrationControllerService {

    
    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    @Autowired
    private EntityService entityService;

    private static final int MAXLENGTH_CONTRIBUTOR = FieldLength.FIELD_LENGTH_512;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public AuthenticationAndAuthorizationService getAuthenticationAndAuthorizationService() {
        return authenticationAndAuthorizationService;
    }
    private Logger getLogger() {
        return logger;
    }

    public <A extends RegistrationInfoProvider & ValidationAware & TextProvider> void validateAction(A action) {
        getLogger().debug("calling validate");
        RegistrationInfo reg = action.getRegistrationInfo();
        TdarUser person = action.getRegistrationInfo().getPerson();

        if (person.getUsername() != null) {
            String normalizedUsername = getAuthenticationAndAuthorizationService().normalizeUsername(person.getUsername());
            if (!normalizedUsername.equals(person.getUsername())) {
                getLogger().info("normalizing username; was:{} \t now:{}", person.getUsername(), normalizedUsername);
                person.setUsername(normalizedUsername);
            }

            if (!getAuthenticationAndAuthorizationService().isValidUsername(person.getUsername())) {
                action.addActionError(action.getText("userAccountController.username_invalid"));
                return;
            }

            if (!getAuthenticationAndAuthorizationService().isValidEmail(person.getEmail())) {
                action.addActionError(action.getText("userAccountController.email_invalid"));
                return;
            }
        }

        // contributorReason
        if (StringUtils.length(reg.getContributorReason()) > MAXLENGTH_CONTRIBUTOR) {
            // FIXME: should we really be doing this? Or just turn contributorReason into a text field instead?
            getLogger().debug("contributor reason too long");
            action.addActionError(String.format(action.getText("userAccountController.could_not_authenticate_at_this_time"), "Contributor Reason", MAXLENGTH_CONTRIBUTOR));
        }

        // firstName required
        if (StringUtils.isBlank(person.getFirstName())) {
            action.addActionError(action.getText("userAccountController.enter_first_name"));
        }

        // lastName required
        if (StringUtils.isBlank(person.getLastName())) {
            action.addActionError(action.getText("userAccountController.enter_last_name"));
        }

        // username required
        if (StringUtils.isBlank(person.getUsername())) {
            action.addActionError(action.getText("userAccountController.error_missing_username"));

        // username must not be claimed
        } else {
            TdarUser existingUser = entityService.findByUsername(person.getUsername());
            if(existingUser != null && existingUser.isRegistered()) {
                getLogger().debug("username was already registered: ", person.getUsername());
                action.addActionError(action.getText("userAccountController.error_username_already_registered"));
            }
        }

        // confirmation email required
        if(StringUtils.isBlank(reg.getConfirmEmail())) {
            action.addActionError(action.getText("userAccountController.error_confirm_email"));

        // email + confirmation email must match
        } else if (!new EqualsBuilder().append(person.getEmail(), reg.getConfirmEmail()).isEquals()) {
            action.addActionError(action.getText("userAccountController.error_emails_dont_match"));
        }
        // validate password + password-confirmation
        if (StringUtils.isBlank(reg.getPassword())) {
            action.addActionError(action.getText("userAccountController.error_choose_password"));
        } else if (StringUtils.isBlank(reg.getConfirmPassword())) {
            action.addActionError(action.getText("userAccountController.error_confirm_password"));
        } else if (!new EqualsBuilder().append(reg.getPassword(), reg.getConfirmPassword()).isEquals()) {
            action.addActionError(action.getText("userAccountController.error_passwords_dont_match"));
        }

        checkForSpammers(action);
    }

    private <A extends RegistrationInfoProvider & ValidationAware & TextProvider> void checkForSpammers(A action) {
        // SPAM CHECKING
        // 1 - check for whether the "bogus" comment field has data
        // 2 - check whether someone is adding characters that should not be there
        // 3 - check for known spammer - fname == lname & phone = 123456
        RegistrationInfo reg = action.getRegistrationInfo();
        try {
            reg.getH().setPerson(reg.getPerson());
            reg.getH().checkForSpammers();
        } catch (TdarRecoverableRuntimeException tre) {
            action.addActionError(action.getText(tre.getMessage()));
        }
    }


    public <A extends TdarActionSupport & RegistrationInfoProvider> String executeAction(A action) {
        RegistrationInfo reg = action.getRegistrationInfo();
        reg.getPerson().setAffiliation(reg.getAffilliation());
        AuthenticationResult result = getAuthenticationAndAuthorizationService().addAndAuthenticateUser(reg.getPerson(), reg.getPassword(), reg.getInstitutionName(),
                action.getServletRequest(), action.getServletResponse(), action.getSessionData(), true);
        if (result.getType().isValid()) {
            reg.setPerson(result.getPerson());
            action.addActionMessage(action.getText("userAccountController.successful_registration_message"));
            return TdarActionSupport.SUCCESS;
        } else {
            return TdarActionSupport.INPUT;
        }
    }
    
}

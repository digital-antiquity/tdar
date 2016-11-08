package org.tdar.struts.action.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/user")
public class TdarUserController extends AbstractPersonController<TdarUser> {

    @Autowired
    private transient ObfuscationService obfuscationService;
    @Autowired
    private transient BillingAccountService accountService;
    @Autowired
    private transient AuthenticationService authenticationService;

    private static final long serialVersionUID = -2666270784609372369L;
    private String proxyInstitutionName;
    private boolean passwordResetRequested;
    private String newUsername;
    private String password;
    private String confirmPassword;
    private Boolean contributor;
    private String contributorReason;
    private String proxyNote;
    private List<BillingAccount> accounts;
    private List<String> groups = new ArrayList<String>();

    public static final String MYPROFILE = "myprofile";

    @Autowired
    private transient EntityService entityService;

    @Action(value = MYPROFILE, results = {
            @Result(name = SUCCESS, location = "edit.ftl")
    })
    @HttpsOnly
    @SkipValidation
    public String myProfile() throws TdarActionException {
        return edit();
    }

    public void validateEmailRequiredForActiveUsers() {
        if (getPersistable().isActive() && getPersistable().isRegistered() && StringUtils.isBlank(getEmail())) {
            addFieldError("email", getText("userAccountController.email_required"));
        }
    }

    @Override
    public void prepare() throws TdarActionException {
        if (getCurrentUrl().contains("myprofile")) {
            setId(getAuthenticatedUser().getId());
        }
        super.prepare();
        contributor = getPersistable().isContributor();
        if (getPersistable().getProxyInstitution() != null)  {
            setProxyInstitutionName(getPersistable().getProxyInstitution().getName());
        }
        setProxyNote(getPersistable().getProxyNote());
    }

    @Override
    public void validate() {
        super.validate();
        validateEmailRequiredForActiveUsers();
    }

    @Override
    @Validations(
            emails = { @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "email", key = "userAccountController.email_invalid") },
            stringLengthFields = { @StringLengthFieldValidator(type = ValidatorType.SIMPLE, fieldName = "contributorReason",
                    key = "userAccountController.contributorReason_invalid", maxLength = "512") }
            )
            public String save(TdarUser person) {
        validateAndProcessPasswordChange(); // TODO: this should just be in validate()
        if (validateAndProcessUsernameChange()) {
            // FIXME: logout?
        }

        prepareUserInformation();
        checkForNonContributorCrud();
        savePersonInfo(person);

        if (passwordResetRequested) {
            authenticationService.getAuthenticationProvider().resetUserPassword(person);
        }
        return SUCCESS;
    }

    private void prepareUserInformation() {
        if (StringUtils.isBlank(proxyInstitutionName)) {
            getPersistable().setProxyInstitution(null);
        } else {
            // if the user changed the person's institution, find or create it
            Institution persistentInstitution = entityService.findOrSaveCreator(new Institution(proxyInstitutionName));
            getLogger().debug("setting institution to persistent: " + persistentInstitution);
            getPersistable().setProxyInstitution(persistentInstitution);
        }
        getPersistable().setContributorReason(contributorReason);
        getPersistable().setProxyNote(proxyNote);
        getPersistable().setContributor(contributor);
    }

    // check whether password change was requested and whether it was valid
    private void validateAndProcessPasswordChange() {
        // no change requested
        if (StringUtils.isBlank(password) && StringUtils.isBlank(confirmPassword)) {
            return;
        }
        if (!StringUtils.equals(password, confirmPassword)) {
            // change requested, passwords don't match
            addActionError(getText("userAccountController.error_passwords_dont_match"));
        } else {
            // passwords match, change the password
            authenticationService.getAuthenticationProvider().updateUserPassword((TdarUser) getPerson(), password);
            addActionMessage(getText("personController.password_successfully_changed"));
        }
    }

    // check whether password change was requested and whether it was valid
    private boolean validateAndProcessUsernameChange() {
        // no change requested
        if (StringUtils.isBlank(newUsername)) {
            return false;
        }

        if (StringUtils.isBlank(password)) {
            throw new TdarRecoverableRuntimeException(getText("userAccountController.error_reenter_password_to_change_username"));
        }

        if (!StringUtils.equals(password, confirmPassword)) {
            // change requested, passwords don't match
            addActionError(getText("userAccountController.error_passwords_dont_match"));
        } else {
            // passwords match, change the password
            authenticationService.updateUsername((TdarUser) getPerson(), newUsername, password);
            // FIXME: we currently have no way to indicate success because we are redirecting to success page, So the message below is lost.
            addActionMessage(getText("userAccountController.username_successfully_changed"));
            return true;
        }
        return false;
    }

    @Override
    public Class<TdarUser> getPersistableClass() {
        return TdarUser.class;
    }

    public TdarUser getPerson() {
        TdarUser p = getPersistable();
        if (getTdarConfiguration().obfuscationInterceptorDisabled()) {
            if (!authorize()) {
                obfuscationService.obfuscate(p, getAuthenticatedUser());
            }
        }
        return p;
    }

    public void setPerson(TdarUser person) {
        setPersistable(person);
    }

    public boolean isPasswordResetRequested() {
        return passwordResetRequested;
    }

    public void setPasswordResetRequested(boolean passwordResetRequested) {
        this.passwordResetRequested = passwordResetRequested;
    }

    public boolean isEditingSelf() {
        return getAuthenticatedUser().equals(getPersistable());
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

    @Override
    public String getSaveSuccessPath() {
        // instead of a custom view page we will co-opt the browse/creator page.
        String path = "browse/creators";
        getLogger().debug("{}?id={}", path, getId());
        return path;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUserName) {
        this.newUsername = newUserName;
    }

    public List<BillingAccount> getAccounts() {
        if (accounts == null) {
            accounts = new ArrayList<BillingAccount>();
            accounts.addAll(accountService.listAvailableAccountsForUser(getPersistable(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
        }
        return accounts;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public String getProxyInstitutionName() {
        return proxyInstitutionName;
    }

    public void setProxyInstitutionName(String proxyInstitutionName) {
        this.proxyInstitutionName = proxyInstitutionName;
    }

    public String getProxyNote() {
        return proxyNote;
    }

    public void setProxyNote(String proxyNote) {
        this.proxyNote = proxyNote;
    }

    public Boolean getContributor() {
        return contributor;
    }

    public void setContributor(Boolean contributor) {
        this.contributor = contributor;
    }

    public String getContributorReason() {
        return contributorReason;
    }

    public void setContributorReason(String contributorReason) {
        this.contributorReason = contributorReason;
    }

}

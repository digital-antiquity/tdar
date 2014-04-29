package org.tdar.struts.action.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.statistics.CreatorViewStatistic;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/user")
public class UserInfoController extends AbstractPersonController<TdarUser> {

    private static final long serialVersionUID = -2666270784609372369L;
    private String proxyInstitutionName;
    private boolean passwordResetRequested;
    private String newUsername;
    private String password;
    private String confirmPassword;
    private Boolean contributor;
    private String contributorReason;
    private String proxyNote;
    private List<Account> accounts;
    private List<String> groups = new ArrayList<String>();

    public static final String MYPROFILE = "myprofile";


    @Action(value = MYPROFILE, results = {
            @Result(name = SUCCESS, location = "edit.ftl")
    })
    @HttpsOnly
    @SkipValidation
    public String myProfile() throws TdarActionException {
        setId(getAuthenticatedUser().getId());
        prepare();
        return edit();
    }

    public void validateEmailRequiredForActiveUsers() {
        if (getPersistable().isActive() && getPersistable().isRegistered() && StringUtils.isBlank(getEmail())) {
            addFieldError("email", getText("userAccountController.email_invalid"));
        }
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
        if (hasErrors()) {
            getLogger().info("errors present, returning INPUT");
            getLogger().info("actionErrors:{}", getActionErrors());
            getLogger().info("fieldErrors:{}", getFieldErrors());
            return INPUT;
        }

        if (StringUtils.isBlank(proxyInstitutionName)) {
            getPersistable().setProxyInstitution(null);
        } else {
            // if the user changed the person's institution, find or create it
            Institution persistentInstitution = getEntityService().findOrSaveCreator(new Institution(proxyInstitutionName));
            getLogger().debug("setting institution to persistent: " + persistentInstitution);
            getPersistable().setProxyInstitution(persistentInstitution);
        }
        getPersistable().setContributorReason(contributorReason);
        getPersistable().setProxyNote(proxyNote);
        getPersistable().setContributor(contributor);

        savePersonInfo(person);
        getGenericService().saveOrUpdate(person);
        getXmlService().logRecordXmlToFilestore(getPersistable());

        // If the user is editing their own profile, refresh the session object if needed
        if (getAuthenticatedUser().equals(person)) {
            getSessionData().getAuthenticationToken().setPerson(person);
        }
        if (passwordResetRequested) {
            getAuthenticationAndAuthorizationService().getAuthenticationProvider().resetUserPassword(person);
        }
        return SUCCESS;
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
            getAuthenticationAndAuthorizationService().getAuthenticationProvider().updateUserPassword((TdarUser)getPerson(), password);
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
            getAuthenticationAndAuthorizationService().updateUsername((TdarUser)getPerson(), newUsername, password);
            // FIXME: we currently have no way to indicate success because we are redirecting to success page, So the message below is lost.
            addActionMessage(getText("userAccountController.username_successfully_changed"));
            return true;
        }
        return false;
    }

    @Override
    public boolean isEditable() {
        return getAuthenticatedUser().equals(getPersistable())
                || getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_PERSONAL_ENTITES, getAuthenticatedUser());
    }

    @Override
    protected void delete(TdarUser persistable) {
        getXmlService().logRecordXmlToFilestore(getPersistable());

        // the actual delete is being done by persistableController. We don't delete any relations since we want the operation to fail if any exist.
    }

    @Override
    public Class<TdarUser> getPersistableClass() {
        return TdarUser.class;
    }

    @Override
    public String loadViewMetadata() {
        // nothing to do here, the person record was already loaded by prepare()
        try {
            getGroups().addAll(getAuthenticationAndAuthorizationService().getGroupMembership(getPersistable()));
        } catch (Throwable e) {
            getLogger().error("problem communicating with crowd getting user info for {} ", getPersistable(), e);
        }

        if (!isEditor() && !Persistable.Base.isEqual(getPersistable(), getAuthenticatedUser())) {
            CreatorViewStatistic cvs = new CreatorViewStatistic(new Date(), getPersistable());
            getGenericService().saveOrUpdate(cvs);
        }

        return SUCCESS;
    }

    public TdarUser getPerson() {
        TdarUser p = getPersistable();
        if (getTdarConfiguration().obfuscationInterceptorDisabled()) {
            if (!isEditable()) {
                getObfuscationService().obfuscate(p, getAuthenticatedUser());
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
        String path = "/browse/creators";
        getLogger().debug("{}?id={}", path, getId());
        return path;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUserName) {
        this.newUsername = newUserName;
    }

    public List<Account> getAccounts() {
        if (accounts == null) {
            accounts = new ArrayList<Account>();
            accounts.addAll(getAccountService().listAvailableAccountsForUser(getPersistable(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
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

package org.tdar.struts.action.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ObfuscationService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.UserAccountController;
import org.tdar.struts.interceptor.HttpsOnly;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/person")
public class PersonController extends AbstractCreatorController<Person> {

    public static final String MYPROFILE = "myprofile";

    private static final long serialVersionUID = 1L;

    private String institutionName;
    private String proxyInstitutionName;
    private boolean passwordResetRequested;
    private String newUsername;
    private String password;
    private String confirmPassword;
    private List<Account> accounts;
    private List<String> groups = new ArrayList<String>();

    @Autowired
    ObfuscationService obfuscationService;

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

    @Override
    protected String save(Person person) {
        validateAndProcessPasswordChange();
        if (validateAndProcessUsernameChange()) {
            // FIXME: logout?
        }
        if (hasActionErrors())
            return INPUT;
        logger.debug("saving person: {} with institution {} ", person, institutionName);
        if (StringUtils.isBlank(institutionName)) {
            person.setInstitution(null);
        }
        else {
            // if the user changed the person's institution, find or create it
            Institution persistentInstitution = getEntityService().findOrSaveCreator(new Institution(institutionName));
            logger.debug("setting institution to persistent: " + persistentInstitution);
            person.setInstitution(persistentInstitution);
        }

        if (StringUtils.isBlank(proxyInstitutionName)) {
            person.setProxyInstitution(null);
        }
        else {
            // if the user changed the person's institution, find or create it
            Institution persistentInstitution = getEntityService().findOrSaveCreator(new Institution(proxyInstitutionName));
            logger.debug("setting institution to persistent: " + persistentInstitution);
            person.setProxyInstitution(persistentInstitution);
        }

        getGenericService().saveOrUpdate(person);

        // If the user is editing their own profile, refresh the session object if needed
        if (getAuthenticatedUser().equals(person)) {
            getSessionData().getAuthenticationToken().setPerson(person);
        }
        if (passwordResetRequested) {
            getAuthenticationAndAuthorizationService().getAuthenticationProvider().resetUserPassword(person);
        }
        return SUCCESS;
    }

    @Override
    public boolean isViewable() throws org.tdar.struts.action.TdarActionException {
        if (!isEditable()) {
            throw new TdarActionException(StatusCode.UNAUTHORIZED, "you are not allowed to view/edit this record");
        }
        return true;
    };

    // check whether password change was requested and whether it was valid
    private void validateAndProcessPasswordChange() {
        // no change requested
        if (StringUtils.isBlank(password) && StringUtils.isBlank(confirmPassword))
            return;
        if (!StringUtils.equals(password, confirmPassword)) {
            // change requested, passwords don't match
            addActionError(UserAccountController.ERROR_PASSWORDS_DONT_MATCH);
        } else {
            // passwords match, change the password
            getAuthenticationAndAuthorizationService().getAuthenticationProvider().updateUserPassword(getPerson(), password);
            // FIXME: we currently have no way to indicate success because we are redirecting to success page, So the message below is lost.
            addActionMessage("Password successfully changed");
        }
    }

    // check whether password change was requested and whether it was valid
    private boolean validateAndProcessUsernameChange() {
        // no change requested
        if (StringUtils.isBlank(newUsername))
            return false;

        if (StringUtils.isBlank(password)) {
            throw new TdarRecoverableRuntimeException("you must re-enter your password to change your username");
        }

        if (!StringUtils.equals(password, confirmPassword)) {
            // change requested, passwords don't match
            addActionError(UserAccountController.ERROR_PASSWORDS_DONT_MATCH);
        } else {
            // passwords match, change the password
            getAuthenticationAndAuthorizationService().updateUsername(getPerson(), newUsername, password);
            // FIXME: we currently have no way to indicate success because we are redirecting to success page, So the message below is lost.
            addActionMessage("Username successfully changed, please logout");
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
    protected void delete(Person persistable) {
        // the actual delete is being done by persistableController. We don't delete any relations since we want the operation to fail if any exist.
    }

    @Override
    public Class<Person> getPersistableClass() {
        return Person.class;
    }

    @Override
    public String loadViewMetadata() {
        // nothing to do here, the person record was already loaded by prepare()
        try {
            getGroups().addAll(getAuthenticationAndAuthorizationService().getGroupMembership((Person) getPersistable()));
        } catch (Throwable e) {
            logger.error("problem communicating with crowd getting user info for {} ", getPersistable(), e);
        }
        return SUCCESS;
    }

    public Person getPerson() {
        Person p = getPersistable();
        if (!isEditable()) {
            obfuscationService.obfuscate(p, getAuthenticatedUser());
        }
        return p;
    }

    public void setPerson(Person person) {
        setPersistable(person);
    }

    public boolean isPasswordResetRequested() {
        return passwordResetRequested;
    }

    public void setPasswordResetRequested(boolean passwordResetRequested) {
        this.passwordResetRequested = passwordResetRequested;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    // return true if the persistable==authUser
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

}

package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
public class BillingAccountController extends AbstractPersistableController<Account> {

    public static final String RIGHTS_TO_ASSIGN_THIS_INVOICE = "you do not have the rights to assign this invoice";
    public static final String INVOICE_IS_REQURIED = "an invoice is requried";
    private static final long serialVersionUID = 2912533895769561917L;
    public static final String NEW_ACCOUNT = "new_account";
    private Long invoiceId;
    private Set<Account> accounts;

    private AccountGroup accountGroup;
    private List<Person> authorizedMembers = new ArrayList<Person>();
    private Long accountGroupId;

    @SkipValidation
    @Action(value = "choose", results = {
            @Result(name = SUCCESS, location = "select-account.ftl"),
            @Result(name = NEW_ACCOUNT, location = "add?invoiceId=${invoiceId}", type = "redirect")
    })
    public String selectAccount() throws TdarActionException {
        Invoice invoice = getGenericService().find(Invoice.class, invoiceId);
        if (invoice == null) {
            throw new TdarRecoverableRuntimeException(INVOICE_IS_REQURIED);
        }
        if (!getAuthenticationAndAuthorizationService().canAssignInvoice(invoice, getAuthenticatedUser())) {
            throw new TdarRecoverableRuntimeException(RIGHTS_TO_ASSIGN_THIS_INVOICE);
        }
        setAccounts(getAccountService().listAvailableAccountsForUser(getAuthenticatedUser()));
        if (CollectionUtils.isNotEmpty(getAccounts())) {
            return SUCCESS;
        }
        return NEW_ACCOUNT;
    }

    @Override
    protected String save(Account persistable) {
        logger.info("invoiceId {}", getInvoiceId());
        if (Persistable.Base.isNotNullOrTransient(invoiceId)) {
            Invoice invoice = getGenericService().find(Invoice.class, invoiceId);
            logger.info("attaching invoice: {} ", invoice);
            // if we have rights
            getAccountService().checkThatInvoiceBeAssigned(invoice, getAccount()); // throw exception if you cannot
            getAccount().getInvoices().add(invoice);
            getGenericService().saveOrUpdate(invoice);
            getGenericService().saveOrUpdate(getAccount());
        }
        getAccount().getAuthorizedMembers().clear();
        getAccount().getAuthorizedMembers().addAll(getGenericService().loadFromSparseEntities(getAuthorizedMembers(), Person.class));
        logger.info("authorized members: {}", getAccount().getAuthorizedMembers());
        // if (Persistable.Base.isTransient(persistable)) {
        // persistable.setName(name);
        // persistable.setDescription(description);
        // }
        return SUCCESS;
    }

    @Override
    protected void delete(Account persistable) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        if (Persistable.Base.isNullOrTransient(getAuthenticatedUser())) {
            return false;
        }

        if (getAuthenticationAndAuthorizationService().can(InternalTdarRights.VIEW_BILLING_INFO, getAuthenticatedUser())) {
            return true;
        }

        if (getAuthenticatedUser().equals(getAccount().getOwner()) || getAccount().getAuthorizedMembers().contains(getAuthenticatedUser())) {
            return true;
        }

        return false;
    }

    @Override
    public Class<Account> getPersistableClass() {
        return Account.class;
    }

    @Override
    public String loadViewMetadata() {
        setAccountGroup(getAccountService().getAccountGroup(getAccount()));
        getAuthorizedMembers().addAll(getAccount().getAuthorizedMembers());
        return SUCCESS;
    }

    public Account getAccount() {
        if (getPersistable() == null)
            setPersistable(createPersistable());

        return (Account) getPersistable();
    }

    @Override
    public boolean isEditable() throws TdarActionException {
        return true;
    }

    public void setAccount(Account account) {
        setPersistable(account);
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    public AccountGroup getAccountGroup() {
        return accountGroup;
    }

    public void setAccountGroup(AccountGroup accountGroup) {
        this.accountGroup = accountGroup;
    }

    public Long getAccountGroupId() {
        return accountGroupId;
    }

    public void setAccountGroupId(Long accountGroupId) {
        this.accountGroupId = accountGroupId;
    }

    public Person getBlankPerson() {
        return new Person();
    }

    public List<Person> getAuthorizedMembers() {
        return authorizedMembers;
    }

    public void setAuthorizedMembers(List<Person> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
    }

    public boolean isBillingAdmin() {
        return getAuthenticationAndAuthorizationService().isMember(getAuthenticatedUser(), TdarGroup.TDAR_BILLING_MANAGER);
    }

}
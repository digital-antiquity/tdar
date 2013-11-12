package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.GenericService;
import org.tdar.struts.DoNotObfuscate;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.interceptor.PostOnly;
import org.tdar.utils.MessageHelper;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
public class BillingAccountController extends AbstractPersistableController<Account> {

    public static final String UPDATE_QUOTAS = "updateQuotas";
    public static final String CHOOSE = "choose";
    public static final String VIEW_ID = "view?id=${id}";
    private static final long serialVersionUID = 2912533895769561917L;
    public static final String NEW_ACCOUNT = "new_account";
    private static final String LIST_INVOICES = "listInvoices";
    private Long invoiceId;
    private Set<Account> accounts = new HashSet<>();
    private List<Invoice> invoices = new ArrayList<>();
    private List<Resource> resources = new ArrayList<>();

    private AccountGroup accountGroup;
    private List<Person> authorizedMembers = new ArrayList<>();
    private Long accountGroupId;
    private String name;
    private Integer quantity = 1;
    private String description;

    private Long numberOfFiles = 0L;
    private Long numberOfMb = 0L;
    private Date exipres = new DateTime().plusYears(1).toDate();

    @SkipValidation
    @Action(value = CHOOSE, results = {
            @Result(name = SUCCESS, location = "select-account.ftl"),
            @Result(name = NEW_ACCOUNT, location = "add?invoiceId=${invoiceId}", type = REDIRECT)
    })
    public String selectAccount() throws TdarActionException {
        Invoice invoice = getInvoice();
        if (invoice == null) {
            throw new TdarRecoverableRuntimeException(getText("billingAccountController.invoice_is_requried"));
        }
        if (!getAuthenticationAndAuthorizationService().canAssignInvoice(invoice, getAuthenticatedUser())) {
            throw new TdarRecoverableRuntimeException(getText("billingAccountController.rights_to_assign_this_invoice"));
        }
        setAccounts(getAccountService().listAvailableAccountsForUser(invoice.getOwner(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
        if (CollectionUtils.isNotEmpty(getAccounts())) {
            return SUCCESS;
        }
        return NEW_ACCOUNT;
    }

    @Action(value = "create-code", results = {
            @Result(name = SUCCESS, location = VIEW_ID, type = "redirect"),
            @Result(name = INPUT, location = VIEW_ID, type = "redirect")
    })
    @PostOnly
    @WriteableSession
    @SkipValidation
    public String createCouponCode() throws TdarActionException {
        try {
            for (int i = 0; i < quantity; i++) {
                getAccountService().generateCouponCode(getAccount(), getNumberOfFiles(), getNumberOfMb(), getExipres());
            }
            getAccountService().updateQuota(getAccount());
        } catch (Throwable e) {
            addActionMessage(e.getMessage());
            return INPUT;
        }
        return SUCCESS;
    }

    @Override
    public void loadListData() {
        if (getAuthenticationAndAuthorizationService().isMember(getAuthenticatedUser(), TdarGroup.TDAR_BILLING_MANAGER)) {
            getAccounts().addAll(getAccountService().findAll());
        }
    }

    @SkipValidation
    @Action(value = LIST_INVOICES, results = { @Result(name = SUCCESS, location = "list-invoices.ftl") })
    public String listInvoices() {
        if (getAuthenticationAndAuthorizationService().isMember(getAuthenticatedUser(), TdarGroup.TDAR_BILLING_MANAGER)) {
            getInvoices().addAll(getGenericService().findAll(Invoice.class));
            Collections.sort(getInvoices(), new Comparator<Invoice>() {
                @Override
                public int compare(Invoice o1, Invoice o2) {
                    return ObjectUtils.compare(o2.getDateCreated(), o1.getDateCreated());
                }
            });
        }
        return SUCCESS;
    }

    public Invoice getInvoice() {
        return getGenericService().find(Invoice.class, invoiceId);
    }

    @Override
    protected String save(Account persistable) {
        logger.info("invoiceId {}", getInvoiceId());

        // if we're coming from "choose" and we want a "new account"
        if (Persistable.Base.isTransient(getAccount()) && StringUtils.isNotBlank(getName())) {
            getAccount().setName(getName());
            getAccount().setDescription(getDescription());
        } else {
            getAuthorizedMembers().addAll(getAccount().getAuthorizedMembers());
        }

        if (Persistable.Base.isNotNullOrTransient(invoiceId)) {
            Invoice invoice = getInvoice();
            logger.info("attaching invoice: {} ", invoice);
            // if we have rights
            if (Persistable.Base.isTransient(getAccount())) {
                getAccount().setOwner(invoice.getOwner());
            }
            getAccountService().checkThatInvoiceBeAssigned(invoice, getAccount()); // throw exception if you cannot
            // make sure you add back all of the valid account holders
            getAccount().getInvoices().add(invoice);
            getGenericService().saveOrUpdate(invoice);
            getGenericService().saveOrUpdate(getAccount());
            updateQuotas();
        }
        getAccount().getAuthorizedMembers().clear();
        getAccount().getAuthorizedMembers().addAll(getGenericService().loadFromSparseEntities(getAuthorizedMembers(), Person.class));

        logger.info("authorized members: {}", getAccount().getAuthorizedMembers());
        return SUCCESS;
    }

    @Override
    protected void delete(Account persistable) {
        // TODO Auto-generated method stub
    }

    @SkipValidation
    @WriteableSession
    @Action(value = UPDATE_QUOTAS, results = {
            @Result(name = SUCCESS, location = "view?id=${id}", type = REDIRECT)
    })
    public String updateQuotas() {
        getAccountService().updateQuota(getAccount(), getAccount().getResources());
        return TdarActionSupport.SUCCESS;
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        logger.info("isViewable {} {}", getAuthenticatedUser(), getAccount().getId());
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
        getResources().addAll(getAccount().getResources());
        GenericService.sortByUpdatedDate(getResources());
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

    @DoNotObfuscate(reason="needs access to Email Address on view page")
    public List<Person> getAuthorizedMembers() {
        return authorizedMembers;
    }

    public void setAuthorizedMembers(List<Person> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
    }

    public boolean isBillingAdmin() {
        return getAuthenticationAndAuthorizationService().isMember(getAuthenticatedUser(), TdarGroup.TDAR_BILLING_MANAGER);
    }

    public BillingActivityModel getBillingActivityModel() {
        return getAccountService().getLatestActivityModel();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Long getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public Long getNumberOfMb() {
        return numberOfMb;
    }

    public void setNumberOfMb(Long numberOfMb) {
        this.numberOfMb = numberOfMb;
    }

    public Date getExipres() {
        return exipres;
    }

    public void setExipres(Date exipres) {
        this.exipres = exipres;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

}
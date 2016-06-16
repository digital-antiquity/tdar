package org.tdar.struts.action.billing;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingAccountGroup;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
@HttpsOnly
public class BillingAccountViewAction extends AbstractPersistableViewableAction<BillingAccount> {

    private static final long serialVersionUID = 3896385613294762404L;
    public static final String UPDATE_QUOTAS = "updateQuotas";
    public static final String FIX_FOR_DELETE_ISSUE = "fix";
    public static final String CHOOSE = "choose";
    public static final String VIEW_ID = "view?id=${id}";
    public static final String NEW_ACCOUNT = "new_account";
    private Long invoiceId;
    private List<BillingAccount> accounts = new ArrayList<>();
    private List<Invoice> invoices = new ArrayList<>();
    private List<Resource> resources = new ArrayList<>();
    private List<Coupon> coupons = new ArrayList<>();
    private BillingAccountGroup accountGroup;
    private List<TdarUser> authorizedMembers = new ArrayList<>();
    private Long accountGroupId;
    private String name;
    private Integer quantity = 1;
    private String description;

    private Long numberOfFiles = 0L;
    private Long numberOfMb = 0L;
    private Date expires = new DateTime().plusYears(1).toDate();

    @Autowired
    private transient BillingAccountService accountService;
    @Autowired
    private transient AuthorizationService authorizationService;

    public Invoice getInvoice() {
        return getGenericService().find(Invoice.class, invoiceId);
    }

    @Override
    public boolean authorize() {
        getLogger().info("isViewable {} {}", getAuthenticatedUser(), getAccount().getId());
        return authorizationService.canViewBillingAccount(getAccount(), getAuthenticatedUser());
    }

    public boolean isEditable() {
        return authorizationService.canEditAccount(getAccount(), getAuthenticatedUser());
    }

    @Override
    public Class<BillingAccount> getPersistableClass() {
        return BillingAccount.class;
    }
    
    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        if (PersistableUtils.isNullOrTransient(getAccount())) {
            addActionError(getText("error.object_does_not_exist"));
            return;
        }
        setAccounts(accountService.listAvailableAccountsForUser(getAuthenticatedUser()));
        setAccountGroup(accountService.getAccountGroup(getAccount()));
        getAuthorizedMembers().addAll(getAccount().getAuthorizedMembers());
        getResources().addAll(getAccount().getResources());
        PersistableUtils.sortByUpdatedDate(getResources());
        setInvoices(new ArrayList<>(getAccount().getInvoices()));
        PersistableUtils.sortByCreatedDate(getInvoices());
    }

    @Override
    public String loadViewMetadata() {
        Iterator<Invoice> iter = getInvoices().iterator();
        setCoupons(new ArrayList<>(getAccount().getCoupons()));
        while (iter.hasNext()) {
            Invoice inv = iter.next();
            if (inv.isModifiable()) {
                iter.remove();
            }
        }
        
        for (TdarUser au : getAuthorizedMembers()) {
            String name = null;
            if (au != null) {
                name = au.getProperName();
            }
            getAuthorizedUsersFullNames().add(name);
        }

        return SUCCESS;
    }

    public BillingAccount getAccount() {
        return getPersistable();
    }

    public void setAccount(BillingAccount account) {
        setPersistable(account);
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
    }

    public BillingAccountGroup getAccountGroup() {
        return accountGroup;
    }

    public void setAccountGroup(BillingAccountGroup accountGroup) {
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

    @DoNotObfuscate(reason = "needs access to Email Address on view page")
    public List<TdarUser> getAuthorizedMembers() {
        return authorizedMembers;
    }

    public void setAuthorizedMembers(List<TdarUser> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
    }

    public boolean isBillingAdmin() {
        return authorizationService.isMember(getAuthenticatedUser(), TdarGroup.TDAR_BILLING_MANAGER);
    }

    public BillingActivityModel getBillingActivityModel() {
        return accountService.getLatestActivityModel();
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
        return getExpires();
    }

    public void setExipres(Date exipres) {
        this.setExpires(exipres);
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

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public List<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons;
    }

}
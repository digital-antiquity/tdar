package org.tdar.struts.action.billing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingAccountGroup;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
@HttpsOnly
public class BillingAccountController extends AbstractPersistableController<BillingAccount> {

    public static final String UPDATE_QUOTAS = "updateQuotas";
    public static final String FIX_FOR_DELETE_ISSUE = "fix";
    public static final String CHOOSE = "choose";
    public static final String VIEW_ID = "${id}";
    private static final long serialVersionUID = 2912533895769561917L;
    public static final String NEW_ACCOUNT = "new_account";
    private Long invoiceId;
    private List<BillingAccount> accounts = new ArrayList<>();
    private List<Resource> resources = new ArrayList<>();

    private BillingAccountGroup accountGroup;
    private List<TdarUser> authorizedMembers = new ArrayList<>();
    private List<String> authorizedUsersFullNames = new ArrayList<String>();
    private Long accountGroupId;
    private String name;
    private Date expires = new DateTime().plusYears(1).toDate();

    private String description;
    private String ownerProperName;
    private TdarUser owner;

    @Autowired
    private transient BillingAccountService accountService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    public boolean authorize() {
        if (PersistableUtils.isNullOrTransient(getPersistable())) {
            return true;
        }
        return authorizationService.canEditAccount(getAuthenticatedUser(), getPersistable());
    }
    
    public Invoice getInvoice() {
        return getGenericService().find(Invoice.class, invoiceId);
    }

    @Override
    protected String save(BillingAccount persistable) {
        getLogger().info("invoiceId {}", getInvoiceId());
        setSaveSuccessPath("billing");
        setupOwnerField();
        List<UserRightsProxy> proxies = new ArrayList<>();
        for (TdarUser user : authorizedMembers) {
            proxies.add(new UserRightsProxy(new AuthorizedUser(null, user, GeneralPermissions.EDIT_ACCOUNT)));
        }
        //saveForController(BillingAccount account, String name, String description, Invoice invoice, Long invoiceId, TdarUser owner, TdarUser authenticatedUser)
        accountService.saveForController(persistable, name, description, getInvoice(), invoiceId, owner, getAuthenticatedUser(), proxies );
        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = UPDATE_QUOTAS, results = {
            @Result(name = SUCCESS, location = "${id}", type = TDAR_REDIRECT)
    })
    public String updateQuotas() {
        accountService.updateQuota(getAccount(), getAccount().getResources(), getAuthenticatedUser());
        return SUCCESS;
    }

    /**
     * Temporary controller to fix issue where deleted items were counted wrong/differently before
     * we're changing the values here automatically
     * 
     * @return stuff
     */
    @SkipValidation
    @Action(value = FIX_FOR_DELETE_ISSUE, results = {
            @Result(name = SUCCESS, location = "${id}", type = TDAR_REDIRECT)
    })
    public String fix() {
        accountService.resetAccountTotalsToHaveOneFileLeft(getAccount(), getAuthenticatedUser());
        return SUCCESS;
    }


    // This is temporary until we break out CreateCouponCodeAction
    public List<Invoice> getInvoices() {
        List<Invoice> invoices = new ArrayList<>(getAccount().getInvoices());
        PersistableUtils.sortByCreatedDate(invoices);
        Iterator<Invoice> iter = invoices.iterator();
        while (iter.hasNext()) {
            Invoice inv = iter.next();
            if (inv.isModifiable()) {
                iter.remove();
            }
        }
        return invoices;
    }

    @Override
    public Class<BillingAccount> getPersistableClass() {
        return BillingAccount.class;
    }

    public BillingAccount getAccount() {
        if (getPersistable() == null) {
            setPersistable(createPersistable());
        }

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

    @Override
    public String loadEditMetadata() throws TdarActionException {
        setOwner(getPersistable().getOwner());
        setupOwnerField();
        return SUCCESS;
    }

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        getAccount().getAuthorizedUsers().forEach(au -> {
            getAuthorizedMembers().add(au.getUser());
            getAuthorizedUsersFullNames().add(au.getUser().getProperName());
        });
    }

    public List<Status> getStatuses() {
        return Arrays.asList(Status.ACTIVE, Status.FLAGGED, Status.FLAGGED_ACCOUNT_BALANCE, Status.DELETED);
    }

    private void setupOwnerField() {
        if (PersistableUtils.isNotNullOrTransient(getOwner()) && StringUtils.isNotBlank(getOwner().getProperName())) {
            if (getOwner().getFirstName() != null && getOwner().getLastName() != null)
                setOwnerProperName(getOwner().getProperName());
        } else {
            setOwnerProperName(getAuthenticatedUser().getProperName());
        }
    }

    public String getOwnerProperName() {
        return ownerProperName;
    }

    public void setOwnerProperName(String ownerProperName) {
        this.ownerProperName = ownerProperName;
    }

    public TdarUser getOwner() {
        return owner;
    }

    public void setOwner(TdarUser owner) {
        this.owner = owner;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }


    public List<String> getAuthorizedUsersFullNames() {
        return authorizedUsersFullNames;
    }

    public void setAuthorizedUsersFullNames(List<String> authorizedUsersFullNames) {
        this.authorizedUsersFullNames = authorizedUsersFullNames;
    }

}
package org.tdar.struts.action.billing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingAccountGroup;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
@HttpsOnly
public class BillingAccountSelectionAction extends AbstractAuthenticatableAction implements Preparable, Validateable {

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

    private Invoice invoice;

    @Override
    public void prepare() {
        if (PersistableUtils.isNotNullOrTransient(invoiceId)) {
            invoice = getGenericService().find(Invoice.class, invoiceId);
        }
    }

    @Override
    public void validate() {
        if (invoice == null) {
            throw new TdarRecoverableRuntimeException(getText("billingAccountController.invoice_is_requried"));
        }
        if (!authorizationService.canAssignInvoice(getAuthenticatedUser(), invoice)) {
            throw new TdarRecoverableRuntimeException(getText("billingAccountController.rights_to_assign_this_invoice"));
        }
    }

    @Action(value = CHOOSE, results = { @Result(name = SUCCESS, location = "select-account.ftl"),
            @Result(name = NEW_ACCOUNT, location = "add?invoiceId=${invoiceId}", type = TDAR_REDIRECT) })
    public String selectAccount() throws TdarActionException {

        setAccounts(accountService.listAvailableAccountsForUser(invoice.getOwner(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));

        if (CollectionUtils.isNotEmpty(getAccounts())) {
            return SUCCESS;
        }

        return NEW_ACCOUNT;
    }

    public Invoice getInvoice() {
        return invoice;
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

    public List<Status> getStatuses() {
        return Arrays.asList(Status.ACTIVE, Status.FLAGGED, Status.FLAGGED_ACCOUNT_BALANCE, Status.DELETED);
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

}
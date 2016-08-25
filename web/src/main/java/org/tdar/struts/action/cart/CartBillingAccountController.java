package org.tdar.struts.action.cart;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.billing.InvoiceService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

/**
 * Created by JAMES on 6/14/2014.
 */
@Component
@Scope("prototype")
@Namespace("/cart")
@ParentPackage("secured")
@Results({
        @Result(name = "redirect-payment", type = TdarActionSupport.TDAR_REDIRECT, location = "/cart/process-payment-request"),
})
public class CartBillingAccountController extends AbstractCartController {

    private static final long serialVersionUID = 563992082346864102L;

    // id of one of the account chosen from the dropdown list
    private long id = -1L;

    // account chosen from the dropdown list
    private BillingAccount selectedAccount = null;

    // Or a user can specify a new account name
    private BillingAccount account = new BillingAccount();

    private boolean acceptContributorAgreement = false;

    @Autowired
    private transient InvoiceService invoiceService;

    @Autowired
    private transient BillingAccountService accountService;

    @Override
    public void prepare() {
        super.prepare();

        if (!validateInvoice()) {
            return;
        }

        TdarUser owner = getInvoice().getOwner();
        if (owner == null) {
            owner = getAuthenticatedUser();
            getInvoice().setOwner(owner);
            getLogger().debug("invoice had no owner, setting to authenticated user {}", owner);
        }

        selectedAccount = accountService.reconcileSelectedAccount(id, getInvoice(), getAccount(), getAccounts());

        getLogger().debug("selected account: {}", selectedAccount);
        getLogger().debug("owner:{}\t accounts:{}", getInvoice().getOwner(), getAccounts());
    }

    @Override
    public void validate() {
        if (selectedAccount == null && PersistableUtils.isNotNullOrTransient(id)) {
            addActionError(getText("cartController.invalid_account"));
        }

        if (getInvoice() == null) {
            addActionError(getText("cartController.missing_invoice"));
        }
        
        // rule: payment method required
        if (getInvoice().getPaymentMethod() == null) {
            addActionError(getText("cartController.valid_payment_method_is_required"));
        }

        if (!getAuthenticatedUser().isContributor() && !acceptContributorAgreement) {
            addActionError(getText("cartController.please_accept_contributor_agreement"));
        }

    }

    /**
     * Assign invoice to (pre-existing or new) billing account.
     * 
     * @return
     */
    @Action(value = "process-billing-account-choice", results = {
            @Result(name = INPUT, location = "review", type = TDAR_REDIRECT),
            @Result(name = SUCCESS, location = "process-payment-request", type = TDAR_REDIRECT) })
    @PostOnly
    @WriteableSession
    public String processBillingAccountChoice() {
        if (!getInvoice().isModifiable()) {
            addActionError(getText("cartController.cannot_modify_completed_invoice"));
            return REDIRECT_START;
        }

        // if user came via unauthenticated page the owner/proxy may not be set. If either is null, we set both to the current user
        if (getInvoice().getOwner() == null || getInvoice().getTransactedBy() == null) {
            TdarUser user = getAuthenticatedUser();
            getInvoice().setOwner(user);
            getInvoice().setTransactedBy(user);
        }

        accountService.processBillingAccountChoice(selectedAccount, getInvoice(), getAuthenticatedUser());
        invoiceService.updateInvoiceStatus(getInvoice());

        return SUCCESS;
    }

    public BillingAccount getSelectedAccount() {
        return selectedAccount;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TdarUser getBlankPerson() {
        return new TdarUser();
    }

    public boolean isAcceptContributorAgreement() {
        return acceptContributorAgreement;
    }

    public void setAcceptContributorAgreement(boolean acceptContributorAgreement) {
        this.acceptContributorAgreement = acceptContributorAgreement;
    }
}

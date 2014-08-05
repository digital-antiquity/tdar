package org.tdar.struts.action.cart;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.service.billing.AccountService;
import org.tdar.core.service.billing.InvoiceService;
import org.tdar.struts.interceptor.annotation.GetOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

/**
 * Created by JAMES on 6/14/2014.
 */
@Component
@Scope("prototype")
@Namespace("/cart")
@ParentPackage("secured")
@Results({
        @Result(name = "redirect-payment", type = "redirect", location = "/cart/process-payment-request"),
        @Result(name = "input", location = "review.ftl")
})
public class CartBillingAccountController extends AbstractCartController {

    private static final long serialVersionUID = 563992082346864102L;

    // id of one of the account chosen from the dropdown list
    private long id = -1L;

    // account chosen from the dropdown list
    private Account selectedAccount = null;

    // Or a user can specify a new account name
    private Account account = new Account();

    @Autowired
    private transient InvoiceService invoiceService;
    
    @Autowired
    private transient AccountService accountService;

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
        setAccounts(accountService.listAvailableAccountsForUser(owner));
        // the account id may have been set already by the "add invoice" link on /billing/{id}/view
        
        //FIXME: move to service layer
        if (id == -1L && getInvoice() != null) {
            getLogger().debug("looking for account by invoice {}", getInvoice());
            selectedAccount = accountService.getAccountForInvoice(getInvoice());
            if (selectedAccount == null && !getAccounts().isEmpty()) {
                selectedAccount = getAccounts().iterator().next();
            }
            if (selectedAccount != null) {
                id = selectedAccount.getId();
            }
        } else {
            selectedAccount = getGenericService().find(Account.class, id);
        }
        getLogger().debug("selected account: {}", selectedAccount);
        getLogger().debug("owner:{}\t accounts:{}", getInvoice().getOwner(), getAccounts());
        // FIXME: seems weird to be here, how about adding this as an option in the FTL select instead?
        if (CollectionUtils.isNotEmpty(getAccounts())) {
            getAccounts().add(new Account("Add an account"));
        }
    }

    @Override
    public void validate() {
        if (isPostRequest()) {
            if (Persistable.Base.isNullOrTransient(getId())) {
                validate(account);
            } else if (selectedAccount == null) {
                addActionError(getText("cartController.invalid_account"));
            }

            // rule: payment method required
            if (getInvoice().getPaymentMethod() == null) {
                addActionError(getText("cartController.valid_payment_method_is_required"));
            }

        }
    }

    //FIXME: this method is mis-named, and does stuff that belongs in the service layer (or at least in the actual controller execute method).
    private void validate(Account account) {
        if (StringUtils.isBlank(account.getName())) {
            // addActionError("Account name required");
            account.setName("Default account for " + getInvoice().getOwner().getProperName());
        }
    }

    /**
     * Assign invoice to (pre-existing or new) billing account.
     * 
     * @return
     */
    @Action(value = "process-billing-account-choice", results = { @Result(name = SUCCESS, location = "process-payment-request", type = "redirect") })
    @PostOnly
    @WriteableSession
    public String processBillingAccountChoice() {

        // if user came via unauthenticated page the owner/proxy may not be set. If either is null, we set both to the current user
        if (getInvoice().getOwner() == null || getInvoice().getTransactedBy() == null) {
            TdarUser user = getAuthenticatedUser();
            getInvoice().setOwner(user);
            getInvoice().setTransactedBy(user);
        }

        Account acct = account;
        // prevent params-prepare-params from modifying pre-existing account
        if (selectedAccount != null) {
            acct = selectedAccount;
        }
        accountService.processBillingAccountChoice(acct, getInvoice(), getAuthenticatedUser());
        //FIXME: Move to service layer
        updateInvoiceStatus();

        return SUCCESS;
    }

    private void updateInvoiceStatus() {
        Invoice invoice = getInvoice();
        PaymentMethod paymentMethod = invoice.getPaymentMethod();

        switch (paymentMethod) {
            case CHECK:
                break;
            case CREDIT_CARD:
                getGenericService().saveOrUpdate(invoice);
                invoiceService.finalizePayment(invoice, paymentMethod);
                break;
            case INVOICE:
            case MANUAL:
                invoice.setTransactionStatus(Invoice.TransactionStatus.TRANSACTION_SUCCESSFUL);
                getGenericService().saveOrUpdate(invoice);
                break;
        }
    }

    @Action(value = "review", results = { @Result(name = INPUT, location = "review.ftl") })
    @GetOnly
    public String showBillingAccounts() {
        return SUCCESS;
    }

    public Account getSelectedAccount() {
        return selectedAccount;
    }

    public Account getAccount() {
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
}

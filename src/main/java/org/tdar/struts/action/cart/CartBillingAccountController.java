package org.tdar.struts.action.cart;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.InvoiceService;
import org.tdar.struts.interceptor.annotation.GetOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

/**
 * Created by JAMES on 6/14/2014.
 */
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
    private transient AccountService accountService;

    @Autowired
    private transient InvoiceService invoiceService;

    @Override
    public void prepare() {
        super.prepare();

        //the account id may have been set already by the "add invoice" link on /billing/{id}/view
        if(id == -1L && getInvoice() != null) {
            selectedAccount = invoiceService.getAccountForInvoice(getInvoice());
        } else {
            selectedAccount = getGenericService().find(Account.class, id);
        }


        TdarUser owner = getInvoice().getOwner();
        if(owner == null) {
            owner = getAuthenticatedUser();
        }
        getAccounts().addAll(accountService.listAvailableAccountsForCartAccountSelection(owner, Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
        getLogger().debug("owner:{}\t accounts:{}", getInvoice().getOwner(), getAccount());
        if (CollectionUtils.isNotEmpty(getAccounts())) {
            getAccounts().add(new Account("Add an account"));
        }
    }

    @Override
    public void validate() {
        if (getInvoice() == null) {
            addActionError("No invoice found");
        }
        if (isPostRequest()) {
            if (Persistable.Base.isNullOrTransient(getId())) {
                validate(account);
            } else if (selectedAccount == null) {
                addActionError("Invalid account selection");
            }

            // rule: payment method required
            if (getInvoice().getPaymentMethod() == null) {
                addActionError(getText("cartController.valid_payment_method_is_required"));
            }

        }
    }

    private void validate(Account account) {
        if (StringUtils.isBlank(account.getName())) {
            //addActionError("Account name required");
            account.setName("Generated account for " + getAuthenticatedUser().getProperName());
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

        //if user came via unauthenticated page the owner/proxy may not be set.  If either is null, we set both to the current user
        if (getInvoice().getOwner() == null || getInvoice().getTransactedBy() == null) {
            getInvoice().setOwner(getAuthenticatedUser());
            getInvoice().setTransactedBy(getAuthenticatedUser());
        }

        Account acct = account;
        // prevent params-prepare-params from modifying pre-existing account
        if (selectedAccount != null) {
            acct = selectedAccount;
        }
        accountService.processBillingAccountChoice(acct, getInvoice(), getAuthenticatedUser());
        return SUCCESS;
    }

    @Action(value = "choose-billing-account", results={@Result(name = "input", location = "choose-billing-account.ftl")})
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

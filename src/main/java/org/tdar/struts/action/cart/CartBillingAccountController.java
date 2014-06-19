package org.tdar.struts.action.cart;

import static org.tdar.core.bean.resource.Status.ACTIVE;
import static org.tdar.core.bean.resource.Status.FLAGGED_ACCOUNT_BALANCE;

import java.util.ArrayList;
import java.util.List;

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
import org.tdar.core.service.AccountService;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

/**
 * Created by JAMES on 6/14/2014.
 */
@Namespace("/cart")
@ParentPackage("secured")
@Results({
        @Result(name = "redirect-payment", type = "redirect", location = "/cart/process-payment-request"),
        @Result(name = "input", location = "show-billing-accounts.ftl")
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

    @Override
    public void prepare() {
        super.prepare();
        if (getInvoice().getOwner() == null) {
            setOwner(getAuthenticatedUser());
        }
        selectedAccount = getGenericService().find(Account.class, id);
        getAccounts().addAll(accountService.listAvailableAccountsForUser(getOwner(), ACTIVE, FLAGGED_ACCOUNT_BALANCE));
        getLogger().debug("owner:{}\t accounts:{}", getOwner(), getAccount());
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
            addActionError("Account name required");
        }
    }

//    /**
//     * A form which allows the user to assign the pending invoice to an existing billing account
//     * or to specify a new billing account. If the user has no existing billing account, skip
//     * this step (assign to implicitly created account) and redirect to the payment page
//     * 
//     * @return
//     */
//    // FIXME: httpget actions should not change state. this implicit account creation needs to happen in the authentication postback (registration or login).
//    @Action("show-billing-accounts")
//    // @GetOnly
//    public String showBillingAccounts() {
//        return SUCCESS;
//    }

    /**
     * Assign invoice to (pre-existing or new) billing account.
     * 
     * @return
     */
    @Action(value = "process-billing-account-choice", results = { @Result(name = SUCCESS, location = "process-payment-request", type = "redirect") })
    @PostOnly
    @WriteableSession
    public String processBillingAccountChoice() {
        Account acct = account;
        // prevent params-prepare-params from modifying pre-existing account
        if (selectedAccount != null) {
            acct = selectedAccount;
        }
        acct.getInvoices().add(getInvoice());
        getGenericService().saveOrUpdate(acct);
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

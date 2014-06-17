package org.tdar.struts.action.cart;

import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.AccountService;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import java.util.ArrayList;
import java.util.List;

import static org.tdar.core.bean.resource.Status.ACTIVE;
import static org.tdar.core.bean.resource.Status.FLAGGED_ACCOUNT_BALANCE;

/**
 * Created by JAMES on 6/14/2014.
 */
@Namespace("/cart")
@ParentPackage("secured")
@Results({
        @Result(name="redirect-payment", type="redirect", location="/cart/process-payment-request"),
        @Result(name="input", location="show-billing-accounts.ftl" )
})
public class CartBillingAccountController extends AbstractCartController {

    //list of billing accounts that the user may choose from when assigning the invoice
    private List<Account> accounts = new ArrayList<>();

    //id of one of the account chosen from the dropdown llit
    private long id = -1L;

    //account chosen from the dropdown list
    private Account selectedAccount = null;

    //Or a user can specify a new account name
    private Account account = new Account();

    @Autowired
    private transient AccountService accountService;

    @Override
    public void prepare() {
        super.prepare();
        //if we created the invoice prior to authentication the owner wont exist (if it does,  that's a problem) So set the owner to be the authuser
        //FIXME: need to figure out which postback action will save the owner (if it wasn't set during /cart/process-choice): e.g. login, process-registration, process-cart-choice, REST endpoint
        if(getInvoice().getOwner() == null) {
            setOwner(getAuthenticatedUser());
        }
        selectedAccount = getGenericService().find(Account.class, id);
        accounts.addAll(accountService.listAvailableAccountsForUser(getOwner(), ACTIVE, FLAGGED_ACCOUNT_BALANCE));
        getLogger().debug("owner:{}\t accounts:{}", getOwner(), accounts);
    }

    @Override
    public void validate() {
        if(getInvoice() == null) {
            addActionError("No invoice found");
        }
        if(isPostRequest()) {
            if(id == -1L) {
                validate(account);
            } else if(selectedAccount == null) {
                addActionError("Invalid account selection");
            }

            //rule: payment method required
            if(getInvoice().getPaymentMethod() == null) {
                addActionError(getText("cartController.valid_payment_method_is_required"));
            }

        }
    }

    private void validate(Account account) {
        if(StringUtils.isBlank(account.getName())) {
            addActionError("Account name required");
        }
    }

    @Action("show-billing-accounts")
    public String showBillingAccounts() {
        //skip this form if user has no assignable billing accounts
        if(accounts.isEmpty() ) {
            return "redirect-payment";
        }
        return SUCCESS;
    }

    @Action(value="process-billing-account-choice", results={@Result(name=SUCCESS, location="process-payment-request", type="redirect")})
    @PostOnly
    @WriteableSession
    public String processBillingAccountChoice() {
        Account acct = account;
        //prevent params-prepare-params from modifying pre-existing account
        if(selectedAccount != null) {
            acct = selectedAccount;
        }
        acct.getInvoices().add(getInvoice());
        getGenericService().saveOrUpdate(acct);
        return SUCCESS;
    }


    public List<Account> getAccounts() {
        return accounts;
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

    public TdarUser getBlankPerson()  {
        return new TdarUser();
    }
}

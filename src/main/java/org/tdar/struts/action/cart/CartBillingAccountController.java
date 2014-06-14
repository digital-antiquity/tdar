package org.tdar.struts.action.cart;

import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.AccountService;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JAMES on 6/14/2014.
 */
@Namespace("/cart")
@ParentPackage("secured")
public class CartBillingAccountController extends AbstractCartController implements Preparable {

    private List<Account> accounts = new ArrayList<>();
    private Account account = new Account();
    private Account selectedAccount = null;
    private long id = -1L;

    @Autowired
    private transient AccountService accountService;

    @Override
    public void prepare() {
        if(id != -1L) {
            account = getGenericService().find(Account.class, id);
        }
        accounts.addAll(accountService.listAvailableAccountsForUser(getInvoice().getOwner(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
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
        }
    }

    private void validate(Account account) {
        if(StringUtils.isBlank(account.getName())) {
            addActionError("Account name required");
        }
    }


    @Action("show-billing-accounts")
    public String showBillingAccounts() {
        return SUCCESS;
    }


    @Action(value = "process-billing-account-choice", results= {
            @Result(name=SUCCESS, type="redirect", location="/cart/show-landing/page"),
            @Result(name=INPUT, location="show-billing-accounts.ftl" )
    })
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
}

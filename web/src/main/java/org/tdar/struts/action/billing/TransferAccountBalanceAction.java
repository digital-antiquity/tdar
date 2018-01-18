package org.tdar.struts.action.billing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing/transfer")
public class TransferAccountBalanceAction extends AbstractBillingAccountAction {

    private static final long serialVersionUID = 4993654895975490884L;

    private Long numberOfFiles;
    private Long toAccountId;
    private BillingAccount toAccount;
    private List<BillingAccount> accounts = new ArrayList<>();

    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNullOrTransient(toAccount)) {
            addActionError(getText("transferAccountBalanceAction.toAccountNotSpecified"));
        }

        if (!authorizationService.canAdministerAccount(getAuthenticatedUser(), getAccount())) {
            addActionError(getText("transferAccountBalanceAction.no_rights", Arrays.asList(getAccount())));
        }
        if (!authorizationService.canAdministerAccount(getAuthenticatedUser(), toAccount)) {
            addActionError(getText("transferAccountBalanceAction.no_rights", Arrays.asList(getAccount())));

        }
    }

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        setAccounts(accountService.listAvailableAccountsForUser(getAuthenticatedUser()));

        toAccount = accountService.find(toAccountId);
    }

    @Action(value = "{id}",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = "../transfer.ftl"),
                    @Result(name = INPUT, location = "error.ftl")
            })
    @SkipValidation
    @Override
    public String execute() throws TdarActionException {
        return SUCCESS;
    }

    @Action(value = "{id}/transfer",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = VIEW_ID, type = TDAR_REDIRECT),
                    @Result(name = INPUT, location = "error.ftl")
            })
    @PostOnly
    public String transfer() throws TdarActionException {
        accountService.transferBalanace(getAuthenticatedUser(), getAccount(), toAccount, numberOfFiles);
        addActionMessage(getText("transferAccountBalanceAction.success", Arrays.asList(getAccount(), toAccount)));
        return SUCCESS;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public Long getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
    }
}

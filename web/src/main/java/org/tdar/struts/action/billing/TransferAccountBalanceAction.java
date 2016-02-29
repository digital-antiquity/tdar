package org.tdar.struts.action.billing;

import java.util.Arrays;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.utils.PersistableUtils;


public class TransferAccountBalanceAction extends AbstractBillingAccountAction {

    private static final long serialVersionUID = 4993654895975490884L;

    private Long numberOfFiles;
    private Long toAccountId;
    private BillingAccount toAccount;

    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNullOrTransient(toAccount)) {
            addActionError(getText("transferAccountBalanceAction.toAccountNotSpecified"));
        }

        if (!authorizationService.canAdministerAccount(getAccount(), getAuthenticatedUser())) {
            addActionError(getText("transferAccountBalanceAction.no_rights", Arrays.asList(getAccount())));
        }
        if (!authorizationService.canAdministerAccount(toAccount, getAuthenticatedUser())) {
            addActionError(getText("transferAccountBalanceAction.no_rights", Arrays.asList(getAccount())));

        }
    }

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        toAccount = accountService.find(toAccountId);
    }

    @Action(value = "transfer-balance",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = VIEW_ID, type = "redirect"),
                    @Result(name = INPUT, location = "error.ftl")
            })
    @PostOnly
    @Override
    public String execute() throws TdarActionException {
        accountService.transferBalanace(getAuthenticatedUser(), getAccount(), toAccount, numberOfFiles);
        accountService.updateQuota(getAccount());
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
}

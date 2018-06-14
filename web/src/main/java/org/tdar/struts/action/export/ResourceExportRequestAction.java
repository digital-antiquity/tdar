package org.tdar.struts.action.export;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.billing.BillingAccountService;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/export")
public class ResourceExportRequestAction extends AbstractResourceExportAction {

    private static final long serialVersionUID = -7292280428686745482L;

    @Autowired
    private transient BillingAccountService billingAccountService;

    private List<BillingAccount> accounts = new ArrayList<>();

    @Override
    public void prepare() throws Exception {
        super.prepare();
        setAccounts(billingAccountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE));
    }

    @Override
    @Action(value = "request", results = {
            @Result(name = SUCCESS, location = "request.ftl")
    })
    public String execute() {
        return SUCCESS;
    }

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
    }

}

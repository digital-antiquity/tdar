package org.tdar.struts.action.billing;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
@RequiresTdarUserGroup(TdarGroup.TDAR_BILLING_MANAGER)
@HttpsOnly
public class ListBillingAccountAction extends AuthenticationAware.Base {

    public static final String LIST = "list";

    private static final long serialVersionUID = -987101577591701115L;

    private List<BillingAccount> accounts = new ArrayList<>();
    
    @Autowired
    private BillingAccountService accountService;
    
    @Action(value = LIST)
    public String list() {
        getAccounts().addAll(accountService.findAll());
        return SUCCESS;
    }

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
    }

}

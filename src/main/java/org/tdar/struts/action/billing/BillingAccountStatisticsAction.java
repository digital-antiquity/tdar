package org.tdar.struts.action.billing;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.service.billing.AccountService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
@HttpsOnly
public class BillingAccountStatisticsAction extends AbstractStatisticsAction implements Preparable {

    private static final long serialVersionUID = -668002786160683089L;
    private Account account;
    
    @Autowired
    private AccountService accountService;
    
    @Override
    public void prepare() throws Exception {
        account = accountService.find(getId());
        setStatsForAccount(statisticsService.getStatsForAccount(account,this, getGranularity()));
    }
    
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}

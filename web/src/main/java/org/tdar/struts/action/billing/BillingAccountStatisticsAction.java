package org.tdar.struts.action.billing;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.AbstractStatisticsAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing/usage")
@HttpsOnly
public class BillingAccountStatisticsAction extends AbstractStatisticsAction implements Preparable {

    private static final long serialVersionUID = -668002786160683089L;
    private BillingAccount account;
    private final BillingAccountService accountService;

    @Autowired
    public BillingAccountStatisticsAction(BillingAccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void prepare() throws Exception {
        account = accountService.find(getId());
        if (account == null) {
            addActionError("billingAccountStatisticsACtion.no_account");
        }

        setStatsForAccount(getStatisticsService().getStatsForAccount(account, this, getGranularity()));
        setupJson();
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

}

package org.tdar.struts.action.billing;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractDeleteAction;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
public class BillingAccountDeleteAction extends AbstractDeleteAction<BillingAccount> implements Preparable {

    private static final long serialVersionUID = -1678413641698666417L;

    private transient BillingAccountService billingAccountService;
    private transient AuthorizationService authorizationService;

    @Autowired
    public BillingAccountDeleteAction(BillingAccountService billingAccountService, AuthorizationService authorizationService) {
        this.billingAccountService = billingAccountService;
        this.authorizationService = authorizationService;
    }

    @Override
    protected BillingAccount loadPersistable() {
        BillingAccount collection = billingAccountService.find(getId());
        return collection;
    }

    @Override
    protected void delete(BillingAccount account) {
        billingAccountService.deleteForController(this, account, getDeletionReason(), getAuthenticatedUser());
    }

    @Override
    protected DeleteIssue getDeletionIssues() {
        return billingAccountService.getDeletionIssues(this, getPersistable());
    }

    @Override
    protected boolean canDelete() {
        return authorizationService.canEditAccount(getAuthenticatedUser(), getPersistable());
    }

}

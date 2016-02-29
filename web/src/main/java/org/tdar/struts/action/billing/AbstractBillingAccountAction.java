package org.tdar.struts.action.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.AbstractPersistableController.RequestType;

import com.opensymphony.xwork2.Preparable;

public abstract class AbstractBillingAccountAction extends AuthenticationAware.Base implements Preparable, PersistableLoadingAction<BillingAccount> {


    public static final String VIEW_ID = "/billing/${id}";

    @Autowired
    protected BillingAccountService accountService;
    @Autowired
    protected AuthorizationService authorizationService;

    private static final long serialVersionUID = 8741001935773762788L;

    private Long id;
    private BillingAccount account;
    
    public boolean isEditable() {
        return authorizationService.canEditAccount(getAccount(), getAuthenticatedUser());
    }

    
    
    @Override
    public void prepare() throws TdarActionException {
        prepareAndLoad(this, RequestType.EDIT);
    }

    

    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_BILLING_INFO;
    }

    @Override
    public void setPersistable(BillingAccount persistable) {
        this.setAccount(persistable);
    }

    
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public BillingAccount getAccount() {
        return account;
    }



    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEditAccount(getAccount(), getAuthenticatedUser());
    }

    @Override
    public Persistable getPersistable() {
        return account;
    }

    @Override
    public Class<BillingAccount> getPersistableClass() {
        return BillingAccount.class;
    }

}

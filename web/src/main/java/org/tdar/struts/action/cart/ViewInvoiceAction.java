package org.tdar.struts.action.cart;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.interceptor.ValidationWorkflowAware;

@Component
@Scope("prototype")
@HttpsOnly
@ParentPackage("secured")
@Namespace("/cart")
public class ViewInvoiceAction extends AuthenticationAware.Base implements Preparable, ValidationWorkflowAware {

    private static final long serialVersionUID = -8280706863708228864L;

    private Long id;

    private Invoice invoice;

    private String inputResultName;

    @Autowired
    private BillingAccountService accountService;

    private BillingAccount account;

    @Override
    public void prepare() {
        invoice = getGenericService().find(Invoice.class, id);
        if (invoice != null) {
            setAccount(accountService.getAccountForInvoice(invoice));
        }
    }

    @Override
    public void validate() {
        if (invoice == null) {
            addActionError(getText("viewInvoiceAction.not_found"));
            inputResultName = BAD_REQUEST;
            return;
        }
        TdarUser user = getAuthenticatedUser();
        if (user.equals(getInvoice().getOwner())) {
            return;
        }
        if (getAuthorizationService().cannot(InternalTdarRights.VIEW_BILLING_INFO, user)) {
            addActionError(getText("viewInvoiceAction.not_allowed"));
            inputResultName = FORBIDDEN;
        }
    }

    @Action(value = "{id}", results = { @Result(name = SUCCESS, location = "view.ftl") })
    public String execute() {
        return SUCCESS;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getInputResultName() {
        return inputResultName;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

}

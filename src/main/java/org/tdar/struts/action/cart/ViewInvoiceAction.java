package org.tdar.struts.action.cart;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.interceptor.ValidationWorkflowAware;

@Component
@Scope("prototype")
@HttpsOnly
@ParentPackage("secured")
public class ViewInvoiceAction extends AuthenticationAware.Base implements Preparable, ValidationWorkflowAware {

    private static final long serialVersionUID = -8280706863708228864L;
    
    private Long id;
    
    private Invoice invoice;
    
    private String inputResultName;
    
    @Override
    public void prepare() {
        invoice = getGenericService().find(Invoice.class, id);
    }
    
    @Override
    public void validate() {
        if (invoice == null) {
            addActionError("No invoice found.");
            inputResultName = BAD_REQUEST;
            return;
        }
        TdarUser user = getAuthenticatedUser();
        if (user.equals(getInvoice().getOwner())) {
            return;
        }
        if (getAuthorizationService().cannot(InternalTdarRights.VIEW_BILLING_INFO, user)) {
            addActionError("You do not have permission to view this invoice.");
            inputResultName = FORBIDDEN;
        }
    }
    
    @Action("/cart/view")
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

}

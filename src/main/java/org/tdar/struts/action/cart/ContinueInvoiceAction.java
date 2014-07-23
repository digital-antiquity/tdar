package org.tdar.struts.action.cart;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.struts.action.AuthenticationAware;

@Component
@Scope("prototype")
@Namespace("/cart")
@ParentPackage("default")
public class ContinueInvoiceAction extends AuthenticationAware.Base {

    private static final long serialVersionUID = -5124643538589250147L;
    private Long invoiceId;

    public void validate() {
        Invoice invoice = getGenericService().find(Invoice.class, invoiceId);
        if (invoice == null) {
            addActionError(getText("cartController.invoice_expected_but_not_found"));
            return;
        }
        if (!invoice.isModifiable()) {
            addActionError(getText("cartController.cannot_modify"));
        }
        // FIXME: check invoice ownership or if the targeted user is correct?
        if (!invoice.getPaymentMethod().isCreditCard()) {
            addActionError(getText("cartController.valid_payment_method_is_required"));
        }
    }

    @Action(value = "continue",
            results = {
                    @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.CART_REVIEW_PURCHASE),
                    @Result(name = "success-unauth", type = REDIRECT, location = URLConstants.CART_REVIEW_UNAUTHENTICATED),
                    @Result(name = INPUT, type = REDIRECT, location = URLConstants.CART_ADD)
            })
    public String execute() {
        getLogger().debug("setting invoice id {} on the session", invoiceId);
        getSessionData().setInvoiceId(invoiceId);
        return isAuthenticated() ? SUCCESS : "success-unauth";
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

}

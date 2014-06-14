package org.tdar.struts.action.cart;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.struts.action.AuthenticationAware;

import java.util.Map;

@Results({
        @Result(name = "redirect-start", location = "/cart/new", type = "redirect")
})
public abstract class AbstractCartController extends AuthenticationAware.Base implements SessionAware{

    private Map<String, Object> session;

    public static final String PENDING_INVOICE_ID_KEY = "pending_invoice_id";
    private Invoice invoice;


    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    /**
     * Return a pending invoice if found in session scope
     *
     * @return
     */
    protected Invoice loadPendingInvoice() {
        Long invoiceId = (Long) session.get(PENDING_INVOICE_ID_KEY);
        return getGenericService().find(Invoice.class, invoiceId);
    }

    protected void storePendingInvoice(Invoice invoice) {
        session.put(PENDING_INVOICE_ID_KEY, invoice.getId());
    }

    /**
     * Remove invoice from session and this object but don't remove it from the database
     */
    protected void clearPendingInvoice() {
        invoice = null;
        session.remove(PENDING_INVOICE_ID_KEY);
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        getLogger().debug("set invoice called");
        this.invoice = invoice;
    }

}

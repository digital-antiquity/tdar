package org.tdar.struts.action.cart;

import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.struts.action.AuthenticationAware;

import java.util.Map;

@Results({
        @Result(name = "redirect-start", location = "/cart/new", type = "redirect")
})
public abstract class AbstractCartController extends AuthenticationAware.Base implements SessionAware, Preparable{

    //FIXME: should use session-scoped bean instead, but I don't like the idea of stuffing everything in SessionData object either. (http://docs.oracle.com/cd/A97688_16/generic.903/bp/j2ee.htm#1010654). Figure it out.

    //Session map provided by struts (because we implement SessionAware)
    private Map<String, Object> session;

    //Key that we use for storing the invoice ID in the session map
    public static final String PENDING_INVOICE_ID_KEY = "pending_invoice_id";

    //Invoice sitting in the user's 'cart'.  This is a pending invoice until the payment-processor contacts our REST endpoint and gives the OK
    private Invoice invoice;

    //Owner of the invoice. Typically the current user, though an administrator may create an invoice on behalf of owner.
    private TdarUser owner;
    private Long ownerId;

    @Override
    public final void setSession(Map<String, Object> session) {
        this.session = session;
    }

    /**
     * Return a pending invoice if found in session scope
     *
     * @return
     */
    protected final Invoice loadPendingInvoice() {
        Long invoiceId = (Long) session.get(PENDING_INVOICE_ID_KEY);
        return getGenericService().find(Invoice.class, invoiceId);
    }

    protected final void storePendingInvoice(Invoice invoice) {
        session.put(PENDING_INVOICE_ID_KEY, invoice.getId());
    }

    /**
     * Remove invoice from session and this object but don't remove it from the database
     */
    protected final void clearPendingInvoice() {
        invoice = null;
        session.remove(PENDING_INVOICE_ID_KEY);
    }

    public final Invoice getInvoice() {
        return invoice;
    }

    public final void setInvoice(Invoice invoice) {
        getLogger().debug("set invoice called");
        this.invoice = invoice;
    }

    @Override
    public void prepare() {
        invoice = loadPendingInvoice();
    }

    public final TdarUser getOwner() {
        return owner;
    }

    //subclasses may set the owner, but we don't want this coming from struts
    protected final void setOwner(TdarUser owner) {
        this.owner = owner;
    }

    public final Long getOwnerId() {
        return ownerId;
    }

    public final void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}

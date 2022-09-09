package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.tdar.UrlConstants;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.interceptor.ValidationWorkflowAware;

@Results({
        @Result(name = "redirect-start", location = UrlConstants.CART_ADD, type = TdarActionSupport.TDAR_REDIRECT)
})
/**
 * Base class for all cart based things.
 *
 */
public abstract class AbstractCartController extends AbstractAuthenticatableAction implements Preparable, ValidationWorkflowAware {

    private static final long serialVersionUID = -8162270388197212817L;

    public static final String REDIRECT_START = "redirect-start";
    public static final String SUCCESS_UNAUTHENTICATED = "success-unauthenticated";

    // Invoice sitting in the user's 'cart'. This is a pending invoice until the payment-processor contacts our REST endpoint and gives the OK
    private Invoice invoice;
    // list of billing accounts that the user may choose from when assigning the invoice
    private List<BillingAccount> accounts = new ArrayList<>();

    protected String inputResultName = INPUT;

    // FIXME: this is unsafe, depends on order of initialization
    private AntiSpamHelper h = new AntiSpamHelper();

    /**
     * Return a pending invoice if found in session scope
     * 
     */
    protected Invoice loadPendingInvoice() {
        Long invoiceId = getSessionData().getInvoiceId();
        getLogger().debug("INVOICE ID: {}", invoiceId);
        Invoice invoice = getGenericService().find(Invoice.class, invoiceId);
        return invoice;
    }

    protected void storePendingInvoice(Invoice invoice) {
        getSessionData().setInvoiceId(invoice.getId());
    }

    /**
     * Add actionError if the specified object is null
     * 
     * @param object
     *            object to check for being null
     * @param textKey
     *            key of error message (the value supplied to to {@link #getText(String, Object...)}
     *
     * @return true if object is valid, otherwise false
     *
     */
    protected final boolean validateNotNull(Object object, String textKey) {
        if (object == null) {
            addActionError(getText(textKey));
        }
        return object != null;
    }

    /**
     * Remove invoice from session and this object but don't remove it from the database
     */
    protected void clearPendingInvoice() {
        invoice = null;
        getSessionData().setInvoiceId(null);
    }

    // final for a reason (if you override this you likely did it on accident)
    public final Invoice getInvoice() {
        return invoice;
    }

    // final for a reason (if you override this you likely did it on accident)
    public final void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    @Override
    public void prepare() {
        invoice = loadPendingInvoice();
    }

    /**
     * Validates an invoices. If invoice is not valid, this method adds an actionError <strong>and also changes the input result name to 'redirect-start'
     * </strong> (the assumption being that the user cannot correct the issue by doing anything other than going back to the start of the cart workflow).
     *
     * @return true if valid (invoice is not null), otherwise false
     */
    protected boolean validateInvoice() {
        if (!validateNotNull(invoice, "abstractCartController.select_invoice")) {
            inputResultName = REDIRECT_START;
            return false;
        }
        return true;
    }

    protected Long getInvoiceId() {
        return getSessionData().getInvoiceId();
    }

    /*
     * FIXME: I'm having second thoughts about this. The only alternative allen and I could think of was to
     * bypass validate() (i.e. don't add actionErrors to ensure the workflow interceptor calls execute), set a special
     * "thisActionIsCorrupt" flag, and check for that flag in execute() (returning "redirect-start"). That's a pretty hacky
     * solution but arguably much easier to follow than this oneif you're familar w/ the struts workflow. This is pretty opaque.
     * 
     * I think a better idea would be to have struts continue to short-ciruit with an "input" if it detects actionErrors
     * after validate(), but allow for customizing the result name to use (e.g. "unprepared") when struts detects actionErrors
     * after prepare() but *before* validate().
     * 
     * That's much less opaque, but now sure how to go about implementing that behavior.
     */
    public String getInputResultName() {
        return inputResultName;
    }

    public final AntiSpamHelper getH() {
        return h;
    }

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
    }

    public boolean isAccessionFeeEnabled() {
        return getTdarConfiguration().isAccessionFeesEnabled();
    }
}

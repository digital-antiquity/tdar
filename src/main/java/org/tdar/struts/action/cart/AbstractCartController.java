package org.tdar.struts.action.cart;

import java.util.HashSet;
import java.util.Set;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.data.AntiSpamHelper;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.interceptor.ValidationWorkflowAware;


@Results({
        @Result(name = "redirect-start", location = "/cart/new", type = "redirect")
})
/**
 * Base class for all cart based things. 
 *
 */
public abstract class AbstractCartController extends AuthenticationAware.Base implements Preparable, ValidationWorkflowAware {

    private static final long serialVersionUID = -8162270388197212817L;

    public static final String CART_NEW_LOCATION = "/cart/new";

    public static final String SUCCESS_UNAUTHENTICATED = "success-unauthenticated";

    // Invoice sitting in the user's 'cart'. This is a pending invoice until the payment-processor contacts our REST endpoint and gives the OK
    private Invoice invoice;
    // list of billing accounts that the user may choose from when assigning the invoice
    private Set<Account> accounts = new HashSet<>();

    protected String inputResultName = INPUT;

    // // Owner of the invoice. Typically the current user, though an administrator may create an invoice on behalf of owner.
    // private TdarUser owner = new TdarUser();
    // private Long ownerId;

    @Autowired
    protected transient RecaptchaService recaptchaService;
    // FIXME: this is unsafe, depends on order of initialization
    private AntiSpamHelper h = new AntiSpamHelper(recaptchaService);

    /**
     * Return a pending invoice if found in session scope
     * 
     * @return
     */
    protected Invoice loadPendingInvoice() {
        Long invoiceId = getSessionData().getInvoiceId();
        getLogger().debug("INVOICE ID: {}", invoiceId);
        return getGenericService().find(Invoice.class, invoiceId);
    }

    protected void storePendingInvoice(Invoice invoice) {
        getSessionData().setInvoiceId(invoice.getId());
    }

    /**
     * Add actionError if the specified object is null
     * 
     * @param object
     *            object to check for nulliosity
     * @param textKey
     *            key of error message (the value supplied to to {@link #getText(String, Object...)}
     */
    protected final void validateNotNull(Object object, String textKey) {
        if (object == null) {
            addActionError(getText(textKey));
        }
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

    protected boolean isValidInvoice() {
        if (invoice == null) {
            addActionError(getText("abstractCartController.select_invoice"));
            inputResultName = "redirect-start";
            return false;
        }
        return true;
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

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
}

package org.tdar.struts.action.cart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.service.billing.InvoiceService;
import org.tdar.struts.data.PricingOption.PricingType;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.GetOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;

/**
 * Manages all aspects of creating and updating an invoice for files and storage for unauthenticated or authenticated users.
 * 
 * Hands off to the CartController for final payment, or the CartBillingAccountController for updating billing information.
 */
@Component
@Scope("prototype")
@HttpsOnly
@Namespace("/cart")
public class InvoiceController extends AbstractCartController {

    private static final long serialVersionUID = -9156927670405819626L;

    public static final String PENDING_INVOICE_ID_KEY = "pending_invoice_id";

    private List<BillingActivity> activities = new ArrayList<>();

    private List<Long> extraItemIds = new ArrayList<>();
    private List<Integer> extraItemQuantities = new ArrayList<>();

    private String code;

    private PricingType pricingType = null;
    private Long accountId;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient InvoiceService invoiceService;

    private Collection<BillingItem> extraBillingItems;

    /**
     * This is the first step of the purchase process. The user specifies the number of files/mb or chooses a
     * predefined small/medium/large bundle. The user may also specify a voucher/discount code.
     * <p/>
     * Re: the client page. This is actually a form. You can submit the form by clicking the "Next/Save" button. or by clicking on one of the small/medium/large
     * buttons. In either case the form submits a POST request to /cart/process-choice.
     * 
     * @return
     */
    @Actions(value = {
            @Action("add"),
            @Action(value = "modify", results = { @Result(name = SUCCESS, location = "add.ftl") })
    })
    @SkipValidation
    // @GetOnly
    public String execute() {
        return SUCCESS;
    }

    /**
     * process new/updated invoice request
     * 
     * @return
     */
    @Action(value = "process-choice",
            results = {
                    @Result(name = INPUT, location = "add.ftl"),
                    @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.CART_REVIEW_PURCHASE),
                    @Result(name = SUCCESS_UNAUTHENTICATED, type = REDIRECT, location = URLConstants.CART_REVIEW_UNAUTHENTICATED)
            // @Result(name = "authenticated", location = "/cart/show-billing-accounts", type = "redirect")
            })
    @DoNotObfuscate(reason = "unnecessary")
    @PostOnly
    public String processInvoice() {
        try {
            setInvoice(invoiceService.processInvoice(getInvoice(), getAuthenticatedUser(), code, extraBillingItems, pricingType, accountId));
        } catch (Exception trex) {
            addActionError(trex.getLocalizedMessage());
            return INPUT;
        }
        storePendingInvoice(getInvoice());
        return isAuthenticated() ? SUCCESS : SUCCESS_UNAUTHENTICATED;
    }

    /**
     * Show the pending invoice review page, potentially show login/auth form.
     * 
     * @return
     */
    @Action(URLConstants.CART_REVIEW_UNAUTHENTICATED)
    @GetOnly
    public String showInvoice() {
        if (getInvoice() == null) {
            return "redirect-start";
        }
        return SUCCESS;
    }

    public List<BillingActivity> getActivities() {
        return activities;
    }

    public PricingType getPricingType() {
        return pricingType;
    }

    public void setPricingType(PricingType pricingType) {
        this.pricingType = pricingType;
    }

    public List<Long> getExtraItemIds() {
        return extraItemIds;
    }

    public void setExtraItemIds(List<Long> extraItemIds) {
        this.extraItemIds = extraItemIds;
    }

    public List<Integer> getExtraItemQuantities() {
        return extraItemQuantities;
    }

    public void setExtraItemQuantities(List<Integer> extraItemQuantities) {
        this.extraItemQuantities = extraItemQuantities;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void prepare() {
        setupActivities();
        // look for pending invoice in the session.
        Invoice persistedInvoice = loadPendingInvoice();
        if (persistedInvoice != null) {
            if (persistedInvoice.isModifiable()) {
                setInvoice(persistedInvoice);
            } else {
                // if invoice is not modifiable, we assume user is creating multiple invoices in the same session (which is rare but legit)
                clearPendingInvoice();
            }
        }

        // set default payment method
        if (getInvoice() != null) {
            getInvoice().setDefaultPaymentMethod();
        }

        extraBillingItems = invoiceService.lookupExtraBillingActivities(extraItemIds, extraItemQuantities);

    }

    public AuthorizedUser getBlankAuthorizedUser() {
        AuthorizedUser user = new AuthorizedUser();
        user.setUser(new TdarUser());
        return user;
    }

    public List<PaymentMethod> getAllPaymentMethods() {
        if (isBillingManager()) {
            return Arrays.asList(PaymentMethod.values());
        } else {
            return Arrays.asList(PaymentMethod.CREDIT_CARD);
        }
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    void setupActivities() {
        // we only care about the production+active activities
        getActivities().addAll(invoiceService.getActiveBillingActivities());
    }

    /**
     * For all actions in this controller, we only want to deal with non-finalized invoices (e.g. invoice.isModifiable() == false). If we detect that
     * the invoice is finalized we consider this action to be invalid.
     */
    @Override
    public void validate() {
        if (!isValidInvoice()) {
            return;
        }
        // rule: invoice must not be finalized
        if (!getInvoice().isModifiable()) {
            addActionError(getText("cartController.cannot_modify"));
        }

        // rule: invoice.paymentMethod required (prepare() should set automatically if only one option exists)
        if (getInvoice().getPaymentMethod() == null) {
            addActionError(getText("cartController.valid_payment_method_is_required"));
        }
    }

}

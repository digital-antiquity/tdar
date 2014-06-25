package org.tdar.struts.action.cart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.InvoiceService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.PricingOption.PricingType;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@Results({
        @Result(name = TdarActionSupport.RESULT_REDIRECT_START, location = TdarActionSupport.LOCATION_START, type = "redirect"),
})
@HttpsOnly
public class UnauthenticatedCartController extends AbstractCartController {

    /*
     * 
     * ** Overview of Cart controller actions & results ***
     * 
     * 
     * AbstractCartController results:
     * redirect-start: @"/cart/new"
     * 
     * UnauthenticatecCartController actions:
     * 
     * new
     * notes:
     * This is the first step of the purchase process. The user specifies the number of files/mb or chooses a
     * predefined small/medium/large bundle. The user may also specify a voucher/discount code.
     * 
     * Re: the client page. This is actually a form. You can submit the form by clicking the "Next/Save" button.
     * or by clicking on one of the small/medium/large buttons. In either case the form submits a POST request to
     * /cart/process-choice.
     * method: get
     * results:
     * success: new.ftl
     * 
     * process-choice
     * notes: process new/updated invoice request
     * method: post
     * results:
     * success: review,
     * input: new.ftl
     * 
     * review
     * notes: review invoice selection, potentially show login/auth form
     * method: get
     * results: (note that results are implicitly mapped by convention plugin)
     * success: review.ftl
     * authenticated: review-authenticated.ftl
     * 
     * CartApiController actions:
     * 
     * api
     * notes: calculate estimated price when user specifies custom files/mb
     * method: post (fixme: could be GET to allow for caching, or ported to javascript outright)
     * results:
     * success: jsonResult
     * input: jsonResult(code: BAD_REQUEST)
     * 
     * 
     * polling-check
     * notes:
     * Indicates whether the external payment process is complete. When complete, the calling
     * page should update the display and/or do a "client-side redirect" to landing page.
     * method: post
     * results:
     * success: jsonResult
     * input: jsonResult(code: BAD_REQUEST)
     * 
     * LoginController actions:
     * 
     * /login/process-login
     * notes: centralized login handling. Redirect back to cart page upon success
     * method: post
     * results:
     * input: @"/cart/review?loginUsername"
     * authenticated: @"/cart/show-billing-accounts"
     * redirect: httpHeader( code: BAD_REQUEST, errorMessage:"returnUrl not expected for login from cart")
     * 
     * CartProcessRegistrationAction:
     * 
     * process-registration
     * notes:
     * Process the user registration. This action subclasses AbstractCartAction so that it can
     * gracefully render INPUT result. e.g. continue to show invoice detail, owner, subtotal, etc.
     * method: post
     * results:
     * input:review.ftl
     * success:@"/cart/process-payment-request"
     * 
     * CartBillingAccountController actions:
     * 
     * process-billing-account-choice
     * notes: assign invoice to (pre-existing or new) billing account.
     * method: post
     * results:
     * success: @"/cart/process-payment-request"
     * input: show-billing-accounts.ftl
     * 
     * CartController actions:
     * 
     * simple
     * notes: fixme: Not sure what this action does (or did). I think it's obviated by /cart/review
     * method: get
     * results:
     * success: (unmapped - no result specified)
     * simple: (unmapped - result value never returned by action)
     * error: freemarkerhttp("/content/errors/error.ftl")
     * exception (via uncaught TdarRecoverableRuntimeException): freemarkerhttp("/content/errors/error.ftl)
     * 
     * process-payment-request
     * FIXME: This action (and client-side functionality) is incomplete! These comments describe the desired behavior.
     * notes:
     * This is the 'launchpad' page were we hand-off control to the external payment processor. The action itself
     * does very little -- most of functionality exists client-side via javascript and ajax.
     * 
     * The user initiates the payment process by clicking on a button that spawns a child window/tab. This child window
     * points to an external url hosted by the payment processing company (i.e. Nelnet) and this host facilitates
     * the entire payment process. Once the process is complete, the child window prompts the user to close the
     * child window.
     * 
     * Meanwhile, the tdar-hosted parent page ("/cart/process-payment-request") polls the "/cart/polling-check"
     * action to determine if the transaction is complete (successful, cancelled, or failed). When complete, the
     * page performs a "client-side redirect" to change browser location to an appropriate landing page.
     * 
     * process-external-payment-response
     * notes:
     * this is the endpoint used by the external payment processor to indicate the result of the
     * pending transaction.
     * fixme: move to CartApiController
     * method: post
     * results:
     * invoice:cc-result.ftl (fixme: should be streamResult)
     */

    private static final String REVIEW = "review";

    private static final long serialVersionUID = -9156927670405819626L;

    public static final String SIMPLE = "simple";
    public static final String PENDING_INVOICE_ID_KEY = "pending_invoice_id";

    private List<BillingActivity> activities = new ArrayList<>();
    // private Long id;
    private Long accountId;

    private List<Long> extraItemIds = new ArrayList<>();
    private List<Integer> extraItemQuantities = new ArrayList<>();

    private PricingType pricingType = null;
    private String code;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient InvoiceService cartService;

    @Autowired
    private transient AccountService accountService;

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
            @Action("new"),
            @Action(value = "modify", results = { @Result(name = SUCCESS, location = "new.ftl") })
    })
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
                    @Result(name = INPUT, location = "new.ftl"),
                    @Result(name = SUCCESS, type = REDIRECT, location = REVIEW)
            // @Result(name = "authenticated", location = "/cart/show-billing-accounts", type = "redirect")
            })
    // FIXME: pretty sure that code redemption is broken. e.g. what if user redeems a code and then wants to make changes to their order?
    @DoNotObfuscate(reason = "unnecessary")
    @PostOnly
    public String preview() {
        try {
            setInvoice(cartService.processInvoice(getInvoice(), getAuthenticatedUser(), code, extraItemIds, extraItemQuantities, pricingType,
                    accountId));
        } catch (Exception trex) {
            addActionError(trex.getLocalizedMessage());
            return INPUT;
        }

        storePendingInvoice(getInvoice());

        return SUCCESS;
    }

    /**
     * Show the pending invoice review page, potentially show login/auth form.
     * 
     * @return
     */
    @Action(REVIEW)
    // @GetOnly
    //FIXME: actions that service GET request should always be idempotent (writeable session on a GET request is a "tdar code smell")
    //FIXME: This action facilitates two distinct steps in the workflow: the "review your filecount/mb selection" page and the  "choose a billing account" page. They should be broken out into two actions.
    public String showInvoice() {
        // todo: if not authenticated, render the review page w/ signup/login form
        if (getInvoice() == null) {
            return "redirect-start";
        }

        //the view layer relies on invoice.isProxy(),  when in-turn relies on invoice.owner and invoice.transactedBy
        if (getAuthenticatedUser() != null) {
            getAccounts().addAll(accountService.listAvailableAccountsForCartAccountSelection(getInvoice().getOwner(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
            getLogger().debug("accounts; {}", getAccounts());
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
            setInvoice(persistedInvoice);
        }

        // set default
        if (getInvoice() != null && getInvoice().getPaymentMethod() == null) {
            getInvoice().setPaymentMethod(PaymentMethod.CREDIT_CARD);
        }

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
        for (BillingActivity activity : cartService.getActiveBillingActivities()) {
            // if (activity.isProduction()) {
            getActivities().add(activity);
            // }
        }
    }

    /**
     * For all actions in this controller, we only want to deal with non-finalized invoices (e.g. invoice.isModifiable() == false). If we detect that
     * the invoice is finalized we consider this action to be invalid.
     */
    @Override
    public void validate() {
        if (getInvoice() == null)
            return;

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
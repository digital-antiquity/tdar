package org.tdar.struts.action.cart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.InvoiceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.data.PricingOption.PricingType;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.ValidationAware;

@Component
@Scope("prototype")
@Results({ @Result(name = "redirect-start", location = "/cart/new", type = "redirect") })
public class UnauthenticatedCartController extends AuthenticationAware.Base implements Preparable, ValidationAware, SessionAware {

    /*
     * Workflow:
     * - add
     * - api (AJAX call for calculations)
     * - review
     * - finalReview
     * - poll-order
     * - order-complete [success: order-complete-success, cancel:order-complete-cancel]
     * - process-choice ==> [input:add, success:review]
     * - register => [input:review, success:finalReview]
     * - login => [input:review, success:finalReview]
     */

    private static final long serialVersionUID = -9156927670405819626L;

    public static final String SIMPLE = "simple";
    public static final String PENDING_INVOICE_ID_KEY = "pending_invoice_id";

    private List<BillingActivity> activities = new ArrayList<>();
    // private Long id;
    private Long accountId;

    private Invoice invoice;
    private List<Long> extraItemIds = new ArrayList<>();
    private List<Integer> extraItemQuantities = new ArrayList<>();
    private TdarUser owner;

    private PricingType pricingType = null;
    private String code;

    // todo: for consistency it would be better to use spring to either a) autowire a session-scoped invoice, or b) put the pending invoice in sessionData
    private Map<String, Object> session;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient InvoiceService cartService;



    /**
     * Show buyable items and pricing
     * 
     * @return
     */
    @Action("new")
    public String execute()
    {
        return SUCCESS;
    }

    /**
     * Process cart selection. If successful, display the pending invoice.
     * 
     * @return
     */
    @Action(value = "process-choice",
            results = {
                    @Result(name = INPUT, location = "new.ftl"),
                    // @Result(name = SUCCESS, type=REDIRECT, location = "review?id=${invoice.id}"),
                    @Result(name = SUCCESS, type = REDIRECT, location = "review"),

            })
    // FIXME: pretty sure that code redemption is broken. e.g. what if user redeems a code and then wants to make changes to their order?
    // FIXME: we want to avoid writeable sessions on unauthenticated requests. Consider storing transient invoice in httpSession, and persist only after we
    // authenticate
    // @WriteableSession
    public String preview() {
        try {
            invoice = cartService.processInvoice(invoice, getAuthenticatedUser(), getOwner(), code, extraItemIds, extraItemQuantities, pricingType, accountId);
        } catch (TdarRecoverableRuntimeException trex) {
            addActionError(trex.getMessage());
            return "input";
        }
        storePendingInvoice(invoice);
        return SUCCESS;
    }

    /**
     * Show the pending invoice.
     * 
     * @return
     */
    @Action("review")
    public String showInvoice() {
        // todo: check for transient invoice in session. If not found, add actionError and redirect to starting page
        // todo: if authenticated, load existing billing accounts then render review page w/ billing-account-edit form
        // todo: if not authenticated, render the review page w/ signup/login form
        if (invoice == null) {
            return "redirect-start";
        }
        if (isAuthenticated()) {
            return "authenticated";
        }
        return SUCCESS;
    }

    private boolean isValid(TdarUser person) {
        boolean valid = true;
        if (person == null) {
            addActionMessage(getText("cartController.no_person"));
            valid = false;
        }
        if (StringUtils.isEmpty(person.getUsername())) {
            valid = false;
        }
        // FIXME: add more server-side validation
        return valid;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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

    public TdarUser getOwner() {
        return owner;
    }

    public void setOwner(TdarUser owner) {
        this.owner = owner;
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

    /**
     * This method is invoked when the paramsPrepareParamsInterceptor stack is
     * applied. It allows us to fetch an entity from the database based on the
     * incoming resourceId param, and then re-apply params on that resource.
     * 
     * @see <a href="http://blog.mattsch.com/2011/04/14/things-discovered-in-struts-2/">Things discovered in Struts 2</a>
     */
    @Override
    public void prepare() {
        setupActivities();
        // look for pending invoice in the session
        // p = getGenericService().find(Invoice.class, getId());
        invoice = loadPendingInvoice();
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
            if (activity.isProduction()) {
                getActivities().add(activity);
            }
        }
    }

    /**
     * For all actions in this controller, we only want to deal with non-finalized invoices (e.g. invoice.isModifiable() == false). If we detect that
     * the invoice is finalized we consider this action to be invalid.
     */
    @Override
    public void validate() {
        if (invoice == null)
            return;
        if (!getInvoice().isModifiable()) {
            addActionError(getText("cartController.cannot_modify"));
        }
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    /**
     * Return a pending invoice if found in session scope
     * 
     * @return
     */
    private Invoice loadPendingInvoice() {
        Long invoiceId = (Long) session.get(PENDING_INVOICE_ID_KEY);
        return getGenericService().find(Invoice.class, invoiceId);
    }

    void storePendingInvoice(Invoice invoice) {
        session.put(PENDING_INVOICE_ID_KEY, invoice.getId());
    }

    /**
     * Remove invoice from session and this object but don't remove it from the database
     */
    void clearPendingInvoice() {
        invoice = null;
        session.remove(PENDING_INVOICE_ID_KEY);
    }

}
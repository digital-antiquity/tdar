package org.tdar.struts.action.cart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opensymphony.xwork2.ValidationAware;
import org.apache.struts2.convention.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.InvoiceService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.PricingOption.PricingType;

import com.opensymphony.xwork2.Preparable;

@Results({@Result(name="redirect-start", location="/cart/new", type="redirect")})
public class UnauthenticatedCartController extends AuthenticationAware.Base implements Preparable {

    /*
     * Workflow:
     *  - add
     *  - api (AJAX call for calculations)
     *  - review
     *  - finalReview
     *  - poll-order
     *  - order-complete [success: order-complete-success, cancel:order-complete-cancel]
     *  - process-choice ==> [input:add, success:review]
     *  - register => [input:review, success:finalReview]
     *  - login => [input:review, success:finalReview]
     */

    private static final long serialVersionUID = -9156927670405819626L;

    public static final String SIMPLE = "simple";

    private List<BillingActivity> activities = new ArrayList<>();
    private Long id;
    private Long accountId;

    private Invoice invoice;
    private List<Long> extraItemIds = new ArrayList<Long>();
    private List<Integer> extraItemQuantities = new ArrayList<Integer>();
    private TdarUser owner;
    private PricingType pricingType = null;
    private String code;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient InvoiceService cartService;

    @Autowired
    private transient AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    /**
     * Show buyable items and pricing
     * @return
     */
    @Action("new")
    public String execute()
    {
        return SUCCESS;
    }

    /**
     * Process cart selection.  If successful, display the pending invoice.
     *
     * @return
     */
    @Action(value = "process-choice",
            results = {
                    @Result(name = INPUT, location = "new.ftl"),
                    @Result(name = SUCCESS, type=REDIRECT, location = "review?id=${invoice.id}") })
    public String preview() {
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(getText("cartController.cannot_modify"));
        }
        try {
            cartService.processInvoice(invoice, getAuthenticatedUser(), getOwner(), code, extraItemIds, extraItemQuantities, pricingType, accountId);
        } catch (Exception e) {
            addActionErrorWithException(e.getMessage(), e);
        }
        return getActionErrors().isEmpty() ? SUCCESS : INPUT;
    }

    /**
     * Show the pending invoice.
     * @return
     */
    @Action("review")
    public String showInvoice() {
        String result = "redirect-start";
        //todo: check for transient invoice in session. If not found, add actionError and redirect to starting page
        //todo: if authenticated, load existing billing accounts then render review page w/ billing-account-edit form
        //todo: if not authenticated, render the review page w/ signup/login form
        if(isAuthenticated() ) {
            result = "authenticated";
        } else {
            result = "success";
        }
        return result;
    }


    public Invoice getInvoice() {
        if (invoice == null) {
            setInvoice(new Invoice());
        }

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
        Invoice p = null;
        setupActivities();
        if (Persistable.Base.isNotNullOrTransient(getInvoice())) {
            getLogger().error("item id should not be set yet -- persistable.id:{}\t controller.id:{}", getInvoice().getId(), getId());
        }
        if (Persistable.Base.isNullOrTransient(getId())) {
            setInvoice(new Invoice());
        } else {

            p = getGenericService().find(Invoice.class, getId());
            setInvoice(p);
        }

        if (!ADD.equals(getActionName())) {
            getLogger().info("id:{}, persistable:{}", getId(), p);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        //we only care about the production+active activities
        for(BillingActivity activity : cartService.getActiveBillingActivities()) {
            if(activity.isProduction()) {
                getActivities().add(activity);
            }
        }

    }


}
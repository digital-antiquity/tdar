package org.tdar.struts.action.cart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opensymphony.xwork2.ValidationAware;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
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

@Namespace("/cart")
public class UnauthenticatedCartController extends AuthenticationAware.Base implements Preparable {

    /*
     * Workflow:
     * 
     *  - add
     *  - api -- AJAX call for calculations
     *  - preview (save / POST) ==> review
     *  - login
     *  - CartController:
     *  - finalreview
     *  - payment processing ...
     */

    /*
    Workflow pt. 2

            cart/add
                "success" => cart/confirm
                "success-login" => cart/confirm
                "input" => cart/add

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
    @Action(value = "new", results = {@Result(name = SUCCESS, type = "freemarker", location = "edit.ftl") })
    public String execute()
    {
        setupActivities();
        return SUCCESS;
    }

    /**
     * Process cart selection
     *
     * @return
     */
    //todo: this should be 'save' or 'add-item'
    @Action(value = "preview",
            results = {
                    @Result(name = INPUT, type = "freemarker", location = "edit.ftl"),
                    @Result(name = SUCCESS, type=REDIRECT, location = "review?id=${invoice.id}") })
    public String preview() {
        setupActivities();
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(getText("cartController.cannot_modify"));
        }
        try {
            cartService.processInvoice(invoice, getAuthenticatedUser(), getOwner(), code, extraItemIds, extraItemQuantities, pricingType, accountId);
        } catch (Exception e) {
            addActionErrorWithException(e.getMessage(), e);
        }


        //TODO: if we put error during validate(), workflow interceptor will set result to be INPUT without calling this method
        return getActionErrors().isEmpty() ? SUCCESS : INPUT;
    }

    //TODO: try out convention result naming convention
    @Action(value = "review",
            results = {
                    @Result(name = "success", type = "freemarker", location = "simple.ftl"),
                    @Result(name = "authenticate", type="freemarker", location = "simple-with-auth.ftl")
            })
    public String showConfirmation() {
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(getText("cartController.cannot_modify"));
        }
        setupActivities();
        return SUCCESS;
    }

    @Action(value = SIMPLE, results = { @Result(name = SUCCESS, location = "simple.ftl") })
    public String simplePaymentProcess() throws TdarActionException {
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(getText("cartController.cannot_modify"));
        }
        if (getInvoice().getTransactionStatus() != TransactionStatus.PREPARED) {
            return ERROR;
        }

        return SUCCESS;
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
package org.tdar.struts.action.cart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
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
import org.tdar.struts.data.PricingOption;
import org.tdar.struts.data.PricingOption.PricingType;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/cart")
public class UnauthenticatedCartController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = -9156927670405819626L;

    public static final String SIMPLE = "simple";

    private List<BillingActivity> activities = new ArrayList<BillingActivity>();
    private Long id;

    public static final String SUCCESS_UPDATE_ACCOUNT = "success-update-account";
    public static final String SUCCESS_ADD_ACCOUNT = "success-add-account";
    private Invoice invoice;
    private Account account;
    public static final String SUCCESS_ADD_ADDRESS = "add-address";
    public static final String SUCCESS_ADD_PAY = "add-payment";
    public static final String INVOICE = "invoice";
    public static final String POLLING = "polling";
    private List<Long> extraItemIds = new ArrayList<Long>();
    private List<Integer> extraItemQuantities = new ArrayList<Integer>();
    private TdarUser owner;
    private String callback;
    private PricingType pricingType = null;
    private String code;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient InvoiceService cartService;

    @Autowired
    private transient AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    private Long lookupMBCount = 0L;
    private Long lookupFileCount = 0L;
    private List<PricingOption> pricingOptions = new ArrayList<PricingOption>();

    @Actions(value = { @Action(value = "new",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "freemarker", location = "edit.ftl") }),
            @Action(value = "modify",
                    interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
                    results = {
                            @Result(name = SUCCESS, type = "freemarker", location = "edit.ftl") }),
                    }
            )
            public String execute() {
        setActivities(cartService.getActiveBillingActivities());
        return SUCCESS;
    }

    @Action(value = "preview",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = INPUT, type = "freemarker", location = "edit.ftl"),
                    @Result(name = SUCCESS, type=TYPE_REDIRECT, location = "review?id=${invoice.id}") })
    @WriteableSession
    public String preview() {
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(getText("cartController.cannot_modify"));
        }
        setActivities(cartService.getActiveBillingActivities());
        try {
            cartService.processInvoice(invoice, getAuthenticatedUser(), getOwner(), code, extraItemIds, extraItemQuantities, pricingType);
        } catch (Exception e) {
            addActionErrorWithException(e.getMessage(), e);
        }

        return getActionErrors().isEmpty() ? SUCCESS : INPUT;
    }

    @Action(value = "review",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "freemarker", location = "simple.ftl") })
    @WriteableSession
    public String modify() {
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(getText("cartController.cannot_modify"));
        }
        setActivities(cartService.getActiveBillingActivities());
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "api",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "freemarker", location = "api.ftl", params = { "contentType", "application/json" }) })
    public String api() {
        if (isNotNullOrZero(lookupFileCount) || isNotNullOrZero(lookupMBCount)) {
            addPricingOption(cartService.getCheapestActivityByFiles(lookupFileCount, lookupMBCount, false));
            addPricingOption(cartService.getCheapestActivityByFiles(lookupFileCount, lookupMBCount, true));
            addPricingOption(cartService.getCheapestActivityBySpace(lookupFileCount, lookupMBCount));
        }
        return SUCCESS;
    }

    public boolean isNotNullOrZero(Long num) {
        if ((num == null) || (num < 1)) {
            return false;
        }
        return true;
    }

    private void addPricingOption(PricingOption incoming) {
        if (incoming == null) {
            return;
        }
        boolean add = true;

        for (PricingOption option : pricingOptions) {
            if ((option == null) || option.sameAs(incoming)) {
                add = false;
            }
        }
        if (add) {
            pricingOptions.add(incoming);
        }
    }

    @SkipValidation
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

    private String loadViewMetadata() {
        setAccount(cartService.getAccountForInvoice(getInvoice()));
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

    public void setActivities(List<BillingActivity> activities) {
        this.activities = activities;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Long getLookupMBCount() {
        return lookupMBCount;
    }

    public void setLookupMBCount(Long lookupMBCount) {
        this.lookupMBCount = lookupMBCount;
    }

    public Long getLookupFileCount() {
        return lookupFileCount;
    }

    public void setLookupFileCount(Long lookupFileCount) {
        this.lookupFileCount = lookupFileCount;
    }

    public List<PricingOption> getPricingOptions() {
        return pricingOptions;
    }

    public void setPricingOptions(List<PricingOption> pricingOptions) {
        this.pricingOptions = pricingOptions;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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
            // from a permissions standpoint... being really strict, we should mark this as read-only
            // getGenericService().markReadOnly(p);
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

}
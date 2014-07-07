package org.tdar.struts.action.cart;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.NotImplementedException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.dao.external.payment.nelnet.TransactionResponse;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.InvoiceService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.PricingOption.PricingType;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
public class CartController extends AbstractPersistableController<Invoice> implements ParameterAware {

    private static final String PROCESS_EXTERNAL_PAYMENT_RESPONSE = "process-external-payment-response";
    public static final String SUCCESS_COMPLETE = "success-complete";
    public static final String PROCESS_PAYMENT_REQUEST = "process-payment-request";
    public static final String SIMPLE = "simple";
    private static final long serialVersionUID = 1592977664145682926L;
    private List<BillingActivity> activities = new ArrayList<>();
    private Long accountId = -1L;
    private String billingPhone;
    public static final String SUCCESS_ADD_ACCOUNT = "success-add-account";
    private Account account;
    public static final String INVOICE = "invoice";
    public static final String POLLING = "polling";
    private Person owner;
    private String callback;
    private PricingType pricingType = null;
    private String code;
    private InputStream resultJson;
    private String redirectUrl;
    private Map<String, String[]> parameters;
    private boolean phoneRequired = false;
    private boolean addressRequired = false;
    private InputStream inputStream;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient InvoiceService cartService;

    @Override
    protected String save(Invoice persistable) {
        throw new NotImplementedException();
    }

    @Override
    protected void delete(Invoice persistable) {
        throw new NotImplementedException();
    }

    public boolean isNotNullOrZero(Long num) {
        if ((num == null) || (num < 1)) {
            return false;
        }
        return true;
    }

    /**
     * This method will take the response and prepare it for the CC processing transaction; admin(s) will have additional rights. Ultimately, the redirect URL
     * will open in a "new  frame" or window and the resulting window will poll for a response.
     * <p/>
     * <p/>
     * This is the 'launchpad' page were we hand-off control to the external payment processor. The action itself does very little -- most of functionality
     * exists client-side via javascript and ajax.
     * <p/>
     * The user initiates the payment process by clicking on a button that spawns a child window/tab. This child window points to an external url hosted by the
     * payment processing company (i.e. Nelnet) and this host facilitates the entire payment process. Once the process is complete, the child window prompts the
     * user to close the child window.
     * <p/>
     * Meanwhile, the tdar-hosted parent page ("/cart/process-payment-request") polls the "/cart/polling-check" action to determine if the transaction is
     * complete (successful, cancelled, or failed). When complete, the page performs a "client-side redirect" to change browser location to an appropriate
     * landing page.
     * 
     * @return
     * @throws TdarActionException
     */
    @SkipValidation
        @WriteableSession
    // @GetOnly
    @Action(value = PROCESS_PAYMENT_REQUEST, results = {
            @Result(name = SUCCESS, type = "redirect", location = "view?id=${invoice.id}"),
            @Result(name = POLLING, location = "polling.ftl"),
            @Result(name = ADD, type = TYPE_REDIRECT, location = "add"),
            @Result(name = SUCCESS_COMPLETE, type = "redirect", location = URLConstants.DASHBOARD)
    })
    @HttpsOnly
    public String processPayment() throws TdarActionException {
        if (Persistable.Base.isNullOrTransient(getInvoice())) {
            return ADD;
        }
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        Invoice invoice = getInvoice();
        if (!invoice.isModifiable()) {
            return SUCCESS;
        }

        if (invoice.getTransactionStatus().isComplete()) {
            return ERROR;
        }
        PaymentMethod paymentMethod = invoice.getPaymentMethod();
        if (paymentMethod == null) {
            throw new TdarRecoverableRuntimeException(getText("cartController.valid_payment_method_is_required"));
        }
        String invoiceNumber = invoice.getInvoiceNumber();

        setInvoice(cartService.processPayment(billingPhone, isPhoneRequired(), invoice, isAddressRequired(), paymentMethod));
        if (invoice.getTransactionStatus() == TransactionStatus.TRANSACTION_SUCCESSFUL) {
            return SUCCESS_COMPLETE;
        }
        setAccount(cartService.getAccountForInvoice(invoice));
        if (getAccount() != null) {
            setAccountId(getAccount().getId());
        }
        switch (paymentMethod) {
            case CHECK:
                break;
            case CREDIT_CARD:
                getGenericService().saveOrUpdate(invoice);
                try {
                    setRedirectUrl(paymentTransactionProcessor.prepareRequest(invoice));
                } catch (URIException e) {
                    getLogger().warn("error happend {}", e);
                }
                return POLLING;
            case INVOICE:
                invoice.setInvoiceNumber(invoiceNumber);
            case MANUAL:
                invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
                getGenericService().saveOrUpdate(invoice);
                return SUCCESS_ADD_ACCOUNT;
        }
        getSessionData().setInvoiceId(null);
        return SUCCESS;
    }

    /**
     * this is the endpoint used by the external payment processor to indicate the result of the
     * pending transaction.
     */
    // fixme: move to CartApiController
//    @SkipValidation
//    @PostOnly
//    @Action(value = PROCESS_EXTERNAL_PAYMENT_RESPONSE,
//            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
//            results = {
//                    @Result(name = INVOICE, type = "stream", params = { "contentType", "text/text", "inputName", "inputStream" })
//            })
    public String processPaymentResponse() throws TdarActionException {
        try {
            getLogger().trace("PROCESS RESPONSE {}", getParameters());
            TransactionResponse response = paymentTransactionProcessor.setupTransactionResponse(getParameters());
            // if transaction is valid (hashKey matches) then mark the session as writeable and go on
            cartService.processTransactionResponse(response, paymentTransactionProcessor);
        } catch (Exception e) {
            getLogger().error("{}", e);
        }
        inputStream = new ByteArrayInputStream("success".getBytes());
        return INVOICE;
    }

    @Override
    public Class<Invoice> getPersistableClass() {
        return Invoice.class;
    }

    @Override
    public String loadAddMetadata() {
        throw new NotImplementedException();
    }

    @Override
    public String loadEditMetadata() {
        setActivities(cartService.getActiveBillingActivities());
        return SUCCESS;
    }

    @Override
    public String loadViewMetadata() {
        setAccount(cartService.getAccountForInvoice(getInvoice()));
        return SUCCESS;
    }

    public Invoice getInvoice() {
        return getPersistable();
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        if (Persistable.Base.isNullOrTransient(getAuthenticatedUser())) {
            return false;
        }
        if (authorizationService.can(InternalTdarRights.VIEW_BILLING_INFO, getAuthenticatedUser())) {
            return true;
        }
        if (getAuthenticatedUser().equals(getInvoice().getOwner())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEditable() throws TdarActionException {
        if (Persistable.Base.isNullOrTransient(getAuthenticatedUser())) {
            return false;
        }
        if (authorizationService.can(InternalTdarRights.EDIT_BILLING_INFO, getAuthenticatedUser())) {
            return true;
        }
        if (getAuthenticatedUser().equals(getInvoice().getOwner())) {
            return true;
        }
        if (Persistable.Base.isNullOrTransient(getInvoice().getOwner()) && getInvoice().isModifiable()) {
            return true;
        }

        return false;
    }

    @Override
    public void prepare() {
        // Normally prepare() would get the persistable ID from the queryString. So we need to also look in the httpsession.
        if (Persistable.Base.isNotNullOrTransient(getSessionData().getInvoiceId())) {
            setId(getSessionData().getInvoiceId());
        }
        // now we can load the persistable
        super.prepare();

        // if an unauthenticated user created this invoice, we have some fields left to fill-in
        if (getInvoice().getOwner() == null) {
            getInvoice().setOwner(getAuthenticatedUser());
            // todo: need to also set this CartBillingAccountController if admin is creating an invoice
        }
        getInvoice().setTransactedBy(getAuthenticatedUser());

    }

    public void setInvoice(Invoice invoice) {
        setPersistable(invoice);
    }

    public List<BillingActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<BillingActivity> activities) {
        this.activities = activities;
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

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public void setParameters(Map<String, String[]> arg0) {
        this.parameters = arg0;
        getLogger().trace("parameters: {} ", getParameters());
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getBillingPhone() {
        return billingPhone;
    }

    public void setBillingPhone(String billingPhone) {
        this.billingPhone = billingPhone;
    }

    public boolean isPhoneRequired() {
        return phoneRequired;
    }

    public void setPhoneRequired(boolean phoneRequired) {
        this.phoneRequired = phoneRequired;
    }

    public boolean isAddressRequired() {
        return addressRequired;
    }

    public void setAddressRequired(boolean addressRequired) {
        this.addressRequired = addressRequired;
    }

    public PricingType getPricingType() {
        return pricingType;
    }

    public void setPricingType(PricingType pricingType) {
        this.pricingType = pricingType;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
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

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

}
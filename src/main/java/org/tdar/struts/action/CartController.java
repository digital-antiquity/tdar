package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionResponseTemplate;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.interceptor.PostOnly;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/cart")
public class CartController extends AbstractPersistableController<Invoice> implements ParameterAware {

    public static final String VALID_PHONE_NUMBER_IS_REQUIRED = "a valid phone number is required (212) 555-1212";
    public static final String ENTER_A_BILLING_ADDERESS = "please enter a billing adderess";
    public static final String CANNOT_MODIFY = "cannot modify existing invocie";
    public static final String VALID_PAYMENT_METHOD_IS_REQUIRED = "a valid payment method is required";
    private static final long serialVersionUID = 1592977664145682926L;
    private List<BillingActivity> activities = new ArrayList<BillingActivity>();
    private Long accountId = -1L;
    public static final String SUCCESS_UPDATE_ACCOUNT = "success-update-account";
    public static final String SUCCESS_ADD_ACCOUNT = "success-add-account";
    public static final String SUCCESS_ADD_ADDRESS = "add-address";
    public static final String SUCCESS_ADD_PAY = "add-payment";
    public static final String INVOICE = "invoice";
    public static final String POLLING = "polling";
    public static final String SPECIFY_SOMETHING = "please choose something";
    private String callback;

    @Autowired
    PaymentTransactionProcessor paymentTransactionProcessor;

    @Override
    protected String save(Invoice persistable) {
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(CANNOT_MODIFY);
        }

        if ((persistable.getNumberOfFiles() == null || persistable.getNumberOfFiles() < 1) &&
                (persistable.getNumberOfMb() == null || persistable.getNumberOfMb() < 1)) {
            throw new TdarRecoverableRuntimeException(SPECIFY_SOMETHING);
        }

        persistable.getItems().clear();
        BillingItem lowest = getAccountService().calculateCheapestActivities(getInvoice());
        getInvoice().getItems().add(lowest);
        if (accountId != -1) {
            getGenericService().find(Account.class, accountId).getInvoices().add(getInvoice());
        }
        // this may be 'different' from the owner
        getInvoice().setTransactedBy(getAuthenticatedUser());
        if (Persistable.Base.isNullOrTransient(getInvoice().getAddress())) {
            setSaveSuccessPath(SUCCESS_ADD_ADDRESS);
        }
        return SUCCESS;
    }

    @Override
    protected void delete(Invoice persistable) {
        // TODO Auto-generated method stub

    }

    @Actions({
            @Action(value = SUCCESS_ADD_ADDRESS),
            @Action(value = SUCCESS_ADD_PAY)
    })
    public String chooseAddress() throws TdarActionException {
        return super.view();
    }

    @SkipValidation
    @Action(value = "credit", results = { @Result(name = SUCCESS, location = "credit-info.ftl") })
    public String addPaymentMethod() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(CANNOT_MODIFY);
        }
        if (getInvoice().getTransactionStatus() != TransactionStatus.PREPARED) {
            return ERROR;
        }

        if (getInvoice().getAddress() == null) {
            throw new TdarRecoverableRuntimeException(ENTER_A_BILLING_ADDERESS);
        }

        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "polling-check", results = {
            @Result(name = WAIT, type = "freemarker", location = "polling-check.ftl", params = { "contentType", "application/json" }) })
    public String pollingCheck() throws TdarActionException {
        if (getInvoice().getTransactionStatus() != TransactionStatus.PENDING_TRANSACTION) {
            return ERROR;
        }
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        return WAIT;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = "save-billing-address", results = {
            @Result(name = SUCCESS_ADD_PAY, type = "redirect", location = "add-payment?id=${invoice.id}")
    })
    public String saveAddress() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(CANNOT_MODIFY);
        }
        getInvoice().setAddress(getGenericService().loadFromSparseEntity(getInvoice().getAddress(), Address.class));
        getGenericService().saveOrUpdate(getInvoice());

        return SUCCESS_ADD_PAY;
    }

    private String redirectUrl;
    private Map<String, String[]> parameters;
    private String successPath;

    /*
     * This method will take the response and prepare it for the CC processing transaction; admin(s) will have additional rights. Ultimately, the redirect URL
     * will open in a "new  frame" or window and the resulting window will poll for a response.
     */
    @SkipValidation
    @WriteableSession
    @Action(value = "process-payment-request", results = {
            @Result(name = SUCCESS, type = "redirect", location = "view?id=${invoice.id}"),
            @Result(name = POLLING, location = "polling.ftl"),
            @Result(name = SUCCESS_ADD_ACCOUNT, type = "redirect", location = "${successPath}")
    })
    public String processPayment() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (!getInvoice().isModifiable()) {
            return SUCCESS;
        }

        if (getInvoice().getTransactionStatus().isComplete()) {
            return ERROR;
        }
        getSuccessPathForPayment(); // initialize
        PaymentMethod paymentMethod = getInvoice().getPaymentMethod();
        if (paymentMethod == null) {
            throw new TdarRecoverableRuntimeException(VALID_PAYMENT_METHOD_IS_REQUIRED);
        }

        Long billingPhone = getInvoice().getBillingPhone();
        if (billingPhone == null || billingPhone.toString().length() < 10) {
            throw new TdarRecoverableRuntimeException(VALID_PHONE_NUMBER_IS_REQUIRED);
        }
        String invoiceNumber = getInvoice().getInvoiceNumber();
        String otherReason = getInvoice().getOtherReason();
        setInvoice(getGenericService().loadFromSparseEntity(getInvoice(), Invoice.class));
        getInvoice().setPaymentMethod(paymentMethod);
        getInvoice().setOtherReason(otherReason);
        getInvoice().setBillingPhone(billingPhone);
        getGenericService().saveOrUpdate(getInvoice());
        // finalize the cost and cache it
        getInvoice().setTotal(getInvoice().getCalculatedCost());
        logger.info("USER: {} IS PROCESSING TRANSACTION FOR: {} ", getInvoice().getId(), getInvoice().getTotal());
        getInvoice().setTransactionStatus(TransactionStatus.PENDING_TRANSACTION);

        switch (paymentMethod) {
            case CHECK:
                break;
            case CREDIT_CARD:
                getGenericService().saveOrUpdate(getInvoice());
                try {
                    setRedirectUrl(paymentTransactionProcessor.prepareRequest(getInvoice()));
                } catch (URIException e) {
                    logger.warn("error happend {}", e);
                }
                return POLLING;
            case INVOICE:
                getInvoice().setInvoiceNumber(invoiceNumber);
            case MANUAL:
                getInvoice().setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
                getGenericService().saveOrUpdate(getInvoice());
                return SUCCESS_ADD_ACCOUNT;
        }
        // validate transaction
        // run transaction
        return SUCCESS;
    }

    /*
     * This method will function under an exec-and-wait response model whereby once this is called, the transaction can move forward to the next step in the
     * process
     */
    @SkipValidation
    @PostOnly
    @Action(value = "process-external-payment-response",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = INVOICE, type = "redirect", location = "cc-result.ftl")
            })
    public String processPaymentResponse() throws TdarActionException {
        try {
            logger.trace("PROCESS RESPONSE {}", getParameters());
            NelNetTransactionResponseTemplate response = paymentTransactionProcessor.processResponse(getParameters());
            // if transaction is valid (hashKey matches) then mark the session as writeable and go on
            if (paymentTransactionProcessor.validateResponse(response)) {
                getGenericService().markWritable();
                Invoice invoice = paymentTransactionProcessor.locateInvoice(response);
                invoice = getGenericService().markWritable(invoice);
                paymentTransactionProcessor.updateInvoiceFromResponse(response, invoice);
                logger.info("processing payment response: {}  -> {} ", invoice, invoice.getTransactionStatus());
                getGenericService().saveOrUpdate(invoice);
            }
        } catch (Exception e) {
            logger.error("{}", e);
        }
        return INVOICE;
    }

    @Override
    public Class<Invoice> getPersistableClass() {
        return Invoice.class;
    }

    @Override
    public String loadAddMetadata() {
        return loadViewMetadata();
    }

    @Override
    public String loadViewMetadata() {
        setActivities(getAccountService().getActiveBillingActivities());
        return SUCCESS;
    }

    public Invoice getInvoice() {
        if (getPersistable() == null)
            setPersistable(createPersistable());

        return (Invoice) getPersistable();
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        if (Persistable.Base.isNullOrTransient(getAuthenticatedUser())) {
            return false;
        }
        if (getAuthenticationAndAuthorizationService().can(InternalTdarRights.VIEW_BILLING_INFO, getAuthenticatedUser())) {
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
        if (getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_BILLING_INFO, getAuthenticatedUser())) {
            return true;
        }
        if (getAuthenticatedUser().equals(getInvoice().getOwner())) {
            return true;
        }
        return false;
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
        if (isAdministrator()) {
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
        logger.info("parameters: {} ", getParameters());
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

    public String getSuccessPathForPayment() {
        successPath = String.format("/billing/choose?invoiceId=%d", getInvoice().getId());
        Account account = getGenericService().find(Account.class, accountId);
        if (account != null) {
            successPath = String.format("%s&id=%d", successPath, account.getId());
        }
        logger.info("successpath: {} ", successPath);
        return successPath;
    }
    
    public String getSuccessPath() {
        return successPath;
    }

    public void setSuccessPath(String successPath) {
        this.successPath = successPath;
    }

}
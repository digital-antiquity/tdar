package org.tdar.struts.action.cart;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingTransactionLog;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.util.Email;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.dao.external.payment.nelnet.TransactionResponse;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.InvoiceService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.PricingOption.PricingType;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.json.JsonLookupFilter;

@Component
@Scope("prototype")
@ParentPackage("secured")
@HttpsOnly
public class CartController extends AbstractPersistableController<Invoice> implements ParameterAware, SessionAware {

    public static final String SIMPLE = "simple";
    private static final long serialVersionUID = 1592977664145682926L;
    private List<BillingActivity> activities = new ArrayList<>();
    private Long accountId = -1L;
    private String billingPhone;
    public static final String SUCCESS_ADD_ACCOUNT = "success-add-account";
    private Account account;
    public static final String INVOICE = "invoice";
    public static final String POLLING = "polling";
    private List<Long> extraItemIds = new ArrayList<>();
    private List<Integer> extraItemQuantities = new ArrayList<>();
    private Person owner;
    private String callback;
    private PricingType pricingType = null;
    private String code;
    private InputStream resultJson;
    private Map<String, Object> session;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient InvoiceService cartService;

    @Autowired
    private transient XmlService xmlService;

    @Autowired
    private transient EmailService emailService;

    @Override
    protected String save(Invoice persistable) {
        throw new NotImplementedException();
    }

    @Override
    protected void delete(Invoice persistable) {
        // TODO Auto-generated method stub

    }


    public boolean isNotNullOrZero(Long num) {
        if ((num == null) || (num < 1)) {
            return false;
        }
        return true;
    }



    /**
     * I have no idea what this action does and where it fits within the larger process.
     * @return
     * @throws TdarActionException
     */
    @SkipValidation
    @Action(value="simple", results={@Result(name="simple", location="review-authenticated.ftl")})
    @WriteableSession
    public String simplePaymentProcess() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException(getText("cartController.cannot_modify"));
        }
        if (getInvoice().getOwner() == null) {
            //FIXME: confirm that owner can still be set by billing-admin as someone else
            cartService.updateOwner(getInvoice(), getAuthenticatedUser());
        }
        if (getInvoice().getTransactionStatus() != TransactionStatus.PREPARED) {
            return ERROR;
        }
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "polling-check", results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }) })
    public String pollingCheck() throws TdarActionException, IOException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        
        setResultJson(new ByteArrayInputStream(xmlService.convertFilteredJsonForStream(getInvoice(), JsonLookupFilter.class, getCallback()).getBytes()));

        return SUCCESS;
    }

    private String redirectUrl;
    private Map<String, String[]> parameters;
    private String successPath;
    private boolean phoneRequired = false;
    private boolean addressRequired = false;

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
        Invoice invoice = getInvoice();
        if (!invoice.isModifiable()) {
            return SUCCESS;
        }

        if (invoice.getTransactionStatus().isComplete()) {
            return ERROR;
        }
        getSuccessPathForPayment(); // initialize
        PaymentMethod paymentMethod = invoice.getPaymentMethod();
        if (paymentMethod == null) {
            throw new TdarRecoverableRuntimeException(getText("cartController.valid_payment_method_is_required"));
        }

        Long phone = null;
        if (StringUtils.isNotBlank(billingPhone)) {
            phone = Long.parseLong(billingPhone.replaceAll("\\D", ""));
        }

        if (isPhoneRequired() && ((phone == null) || (phone.toString().length() < 10))) {
            throw new TdarRecoverableRuntimeException(getText("cartController.valid_phone_number_is_required"));
        }

        invoice.setAddress(getGenericService().loadFromSparseEntity(invoice.getAddress(), Address.class));
        if (isAddressRequired() && Persistable.Base.isNullOrTransient(invoice.getAddress())) {
            throw new TdarRecoverableRuntimeException(getText("cartController.a_biling_address_is_required"));
        }

        String invoiceNumber = invoice.getInvoiceNumber();
        String otherReason = invoice.getOtherReason();
        setInvoice(getGenericService().loadFromSparseEntity(invoice, Invoice.class));
        invoice.setPaymentMethod(paymentMethod);
        invoice.setOtherReason(otherReason);
        invoice.setBillingPhone(phone);
        getGenericService().saveOrUpdate(invoice);
        // finalize the cost and cache it
        invoice.markFinal();
        getLogger().info("USER: {} IS PROCESSING TRANSACTION FOR: {} ", invoice.getId(), invoice.getTotal());

        // if the discount brings the total cost down to 0, then skip the credit card process
        if ((invoice.getTotal() <= 0) && CollectionUtils.isNotEmpty(invoice.getItems())) {
            if (Persistable.Base.isNotNullOrTransient(invoice.getCoupon())) {
                // accountService.redeemCode(invoice, invoice.getOwner(), invoice.getCoupon().getCode());
                cartService.checkCouponStillValidForCheckout(invoice.getCoupon(), invoice);
            }
            invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
            getGenericService().saveOrUpdate(invoice);
            return SUCCESS_ADD_ACCOUNT;
        } else {
            invoice.setTransactionStatus(TransactionStatus.PENDING_TRANSACTION);
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
                    @Result(name = INVOICE, location = "cc-result.ftl")
            })
    public String processPaymentResponse() throws TdarActionException {
        try {
            getLogger().trace("PROCESS RESPONSE {}", getParameters());
            TransactionResponse response = paymentTransactionProcessor.setupTransactionResponse(getParameters());
            // if transaction is valid (hashKey matches) then mark the session as writeable and go on
            if (paymentTransactionProcessor.validateResponse(response)) {
                getGenericService().markWritable();
                Invoice invoice = paymentTransactionProcessor.locateInvoice(response);

                BillingTransactionLog billingResponse = new BillingTransactionLog(xmlService.convertToJson(response), response.getTransactionId());
                billingResponse = getGenericService().markWritable(billingResponse);
                getGenericService().saveOrUpdate(billingResponse);
                if (invoice != null) {
                    invoice = getGenericService().markWritable(invoice);
                    Person p = invoice.getOwner();
                    boolean found = false;
                    Address addressToSave = response.getAddress();
                    for (Address address : p.getAddresses()) {
                        if (address.isSameAs(addressToSave)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        p.getAddresses().add(addressToSave);
                        getLogger().info(addressToSave.getAddressSingleLine());
                        getGenericService().saveOrUpdate(addressToSave);
                        invoice.setAddress(addressToSave);
                    }
                    paymentTransactionProcessor.updateInvoiceFromResponse(response, invoice);
                    invoice.setResponse(billingResponse);
                    getLogger().info("processing payment response: {}  -> {} ", invoice, invoice.getTransactionStatus());
                    Map<String, Object> map = new HashMap<>();
                    map.put("invoice", invoice);
                    map.put("date", new Date());
                    try {
                        List<Person> people = new ArrayList<>();
                        for (String email : StringUtils.split(getTdarConfiguration().getBillingAdminEmail(), ";")) {
                            if (StringUtils.isBlank(email)) {
                                continue;
                            }
                            Person person = new Person("Billing", "Info", email.trim());
                            getGenericService().markReadOnly(person);
                            people.add(person);
                        }
                        Email email = new Email();
                        email.setSubject(getText("cartController.subject"));
                        for (Person person : people) {
                            email.addToAddress(person.getEmail());
                        }
                        emailService.queueWithFreemarkerTemplate("transaction-complete-admin.ftl", map, email);
                    } catch (Exception e) {
                        getLogger().error("could not send email: {} ", e);
                    }
                    getGenericService().saveOrUpdate(invoice);
                }
            }
        } catch (Exception e) {
            getLogger().error("{}", e);
        }
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
        if (getPersistable() == null) {
            setPersistable(createPersistable());
        }

        return getPersistable();
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
        if (Persistable.Base.isNullOrTransient(getInvoice().getOwner()) && getInvoice().isModifiable()) {
            return true;
        }

        return false;
    }

    @Override
    public void prepare() {
        //Grab the invoice id from the session. Otherwise,  grab id from request params.
        Long invoiceId = (Long) session.get(UnauthenticatedCartController.PENDING_INVOICE_ID_KEY);
        setId(invoiceId);
        super.prepare();
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

    /**
     * Build a URL to the 'choose billing account' page.  If accountId is set, append as ID value in querystring. (used in add-invoice-to-account workflow.. i think)
     * @return
     */
    public String getSuccessPathForPayment() {
        successPath = String.format("/billing/choose?invoiceId=%d", getInvoice().getId());
        Account account = getGenericService().find(Account.class, accountId);
        if (account != null) {
            successPath = String.format("%s&id=%d", successPath, account.getId());
        }
        getLogger().trace("successpath: {} ", successPath);
        return successPath;
    }

    public String getSuccessPath() {
        return successPath;
    }

    public void setSuccessPath(String successPath) {
        this.successPath = successPath;
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

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.billing.TransactionType;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.external.payment.NelNetPaymentDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/cart")
public class CartController extends AbstractPersistableController<Invoice> {

    private static final long serialVersionUID = 1592977664145682926L;
    private List<BillingActivity> activities = new ArrayList<BillingActivity>();
    private Long creditCardNumber;
    private Integer verificationNumber;
    private Integer expirationYear;
    private Integer expirationMonth;
    private Long accountId = -1L;
    public static final String SUCCESS_UPDATE_ACCOUNT = "success-update-account";
    public static final String SUCCESS_ADD_ACCOUNT = "success-add-account";

    @Autowired
    // I will be pushed down into a service later on...
    NelNetPaymentDao nelnetPaymentDao;

    @Override
    protected String save(Invoice persistable) {
        if (!getInvoice().isModifiable()) {
            throw new TdarRecoverableRuntimeException("cannot modify");
        }
        for (BillingItem item : persistable.getItems()) {
            item.setActivity(getGenericService().loadFromSparseEntity(item.getActivity(), BillingActivity.class));
        }
        if (accountId != -1) {
            getGenericService().find(Account.class, accountId).getInvoices().add(getInvoice());
        }
        // this may be 'different' from the owner
        getInvoice().setTransactedBy(getAuthenticatedUser());
        return SUCCESS;
    }

    @Override
    protected void delete(Invoice persistable) {
        // TODO Auto-generated method stub

    }

    @SkipValidation
    @Action(value = "credit", results = { @Result(name = SUCCESS, location = "credit-info.ftl") })
    public String editCredit() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = "save-billing-address", results = {
            @Result(name = SUCCESS, type = "redirect", location = "view?id=${invoice.id}&review=true")
    })
    public String saveBilling() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getInvoice().setAddress(getGenericService().loadFromSparseEntity(getInvoice().getAddress(), Address.class));
        getGenericService().saveOrUpdate(getInvoice());

        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = "process-payment-info", results = {
            @Result(name = SUCCESS, type = "redirect", location = "view?id=${invoice.id}&review=true"),
            @Result(name = SUCCESS_UPDATE_ACCOUNT, type = "redirect", location = "/billing/choose?invoiceId=${invoice.id}&id=${accountId}"),
            @Result(name = SUCCESS_ADD_ACCOUNT, type = "redirect", location = "/billing/choose?invoiceId=${invoice.id}")
    })
    public String processPayment() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        String successReturn = SUCCESS_ADD_ACCOUNT;
        Account account = getGenericService().find(Account.class, accountId);
        if (account != null) {
            successReturn = SUCCESS_UPDATE_ACCOUNT;
        }

        TransactionType transactionType = getInvoice().getTransactionType();
        String invoiceNumber = getInvoice().getInvoiceNumber();
        String otherReason = getInvoice().getOtherReason();
        Integer billingPhone = getInvoice().getBillingPhone();
        setInvoice(getGenericService().loadFromSparseEntity(getInvoice(), Invoice.class));
        getInvoice().setTransactionType(transactionType);
        getInvoice().setOtherReason(otherReason);
        getInvoice().setBillingPhone(billingPhone);
        getGenericService().saveOrUpdate(getInvoice());
        //finalize the cost and cache it
        getInvoice().setTotal(getInvoice().getCalculatedCost());
        getInvoice().setTransactionStatus(TransactionStatus.PENDING_TRANSACTION);

        switch (transactionType) {
            case CHECK:
                break;
            case CREDIT_CARD:
                getInvoice().setCreditCardNumber(getCreditCardNumber());
                getInvoice().setVerificationNumber(getVerificationNumber());
                getInvoice().setExpirationMonth(getExpirationMonth());
                getInvoice().setExpirationYear(getExpirationYear());
                getGenericService().saveOrUpdate(getInvoice());
                nelnetPaymentDao.processInvoice(getInvoice());
                getGenericService().saveOrUpdate(getInvoice());
                break;
            case INVOICE:
                getInvoice().setInvoiceNumber(invoiceNumber);
                getGenericService().saveOrUpdate(getInvoice());
                break;
            case MANUAL:
                getInvoice().setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
                getGenericService().saveOrUpdate(getInvoice());
                return successReturn;
        }
        // validate transaction
        // run transaction
        return SUCCESS;
    }

    @Override
    public Class<Invoice> getPersistableClass() {
        return Invoice.class;
    }

    @Override
    public String loadAddMetadata() {
        return loadMetadata();
    }

    @Override
    public String loadMetadata() {
        setActivities(getAccountService().getActiveBillingActivities());
        return SUCCESS;
    }

    public Invoice getInvoice() {
        if (getPersistable() == null)
            setPersistable(createPersistable());

        return (Invoice) getPersistable();
    }

    @Override
    public boolean isEditable() throws TdarActionException {
        return true;
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

    public Long getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(Long creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public Integer getVerificationNumber() {
        return verificationNumber;
    }

    public void setVerificationNumber(Integer verificationNumber) {
        this.verificationNumber = verificationNumber;
    }

    public Integer getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(Integer expirationYear) {
        this.expirationYear = expirationYear;
    }

    public Integer getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(Integer expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public List<TransactionType> getAllTransactionTypes() {
        if (isAdministrator()) {
            return Arrays.asList(TransactionType.values());
        } else {
            return Arrays.asList(TransactionType.CREDIT_CARD);
        }
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
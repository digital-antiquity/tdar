package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionType;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.dao.external.auth.InternalTdarRights;
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

    @Override
    protected String save(Invoice persistable) {
        for (BillingItem item : persistable.getItems()) {
            item.setActivity(getGenericService().loadFromSparseEntity(item.getActivity(), BillingActivity.class));
        }
        if (accountId != -1) {
            getGenericService().find(Account.class, accountId).getInvoices().add(getInvoice());
        }
        return SUCCESS;
    }

    @Override
    protected void delete(Invoice persistable) {
        // TODO Auto-generated method stub

    }

    @SkipValidation
    @Action(value = "billing", results = { @Result(name = SUCCESS, location = "address-info.ftl") })
    public String editBillingAddress() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        return SUCCESS;
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
        if (Persistable.Base.isNotNullOrTransient(getInvoice().getAddress())) {
            getInvoice().setAddress(getGenericService().loadFromSparseEntity(getInvoice().getAddress(), Address.class));
        } else {
            getInvoice().getAddress().setType(AddressType.BILLING);
            getInvoice().getPerson().getAddresses().add(getInvoice().getAddress());
        }
        getGenericService().saveOrUpdate(getInvoice());
        // add the address to the person
        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = "process-payment-info", results = {
            @Result(name = SUCCESS, type = "redirect", location = "view?id=${invoice.id}&review=true"),
            @Result(name = SUCCESS_UPDATE_ACCOUNT, type = "redirect", location = "account?invoiceId=${invoice.id}&id=${accountId}"),
    })
    public String processPayment() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        TransactionType transactionType = getInvoice().getTransactionType();
        String invoiceNumber = getInvoice().getInvoiceNumber();
        String otherReason = getInvoice().getOtherReason();
        setInvoice(getGenericService().loadFromSparseEntity(getInvoice(), Invoice.class));
        getInvoice().setTransactionType(transactionType);
        getInvoice().setOtherReason(otherReason);
        switch (transactionType) {
            case CHECK:
                break;
            case CREDIT_CARD:
                break;
            case INVOICE:
                getInvoice().setInvoiceNumber(invoiceNumber);

            case MANUAL:
                break;
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
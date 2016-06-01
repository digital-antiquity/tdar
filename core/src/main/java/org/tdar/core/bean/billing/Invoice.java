package org.tdar.core.bean.billing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.utils.MathUtils;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * Represents a financial transaction to purchase space and number of resources for a given Account.
 * 
 * @author TDAR
 * @version $Rev$
 */
@Entity
@Table(name = "pos_invoice")
public class Invoice extends AbstractPersistable implements Updatable {

    private static final long serialVersionUID = -3613460318580954253L;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    @Column(name = "date_created", updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
    // the confirmation id for this invoice

    @Column(name = "transaction_id")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private PaymentMethod paymentMethod;

    private Long billingPhone;

    @Column(name = "account_type")
    @Length(max = 50)
    private String accountType;

    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = true, name = "owner_id")
    private TdarUser owner;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = true, name = "coupon_id")
    private Coupon coupon;

    @OneToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    private BillingTransactionLog response;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = true, name = "executor_id")
    private TdarUser transactedBy;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = true, name = "address_id", updatable = true)
    private Address address;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "invoice_id")
    private List<BillingItem> items = new ArrayList<>();

    @Column(name = "number_of_files")
    private Long numberOfFiles = 0L;

    @Column(name = "number_of_mb")
    private Long numberOfMb = 0L;

    @JsonView(JsonLookupFilter.class)
    private Float total;

    @Length(max = 25)
    private String invoiceNumber;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String otherReason;

    @Enumerated(EnumType.STRING)
    @JsonView(JsonLookupFilter.class)
    @Column(name = "transactionstatus", length = FieldLength.FIELD_LENGTH_25)
    private TransactionStatus transactionStatus = TransactionStatus.PREPARED;

    public Invoice() {
    }

    public Invoice(TdarUser owner, PaymentMethod method, Long numberOfFiles, Long numberOfMb, List<BillingItem> items) {
        markUpdated(owner);
        setPaymentMethod(method);
        setNumberOfFiles(numberOfFiles);
        setNumberOfMb(numberOfMb);
        if (CollectionUtils.isNotEmpty(items)) {
            getItems().addAll(items);
        }
    }

    /**
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated
     *            the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the transactionId
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * @param transactionId
     *            the transactionId to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public TdarUser getOwner() {
        return owner;
    }

    public void setOwner(TdarUser person) {
        this.owner = person;
    }

    public List<BillingItem> getItems() {
        return items;
    }

    public void setItems(List<BillingItem> items) {
        this.items = items;
    }

    public Float getTotal() {
        if (total == null) {
            return getCalculatedCost();
        }
        return total;
    }

    /* not sure if this can be 'private', but ideally only called by finalize method and hibernate internally */
    private void setTotal(Float total) {
        this.total = total;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod transactionType) {
        this.paymentMethod = transactionType;
    }

    public Long getTotalResources() {
        initTotals();
        return totalResources;
    }

    public Long getTotalSpaceInMb() {
        initTotals();
        return totalSpaceInMb;
    }

    public Long getTotalSpaceInBytes() {
        return getTotalSpaceInMb() * MathUtils.ONE_MB;
    }

    public Long getTotalNumberOfFiles() {
        initTotals();
        return totalFiles;
    }

    @SuppressWarnings("unchecked")
    private <T> T coalesce( T... items) {
        for (T i : items) {
            if (i != null) {
                return i;
            }
        }
        return null;
    }

    private void initTotals() {
        if (!initialized) {
            // if (coupon != null) {
            // calculatedCost -= coupon.getNumberOfDollars();
            // }

            Long discountedSpace = 0L;
            Long discountedFiles = 0L;
            if (coupon != null) {
                discountedFiles = coalesce(coupon.getNumberOfFiles(), 0L);
                discountedSpace = coalesce(coupon.getNumberOfMb(), 0L);
            }

            for (BillingItem item : getItems()) {
                BillingActivity activity = item.getActivity();
                Long numberOfFiles = coalesce(activity.getNumberOfFiles(), 0L);
                Long space = coalesce(activity.getNumberOfMb(), 0L);
                Long numberOfResources = coalesce(activity.getNumberOfResources(), 0L);

                if ((numberOfFiles > 0L) && (discountedFiles > 0L)) {
                    couponValue += activity.getPrice() * discountedFiles;
                    discountedFiles = 0L;
                }

                if ((space > 0L) && (discountedSpace > 0L)) {
                    couponValue += activity.getPrice() * discountedSpace;
                    discountedSpace = 0L;
                }
                long quantity = item.getQuantity().longValue();
                totalFiles += numberOfFiles * quantity;
                totalSpaceInMb += space * quantity;

                if (numberOfResources != null) {
                    totalResources += numberOfResources * quantity;
                }
                calculatedCost += item.getSubtotal();
                logger.trace("{}", this);
            }
            calculatedCost -= couponValue;
            if (calculatedCost < 0) {
                calculatedCost = 0f;
            }
            initialized = true;
        }
    }

    public void resetTransientValues() {
        totalResources = 0L;
        totalSpaceInMb = 0L;
        calculatedCost = 0F;
        totalResources = 0L;
        couponValue = 0F;
        initialized = false;
    }

    @JsonView(JsonLookupFilter.class)
    private transient Long totalResources = 0L;
    @JsonView(JsonLookupFilter.class)
    private transient Long totalSpaceInMb = 0L;
    @JsonView(JsonLookupFilter.class)
    private transient Long totalFiles = 0L;
    @JsonView(JsonLookupFilter.class)
    private transient Float calculatedCost = 0F;
    private transient boolean initialized = false;
    private transient Float couponValue = 0f;

    @Override
    public void markUpdated(TdarUser p) {
        if (getOwner() == null) {
            setOwner(p);
        }
        if (getTransactedBy() == null) {
            setTransactedBy(p);
        }

        if (dateCreated == null) {
            setDateCreated(new Date());
        }
    }

    public void setBillingPhone(Long billingPhone) {
        this.billingPhone = billingPhone;
    }

    public Long getBillingPhone() {
        return billingPhone;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getOtherReason() {
        return otherReason;
    }

    public void setOtherReason(String otherReason) {
        this.otherReason = otherReason;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public boolean isCancelled() {
        if (TransactionStatus.TRANSACTION_CANCELLED.equals(getTransactionStatus()) || TransactionStatus.TRANSACTION_FAILED.equals(getTransactionStatus())) {
            return true;
        }
        return false;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public TdarUser getTransactedBy() {
        return transactedBy;
    }

    public void setTransactedBy(TdarUser transactedBy) {
        this.transactedBy = transactedBy;
    }

    public boolean isModifiable() {
        return transactionStatus.isModifiable();
    }

    public Float getCalculatedCost() {
        initTotals();
        return calculatedCost;
    }

    public void setCalculatedCost(Float calculatedCost) {
        this.calculatedCost = calculatedCost;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public Long getNumberOfMb() {
        return numberOfMb;
    }

    public void setNumberOfMb(Long numberOfMb) {
        this.numberOfMb = numberOfMb;
    }

    public void markFinal() {
        setTotal(getCalculatedCost());
    }

    public BillingTransactionLog getResponse() {
        return response;
    }

    public void setResponse(BillingTransactionLog response) {
        this.response = response;
    }

    @Transient
    public boolean isProxy() {
        if (PersistableUtils.isNullOrTransient(owner) || PersistableUtils.isNullOrTransient(transactedBy)) {
            return false;
        }
        return ObjectUtils.notEqual(owner.getId(), transactedBy.getId());
    }

    @Override
    public Date getDateUpdated() {
        return dateCreated;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public Float getCouponValue() {
        return couponValue;
    }

    public void setCouponValue(Float couponValue) {
        this.couponValue = couponValue;
    }

    /**
     * NOTE: Returns transient data, not the actual data stored in this object. If invoked before
     * getCalculatedCost() or initTotals() is called, all these values will be zero/empty (except the ID).
     * 
     * Consider emitting persistable values in addition to transient values.
     */
    @Override
    public String toString() {
        return String.format("%s files, %s mb, %s resources [calculated cost: $%s] %s (id: %d)",
                totalFiles, totalSpaceInMb, totalResources, calculatedCost, coupon, getId());
    }

    public boolean hasValidValue() {
        logger.trace("files: {} space: {}", getNumberOfFiles(), getNumberOfMb());
        if (isLessThan(getNumberOfFiles(), 1) && isLessThan(getNumberOfMb(), 1) && (getCoupon() == null)) {
            return false;
        }
        return true;
    }

    public void setDefaultPaymentMethod() {
        if (paymentMethod == null) {
            setPaymentMethod(PaymentMethod.CREDIT_CARD);
        }
    }

    private boolean isLessThan(Long val, long comp) {
        if (val == null) {
            return false;
        }
        return val.longValue() < comp;
    }

}

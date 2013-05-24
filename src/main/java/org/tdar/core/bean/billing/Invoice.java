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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.payment.PaymentMethod;

/**
 * $Id$
 * 
 * Represents a credit card swipe to purchase space and number of resources for a given Account.
 * 
 * @author TDAR
 * @version $Rev$
 */
@Entity
@Table(name = "pos_invoice")
public class Invoice extends Base implements Updatable {

    private static final long serialVersionUID = -3613460318580954253L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Transient
    private final static String[] JSON_PROPERTIES = { "id", "paymentMethod", "transactionStatus", "totalFiles", "totalResources", "totalSpace",
            "calculatedCost", "total" };

    public enum TransactionStatus implements HasLabel {
        PREPARED("Prepared"),
        PENDING_TRANSACTION("Pending Transaction"),
        TRANSACTION_SUCCESSFUL("Transaction Successful"),
        TRANSACTION_FAILED("Transaction Failed"),
        TRANSACTION_CANCELLED("Transaction Cancelled");

        private String label;

        private TransactionStatus(String label) {
            this.label = label;
        }

        public boolean isComplete() {
            switch (this) {
                case PENDING_TRANSACTION:
                case PREPARED:
                    return false;
            }
            return true;
        }

        public boolean isInvalid() {
            switch (this) {
                case TRANSACTION_CANCELLED:
                case TRANSACTION_FAILED:
                    return true;
                default:
                    return false;
            }
        }

        public String getLabel() {
            return this.label;
        }
    }

    public Invoice() {
    }

    public Invoice(Person owner, PaymentMethod method, Long numberOfFiles, Long numberOfMb, List<BillingItem> items) {
        markUpdated(owner);
        setPaymentMethod(method);
        setNumberOfFiles(numberOfFiles);
        setNumberOfMb(numberOfMb);
        if (CollectionUtils.isNotEmpty(items)) {
            getItems().addAll(items);
        }
    }

    @NotNull
    @Column(name = "date_created")
    private Date dateCreated;
    // the confirmation id for this invoice

    @Column(name = "transaction_id")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 255)
    private PaymentMethod paymentMethod;

    private Long billingPhone;

    @Column(name = "account_type")
    @Length(max = 50)
    private String accountType;

    @Column(name = "transaction_date")
    private Date transactionDate;


    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "owner_id")
    @NotNull
    private Person owner;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = true, name = "coupon_id")
    private Coupon coupon;

    @OneToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    private BillingTransactionLog response;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "executor_id")
    @NotNull
    private Person transactedBy;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = true, name = "address_id", updatable = true)
    private Address address;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "invoice_id")
    private List<BillingItem> items = new ArrayList<BillingItem>();

    @Column(name = "number_of_files")
    private Long numberOfFiles = 0L;

    @Column(name = "number_of_mb")
    private Long numberOfMb = 0L;

    private Float total;

    @Length(max = 25)
    private String invoiceNumber;

    @Length(max = 255)
    private String otherReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "transactionstatus", length = 25)
    private TransactionStatus transactionStatus = TransactionStatus.PREPARED;

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

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person person) {
        this.owner = person;
    }

    public List<BillingItem> getItems() {
        return items;
    }

    public void setItems(List<BillingItem> items) {
        this.items = items;
    }

    public Float getTotal() {
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
        return getTotalSpaceInMb() * ONE_MB;
    }

    public Long getTotalNumberOfFiles() {
        initTotals();
        return totalFiles;
    }
    

    private void initTotals() {
        if (!initialized) {
            // if (coupon != null) {
            // calculatedCost -= coupon.getNumberOfDollars();
            // }

            Long discountedSpace = 0L;
            Long discountedFiles = 0L;
            if (coupon != null) {
                discountedFiles = coupon.getNumberOfFiles();
                discountedSpace = coupon.getNumberOfMb();
            }

            for (BillingItem item : getItems()) {
                BillingActivity activity = item.getActivity();
                Long numberOfFiles = activity.getNumberOfFiles();
                Long space = activity.getNumberOfMb();
                Long numberOfResources = activity.getNumberOfResources();

                if (activity.getNumberOfFiles() > 0 && discountedFiles > 0) {
                    couponValue += activity.getPrice() * discountedFiles;
                    discountedFiles = 0L;
                }

                if (activity.getNumberOfMb() > 0 && discountedSpace > 0) {
                    couponValue +=  activity.getPrice() * discountedSpace;
                    discountedSpace = 0L;
                }
                long quantity = item.getQuantity().longValue();
                if (numberOfFiles != null) {
                    totalFiles += numberOfFiles * quantity;
                }
                if (space != null) {
                    totalSpaceInMb += space * quantity;
                }
                if (numberOfResources != null) {
                    totalResources += numberOfResources * quantity;
                }
                calculatedCost += item.getSubtotal();
                logger.debug(String.format("%s (%s) files, %s(%s) mb, %s(%s) resources [$%s] %s", numberOfFiles, totalFiles, space, totalSpaceInMb,
                        numberOfResources, totalResources, calculatedCost, coupon));
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

    private transient Long totalResources = 0L;
    private transient Long totalSpaceInMb = 0L;
    private transient Long totalFiles = 0L;
    private transient Float calculatedCost = 0F;
    private transient boolean initialized = false;
    private transient Float couponValue = 0f;

    @Override
    public void markUpdated(Person p) {
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

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Person getTransactedBy() {
        return transactedBy;
    }

    public void setTransactedBy(Person transactedBy) {
        this.transactedBy = transactedBy;
    }

    public boolean isModifiable() {
        switch (transactionStatus) {
            case TRANSACTION_FAILED:
            case TRANSACTION_CANCELLED:
            case TRANSACTION_SUCCESSFUL:
                return false;
            default:
                return true;
        }
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

    @Override
    public String[] getIncludedJsonProperties() {
        return JSON_PROPERTIES;
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
        return ObjectUtils.notEqual(owner, transactedBy);
    }

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

}

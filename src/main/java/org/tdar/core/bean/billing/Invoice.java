package org.tdar.core.bean.billing;

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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.Person;

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

    public enum TransactionStatus {
        PREPARED,
        PENDING_TRANSACTION,
        TRANSACTION_SUCCESSFUL,
        TRANSACTION_FAILED
    }

    @NotNull
    @Column(name = "date_created")
    private Date dateCreated = new Date();
    // the confirmation id for this invoice

    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transactionType")
    private TransactionType transactionType;

    private Integer billingPhone;
    private Integer expirationYear;
    private Integer expirationMonth;
    private String creditCardType;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "owner_id")
    @NotNull
    private Person person;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "executor_id")
    @NotNull
    private Person transactedBy;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = true, name = "address_id")
    private Address address;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "invoice_id")
    private List<BillingItem> items;

    private Float total;

    private String invoiceNumber;
    private String otherReason;

    @Enumerated(EnumType.STRING)
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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
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

    public void setTotal(Float total) {
        this.total = total;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Long getTotalResources() {
        if (totalResources == null) {
            initTotals();
        }
        return totalResources;
    }

    public Long getTotalSpace() {
        if (totalSpace == null) {
            initTotals();
        }
        return totalSpace;
    }

    public Long getTotalNumberOfFiles() {
        if (totalFiles == null) {
            initTotals();
        }
        return totalFiles;
    }

    private void initTotals() {
        totalResources = 0L;
        totalFiles = 0L;
        totalSpace = 0L;
        calculatedCost = 0f;
        for (BillingItem item : getItems()) {
            Long numberOfFiles = item.getActivity().getNumberOfFiles();
            Long space = item.getActivity().getNumberOfBytes();
            Long numberOfResources = item.getActivity().getNumberOfResources();
            if (numberOfFiles != 0) {
                totalFiles += numberOfFiles * item.getQuantity().longValue();
                totalSpace += space * item.getQuantity().longValue();
                totalResources += numberOfResources * item.getQuantity().longValue();
                calculatedCost += item.getSubtotal();
            }
        }
    }

    private transient Long totalResources = null;
    private transient Long totalSpace = null;
    private transient Long totalFiles = null;
    private transient Float calculatedCost = null;

    @Override
    public void markUpdated(Person p) {
        if (getPerson() == null) {
            setPerson(p);
        }
        if (dateCreated == null) {
            setDateCreated(new Date());
        }
    }

    public void setBillingPhone(Integer billingPhone) {
        this.billingPhone = billingPhone;
    }

    public Integer getBillingPhone() {
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

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
        this.creditCardType = creditCardType;
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
            case TRANSACTION_SUCCESSFUL:
                return false;
            default:
                return true;
        }
    }

    public Float getCalculatedCost() {
        if (calculatedCost == null) {
            initTotals();
        }
        return calculatedCost;
    }

    public void setCalculatedCost(Float calculatedCost) {
        this.calculatedCost = calculatedCost;
    }
}

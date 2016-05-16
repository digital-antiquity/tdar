package org.tdar.core.bean.billing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.utils.MathUtils;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * $Id$
 * 
 * An Account maintains a set of Invoices and is the entity against which people can charge resource uploads. It also tracks a set of users who can charge
 * against those invoices.
 * 
 * @author TDAR
 * @version $Rev$
 */
@Entity
@Table(name = "pos_account")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.billing.Account")
public class BillingAccount extends AbstractPersistable implements Updatable, HasStatus, Addressable, HasUsers {

    private static final long serialVersionUID = -1728904030701477101L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = FieldLength.FIELD_LENGTH_25)
    private Status status = Status.ACTIVE;

    @NotNull
    @Column(name = "date_created", nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();

    @NotNull
    @Column(name = "date_updated", nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified = new Date();

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = false, name = "owner_id")
    @NotNull
    private TdarUser owner;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = false, name = "modifier_id")
    @NotNull
    private TdarUser modifiedBy;

    @NotNull
    @Column(name = "date_expires")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires = new Date();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = true, updatable = true, name = "account_id")
    private Set<Invoice> invoices = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = true, updatable = true, name = "account_id")
    private Set<Coupon> coupons = new HashSet<>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, fetch = FetchType.LAZY)
    @JoinTable(name = "pos_members", joinColumns = { @JoinColumn(nullable = false, name = "account_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "user_id") })
    private Set<TdarUser> authorizedMembers = new HashSet<>();

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true, name = "account_id")
    private Set<Resource> resources = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "account_id")
    @OrderBy("date_created DESC")
    private List<AccountUsageHistory> usageHistory = new ArrayList<>();

    private transient Long totalResources = 0L;
    private transient Long totalFiles = 0L;
    private transient Long totalSpaceInBytes = 0L;

    @Column(name = "files_used")
    private Long filesUsed = 0L;
    @Column(name = "space_used")
    private Long spaceUsedInBytes = 0L;
    @Column(name = "resources_used")
    private Long resourcesUsed = 0L;

    public BillingAccount() {
    }

    public BillingAccount(String name) {
        this.name = name;
    }

    /**
     * @return the invoices
     */
    public Set<Invoice> getInvoices() {
        return invoices;
    }

    /**
     * @param invoices
     *            the invoices to set
     */
    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
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
     * @return the lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return the resources
     */

    @XmlElementWrapper(name = "resources")
    @XmlElement(name = "resource")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    // FIXME: THIS IS A POTENTIAL ISSUE FOR PERFORMANCE WHEREBY IT COULD BE LINKED TO THOUSANDS OF THINGS
    public Set<Resource> getResources() {
        return resources;
    }

    @Transient
    public Set<Resource> getFlaggedResources() {
        Set<Resource> flagged = new HashSet<>();
        for (Resource resource : getResources()) {
            if (resource.getStatus().isFlaggedForBilling()) {
                flagged.add(resource);
            }
        }
        return flagged;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    private void initTotals() {
        resetTransientTotals();
        for (Invoice invoice : getInvoices()) {
            if (invoice.getTransactionStatus() != TransactionStatus.TRANSACTION_SUCCESSFUL) {
                continue;
            }
            totalResources += invoice.getTotalResources();
            totalFiles += invoice.getTotalNumberOfFiles();
            totalSpaceInBytes += invoice.getTotalSpaceInBytes();
        }

        logger.trace(String.format("Totals: %s r %s f %s b", totalResources, totalFiles, totalSpaceInBytes));
    }

    public void resetTransientTotals() {
        totalFiles = 0L;
        totalResources = 0L;
        totalSpaceInBytes = 0L;
    }

    public Long getTotalNumberOfResources() {
        initTotals();
        return totalResources;
    }

    public Long getTotalNumberOfFiles() {
        initTotals();
        return totalFiles;
    }

    public Long getTotalSpaceInMb() {
        initTotals();
        return MathUtils.divideByRoundUp(totalSpaceInBytes, MathUtils.ONE_MB);
    }

    public Long getTotalSpaceInBytes() {
        initTotals();
        return totalSpaceInBytes;
    }

    public Long getAvailableNumberOfFiles() {
        Long totalFiles = getTotalNumberOfFiles();
        return totalFiles - getFilesUsed();
    }

    public Long getAvailableSpaceInBytes() {
        Long totalSpace = getTotalSpaceInBytes();
        logger.trace("total space: {} , used {} ", totalSpace, getSpaceUsedInBytes());
        return totalSpace - getSpaceUsedInBytes();
    }

    public Long getAvailableSpaceInMb() {
        return MathUtils.divideByRoundDown(getAvailableSpaceInBytes(), (double) MathUtils.ONE_MB);
    }

    public Long getAvailableResources() {
        Long totalResources = getTotalNumberOfResources();
        return totalResources - getResourcesUsed();
    }

    @Override
    public void markUpdated(TdarUser p) {
        if (getOwner() == null) {
            setDateCreated(new Date());
            setOwner(p);
        }
        setLastModified(new Date());
        setModifiedBy(p);
    }

    public TdarUser getOwner() {
        return owner;
    }

    public void setOwner(TdarUser owner) {
        this.owner = owner;
    }

    public TdarUser getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(TdarUser modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    @Override
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    /**
     * @return the status
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Set<TdarUser> getAuthorizedMembers() {
        return authorizedMembers;
    }

    @Override
    public void setAuthorizedMembers(Set<TdarUser> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
    }

    public Long getFilesUsed() {
        return filesUsed;
    }

    public Float getTotalCost() {
        Float total = 0f;
        for (Invoice invoice : invoices) {
            TransactionStatus status2 = invoice.getTransactionStatus();
            if (status2.isComplete() && !status2.isInvalid()) {
                total += invoice.getTotal();
            }
        }
        return total;
    }

    public void setFilesUsed(Long filesUsed) {
        this.filesUsed = filesUsed;
    }

    public Long getSpaceUsedInBytes() {
        return spaceUsedInBytes;
    }

    public Long getSpaceUsedInMb() {
        return MathUtils.divideByRoundUp(spaceUsedInBytes, MathUtils.ONE_MB);
    }

    public void setSpaceUsedInBytes(Long spaceUsed) {
        this.spaceUsedInBytes = spaceUsed;
    }

    public Long getResourcesUsed() {
        return resourcesUsed;
    }

    public void setResourcesUsed(Long resourcesUsed) {
        this.resourcesUsed = resourcesUsed;
    }

    @Override
    public String getUrlNamespace() {
        return "billing";
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    @Override
    public Date getDateUpdated() {
        return lastModified;
    }

    @Override
    public boolean isDraft() {
        return status == Status.DRAFT;
    }

    @Override
    public boolean isFlagged() {
        return status == Status.FLAGGED;
    }

    @Override
    public boolean isDuplicate() {
        return status == Status.DUPLICATE;
    }

    public Set<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(Set<Coupon> coupons) {
        this.coupons = coupons;
    }

    public void reset() {
        setStatus(Status.ACTIVE);
        setSpaceUsedInBytes(0L);
        setFilesUsed(0L);
        initTotals();
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getId());
    }

    @Override
    public String getDetailUrl() {
        return String.format("/%s/%s", getUrlNamespace(), getId());
    }

    public List<AccountUsageHistory> getUsageHistory() {
        return usageHistory;
    }

    public void setUsageHistory(List<AccountUsageHistory> history) {
        this.usageHistory = history;
    }

    public String usedString() {
        return String.format("f: %s s: %s", filesUsed, spaceUsedInBytes);
    }

    public String availableString() {
        return String.format("f: %s s: %s", totalFiles - filesUsed, totalSpaceInBytes - spaceUsedInBytes);
    }
}

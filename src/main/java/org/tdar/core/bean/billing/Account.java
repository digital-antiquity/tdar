package org.tdar.core.bean.billing;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;

/**
 * $Id$
 * 
 * An Account maintains a set of Invoices and is the entity against which people can charge resource uploads.
 * 
 * @author TDAR
 * @version $Rev$
 */
@Entity
@Table(name = "pos_account")
public class Account extends Persistable.Base implements Updatable, HasStatus {

    private static final long serialVersionUID = -1728904030701477101L;

    public Account() {
    }

    public Account(String name) {
        this.name = name;
    }

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.ACTIVE;

    @NotNull
    @Column(name = "date_created")
    private Date dateCreated = new Date();

    @NotNull
    @Column(name = "date_updated")
    private Date lastModified = new Date();

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "owner_id")
    @NotNull
    private Person owner;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "modifier_id")
    @NotNull
    private Person modifiedBy;

    @NotNull
    @Column(name = "date_expires")
    private Date expires = new Date();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = true, name = "account_id")
    @NotNull
    private Set<Invoice> invoices = new HashSet<Invoice>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "pos_members", joinColumns = { @JoinColumn(nullable = false, name = "account_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "user_id") })
    private Set<Person> authorizedMembers = new HashSet<Person>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true, name = "account_id")
    private Set<Resource> resources = new HashSet<Resource>();


    private transient Long totalResources = 0L;
    private transient Long totalFiles = 0L;
    private transient Long totalSpace = 0L;
    private transient boolean initialized = false;
    @Column(name="files_used")
    private Long filesUsed = 0L;
    @Column(name="space_used")
    private Long spaceUsed = 0L;
    @Column(name="resources_used")
    private Long resourcesUsed = 0L;

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
    public Set<Resource> getResources() {
        return resources;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public void initTotals() {
        for (Invoice invoice : getInvoices()) {
            if (invoice.getTransactionStatus() != TransactionStatus.TRANSACTION_SUCCESSFUL)
                continue;
            totalResources += invoice.getTotalResources();
            totalFiles += invoice.getTotalNumberOfFiles();
            totalSpace += invoice.getTotalSpace();
        }

        ResourceEvaluator re = new ResourceEvaluator();
        re.evaluateResource((Resource[]) resources.toArray());
        setFilesUsed(getFilesUsed() + re.getFilesUsed());
        setSpaceUsed(getSpaceUsed() + re.getSpaceUsed());
        setResourcesUsed(getResourcesUsed() + re.getResourcesUsed());
    }

    public Long getTotalNumberOfResources() {
        if (!initialized) {
            initTotals();
        }
        return totalResources;
    }

    public Long getTotalNumberOfFiles() {
        if (!initialized) {
            initTotals();
        }
        return totalFiles;
    }

    public Long getTotalNumberOfSpace() {
        if (!initialized) {
            initTotals();
        }
        return totalSpace;
    }

    public Long getAvailableNumberOfFiles() {
        Long totalFiles = getTotalNumberOfFiles();
        return totalFiles - getFilesUsed();
    }

    public Long getAvailableSpace() {
        Long totalSpace = getTotalNumberOfSpace();
        return totalSpace - getSpaceUsed();
    }

    public Long getAvailableResources() {
        Long totalResources = getTotalNumberOfResources();
        return totalResources - getResourcesUsed();
    }

    public enum AccountAdditionStatus {
        CAN_ADD_RESOURCE,
        NOT_ENOUGH_SPACE,
        NOT_ENOUGH_FILES,
        NOT_ENOUGH_RESOURCES;
    }

    public AccountAdditionStatus canAddResource(Resource resource) {
        ResourceEvaluator re = new ResourceEvaluator(resource);
        return canAddResource(re);
    }

    public AccountAdditionStatus canAddResource(ResourceEvaluator re) {
        if (getAvailableNumberOfFiles() - re.getFilesUsed() < 0) {
            return AccountAdditionStatus.NOT_ENOUGH_FILES;
        }

        if (getAvailableResources() - re.getResourcesUsed() < 0) {
            return AccountAdditionStatus.NOT_ENOUGH_RESOURCES;
        }

        if (getAvailableSpace() - re.getSpaceUsed() < 0) {
            return AccountAdditionStatus.NOT_ENOUGH_SPACE;
        }
        return AccountAdditionStatus.CAN_ADD_RESOURCE;
    }

    @Override
    public void markUpdated(Person p) {
        if (getOwner() == null) {
            setDateCreated(new Date());
            setOwner(p);
        }
        setLastModified(new Date());
        setModifiedBy(p);
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public Person getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Person modifiedBy) {
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
    public Status getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<Person> getAuthorizedMembers() {
        return authorizedMembers;
    }

    public void setAuthorizedMembers(Set<Person> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
    }

    public boolean hasMinimumForNewRecord() {
        initTotals();
        ResourceEvaluator re = new ResourceEvaluator();
        return (re.invoiceHasMinimumForNewResource(this));
    }

    public void updateQuotas(ResourceEvaluator endingEvaluator) {
        if (canAddResource(endingEvaluator) == AccountAdditionStatus.CAN_ADD_RESOURCE) {
            setFilesUsed(getFilesUsed() + endingEvaluator.getFilesUsed());
            setResourcesUsed(getResourcesUsed() + endingEvaluator.getResourcesUsed());
            setSpaceUsed(getSpaceUsed() + endingEvaluator.getSpaceUsed());
        }
    }

    public Long getFilesUsed() {
        return filesUsed;
    }

    public void setFilesUsed(Long filesUsed) {
        this.filesUsed = filesUsed;
    }

    public Long getSpaceUsed() {
        return spaceUsed;
    }

    public void setSpaceUsed(Long spaceUsed) {
        this.spaceUsed = spaceUsed;
    }

    public Long getResourcesUsed() {
        return resourcesUsed;
    }

    public void setResourcesUsed(Long resourcesUsed) {
        this.resourcesUsed = resourcesUsed;
    }

    // set current values as saved fields
    // save trasnactions update available values...
}

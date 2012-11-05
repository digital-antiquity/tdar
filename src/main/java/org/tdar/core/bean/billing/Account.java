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
    @JoinColumn(nullable = false, updatable = false, name = "account_id")
    private Set<Invoice> invoices = new HashSet<Invoice>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "pos_members", joinColumns = { @JoinColumn(nullable = false, name = "user_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "account_id") })
    private Set<Person> authorizedMembers = new HashSet<Person>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true, name = "account_id")
    private Set<Resource> resources = new HashSet<Resource>();

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

    private transient Long totalResources;
    private transient Long totalFiles;
    private transient Long totalSpace;

    private transient Long filesUsed;
    private transient Long spaceUsed;
    private transient Long resourcesUsed;

    public void initTotals() {
        for (Invoice invoice : getInvoices()) {
            if (invoice.getTransactionStatus() != TransactionStatus.TRANSACTION_SUCCESSFUL)
                continue;
            totalResources += invoice.getTotalResources();
            totalFiles += invoice.getTotalNumberOfFiles();
            totalSpace += invoice.getTotalSpace();
        }

        for (Resource resource : resources) {
            ResourceEvaluator re = new ResourceEvaluator();
            re.evaluateResource(resource);
            filesUsed += re.getFilesUsed();
            spaceUsed += re.getSpaceUsed();
            resourcesUsed += re.getResourcesUsed();
        }
    }

    public Long getTotalNumberOfResources() {
        if (totalResources == null) {
            initTotals();
        }
        return totalResources;
    }

    public Long getTotalNumberOfFiles() {
        if (totalFiles == null) {
            initTotals();
        }
        return totalFiles;
    }

    public Long getTotalNumberOfSpace() {
        if (totalSpace == null) {
            initTotals();
        }
        return totalSpace;
    }

    public Long getAvailableNumberOfFiles() {
        Long totalFiles = getTotalNumberOfFiles();
        return totalFiles - filesUsed;
    }

    public Long getAvailableSpace() {
        Long totalSpace = getTotalNumberOfSpace();
        return totalSpace - spaceUsed;
    }

    public Long getAvailableResources() {
        Long totalResources = getTotalNumberOfResources();
        return totalResources - resourcesUsed;
    }

    public enum AccountAdditionStatus {
        CAN_ADD_RESOURCE,
        NOT_ENOUGH_SPACE,
        NOT_ENOUGH_FILES,
        NOT_ENOUGH_RESOURCES;
    }

    public AccountAdditionStatus canAddResource(Resource resource) {
        ResourceEvaluator re = new ResourceEvaluator();
        re.evaluateResource(resource);
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

}

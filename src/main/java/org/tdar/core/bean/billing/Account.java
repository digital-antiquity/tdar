package org.tdar.core.bean.billing;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;

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
public class Account extends Persistable.Base {

    private static final long serialVersionUID = -1728904030701477101L;

    private Account() {
    }

    public Account(String name) {
        this.name = name;
    }

    private String name;
    private String description;

    @NotNull
    @Column(name = "date_created")
    private Date dateCreated = new Date();

    @NotNull
    @Column(name = "date_updated")
    private Date lastModified = new Date();

    @NotNull
    @Column(name = "date_expires")
    private Date expires = new Date();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "account_id")
    private Set<Invoice> invoices = new HashSet<Invoice>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "pos_members", joinColumns = { @JoinColumn(nullable = false, name = "account_group_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "account_id") })
    private Set<Person> authorizedMembers = new HashSet<Person>();

    // @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JoinColumn(nullable = false, updatable = false, name = "resource_id")
    @Transient
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

}

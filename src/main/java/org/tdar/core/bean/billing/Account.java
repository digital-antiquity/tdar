package org.tdar.core.bean.billing;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;

/**
 * $Id$
 * 
 * An Account maintains a set of Invoices and is the entity against which people can charge resource uploads.
 * 
 * @author TDAR
 * @version $Rev$
 */
public class Account extends Persistable.Base {

    private static final long serialVersionUID = -1728904030701477101L;
    
    private String name;
    private String description;
    private Date dateCreated = new Date();
    private Date lastModified = new Date();
    private Set<Invoice> invoices = new HashSet<Invoice>();
    private Set<Resource> resources = new HashSet<Resource>();    
    
    /**
     * @return the invoices
     */
    public Set<Invoice> getInvoices() {
        return invoices;
    }
    /**
     * @param invoices the invoices to set
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
     * @param name the name to set
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
     * @param description the description to set
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
     * @param dateCreated the dateCreated to set
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
     * @param lastModified the lastModified to set
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
     * @param resources the resources to set
     */
    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

}

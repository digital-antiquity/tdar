package org.tdar.core.bean.billing;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Status;

/**
 * $Id$
 * 
 * 
 * 
 * @author TDAR
 * @version $Rev$
 */
public class AccountGroup extends Persistable.Base implements HasStatus {

    private static final long serialVersionUID = 3939132209828344622L;
    
    private Status status = Status.ACTIVE;
    
    private Person owner;
    private Set<Account> accounts = new HashSet<Account>();
    private Set<Person> authorizedMembers = new HashSet<Person>();
    private Date dateCreated = new Date();
    private Date lastModified = new Date();
    

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
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the owner
     */
    public Person getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(Person owner) {
        this.owner = owner;
    }

    /**
     * @return the accounts
     */
    public Set<Account> getAccounts() {
        return accounts;
    }

    /**
     * @param accounts the accounts to set
     */
    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    /**
     * @return the authorizedMembers
     */
    public Set<Person> getAuthorizedMembers() {
        return authorizedMembers;
    }

    /**
     * @param authorizedMembers the authorizedMembers to set
     */
    public void setAuthorizedMembers(Set<Person> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
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

}

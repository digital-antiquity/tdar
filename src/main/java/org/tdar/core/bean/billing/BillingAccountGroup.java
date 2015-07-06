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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;

/**
 * Represents a group of Accounts. Each account may be associated with people who can charge. This "group of groups" allows for super-admins to manage lots of
 * accounts.
 * 
 * @author TDAR
 * @version $Rev$
 */
@Entity
@Table(name = "pos_account_group")
public class BillingAccountGroup extends Base implements Updatable {

    private static final long serialVersionUID = 3939132209828344622L;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = true, updatable = true, name = "account_group_id")
    private Set<BillingAccount> accounts = new HashSet<BillingAccount>();

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

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "owner_id")
    @NotNull
    private TdarUser owner;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "modifier_id")
    @NotNull
    private TdarUser modifiedBy;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "pos_group_members", joinColumns = { @JoinColumn(nullable = false, name = "user_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "account_id") })
    private Set<TdarUser> authorizedMembers = new HashSet<>();

    public Set<BillingAccount> getAccounts() {
        return accounts;
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

    public void setAccounts(Set<BillingAccount> accounts) {
        this.accounts = accounts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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

    public Set<TdarUser> getAuthorizedMembers() {
        return authorizedMembers;
    }

    public void setAuthorizedMembers(Set<TdarUser> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Date getDateUpdated() {
        return lastModified;
    }

}

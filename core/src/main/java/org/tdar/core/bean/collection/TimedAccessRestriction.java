package org.tdar.core.bean.collection;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;

@Entity
@Table(name = "timed_access")
public class TimedAccessRestriction extends AbstractPersistable {

    private static final long serialVersionUID = -500700001020610812L;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable=false)
    private Date dateCreated;

    @Temporal(TemporalType.DATE)
    @Column(name = "until", nullable=false)
    private Date until;
    
    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private TdarUser user;

    @ManyToOne
    @JoinColumn(nullable = false, name = "owner_id")
    private TdarUser createdBy;

    @ManyToOne
    @JoinColumn(nullable = false, name = "invite_id")
    private UserInvite invite;

    @ManyToOne
    @JoinColumn(nullable = false, name = "collection_id")
    private ResourceCollection collection;


    public TimedAccessRestriction() {}
    
    public TimedAccessRestriction(Date expires) {
        this.until = expires;
        this.dateCreated = new Date();
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public UserInvite getInvite() {
        return invite;
    }

    public void setInvite(UserInvite invite) {
        this.invite = invite;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
        this.collection = collection;
    }

    public TdarUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(TdarUser createdBy) {
        this.createdBy = createdBy;
    }

}

package org.tdar.core.bean.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;

/**
 * Bean for inviting a person to tDAR -- grants them implicit access to the collection(s)
 * 
 * @author abrin
 *
 */
@Entity
@Table(name = "user_invite")
//@Check(constraints = "email <> ''")
public class UserInvite extends AbstractPersistable {

    private static final long serialVersionUID = 2915969311944606586L;

//    @Column(unique = true, nullable = true, name="email")
//    @Length(min = 1, max = FieldLength.FIELD_LENGTH_255)
//    private String emailAddress;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable=false)
    private Date dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_expires", nullable=true)
    private Date dateExpires;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_redeemed", nullable=true)
    private Date dateRedeemed;

    @ManyToOne
    @JoinColumn(nullable = false, name = "collection_id")
    private ResourceCollection resourceCollection;

    @ManyToOne
    @JoinColumn(nullable = false, name = "authorizer_id")
    private TdarUser authorizer;

    
    @ManyToOne
    @JoinColumn(nullable = false, name = "person_id")
    private Person user;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = FieldLength.FIELD_LENGTH_255)
    private GeneralPermissions permissions;

    public GeneralPermissions getPermissions() {
        return permissions;
    }

    public void setPermissions(GeneralPermissions permissions) {
        this.permissions = permissions;
    }

    public ResourceCollection getResourceCollection() {
        return resourceCollection;
    }

    public void setResourceCollection(ResourceCollection resourceCollection) {
        this.resourceCollection = resourceCollection;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public TdarUser getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(TdarUser user) {
        this.authorizer = user;
    }

    public Date getDateRedeemed() {
        return dateRedeemed;
    }

    public void setDateRedeemed(Date dateRedeemed) {
        this.dateRedeemed = dateRedeemed;
    }

    public Person getUser() {
        return user;
    }

    public void setPerson(Person user) {
        this.user = user;
    }

    public Date getDateExpires() {
        return dateExpires;
    }

    public void setDateExpires(Date dateExpires) {
        this.dateExpires = dateExpires;
    }

}

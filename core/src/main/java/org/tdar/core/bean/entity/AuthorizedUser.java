/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.entity;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * @author Adam Brin
 *         This is the representation of a user and a permission combined and an association with a resource collection.
 */
@Table(name = "authorized_user", indexes = {
        @Index(name = "authorized_user_cid", columnList = "id, resource_collection_id"),
        @Index(name = "authorized_user_cid2", columnList = "user_id, resource_collection_id"),
        @Index(name = "authorized_user_perm", columnList = "resource_collection_id, general_permission_int, user_id"),
        @Index(name = "authorized_user_resource_collection_id_idx", columnList = "resource_collection_id"),
        @Index(name = "authorized_user_user_id_idx", columnList = "user_id")
})
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.entity.AuthorizedUser")
public class AuthorizedUser extends AbstractPersistable {

    private static final long serialVersionUID = -6747818149357146542L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Enumerated(EnumType.STRING)
    @Column(name = "general_permission", length = FieldLength.FIELD_LENGTH_50)
    private GeneralPermissions generalPermission;

    @Column(name = "general_permission_int")
    private Integer effectiveGeneralPermission;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "user_id")
    private TdarUser user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private Date dateCreated = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "date_expires", nullable = true)
    private Date dateExpires;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "creator_id")
    private TdarUser createdBy;

    @Column(name="resource_collection_id", insertable=false, updatable=false)
    private Long collectionId;

    
    @Column(name="account_id", insertable=false, updatable=false)
    private Long accountId;

    @Column(name="integration_id", insertable=false, updatable=false)
    private Long integrationId;

    @Column(name="resource_id", insertable=false, updatable=false)
    private Long resourceId;

    private transient boolean enabled = false;

    /**
     * @param person
     * @param modifyRecord
     */
    public AuthorizedUser() {
    }

    public AuthorizedUser(TdarUser createdBy, TdarUser person, GeneralPermissions permission) {
        this.createdBy = createdBy;
        this.user = person;
        setGeneralPermission(permission);
    }

    public AuthorizedUser(TdarUser authenticatedUser, TdarUser person, GeneralPermissions permission, Date date) {
        this(authenticatedUser, person, permission);
        if (date != null) {
            setDateExpires(date);
        }
    }

    @XmlElement(name = "personRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    /**
     * @param generalPermission
     *            the generalPermission to set
     */
    public void setGeneralPermission(GeneralPermissions generalPermission) {
        this.generalPermission = generalPermission;
        this.setEffectiveGeneralPermission(generalPermission.getEffectivePermissions());
    }

    /**
     * @return the generalPermission
     */
    public GeneralPermissions getGeneralPermission() {
        return generalPermission;
    }

    @Transient
    // is the authorizedUser valid not taking into account whether a collection is present
    public boolean isValid() {
        boolean registered = false;
        String name = "";
        if (user != null) {
            registered = user.isRegistered();
            name = user.toString();
        }
        logger.trace("calling validate collection for user/permission/registered: [{} / {} / {}]", name, generalPermission != null, registered);
        return (user != null) && (generalPermission != null) && user.isRegistered();
    }

    @Override
    public String toString() {
        Long userid = null;
        String properName = null;
        if (user != null) {
            userid = user.getId();
            properName = user.getProperName();
        }
        String ex = "";
        if (getDateExpires() != null) {
            ex = dateExpires.toString();
        }
        return String.format("%s[%s] (%s - %s %s)", properName, userid, generalPermission, getId(), ex);
    }

    /**
     * @param effectiveGeneralPermission
     *            the effectiveGeneralPermission to set
     */
    // I should only be called internally
    private void setEffectiveGeneralPermission(Integer effectiveGeneralPermission) {
        this.effectiveGeneralPermission = effectiveGeneralPermission;
    }

    /**
     * @return the effectiveGeneralPermission
     */
    public Integer getEffectiveGeneralPermission() {
        return effectiveGeneralPermission;
    }

    /**
     * 'Enabled' in this context refers to whether the system should allow modification of this object in the context UI edit operation. When enabled is false,
     * the system should not allow operations which would alter the fields in this object, and also should not allow operations that would add or remove the
     * object to/from an authorized user list.
     *
     * @return
     */
    public boolean isEnabled() {
        if (PersistableUtils.isNullOrTransient(this) && PersistableUtils.isNullOrTransient(user)) {
            return true;
        }
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getDateExpires() {
        return dateExpires;
    }

    public void setDateExpires(Date dateExpires) {
        this.dateExpires = dateExpires;
    }

    @XmlElement(name = "createdByRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(TdarUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

}
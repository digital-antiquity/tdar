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
import org.tdar.core.bean.Persistable;
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
public class AuthorizedUser extends AbstractPersistable implements Persistable {

    private static final long serialVersionUID = -6747818149357146542L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /* Right now not used */
    enum AdminPermissions {
        NONE,
        PUBLISH,
        CAN_DELETE,
        ALL
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "general_permission", length = FieldLength.FIELD_LENGTH_50)
    private GeneralPermissions generalPermission;

    @Column(name = "general_permission_int")
    private Integer effectiveGeneralPermission;
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_permission", length = FieldLength.FIELD_LENGTH_255)
    private AdminPermissions adminPermission;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "user_id")
    private TdarUser user;

    @Column(name = "date_expires")
    @Temporal(TemporalType.DATE)
    private Date dateExpires;

    
    private transient boolean enabled = false;

    /**
     * @param person
     * @param modifyRecord
     */
    public AuthorizedUser() {
    }

    public AuthorizedUser(TdarUser person, GeneralPermissions permission) {
        this.user = person;
        setGeneralPermission(permission);
    }

    public AdminPermissions getAdminPermission() {
        return adminPermission;
    }

    public void setAdminPermission(AdminPermissions adminPermission) {
        this.adminPermission = adminPermission;
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
        return String.format("%s[%s] ( %s)", properName, userid, generalPermission);
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

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
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

    private transient String test = "";

}
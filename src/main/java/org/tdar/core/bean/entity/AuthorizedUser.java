/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * @author Adam Brin
 *         This is the representation of a user and a permission combined and an association with a resource collection.
 */
@Table(name = "authorized_user")
@Entity
public class AuthorizedUser extends Base implements Persistable {

    private static final long serialVersionUID = -6747818149357146542L;

    /* Right now not used */
    enum AdminPermissions {
        NONE,
        PUBLISH,
        CAN_DELETE,
        ALL
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "general_permission", length = 50)
    private GeneralPermissions generalPermission;

    @Column(name = "general_permission_int")
    private Integer effectiveGeneralPermission;
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_permission", length = 255)
    private AdminPermissions adminPermission;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "user_id")
    private Person user;

    /**
     * @param person
     * @param modifyRecord
     */
    public AuthorizedUser() {
    }

    public AuthorizedUser(Person person, GeneralPermissions permission) {
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
    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
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
        logger.trace("calling validate collection for user/permission/registered: [{} / {} / {}]",
                new Object[] { user != null, generalPermission != null, user.isRegistered() });
        return user != null && generalPermission != null && user.isRegistered();
    }

    @Override
    public String toString() {
        return String.format("%s[%s] ( %s)", getUser().getProperName(), getUser().getId(), generalPermission);
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

    private transient String test = "";

}
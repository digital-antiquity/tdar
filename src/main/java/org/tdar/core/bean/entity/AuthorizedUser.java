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

import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.collection.ResourceCollection;

/**
 * @author Adam Brin
 * 
 */
@Table(name = "authorized_user")
@Entity
public class AuthorizedUser extends Base {

    private static final long serialVersionUID = -6747818149357146542L;

    enum ViewPermissions {
        VIEW_CITATION,
        VIEW_ALL_METADATA,
        VIEW_ALL
    }

    enum ModifyPermissions {
        NONE,
        MODIFY_RECORD
    }

    enum AdminPermissions {
        NONE,
        PUBLISH,
        CAN_DELETE,
        ALL
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "view_permission")
    private ViewPermissions viewPermission;

    @Enumerated(EnumType.STRING)
    @Column(name = "modify_permission")
    private ModifyPermissions modifyPermission;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_permission")
    private AdminPermissions adminPermission;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "user_id")
    private Person user;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "resource_collection_id")
    private ResourceCollection resourceCollection;

    public ViewPermissions getViewPermission() {
        return viewPermission;
    }

    public void setViewPermission(ViewPermissions viewPermission) {
        this.viewPermission = viewPermission;
    }

    public ModifyPermissions getModifyPermission() {
        return modifyPermission;
    }

    public void setModifyPermission(ModifyPermissions modifyPermission) {
        this.modifyPermission = modifyPermission;
    }

    public AdminPermissions getAdminPermission() {
        return adminPermission;
    }

    public void setAdminPermission(AdminPermissions adminPermission) {
        this.adminPermission = adminPermission;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public ResourceCollection getResourceCollection() {
        return resourceCollection;
    }

    public void setResourceCollection(ResourceCollection resourceCollection) {
        this.resourceCollection = resourceCollection;
    }

    public boolean isValid() {
        return (user != null);
    }

}

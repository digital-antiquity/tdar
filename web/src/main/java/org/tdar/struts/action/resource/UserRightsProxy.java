package org.tdar.struts.action.resource;

import java.io.Serializable;
import java.util.Date;

import org.tdar.core.bean.entity.permissions.GeneralPermissions;

public class UserRightsProxy implements Serializable {

    private static final long serialVersionUID = 4514239731180311718L;

    private String displayName;
    private Long id;
    private Long inviteId;
    private String firstName;
    private String lastName;
    private String email;
    private GeneralPermissions permission;
    private Date until;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public GeneralPermissions getPermission() {
        return permission;
    }

    public void setPermission(GeneralPermissions permission) {
        this.permission = permission;
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public Long getInviteId() {
        return inviteId;
    }

    public void setInviteId(Long inviteId) {
        this.inviteId = inviteId;
    }

}

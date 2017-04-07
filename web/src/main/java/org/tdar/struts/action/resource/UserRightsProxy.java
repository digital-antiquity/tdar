package org.tdar.struts.action.resource;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

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

    public String getUntil() {
        if (until != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            return sdf.format(until);
        }
        return "";
    }

    public void setUntilDate(Date date) {
        this.until = date;
    }

    public Date getUntilDate() {
        return until;
    }

    public void setUntil(String until) {
        this.until = DateTime.parse(until, DateTimeFormat.forPattern("MM/dd/yyyy")).toDate();
    }

    public Long getInviteId() {
        return inviteId;
    }

    public void setInviteId(Long inviteId) {
        this.inviteId = inviteId;
    }

    public boolean isEmpty() {
        if (inviteId == null && email == null && id == null) {

            return true;
        }
        return false;
    }

}

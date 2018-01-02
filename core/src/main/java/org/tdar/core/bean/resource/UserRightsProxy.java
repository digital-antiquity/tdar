package org.tdar.core.bean.resource;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.Permissions;

public class UserRightsProxy implements Serializable {

    private static final long serialVersionUID = 4514239731180311718L;

    private String displayName;
    private Long id;
    private Long inviteId;
    private String firstName;
    private String lastName;
    private String email;
    private String note;
    private Permissions permission;
    private Date until;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public UserRightsProxy() {}
    
    public UserRightsProxy(UserInvite invite) {
        Person user = invite.getUser();
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setDisplayName(user.getProperName());
        setInviteId(invite.getId());
        setPermission(invite.getPermissions());
        setUntilDate(invite.getDateExpires());
    }

    public UserRightsProxy(AuthorizedUser au) {
        setDisplayName(au.getUser().getProperName());
        setId(au.getUser().getId());
        setPermission(au.getGeneralPermission());
        setUntilDate(au.getDateExpires());
    }

    @Override
    public String toString() {
        return String.format("%s %s - [id:%s ; inviteId:%s] %s", getDisplayName(), getPermission(), getId(), getInviteId(), getEmail());
    }
    
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

    public Permissions getPermission() {
        return permission;
    }

    public void setPermission(Permissions permission) {
        this.permission = permission;
    }

    public String getUntil() {
        if (until != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            return sdf.format(until);
        }
        return "";
    }

    public void setUntilDate(Date date) {
        this.until = date;
        logger.trace("{}", this.until);
        }

    public Date getUntilDate() {
        return until;
    }

    public void setUntil(String until) {
        this.until = DateTime.parse(until, DateTimeFormat.forPattern("MM-dd-yyyy")).toDate();
        logger.trace("{} {}", until, this.until);
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}

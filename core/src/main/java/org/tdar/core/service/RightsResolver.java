package org.tdar.core.service;

import java.util.Date;
import java.util.List;

import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;

/**
 * Helper class to take a list of AuthorizedUsers and get the Maximum permissions alotted
 */
public class RightsResolver {

    private Date minDate = null;
    private boolean admin = false;
    private GeneralPermissions minPerm = null;
    private List<AuthorizedUser> authorizedUsers;

    public RightsResolver(List<AuthorizedUser> authorizedUsers) {
        this.setAuthorizedUsers(authorizedUsers);
        for (AuthorizedUser au : authorizedUsers) {
            GeneralPermissions permission = au.getGeneralPermission();
            Date expires = au.getDateExpires();

            if (getMinPerm() != null && permission != null && (permission.ordinal() > getMinPerm().ordinal() || getMinPerm() == null)) {
                setMinPerm(permission);
            }

            if (getMinDate() != null && expires != null && (expires.after(getMinDate()) || getMinDate() == null)) {
                setMinDate(expires);
            }
        }
    }

    public RightsResolver() {
        // TODO Auto-generated constructor stub
    }

    public static RightsResolver evaluate(List<AuthorizedUser> checkSelfEscalation) {
        return new RightsResolver(checkSelfEscalation);
    }

    public GeneralPermissions getMinPerm() {
        return minPerm;
    }

    public void setMinPerm(GeneralPermissions minPerm) {
        this.minPerm = minPerm;
    }

    public Date getMinDate() {
        return minDate;
    }

    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }

    public List<AuthorizedUser> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public void setAuthorizedUsers(List<AuthorizedUser> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean canModifyUsersOnResource() {
        if (isAdmin()) {
            return true;
        }
        if (getMinPerm() == null) {
            return false;
        }
        if (getMinPerm().ordinal() >= GeneralPermissions.MODIFY_RECORD.ordinal()) {
            return true;
        }
        return false;
    }

    public boolean canModifyUsersOnShare() {
        if (isAdmin()) {
            return true;
        }
        if (getMinPerm() == null) {
            return false;
        }
        if (getMinPerm().ordinal() >= GeneralPermissions.ADMINISTER_SHARE.ordinal()) {
            return true;
        }
        return false;
    }

    public boolean hasPermissionsEscalation(AuthorizedUser userToAdd) {
        if (admin) {
            return false;
        }

        // if we have no permissions, issue
        if (getMinPerm() == null) {
            return true;
        }

        // if we have increase in permissions, issue
        if (getMinPerm() != null && userToAdd.getGeneralPermission().ordinal() > getMinPerm().ordinal()) {
            return true;
        }

        // if we have no expiry (but min date)
        Date expiry = userToAdd.getDateExpires();
        if (minDate != null && expiry == null) {
            return true;
        }

        // if we have expiry and min date, but expiry is AFTER min-date
        if (minDate != null && expiry.after(minDate)) {
            return true;
        }

        return false;
    }

}

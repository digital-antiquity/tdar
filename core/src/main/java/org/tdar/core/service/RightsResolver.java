package org.tdar.core.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;

/**
 * Helper class to take a list of AuthorizedUsers and get the Maximum permissions alotted
 */
public class RightsResolver {

    private Date minDate = null;
    private boolean seenInfinite = false;
    private boolean admin = false;
    private GeneralPermissions minPerm = null;
    private List<AuthorizedUser> authorizedUsers;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public RightsResolver(List<AuthorizedUser> authorizedUsers) {
        this.setAuthorizedUsers(authorizedUsers);
        for (AuthorizedUser au : authorizedUsers) {
            GeneralPermissions permission = au.getGeneralPermission();
            Date expires = au.getDateExpires();

            if (getMinPerm() == null && permission != null // if existing is null and new is not, or
                    || getMinPerm() != null && permission != null && // both are not null AND
                            permission.ordinal() > getMinPerm().ordinal()) { // incoming is greater
                setMinPerm(permission);
            }

            if (expires == null) {
                // we've seen an AuthorizedUser with no expiration date... so always expire
                seenInfinite = true;
                setMinDate(null);
            }
            if (!seenInfinite) {
                if (getMinDate() == null && expires != null // if existing is null and new is not, or
                        || getMinDate() != null && expires != null && // both are not null AND
                                expires.after(getMinDate())) { // incoming is later
                    setMinDate(expires);
                }
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

    public void logDebug(TdarUser actor, AuthorizedUser userToAdd) {
        logger.debug("~~ EscalationIssue ~~");
        logger.debug("actor:{}", actor);
        logger.debug("  min:{} ; exp: {}", getMinPerm(), getMinDate());
        logger.debug("  all:{}", getAuthorizedUsers());
        logger.debug(" user:{}", userToAdd);
    }

}

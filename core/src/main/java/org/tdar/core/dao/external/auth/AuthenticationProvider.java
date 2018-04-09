package org.tdar.core.dao.external.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.Configurable;

public interface AuthenticationProvider extends Configurable {

    TdarGroup[] DEFAULT_GROUPS = { TdarGroup.TDAR_USERS, TdarGroup.JIRA_USERS };

    void logout(HttpServletRequest request, HttpServletResponse response, String token, TdarUser user);

    AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, String name, String password);

    boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response);

    AuthenticationResult addUser(TdarUser person, String password, TdarGroup... groups);

    boolean deleteUser(TdarUser person);

    void requestPasswordReset(TdarUser person);

    /**
     * Resets a Person's password to a random password and emails the new password to them.
     * Handles the case where an administrator resets the password of a user, presumably if the admin thinks the account has been compromised.
     */
    void resetUserPassword(TdarUser person);

    void updateUserPassword(TdarUser person, String password);

    String[] findGroupMemberships(TdarUser person);

    String getPasswordResetURL();

    AuthenticationResult checkToken(String token, HttpServletRequest request);

    boolean updateBasicUserInformation(TdarUser user);

    boolean renameUser(TdarUser user, String newUserName);

}
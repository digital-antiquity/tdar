package org.tdar.core.dao.external.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.Configurable;

public interface AuthenticationProvider extends Configurable {

    TdarGroup[] DEFAULT_GROUPS = { TdarGroup.TDAR_USERS, TdarGroup.JIRA_USERS, TdarGroup.CONFLUENCE_USERS };

    abstract void logout(HttpServletRequest request, HttpServletResponse response);

    abstract AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, String name, String password);

    abstract boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response);

    abstract boolean addUser(Person person, String password, TdarGroup... groups);

    abstract boolean deleteUser(Person person);

    /**
     * Resets a Person's password to a random password and emails the new password to them.
     * Handles the case where an administrator resets the password of a user, presumably if the admin thinks the account has been compromised.
     */
    abstract void resetUserPassword(Person person);

    abstract void updateUserPassword(Person person, String password);

    abstract String[] findGroupMemberships(Person person);

    abstract String getPasswordResetURL();

}
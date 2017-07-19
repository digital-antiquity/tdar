package org.tdar.core.service.external;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.core.service.external.session.SessionData;

public interface AuthenticationService {

    String LEGACY_USERNAME_VALID_REGEX = "^\\w[a-zA-Z0-9+@._\\-\\s]{0,253}\\w$";
    String USERNAME_VALID_REGEX = "^[a-zA-Z0-9+@\\.\\-_]{5,255}$";
    String EMAIL_VALID_REGEX = "^[a-zA-Z0-9+@\\.\\-_]{4,255}$";

    TdarUser findByUsername(String username);

    /*
     * exposes the groups the user is a member of from the external Provider; exposes groups as a String, as the external provider may include other permissions
     * beyond just tDAR groups
     */
    Collection<String> getGroupMembership(TdarUser person);

    /*
     * Returns a list of the people in the @link groupMembershipCache which is useful in tracking what's going on with tDAR at a given moment. This would be
     * helpful for
     * a shutdown hook, as well as, for knowing when it's safe to deploy.
     */
    List<TdarUser> getCurrentlyActiveUsers();

    List<TdarGroup> findGroupMemberships(TdarUser user);

    /**
     * Checks whether a user has pending policy agreements they must accept
     * 
     * @param user
     * @return true if user has pending requirements, otherwise false
     */
    boolean userHasPendingRequirements(TdarUser user);

    /*
     * Not currently used; but would allow for the updating of a username in the external auth system by deleting the user and adding them again. In Crowd 2.8
     * this is builtin function; but it might not be for LDAP.
     */
    void updateUsername(TdarUser person, String newUsername, String password);

    /*
     * Authenticate a web user passing in the Request, Response, username and password. Checks that (a) the username is valid (b) that the user can authenticate
     * (c) that user exists and is valid within tDAR (active);
     * 
     * @param UserLogin - a bean containing username and password data
     * 
     * @param request - the @link HttpServletRequest to read cookies from or other information
     * 
     * @param response - the @link HttpServletResponse to set the error code on
     * 
     * @param sessionData - the @SessionData object to intialize with the user's session / cookie information if logged in properly.
     */
    AuthenticationResult authenticatePerson(UserLogin userLogin, HttpServletRequest request, HttpServletResponse response,
            SessionData sessionData);

    /**
     * TdarGroups are represented in the external auth systems, but enable global permissions in tDAR; Admins, Billing Administrators, etc.
     */
    boolean isMember(TdarUser person, TdarGroup group);

    /**
     * Depending on how a person was added to CROWD or LDAP, they may have redundant group permissions (and probably should). Thus, given a set of permissions,
     * we find the one with the greatest rights
     */
    TdarGroup findGroupWithGreatestPermissions(TdarUser person);

    /*
     * creates an authentication token (last step in authenticating); that tDAR can use for the entire session
     */
    void createAuthenticationToken(TdarUser person, SessionData session);

    /*
     * Checks that a username to be added is valid
     */
    boolean isValidUsername(String username);

    /*
     * This is separate to ensure that legacy usernames are supported by the system
     */
    boolean isPossibleValidUsername(String username);

    /*
     * Checks that the email is a valid email address
     */
    boolean isValidEmail(String email);

    /**
     * allow for the clearing of the permissions cache. This is used both by "tests" and by the @link ScheduledProcessService to rest the
     * cache externally on a scheduled basis.
     */
    void clearPermissionsCache();

    /*
     * Removes a specific @link Person from the Permissions cache (e.g. when they log out).
     */
    void clearPermissionsCache(TdarUser tdarUser);

    /**
     * Authenticate a user, and optionally create the user account prior to authentication.
     * 
     * FIXME: handling of transient reconciliation needs refactoring
     * 
     * @param person
     * @param password
     * @param institutionName
     * @param request
     * @param response
     * @param sessionData
     * @param contributor
     * @return
     */
    AuthenticationResult addAndAuthenticateUser(UserRegistration reg, HttpServletRequest request,
            HttpServletResponse response, SessionData sessionData);

    void updatePersonWithInvites(TdarUser person);

    AuthenticationResult checkToken(String token, SessionData sessionData, HttpServletRequest request);

    void setProvider(AuthenticationProvider provider);

    AuthenticationProvider getProvider();

    void logout(SessionData sessionData, HttpServletRequest servletRequest, HttpServletResponse servletResponse, TdarUser user);

    String getSsoTokenFromRequest(HttpServletRequest request);

    /**
     * Provides access to the configured @link AuthenticationProvider -- CROWD or LDAP, for example. Consider making private.
     */
    AuthenticationProvider getAuthenticationProvider();

    /**
     * @param user
     * @return List containing pending requirements for the specified user
     */
    List<AuthNotice> getUserRequirements(TdarUser user);

    /**
     * Update Person record to indicate that the specified user has satisfied a required task
     * 
     * @param user
     * @param req
     */
    void satisfyPrerequisite(TdarUser user, AuthNotice req);

    /**
     * Indicate that the user associated with the specified session has acknowledged/accepted the specified notices
     * (e.g. user agreements)
     * 
     * @param sessionData
     * @param notices
     */
    void satisfyUserPrerequisites(SessionData sessionData, Collection<AuthNotice> notices);

    /*
     * Normalize the username being passed in; we may need to do more than lowercase it, such as run it through a REGEXP.
     */
    String normalizeUsername(String userName);

    void requestPasswordReset(String usernameOrEmail);

}
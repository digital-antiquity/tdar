package org.tdar.balk.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.core.service.external.session.SessionData;

public interface SimpleAuthenticationService {

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

    AuthenticationResult checkToken(String token, SessionData sessionData, HttpServletRequest request);

    void setProvider(AuthenticationProvider provider);

    AuthenticationProvider getProvider();

    void logout(SessionData sessionData, HttpServletRequest servletRequest, HttpServletResponse servletResponse);

    String getSsoTokenFromRequest(HttpServletRequest request);

    /**
     * Depending on how a person was added to CROWD or LDAP, they may have redundant group permissions (and probably should). Thus, given a set of permissions,
     * we find the one with the greatest rights
     */
    TdarGroup findGroupWithGreatestPermissions(String username);

    List<TdarGroup> findGroupMemberships(TdarUser user);

    boolean isMember(String username, TdarGroup group);

}
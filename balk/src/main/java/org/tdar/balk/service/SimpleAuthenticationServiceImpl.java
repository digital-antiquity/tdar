package org.tdar.balk.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationStatus;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.core.service.external.session.SessionData;

@Service
public class SimpleAuthenticationServiceImpl implements SimpleAuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AuthenticationProvider provider;
    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();


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
    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#authenticatePerson(org.tdar.core.service.external.auth.UserLogin, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.tdar.core.service.external.session.SessionData)
     */
    @Override
    @Transactional
    public AuthenticationResult authenticatePerson(UserLogin userLogin, HttpServletRequest request, HttpServletResponse response,
            SessionData sessionData) {

        AuthenticationResult result = getProvider().authenticate(request, response, userLogin.getLoginUsername(), userLogin.getLoginPassword());
        if (!result.getType().isValid()) {
            logger.debug("Couldn't authenticate {} - (reason: {})", userLogin.getLoginUsername(), result);
            throw new TdarRecoverableRuntimeException("auth.couldnt_authenticate", Arrays.asList(result.getType().getMessage()));
        }
        setupUser(userLogin.getLoginUsername(), sessionData);

        result.setStatus(AuthenticationStatus.AUTHENTICATED);
        return result;
    }


    private void setupUser(String username, SessionData sessionData) {
        TdarUser user = new TdarUser();
        user.setUsername(username);
        user.setId(1L);
        sessionData.setTdarUser(user);
    }
    

    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#checkToken(java.lang.String, org.tdar.core.service.external.session.SessionData, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public AuthenticationResult checkToken(String token, SessionData sessionData, HttpServletRequest request) {
        AuthenticationResult result = provider.checkToken(token, request);
        logger.debug("token check result: {}", result.getStatus());
        if (result.getType().isValid()) {
            setupUser(result.getTokenUsername(), sessionData);
        }
        return result;
    }


    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#setProvider(org.tdar.core.dao.external.auth.AuthenticationProvider)
     */
    @Override
    @Autowired(required=false)
    @Qualifier("AuthenticationProvider")
    public void setProvider(AuthenticationProvider provider) {
        this.provider = provider;
        if (provider != null) {
            logger.debug("Authentication Provider: {}", provider.getClass().getSimpleName());
        } else {
            logger.debug("Authentication Provider: NOT CONFIGURED");
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#getProvider()
     */
    @Override
    public AuthenticationProvider getProvider() {
        return provider;
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#logout(org.tdar.core.service.external.session.SessionData, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    @Transactional(readOnly = true)
    public void logout(SessionData sessionData, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        sessionData.clearAuthenticationToken();
        String token = getSsoTokenFromRequest(servletRequest);
        getProvider().logout(servletRequest, servletResponse, token, null);
    }
    
    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#getSsoTokenFromRequest(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getSsoTokenFromRequest(HttpServletRequest request) {
        String token = request.getParameter(CONFIG.getRequestTokenName());
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        if (!ArrayUtils.isEmpty(request.getCookies())) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals(CONFIG.getRequestTokenName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }



    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#findGroupWithGreatestPermissions(java.lang.String)
     */
    @Override
    public TdarGroup findGroupWithGreatestPermissions(String username) {
        if (username == null) {
            return TdarGroup.UNAUTHORIZED;
        }
        TdarUser person = new TdarUser();
        person.setId(1L);
        person.setUsername(username);
        TdarGroup greatestPermissionGroup = TdarGroup.UNAUTHORIZED;
        List<TdarGroup> groups = findGroupMemberships(person);
        logger.trace("Found {} memberships for {}", Arrays.asList(groups), username);
        for (TdarGroup group : groups) {
            if (group.hasGreaterPermissions(greatestPermissionGroup)) {
                greatestPermissionGroup = group;
            }
        }
        return greatestPermissionGroup;
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#findGroupMemberships(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public List<TdarGroup> findGroupMemberships(TdarUser user) {
        String[] groups = getProvider().findGroupMemberships(user);
        logger.trace("Found {} memberships for {}", Arrays.asList(groups), user.getUsername());
        List<TdarGroup> toReturn = new ArrayList<>();
        for (String groupString : groups) {
            toReturn.add(TdarGroup.fromString(groupString));
        }
        return toReturn;
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.SimpleAuthenticationService#isMember(java.lang.String, org.tdar.core.bean.TdarGroup)
     */
    @Override
    public boolean isMember(String username, TdarGroup group) {
        TdarGroup greatestPermissionGroup = findGroupWithGreatestPermissions(username);
        return greatestPermissionGroup.hasGreaterPermissions(group);
    }
}

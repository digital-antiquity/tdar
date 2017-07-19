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
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.core.service.external.session.SessionData;

@Service
public class SimpleAuthenticationService {

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
    

    public AuthenticationResult checkToken(String token, SessionData sessionData, HttpServletRequest request) {
        AuthenticationResult result = provider.checkToken(token, request);
        logger.debug("token check result: {}", result.getStatus());
        if (result.getType().isValid()) {
            setupUser(result.getTokenUsername(), sessionData);
        }
        return result;
    }


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

    public AuthenticationProvider getProvider() {
        return provider;
    }

    @Transactional(readOnly = true)
    public void logout(SessionData sessionData, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        sessionData.clearAuthenticationToken();
        String token = getSsoTokenFromRequest(servletRequest);
        getProvider().logout(servletRequest, servletResponse, token, null);
    }
    
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



    /**
     * Depending on how a person was added to CROWD or LDAP, they may have redundant group permissions (and probably should). Thus, given a set of permissions,
     * we find the one with the greatest rights
     */
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

    public List<TdarGroup> findGroupMemberships(TdarUser user) {
        String[] groups = getProvider().findGroupMemberships(user);
        logger.trace("Found {} memberships for {}", Arrays.asList(groups), user.getUsername());
        List<TdarGroup> toReturn = new ArrayList<>();
        for (String groupString : groups) {
            toReturn.add(TdarGroup.fromString(groupString));
        }
        return toReturn;
    }

    public boolean isMember(String username, TdarGroup group) {
        TdarGroup greatestPermissionGroup = findGroupWithGreatestPermissions(username);
        return greatestPermissionGroup.hasGreaterPermissions(group);
    }
}

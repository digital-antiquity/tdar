package org.tdar.core.dao.external.auth;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.xfire.XFireRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;

import com.atlassian.crowd.integration.authentication.PasswordCredential;
import com.atlassian.crowd.integration.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.integration.exception.ApplicationPermissionException;
import com.atlassian.crowd.integration.exception.InactiveAccountException;
import com.atlassian.crowd.integration.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.integration.exception.InvalidCredentialException;
import com.atlassian.crowd.integration.exception.InvalidUserException;
import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
import com.atlassian.crowd.integration.http.HttpAuthenticator;
import com.atlassian.crowd.integration.model.UserConstants;
import com.atlassian.crowd.integration.service.soap.client.SecurityServerClient;
import com.atlassian.crowd.integration.soap.SOAPAttribute;
import com.atlassian.crowd.integration.soap.SOAPPrincipal;

/**
 * $Id$
 * 
 * <p>
 * Provides authentication services and group management via Atlassian Crowd.
 * </p>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 * @see <a href='http://confluence.atlassian.com/display/CROWD/Creating+a+Crowd+Client+for+your+Custom+Application'>Crowd documentation</a>
 */
@Service
public class CrowdSoapDao extends BaseAuthenticationProvider {

    private final SecurityServerClient securityServerClient;

    private final HttpAuthenticator httpAuthenticator;

    private String passwordResetURL;

    public CrowdSoapDao() {
        this.httpAuthenticator = null;
        this.securityServerClient = null;
    }

    @Autowired(required = false)
    public CrowdSoapDao(SecurityServerClient securityServerClient, HttpAuthenticator httpAuthenticator) {
        this.securityServerClient = securityServerClient;
        this.httpAuthenticator = httpAuthenticator;
    }

    @Override
    public boolean isConfigured() {
        if (securityServerClient == null || httpAuthenticator == null) {
            logger.debug("client and/or authenticator are null " + securityServerClient + " " + httpAuthenticator);
            return false;
        }
        try {
            securityServerClient.authenticate();
        } catch (Exception e) {
            logger.info(e);
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#logout(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            httpAuthenticator.logoff(request, response);
        } catch (RemoteException e) {
            logger.error("Couldn't reach crowd server", e);
        } catch (InvalidAuthorizationTokenException e) {
            logger.error("Application didn't authenticate properly with the crowd server, check crowd.properties", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#authenticate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
     * java.lang.String, java.lang.String)
     */
    @Override
    public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, String name, String password) {
        try {
            httpAuthenticator.authenticate(request, response, name, password);
            return AuthenticationResult.VALID;
        } catch (RemoteException e) {
            logger.error("could not reach crowd server", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        } catch (InvalidAuthorizationTokenException e) {
            logger.error("Application did not authenticate properly with the server, check crowd.properties", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        } catch (InvalidAuthenticationException e) {
            // this is the only exception that should be DEBUG level only.
            logger.debug("Invalid authentication for " + name, e);
            return AuthenticationResult.INVALID_PASSWORD.exception(e);
        } catch (InactiveAccountException e) {
            logger.debug("Inactive account for " + name, e);
            return AuthenticationResult.INACTIVE_ACCOUNT.exception(e);
        } catch (ApplicationAccessDeniedException e) {
            logger.error("This webapp is not currently authorized to access crowd server", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        } catch (XFireRuntimeException e) {
            logger.error("Unhandled RuntimeException", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#isAuthenticated(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
        try {
            return httpAuthenticator.isAuthenticated(request, response);
        } catch (InvalidAuthorizationTokenException exception) {
            logger.error("Invalid authorization token", exception);
            return false;
        } catch (RemoteException exception) {
            logger.error("Unable to connect to crowd authorization server", exception);
            throw new RuntimeException(exception);
        } catch (ApplicationAccessDeniedException e) {
            logger.error("This application denied access to crowd server, check crowd.properties and crowd server configuration", e);
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#addUser(org.tdar.core.bean.entity.Person, java.lang.String)
     */
    @Override
    public boolean addUser(Person person, String password, TdarGroup... groups) {
        String login = person.getUsername();
        try {
            securityServerClient.findPrincipalByName(login);
            // if this succeeds, then this principal already exists.
            // FIXME: if they already exist in the system, we should let them know
            // that they already have an account in the system and that they can
            // just authenticate to edit their account / profile.
            logger.warn("XXX: Trying to add a user that already exists: [" + person.toString() + "]\n Returning and attempting to authenticate them.");
            // just check if authentication works then.
            return false;
        } catch (ObjectNotFoundException expected) {
            logger.debug("Object not found, as expected.");
        } catch (RemoteException e) {
            logger.error("Caught RemoteException while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthorizationTokenException e) {
            logger.error("Invalid auth token", e);
            throw new RuntimeException(e);
        }
        logger.debug("Adding user : " + person);

        SOAPPrincipal principal = new SOAPPrincipal();
        principal.setActive(true);
        principal.setName(login);
        SOAPAttribute[] attributes = new SOAPAttribute[5];
        attributes[0] = createSimpleAttribute(UserConstants.EMAIL, person.getEmail());
        attributes[1] = createSimpleAttribute(UserConstants.FIRSTNAME, person.getFirstName());
        attributes[2] = createSimpleAttribute(UserConstants.LASTNAME, person.getLastName());
        attributes[3] = createSimpleAttribute(UserConstants.DISPLAYNAME, person.getProperName());
        attributes[4] = createSimpleAttribute(UserConstants.USERNAME, person.getUsername());
        principal.setAttributes(attributes);
        PasswordCredential credential = new PasswordCredential(password);
        if (ArrayUtils.isEmpty(groups)) {
            groups = AuthenticationProvider.DEFAULT_GROUPS;
        }
        try {
            principal = securityServerClient.addPrincipal(principal, credential);
            for (TdarGroup group : groups) {
                securityServerClient.addPrincipalToGroup(login, group.getGroupName());
            }
        } catch (RemoteException e) {
            logger.error("Unable to connect to crowd server", e);
            throw new RuntimeException(e);
        } catch (ApplicationPermissionException e) {
            logger.error("Crowd server does not permit this application to connect", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthorizationTokenException e) {
            logger.debug("invalid auth token", e);
        } catch (InvalidCredentialException e) {
            logger.debug("invalid credentials", e);
        } catch (InvalidUserException e) {
            logger.error("Unable to add user: " + login, e);
        } catch (ObjectNotFoundException e) {
            logger.error("Unable to add principal " + principal + " to group", e);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#deleteUser(org.tdar.core.bean.entity.Person)
     */
    @Override
    public boolean deleteUser(Person person) {
        String login = person.getUsername();
        SOAPPrincipal principal = null;
        try {
            principal = securityServerClient.findPrincipalByName(login);
            if (principal == null) {
                return false;
            }
            securityServerClient.removePrincipal(principal.getName());

        } catch (ObjectNotFoundException expected) {
            logger.debug("Object not found, as expected.", expected);
        } catch (RemoteException e) {
            logger.error("Caught RemoteException while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthorizationTokenException e) {
            logger.error("Invalid auth token", e);
            throw new RuntimeException(e);
        } catch (ApplicationPermissionException e) {
            logger.error("could not remove user", e);
        }
        logger.debug("Removed user : " + person);
        return true;
    }

    private SOAPAttribute createSimpleAttribute(String key, String value) {
        SOAPAttribute attribute = new SOAPAttribute();
        attribute.setName(key);
        attribute.setValues(new String[] { value });
        return attribute;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#resetUserPassword(org.tdar.core.bean.entity.Person)
     */
    @Override
    public void resetUserPassword(Person person) {
        // TODO all manner of validation required here
        try {
            securityServerClient.resetPrincipalCredential(person.getUsername().toLowerCase());
        } catch (Exception e) {
            logger.error("could not reset password", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#updateUserPassword(org.tdar.core.bean.entity.Person, java.lang.String)
     */
    @Override
    public void updateUserPassword(Person person, String password) {
        PasswordCredential credential = new PasswordCredential(password);
        // TODO all manner of validation required here
        try {
            securityServerClient.updatePrincipalCredential(person.getUsername().toLowerCase(), credential);
        } catch (Exception e) {
            logger.error("could not change password", e);
        }

    }

    @Override
    public String[] findGroupMemberships(Person person) {
        String toFind = person.getUsername();
        if (StringUtils.isBlank(toFind)) {
            toFind = person.getEmail();
        }
        try {
            return securityServerClient.findGroupMemberships(toFind);
        } catch (RemoteException e) {
            logger.error("Caught RemoteException while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthorizationTokenException e) {
            logger.error("Caught Invalid Authorization Exception while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (ObjectNotFoundException e) {
            logger.error("Caught Object Not Found Exception while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPasswordResetURL()
    {
        return passwordResetURL;
    }

    @Value("${crowd.passwordreseturl:http://auth.tdar.org/crowd/console/forgottenlogindetails!default.action}")
    public void setPasswordResetURL(String url)
    {
        this.passwordResetURL = url;
    }
}

package org.tdar.core.service;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.xwork.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.xfire.XFireRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CrowdService {

    public enum TdarGroup {

        TDAR_ADMIN("tdar-admins"), TDAR_USERS("tdar-users"),
        JIRA_USERS("jira-users"), CONFLUENCE_USERS("confluence-users");

        private final String groupName;

        TdarGroup(String groupName) {
            this.groupName = groupName;
        }

        public String getGroupName() {
            return groupName;
        }

        public String toString() {
            return groupName;
        }

        public static List<TdarGroup> getUserGroups() {
            return Arrays.asList(TDAR_USERS, JIRA_USERS, CONFLUENCE_USERS);
        }
    }

    public enum AuthenticationResult implements Serializable {
        VALID(""), INVALID_PASSWORD("Authentication failed.  Please check that your email and password were entered correctly."),
        INACTIVE_ACCOUNT("This account is inactive."),
        REMOTE_EXCEPTION("The authentication server is currently down.  Please try authenticating again in a few minutes.");
        private final String message;
        private transient ThreadLocal<Throwable> threadLocalThrowable = new ThreadLocal<Throwable>();

        AuthenticationResult(String message) {
            this.message = message;
        }

        public AuthenticationResult exception(Throwable throwable) {
            threadLocalThrowable.set(throwable);
            return this;
        }

        public String getMessage() {
            return message;
        }

        public String toString() {
            Throwable throwable = threadLocalThrowable.get();
            if (throwable == null) {
                return message;
            }
            return message + " Exception: " + throwable.getLocalizedMessage();
        }

        public boolean isValid() {
            return this == VALID;
        }
    }

    private final Logger logger = Logger.getLogger(getClass());

    private final SecurityServerClient securityServerClient;

    private final HttpAuthenticator httpAuthenticator;

    @Autowired
    public CrowdService(SecurityServerClient securityServerClient, HttpAuthenticator httpAuthenticator) {
        this.securityServerClient = securityServerClient;
        this.httpAuthenticator = httpAuthenticator;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            httpAuthenticator.logoff(request, response);
        } catch (RemoteException e) {
            e.printStackTrace();
            logger.error("Couldn't reach crowd server", e);
        } catch (InvalidAuthorizationTokenException e) {
            e.printStackTrace();
            logger.error("Application didn't authenticate properly with the crowd server, check crowd.properties", e);
        }
    }

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
            logger.error("Inactive account for " + name, e);
            return AuthenticationResult.INACTIVE_ACCOUNT.exception(e);
        } catch (ApplicationAccessDeniedException e) {
            logger.error("This webapp is not currently authorized to access crowd server", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        } catch (XFireRuntimeException e) {
            logger.error("Unhandled RuntimeException", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        }
    }

    public boolean isAdministrator(Person person) {
        String email = person.getEmail();
        if (StringUtils.isBlank(email)) {
            return false;
        }
        try {
            return securityServerClient.isGroupMember(TdarGroup.TDAR_ADMIN.getGroupName(), email);
        } catch (InvalidAuthorizationTokenException exception) {
            logger.error("Invalid authorization exception", exception);
            return false;
        } catch (RemoteException exception) {
            logger.error("Unable to contact crowd server", exception);
            return false;
        }
    }

    public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
        try {
            return httpAuthenticator.isAuthenticated(request, response);
        } catch (InvalidAuthorizationTokenException exception) {
            exception.printStackTrace();
            logger.error("Invalid authorization token", exception);
            return false;
        } catch (RemoteException exception) {
            exception.printStackTrace();
            logger.error("Unable to connect to crowd authorization server", exception);
            throw new RuntimeException(exception);
        } catch (ApplicationAccessDeniedException e) {
            e.printStackTrace();
            logger.error("This application denied access to crowd server, check crowd.properties and crowd server configuration", e);
            throw new RuntimeException(e);
        }
    }

    public boolean addUser(Person person, String password) {
        String email = person.getEmail();
        try {
            securityServerClient.findPrincipalByName(email);
            // if this succeeds, then this principal already exists.
            // FIXME: if they already exist in the system, we should let them
            // know
            // that they already have an account in the system and that they can
            // just authenticate
            // to edit their account / profile.
            logger.warn("XXX: Trying to add a user that already exists: [" + person.toString() + "]\n Returning and attempting to authenticate them.");
            // just check if authentication works then.
            return false;
        } catch (ObjectNotFoundException expected) {
            logger.debug("Object not found, as expected.");
        } catch (RemoteException e) {
            e.printStackTrace();
            logger.error("Caught RemoteException while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthorizationTokenException e) {
            e.printStackTrace();
            logger.error("Invalid auth token", e);
            throw new RuntimeException(e);
        }
        logger.debug("Adding user : " + person);

        SOAPPrincipal principal = new SOAPPrincipal();
        principal.setActive(true);
        principal.setName(email);
        SOAPAttribute[] attributes = new SOAPAttribute[4];
        attributes[0] = createSimpleAttribute(UserConstants.EMAIL, email);
        attributes[1] = createSimpleAttribute(UserConstants.FIRSTNAME, person.getFirstName());
        attributes[2] = createSimpleAttribute(UserConstants.LASTNAME, person.getLastName());
        attributes[3] = createSimpleAttribute(UserConstants.DISPLAYNAME, person.getProperName());
        // FIXME: is this necessary?
        // attributes[4] =
        // createSimpleAttribute(RemotePrincipalConstants.USERNAME, email);
        principal.setAttributes(attributes);
        PasswordCredential credential = new PasswordCredential(password);
        try {
            principal = securityServerClient.addPrincipal(principal, credential);
            securityServerClient.addPrincipalToGroup(email, TdarGroup.TDAR_USERS.getGroupName());
            // FIXME: we're running into user limits for confluence/jira.
//            securityServerClient.addPrincipalToGroup(email, "confluence-users");
            securityServerClient.addPrincipalToGroup(email, TdarGroup.JIRA_USERS.getGroupName());
        } catch (RemoteException e) {
            e.printStackTrace();
            logger.error("Unable to connect to crowd server", e);
            throw new RuntimeException(e);
        } catch (ApplicationPermissionException e) {
            e.printStackTrace();
            logger.error("Crowd server does not permit this application to connect", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthorizationTokenException e) {
            e.printStackTrace();
            logger.debug("invalid auth token", e);
        } catch (InvalidCredentialException e) {
            e.printStackTrace();
            logger.debug("invalid credentials", e);
        } catch (InvalidUserException e) {
            e.printStackTrace();
            logger.error("Unable to add user: " + email, e);
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
            logger.error("Unable to add principal " + principal + " to group", e);
        }
        return true;
    }

    public boolean deleteUser(Person person) {
        String email = person.getEmail();
        SOAPPrincipal principal = null;
        try {
            principal = securityServerClient.findPrincipalByName(email);
            if (principal == null) {
                return false;
            }
            securityServerClient.removePrincipal(principal.getName());

        } catch (ObjectNotFoundException expected) {
            logger.debug("Object not found, as expected.");
        } catch (RemoteException e) {
            e.printStackTrace();
            logger.error("Caught RemoteException while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthorizationTokenException e) {
            e.printStackTrace();
            logger.error("Invalid auth token", e);
            throw new RuntimeException(e);
        } catch (ApplicationPermissionException e) {
            logger.error("could not remove user");
            e.printStackTrace();
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
}

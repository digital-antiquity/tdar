package org.tdar.core.dao.external.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticatorImpl;
import com.atlassian.crowd.integration.http.util.CrowdHttpTokenHelperImpl;
import com.atlassian.crowd.integration.http.util.CrowdHttpValidationFactorExtractorImpl;
import com.atlassian.crowd.integration.rest.entity.PasswordEntity;
import com.atlassian.crowd.integration.rest.entity.UserEntity;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.CrowdClient;

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
public class CrowdRestDao extends BaseAuthenticationProvider {

    private CrowdClient securityServerClient;
    private CrowdHttpAuthenticator httpAuthenticator; 


    private Properties crowdProperties;
    private String passwordResetURL;

    @Autowired
    public CrowdRestDao(@Qualifier("crowdProperties") Properties crowdProperties) {
        logger.info("initializing crowd rest dao: {}", crowdProperties);
        this.crowdProperties = crowdProperties;
        try {
            ClientProperties clientProperties = ClientPropertiesImpl.newInstanceFromProperties(crowdProperties);
            RestCrowdClientFactory factory = new RestCrowdClientFactory();
            securityServerClient = factory.newInstance(clientProperties);
            httpAuthenticator = new CrowdHttpAuthenticatorImpl(securityServerClient, clientProperties, CrowdHttpTokenHelperImpl.getInstance(CrowdHttpValidationFactorExtractorImpl.getInstance()));
        } catch (Exception e) {
            logger.error("exception: {}", e);
        }
    }


    @Override
    public boolean isConfigured() {
        logger.info("testing crowdRestDao: {} {}", securityServerClient, httpAuthenticator);
        if (securityServerClient == null || httpAuthenticator == null) {
            logger.debug("client and/or authenticator are null " + securityServerClient + " " + httpAuthenticator);
            return false;
        }
        try {
            securityServerClient.testConnection();
        } catch (Exception e) {
            logger.info("{}", e);
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
            httpAuthenticator.logout(request, response);
        } catch (ApplicationPermissionException e) {
            logger.error("application permission exception",e);
        } catch (InvalidAuthenticationException e) {
            logger.error("invalid authentication token",e);
        } catch (OperationFailedException e) {
            logger.error("operation failed",e);
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
        } catch (ExpiredCredentialException e) {
            logger.error("Credentials Expired", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        } catch (ApplicationPermissionException e) {
            logger.error("Application Permissions Exception", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        } catch (OperationFailedException e) {
            logger.error("Operation Failed", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        } catch (InvalidTokenException e) {
            logger.error("Invalid Token", e);
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
        } catch (OperationFailedException e) {
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
            
            User user = securityServerClient.getUser(login);
            // if this succeeds, then this principal already exists.
            // FIXME: if they already exist in the system, we should let them know
            // that they already have an account in the system and that they can
            // just authenticate to edit their account / profile.
            logger.warn("XXX: Trying to add a user that already exists: [" + person.toString() + "]\n Returning and attempting to authenticate them.");
            // just check if authentication works then.
            return false;
        } catch (ObjectNotFoundException expected) {
            logger.debug("Object not found, as expected.");
        } catch (OperationFailedException e) {
            logger.error("Caught RemoteException while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (ApplicationPermissionException e) {
            logger.error("Caught Permissions Exception while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthenticationException e) {
            logger.error("Invalid auth token", e);
            throw new RuntimeException(e);
        }
        logger.debug("Adding user : " + person);
        PasswordEntity passwordEntity = new PasswordEntity(password);
        PasswordCredential credential = new PasswordCredential(password);
        
        UserEntity user = new UserEntity(person.getUsername(), person.getFirstName(), person.getLastName(), person.getProperName(), person.getEmail(), passwordEntity , true);

        if (ArrayUtils.isEmpty(groups)) {
            groups = AuthenticationProvider.DEFAULT_GROUPS;
        }
        try {
            securityServerClient.addUser(user, credential);
            for (TdarGroup group : groups) {
                securityServerClient.addUserToGroup(login, group.getGroupName());
            }
        } catch (ApplicationPermissionException e) {
            logger.error("Crowd server does not permit this application to connect", e);
            throw new RuntimeException(e);
        } catch (InvalidCredentialException e) {
            logger.debug("invalid credentials", e);
        } catch (InvalidUserException e) {
            logger.error("Unable to add user (invalid user): " + login, e);
        } catch (ObjectNotFoundException e) {
            logger.error("Unable to add principal " + user + " to group", e);
        } catch (OperationFailedException e) {
            logger.error("Unable to add user (operation failed): " + login, e);
        } catch (InvalidAuthenticationException e) {
            logger.error("Unable to add user (invalid authentication): " + login, e);
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
        User principal = null;
        try {
            principal = securityServerClient.getUser(login);
            if (principal == null) {
                return false;
            }
            securityServerClient.removeUser(principal.getName());

        } catch (ObjectNotFoundException expected) {
            logger.debug("Object not found, as expected.", expected);
        } catch (ApplicationPermissionException e) {
            logger.error("could not remove user", e);
        } catch (OperationFailedException e) {
            logger.error("Caught OperationFailed while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthenticationException e) {
            logger.error("Invalid auth token", e);
            throw new RuntimeException(e);
        }
        logger.debug("Removed user : " + person);
        return true;
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
            securityServerClient.requestPasswordReset(person.getUsername());
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
        try {
            securityServerClient.updateUserCredential(person.getUsername().toLowerCase(), password);
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
            List<Group> groupsForUser = securityServerClient.getGroupsForUser(toFind, 0, 100);
            List<String> groups = new ArrayList<>();
            for (Group group: groupsForUser) {
                groups.add(group.getName());
            }
            return groups.toArray(new String[0]);
        } catch (ObjectNotFoundException e) {
            logger.error("Caught Object Not Found Exception while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (OperationFailedException e) {
            logger.error("Caught OperationFailed while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (InvalidAuthenticationException e) {
            logger.error("Caught Invalid Authorization Exception while trying to contact the crowd server", e);
            throw new RuntimeException(e);
        } catch (ApplicationPermissionException e) {
            logger.error("Caught ApplicationPermissonException while trying to contact the crowd server", e);
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
    
    @Override
    public boolean isEnabled() {
        if (crowdProperties == null || crowdProperties.getProperty("crowd.server.url") == null) {
            return false;
        }
        return true;
    }

}

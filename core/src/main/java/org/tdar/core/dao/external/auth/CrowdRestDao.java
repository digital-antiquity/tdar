package org.tdar.core.dao.external.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.ConfigurationAssistant;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.exception.TdarAuthorizationException;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidEmailAddressException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticatorImpl;
import com.atlassian.crowd.integration.http.util.CrowdHttpTokenHelperImpl;
import com.atlassian.crowd.integration.http.util.CrowdHttpValidationFactorExtractorImpl;
import com.atlassian.crowd.integration.rest.entity.PasswordEntity;
import com.atlassian.crowd.integration.rest.entity.UserEntity;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.model.authentication.ValidationFactor;
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
// @Service
public class CrowdRestDao extends BaseAuthenticationProvider {
    private static final String CROWD_PROPERTIES = "crowd.properties";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private CrowdClient securityServerClient;
    private CrowdHttpAuthenticator httpAuthenticator;

    private Properties crowdProperties;
    private String passwordResetURL;

    public CrowdRestDao() throws IOException {
        Properties properties = new Properties();
        // leveraging factory method over spring autowiring
        // https://developer.atlassian.com/display/CROWDDEV/Java+Integration+Libraries
        InputStream inputStream = ConfigurationAssistant.toInputStream(CROWD_PROPERTIES);
        properties.load(inputStream);
        init(properties);
    }

    private void init(Properties properties) {
        this.crowdProperties = properties;
        logger.info("initializing crowd rest dao: {}", crowdProperties);
        try {
            ClientProperties clientProperties = ClientPropertiesImpl.newInstanceFromProperties(crowdProperties);
            RestCrowdClientFactory factory = new RestCrowdClientFactory();
            securityServerClient = factory.newInstance(clientProperties);
            setPasswordResetURL(
                    crowdProperties.getProperty("crowd.passwordreseturl", "http://auth.tdar.org/crowd/console/forgottenlogindetails!default.action"));
            httpAuthenticator = new CrowdHttpAuthenticatorImpl(securityServerClient, clientProperties,
                    CrowdHttpTokenHelperImpl.getInstance(CrowdHttpValidationFactorExtractorImpl.getInstance()));
            logger.debug("maxHttpConnections: {} timeout: {}", clientProperties.getHttpMaxConnections(), clientProperties.getHttpTimeout());
        } catch (Exception e) {
            logger.error("exception: {}", e);
        }
    }

    public CrowdRestDao(Properties properties) {
        init(properties);
    }

    @Override
    public boolean isConfigured() {
        logger.info("testing crowdRestDao: {} {}", securityServerClient, httpAuthenticator);
        if ((securityServerClient == null) || (httpAuthenticator == null)) {
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
    public void logout(HttpServletRequest request, HttpServletResponse response, String token_, TdarUser user) {
        String token = token_;
        try {
            httpAuthenticator.logout(request, response);
            if (StringUtils.isBlank(token)) {
                token = httpAuthenticator.getToken(request);
            }
            logger.debug("token: " + token);
            if (token != null) {
                securityServerClient.invalidateSSOToken(token);
            }
            logger.debug("logged out");
        } catch (ApplicationPermissionException e) {
            logger.error("application permission exception", e);
        } catch (InvalidAuthenticationException e) {
            logger.error("invalid authentication token", e);
        } catch (OperationFailedException e) {
            logger.error("operation failed", e);
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
            String token = httpAuthenticator.getToken(request);
            AuthenticationResult result = new AuthenticationResult(AuthenticationResultType.VALID, token);
            result.setTokenUsername(name);
            return result;
        } catch (InvalidAuthenticationException e) {
            // this is the only exception that should be DEBUG level only.
            logger.debug("++++ CROWD: Invalid authentication for " + name);
            return new AuthenticationResult(AuthenticationResultType.INVALID_PASSWORD, e);
        } catch (InactiveAccountException e) {
            logger.debug("++++ CROWD: Inactive account for " + name);
            return new AuthenticationResult(AuthenticationResultType.INACTIVE_ACCOUNT, e);
        } catch (ApplicationAccessDeniedException e) {
            logger.error("This webapp is not currently authorized to access crowd server", e);
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (ExpiredCredentialException e) {
            logger.error("++++ CROWD: Credentials Expired");
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (ApplicationPermissionException e) {
            logger.error("++++ CROWD: Application Permissions Exception");
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (OperationFailedException e) {
            logger.error("Operation Failed", e);
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (InvalidTokenException e) {
            logger.error("Invalid Token", e);
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
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
            throw new TdarAuthorizationException(e);
        }
    }

    @Override
    public void requestPasswordReset(TdarUser person) {
        try {
            securityServerClient.requestPasswordReset(person.getUsername());
        } catch (UserNotFoundException | OperationFailedException | InvalidAuthenticationException | ApplicationPermissionException
                | InvalidEmailAddressException e) {
            logger.error("exception requesting password reset", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#addUser(org.tdar.core.bean.entity.Person, java.lang.String)
     */
    @Override
    public AuthenticationResult addUser(TdarUser person, String password, TdarGroup... groups_) {
        TdarGroup[] groups = groups_;
        String login = person.getUsername();
        User user = null;
        try {

            user = securityServerClient.getUser(login);

            // if this succeeds, then this principal already exists.
            // FIXME: if they already exist in the system, we should let them know
            // that they already have an account in the system and that they can
            // just authenticate to edit their account / profile.
            logger.warn("XXX: Trying to add a user that already exists: [" + person.toString() + "]\n Returning and attempting to authenticate them.");
            // just check if authentication works then.
            if (user != null) {
                try {
                    securityServerClient.authenticateUser(login, password);
                } catch (Exception e) {
                    logger.error("AccountExists, but issues... ", e);
                    return new AuthenticationResult(AuthenticationResultType.ACCOUNT_EXISTS);
                }
            }
        } catch (ObjectNotFoundException expected) {
            logger.debug("Object not found, as expected.");
        } catch (OperationFailedException e) {
            logger.error("++++ CROWD: Caught RemoteException while trying to contact the crowd server", e);
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (ApplicationPermissionException e) {
            logger.error("++++ CROWD: Caught Permissions Exception while trying to contact the crowd server", e);
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (InvalidAuthenticationException e) {
            logger.error("++++ CROWD: Invalid auth token");
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        }
        boolean userNew = false;
        PasswordEntity passwordEntity = new PasswordEntity(password);
        PasswordCredential credential = new PasswordCredential(password);

        if (user == null) {
            userNew = true;
            logger.debug("Adding user : {} {} {} {} {} {} ", person.getUsername(), person.getFirstName(), person.getLastName(), person.getProperName(),
                    person.getEmail(), person.getId());
            user = new UserEntity(person.getUsername(), person.getFirstName(), person.getLastName(), person.getProperName(), person.getEmail(), passwordEntity,
                    true);
        }
        if (ArrayUtils.isEmpty(groups)) {
            groups = AuthenticationProvider.DEFAULT_GROUPS;
        }
        try {
            if (userNew) {
                securityServerClient.addUser(user, credential);
            }
            for (TdarGroup group : groups) {
                try {
                    securityServerClient.addUserToGroup(login, group.getGroupName());
                } catch (MembershipAlreadyExistsException e) {
                    // we'll ignore it if membership already exissts
                }
            }
        } catch (ApplicationPermissionException e) {
            logger.error("++++ CROWD: Crowd server does not permit this application to connect");
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (InvalidCredentialException e) {
            logger.debug("++++ CROWD: invalid credentials");
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (InvalidUserException e) {
            logger.error("++++ CROWD: Unable to add user (invalid user): " + login + " " + e.getMessage());
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (ObjectNotFoundException e) {
            logger.error("++++ CROWD: Unable to add principal " + user + " to group " + e.getMessage());
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (OperationFailedException e) {
            logger.error("++++ CROWD: Unable to add user (operation failed): " + login + " " + e.getMessage());
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        } catch (InvalidAuthenticationException e) {
            logger.error("++++ CROWD: Unable to add user (invalid authentication): " + login + " " + e.getMessage());
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        }

        return new AuthenticationResult(AuthenticationResultType.VALID);
    }

    public AuthenticationResult checkToken(String token, HttpServletRequest request) {
        try {
            User user = securityServerClient.findUserFromSSOToken(token);
            ArrayList<ValidationFactor> arrayList = new ArrayList<ValidationFactor>();
            arrayList.add(new ValidationFactor("remote_address", request.getRemoteAddr()));
            securityServerClient.validateSSOAuthentication(token, arrayList);
            AuthenticationResult result = new AuthenticationResult(AuthenticationResultType.VALID);
            result.setTokenUsername(user.getName());
            result.setToken(token);
            return result;
        } catch (OperationFailedException | InvalidAuthenticationException | ApplicationPermissionException | InvalidTokenException e) {
            logger.warn("++++ CROWD: Unable to process token " + token, e);
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthenticationProvider#deleteUser(org.tdar.core.bean.entity.Person)
     */
    @Override
    public boolean deleteUser(TdarUser person) {
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
            throw new TdarAuthorizationException(e);
        } catch (InvalidAuthenticationException e) {
            logger.error("Invalid auth token", e);
            throw new TdarAuthorizationException(e);
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
    public void resetUserPassword(TdarUser person) {
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
    public void updateUserPassword(TdarUser person, String password) {
        try {
            securityServerClient.updateUserCredential(person.getUsername().toLowerCase(), password);
        } catch (Exception e) {
            logger.error("could not change password", e);
        }

    }

    @Override
    public String[] findGroupMemberships(TdarUser person) {
        String toFind = person.getUsername();
        if (StringUtils.isBlank(toFind)) {
            toFind = person.getEmail();
        }
        try {
            List<Group> groupsForUser = securityServerClient.getGroupsForUser(toFind, 0, 100);
            List<String> groups = new ArrayList<>();
            for (Group group : groupsForUser) {
                groups.add(group.getName());
            }
            return groups.toArray(new String[0]);
        } catch (ObjectNotFoundException e) {
            logger.error("Caught Object Not Found Exception while trying to contact the crowd server", e);
            throw new TdarAuthorizationException(e);
        } catch (OperationFailedException e) {
            logger.error("Caught OperationFailed while trying to contact the crowd server", e);
            throw new TdarAuthorizationException(e);
        } catch (InvalidAuthenticationException e) {
            logger.error("Caught Invalid Authorization Exception while trying to contact the crowd server", e);
            throw new TdarAuthorizationException(e);
        } catch (ApplicationPermissionException e) {
            logger.error("Caught ApplicationPermissonException while trying to contact the crowd server", e);
            throw new TdarAuthorizationException(e);
        }
    }

    @Override
    public String getPasswordResetURL() {
        return passwordResetURL;
    }

    public void setPasswordResetURL(String url) {
        this.passwordResetURL = url;
    }

    @Override
    public boolean isEnabled() {
        if ((crowdProperties == null) || (crowdProperties.getProperty("crowd.server.url") == null)) {
            return false;
        }
        return true;
    }

    /**
     * Update user information (not including password) via the Atlassian CrowdClient Java API.
     * 
     * @param user
     * @return
     */
    @Override
    public boolean updateBasicUserInformation(TdarUser user) {
        UserEntity crowdUser = new UserEntity(user.getUsername(), user.getFirstName(), user.getLastName(), user.getProperName(), user.getEmail(), null,
                user.isActive());
        try {
            securityServerClient.updateUser(crowdUser);
        } catch (InvalidUserException | UserNotFoundException | OperationFailedException | InvalidAuthenticationException | ApplicationPermissionException e) {
            logger.debug("failed to update user: {}", e);
            return false;
        }
        return true;

    }

    @Override
    public boolean renameUser(TdarUser user, String newUserName) {
        try {
            securityServerClient.renameUser(user.getUsername(), newUserName);
        } catch (UserNotFoundException | InvalidUserException | OperationFailedException | ApplicationPermissionException | InvalidAuthenticationException e) {
            logger.error("could not rename user: {}", e, e);
            return false;
        }
        return true;
    }

    /**
     * Used for testing to get User Information
     * 
     * @param user
     * @return
     * @throws UserNotFoundException
     * @throws OperationFailedException
     * @throws ApplicationPermissionException
     * @throws InvalidAuthenticationException
     */
    protected User getUser(TdarUser user)
            throws UserNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException {
        return securityServerClient.getUser(user.getUsername());

    }

}

package org.tdar.core.dao.external.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult.AuthenticationResultType;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.service.EntityService;
//import org.tdar.utils.TestConfiguration;


/*
 * This provider is designed specifically for testing where no valid connection to an external service
 * is available.  Great for testing* and bascially nothing else. DO NOT USE IN PRODUCTION**
 *
 * To authenticate: follow the normal process, simply either register, or use our "test" usernames and "passwords"
 *
 * *As you might expect,  authentication-tests will probably fail
 * ** This class has failsafes to prevent use in production.  Don't rely on them.
 */
public class MockAuthenticationProvider extends BaseAuthenticationProvider {
    // hardcoded for testing
    public static final String ADMIN_USERNAME = "admin@tdar.org";
    public static final String ADMIN_PASSWORD = "admin";

    public static final String EDITOR_USERNAME = "editor@tdar.org";
    public static final String EDITOR_PASSWORD = "editor";

    public static final String BILLING_USERNAME = "admin@tdar.org";
    public static final String BILLING_PASSWORD = "admin";

    public static final String USERNAME = "test@tdar.org";
    public static final String PASSWORD = "test";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, MockAuthenticationInfo> users = new ConcurrentHashMap<String, MockAuthenticationInfo>();

    public MockAuthenticationProvider() {
        logger.debug("is prod?: {}", TdarConfiguration.getInstance().isProductionEnvironment());
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, String token) {
    }

    @Override
    public boolean isConfigured() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    };

    @Override
    public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, String name, String password) {

        if (TdarConfiguration.getInstance().isProductionEnvironment()) {
            logger.error("Mock Authentication is not allowed in production.");
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION);
        }

        AuthenticationResult result = new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION);

        logger.debug("trying to authenticate:: user: {}  password:{}", name, password);
        MockAuthenticationInfo user = users.get(name);
        if (user != null && Objects.equals(password, user.getPassword())) {
            result.setType(AuthenticationResultType.VALID);
        } else if (user != null) {
            result.setType(AuthenticationResultType.INVALID_PASSWORD);
        } else {
            result.setType(AuthenticationResultType.ACCOUNT_DOES_NOT_EXIST);
        }
        return result;
    }

    @Override
    public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
        return false;
    }

    @Override
    public AuthenticationResult addUser(TdarUser person, String password, TdarGroup... groups) {
        if (users.containsKey(person.getUsername())) {
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION);
        } else {
            MockAuthenticationInfo info = new MockAuthenticationInfo();
            info.setPassword(password);
            info.getMemberships().addAll(Arrays.asList(groups));
            users.put(person.getUsername(), info);
            return new AuthenticationResult(AuthenticationResultType.VALID);
        }
    }

    @Override
    public boolean deleteUser(TdarUser person) {
        try {
            users.remove(person.getEmail());
            return true;
        } catch (Exception e) {
            logger.debug("{}", e);
        }
        return false;
    }

    @Override
    public void resetUserPassword(TdarUser person) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUserPassword(TdarUser person, String password) {
        users.get(person.getUsername()).setPassword(password);
    }

    @Override
    public String[] findGroupMemberships(TdarUser person) {
        List<String> toReturn = new ArrayList<String>();
        for (TdarGroup group : users.get(person.getUsername()).getMemberships()) {
            toReturn.add(group.getGroupName());
        }
        String[] result = toReturn.toArray(new String[0]);
        logger.debug("group membership request: name:{}   membership:[{}]",
                new Object[] { person.getUsername(), users.get(person.getUsername()), result });
        return result;
    }

    @Override
    public String getPasswordResetURL() {
        // TODO Auto-generated method stub
        return null;
    }

    @Autowired
    public void setEntityService(EntityService entityService) {
        if (TdarConfiguration.getInstance().isProductionEnvironment()) {
            logger.info("Mock Authentication is not allowed in production. System will not load mock user db");
            return;
        }
        List<TdarUser> registeredUsers = entityService.findAllRegisteredUsers();
        for (TdarUser user : registeredUsers) {
            MockAuthenticationInfo info = new MockAuthenticationInfo();
            info.setPassword(user.getUsername());
            info.getMemberships().add(TdarGroup.TDAR_USERS);
            info.getMemberships().add(TdarGroup.JIRA_USERS);
            info.getMemberships().add(TdarGroup.CONFLUENCE_USERS);
            if (user.getUsername().equals(ADMIN_USERNAME)) {
                info.getMemberships().add(TdarGroup.TDAR_ADMIN);
                info.getMemberships().add(TdarGroup.TDAR_BILLING_MANAGER);
                info.getMemberships().add(TdarGroup.TDAR_API_USER);
                info.setPassword("admin");
            }

            if (user.getUsername().equals(EDITOR_USERNAME)) {
                info.getMemberships().add(TdarGroup.TDAR_EDITOR);
                info.setPassword(EDITOR_PASSWORD);
            }

            if (user.getUsername().equals(BILLING_USERNAME)) {
                info.getMemberships().add(TdarGroup.TDAR_BILLING_MANAGER);
                info.setPassword(BILLING_PASSWORD);
            }

            if (user.getUsername().equals(USERNAME)) {
                info.setPassword(PASSWORD);
            }
            users.put(user.getUsername(), info);
        }
    }

    @Override
    public AuthenticationResult checkToken(String token, HttpServletRequest request) {
        throw new TdarRuntimeException("error.not_implemented");
    }
}

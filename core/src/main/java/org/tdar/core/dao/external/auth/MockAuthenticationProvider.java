package org.tdar.core.dao.external.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
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

    public static final String BILLING_USERNAME = "billing@tdar.org";
    public static final String BILLING_PASSWORD = "billing";

    public static final String USERNAME = "test@tdar.org";
    public static final String PASSWORD = "test";

    // used for defining local passwords and rights for users
    private Map<String, MockAuthenticationInfo> localValues = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, MockAuthenticationInfo> users = new ConcurrentHashMap<String, MockAuthenticationInfo>();

    public MockAuthenticationProvider() {
        logger.debug("is prod?: {}", TdarConfiguration.getInstance().isProductionEnvironment());
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, String token, TdarUser user) {
        MockAuthenticationInfo info = users.get(user.getUsername().toLowerCase());
        if (info == null) {
            return;
        }
        info.setToken("abc123");
        logger.debug("mock logout: {} ({})", user.getUsername().toLowerCase(), token);
        // info.setToken(null);
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(TdarConfiguration.getInstance().getRequestTokenName())) {
                cookie.setMaxAge(0);
                cookie.setValue("abc123");
            }
            response.addCookie(cookie);
        }
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
    public void requestPasswordReset(TdarUser person) {
        return;
    }

    @Override
    public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, String name, String password) {

        if (TdarConfiguration.getInstance().isProductionEnvironment()) {
            logger.error("Mock Authentication is not allowed in production.");
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION);
        }

        AuthenticationResult result = new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION);

        logger.debug("trying to authenticate:: user: {}  password:{}", name, password);
        MockAuthenticationInfo user = users.get(name.toLowerCase());
        if (user != null && Objects.equals(password, user.getPassword())) {
            result.setType(AuthenticationResultType.VALID);
            String token = Long.toString(user.hashCode() + System.currentTimeMillis());
            result.setToken(token);
            result.setTokenUsername(name.toLowerCase());
            user.setToken(token);
            Cookie cookie = new Cookie(TdarConfiguration.getInstance().getRequestTokenName(), token);
            cookie.setMaxAge(1024);
            response.addCookie(cookie);
        } else if (user != null) {
            result.setType(AuthenticationResultType.INVALID_PASSWORD);
        } else {
            result.setType(AuthenticationResultType.INVALID_PASSWORD);
            // mirroring settings for crowd which are obfuscated for security
            // result.setType(AuthenticationResultType.ACCOUNT_DOES_NOT_EXIST);
        }
        return result;
    }

    @Override
    public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
        return false;
    }

    @Override
    public AuthenticationResult addUser(TdarUser person, String password, TdarGroup... groups_) {
        TdarGroup[] groups = groups_;
        if (users.containsKey(person.getUsername().toLowerCase())) {
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION);
        } else {
            if (ArrayUtils.isEmpty(groups)) {
                groups = AuthenticationProvider.DEFAULT_GROUPS;
            }
            logger.trace("adding: {} [{}]", person.getUsername().toLowerCase(), groups);
            MockAuthenticationInfo info = new MockAuthenticationInfo();
            info.setPassword(password);
            info.setUsername(person.getUsername().toLowerCase());
            info.getMemberships().addAll(Arrays.asList(groups));
            users.put(person.getUsername().toLowerCase(), info);
            return new AuthenticationResult(AuthenticationResultType.VALID);
        }
    }

    @Override
    public boolean deleteUser(TdarUser person) {
        try {
            String key = person.getUsername().toLowerCase();
            logger.debug("removing: {}", key);
            if (users.containsKey(key)) {
                users.remove(key);
            }
            if (users.containsKey(key)) {
                logger.error("USERS still contains key: {}", key);
            }
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
        users.get(person.getUsername().toLowerCase()).setPassword(password);
    }

    @Override
    public String[] findGroupMemberships(TdarUser person) {
        List<String> toReturn = new ArrayList<String>();
        MockAuthenticationInfo mockAuthenticationInfo = users.get(person.getUsername().toLowerCase());
        if (mockAuthenticationInfo == null) {
            return new String[0];
        }
        for (TdarGroup group : mockAuthenticationInfo.getMemberships()) {
            toReturn.add(group.getGroupName());
        }
        String[] result = toReturn.toArray(new String[0]);
        logger.debug("group membership request: name:{}   membership:[{}]",
                new Object[] { person.getUsername(), mockAuthenticationInfo, result });
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
            addUser(user, user.getUsername(), TdarGroup.TDAR_USERS, TdarGroup.JIRA_USERS, TdarGroup.CONFLUENCE_USERS);
            MockAuthenticationInfo info = users.get(user.getUsername().toLowerCase());

            switch (user.getUsername()) {
                case ADMIN_USERNAME:
                    info.getMemberships().add(TdarGroup.TDAR_ADMIN);
                    info.getMemberships().add(TdarGroup.TDAR_BILLING_MANAGER);
                    info.getMemberships().add(TdarGroup.TDAR_API_USER);
                    info.setPassword("admin");
                    break;

                case EDITOR_USERNAME:
                    info.getMemberships().add(TdarGroup.TDAR_EDITOR);
                    info.setPassword(EDITOR_PASSWORD);
                    break;

                case BILLING_USERNAME:
                    info.getMemberships().add(TdarGroup.TDAR_BILLING_MANAGER);
                    info.setPassword(BILLING_PASSWORD);
                    break;

                default:
                    info.setPassword(PASSWORD);
            }

            if (localValues.containsKey(user.getUsername())) {
                MockAuthenticationInfo local = localValues.get(user.getUsername());
                info.setPassword(local.getPassword());
                info.getMemberships().addAll(local.getMemberships());
                logger.debug("init: {}", user.getUsername().toLowerCase());
            }
            logger.trace("init: {}", user.getUsername().toLowerCase());
            users.put(user.getUsername().toLowerCase(), info);
        }
    }

    @Override
    public AuthenticationResult checkToken(String token, HttpServletRequest request) {
        AuthenticationResult result = new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION);
        logger.debug("checkToken:{}", token);

        if (StringUtils.isBlank(token)) {
            return result;
        }
        for (MockAuthenticationInfo info : users.values()) {
            logger.trace("checkToken:{} --> {} ({})", token, info.getUsername(), info.getToken());
            if (Objects.equals(token, info.getToken())) {
                result.setTokenUsername(info.getUsername());
                result.setType(AuthenticationResultType.VALID);
                result.setToken(token);
            }
        }
        return result;
    }

    public Map<String, MockAuthenticationInfo> getLocalValues() {
        return localValues;
    }

    public void setLocalValues(Map<String, MockAuthenticationInfo> localValues) {
        this.localValues = localValues;
    }

    @Override
    public boolean updateBasicUserInformation(TdarUser user) {
        MockAuthenticationInfo authenticationInfo = users.get(user.getUsername());
        if (!user.isActive()) {
            users.remove(user.getUsername());
        }
        return true;
    }

    @Override
    public boolean renameUser(TdarUser user, String newUserName) {
        throw new TdarRecoverableRuntimeException("error.not_implemented");
    }
}

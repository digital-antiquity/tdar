package org.tdar.core.service.external.auth.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.external.auth.AuthenticationResult;
import org.tdar.core.service.external.auth.TdarGroup;

/*
 * This provider is designed specifically for testing where no valid connection to an external service
 * is available.  Great for testing and bascially nothing else. DO NOT USE IN PRODUCTION
 */
@Service
public class MockAuthenticationProvider extends BaseAuthenticationProvider {

    private Map<String, String> users = new ConcurrentHashMap<String, String>();
    private List<String> adminUsers = new ArrayList<String>();
    private List<String> editors = new ArrayList<String>();

    public MockAuthenticationProvider() {
        users.put("admin@tdar.org", "admin");
        users.put("test@tdar.org", "test");
        adminUsers.add("admin@tdar.org");

    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
    }

    @Override
    public boolean isConfigured() {
        return Boolean.parseBoolean(System.getProperty("enableMockAuth", "false"));
    }

    @Override
    public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, String name, String password) {
        try {
            if (!users.containsKey(name)) {
                return AuthenticationResult.ACCOUNT_DOES_NOT_EXIST;
            }
            if (!users.get(name).equals(password)) {
                return AuthenticationResult.INVALID_PASSWORD;
            }
            if (users.get(name).equals(password)) {
                return AuthenticationResult.VALID;
            }
        } catch (Exception e) {
            logger.debug(e);
        }
        return AuthenticationResult.REMOTE_EXCEPTION;
    }

    @Override
    public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addUser(Person person, String password, TdarGroup... groups) {
        if (users.containsKey(person.getEmail())) {
            return false;
        } else {
            users.put(person.getEmail(), password);
            return true;
        }
    }

    @Override
    public boolean deleteUser(Person person) {
        try {
            users.remove(person.getEmail());
            return true;
        } catch (Exception e) {
            logger.debug(e);
        }
        return false;
    }

    @Override
    public void resetUserPassword(Person person) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUserPassword(Person person, String password) {
        users.put(person.getEmail(), password);
    }

    @Override
    public String[] findGroupMemberships(Person person) {
        List<String> toReturn = new ArrayList<String>();
        if (adminUsers.contains(person.getEmail())) {
            toReturn.add(TdarGroup.TDAR_ADMIN.name());
        }
        if (editors.contains(person.getEmail())) {
            toReturn.add(TdarGroup.TDAR_EDITOR.name());
        }
        toReturn.add(TdarGroup.TDAR_USERS.name());

        return toReturn.toArray(new String[0]);
    }

    @Override
    public String getPasswordResetURL() {
        // TODO Auto-generated method stub
        return null;
    }

}

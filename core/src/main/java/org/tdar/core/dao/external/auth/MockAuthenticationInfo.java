package org.tdar.core.dao.external.auth;

import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.TdarGroup;

public class MockAuthenticationInfo {

    private Set<TdarGroup> memberships = new HashSet<>();
    private String password;
    private String token;
    private String username;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<TdarGroup> getMemberships() {
        return memberships;
    }

    public void setMemberships(Set<TdarGroup> memberships) {
        this.memberships = memberships;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

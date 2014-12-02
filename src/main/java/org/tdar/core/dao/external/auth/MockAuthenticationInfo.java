package org.tdar.core.dao.external.auth;

import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.TdarGroup;

public class MockAuthenticationInfo {

    private Set<TdarGroup> memberships = new HashSet<>();
    private String password;
    
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
}

package org.tdar.struts.data;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;

public  class AuthWrapper<P extends Persistable> {

    private boolean authenticated = false;
    private TdarUser authenticatedUser;
    private P item;

    public AuthWrapper(P item, boolean authenticated, TdarUser authenticatedUser) {
        this.authenticated = authenticated;
        this.authenticatedUser = authenticatedUser;
        this.item = item;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public TdarUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(TdarUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public P getItem() {
        return item;
    }

    public void setItem(P item) {
        this.item = item;
    }

}

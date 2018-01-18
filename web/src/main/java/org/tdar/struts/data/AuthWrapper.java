package org.tdar.struts.data;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;

public class AuthWrapper<P extends Persistable> {

    private boolean authenticated = false;
    private TdarUser authenticatedUser;
    private boolean editor;
    private P item;

    public AuthWrapper(P item, boolean authenticated, TdarUser authenticatedUser, boolean editer) {
        this.authenticated = authenticated;
        this.authenticatedUser = authenticatedUser;
        this.item = item;
        this.setEditor(editer);
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

    public boolean isEditor() {
        return editor;
    }

    public void setEditor(boolean editor) {
        this.editor = editor;
    }

}

package org.tdar.struts.action;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;

public interface PersistableLoadingAction<P extends Persistable> extends ViewableAction<P> {

    P getPersistable();
    
    void setPersistable(P persistable);
    
    Long getId();
    
    Class<P> getPersistableClass();

    TdarUser getAuthenticatedUser();

    boolean isAuthenticationRequired();
    
    
}

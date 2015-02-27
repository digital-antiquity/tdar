package org.tdar.struts.action;

import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.external.auth.InternalTdarRights;

public interface PersistableLoadingAction<P extends Persistable> extends ViewableAction<P> {

    void setPersistable(P persistable);
    
    Long getId();
    
    InternalTdarRights getAdminRights();

}

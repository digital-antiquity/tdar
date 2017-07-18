package org.tdar.struts.action;

import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.external.auth.InternalTdarRights;

import com.opensymphony.xwork2.Preparable;

public interface PersistableLoadingAction<P extends Persistable> extends ViewableAction<P>, Preparable {

    void setPersistable(P persistable);

    Long getId();

    InternalTdarRights getAdminRights();

}

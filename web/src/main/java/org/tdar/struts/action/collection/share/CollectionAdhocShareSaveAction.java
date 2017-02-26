package org.tdar.struts.action.collection.share;

import org.apache.struts2.convention.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

/**
 * Created by jimdevos on 2/23/17.
 */
@ParentPackage("secured")
@Namespace("/collection/share")
@Component
@Scope("prototype")
public class CollectionAdhocShareSaveAction extends AbstractPersistableViewableAction<SharedCollection> {
    @Override
    public Class<SharedCollection> getPersistableClass() {
        return null;
    }

    @Override
    public String loadViewMetadata() throws TdarActionException {
        return null;
    }

    @Override
    public boolean authorize() {
        return getAuthorizationService().canAddToCollection(getPersistable(), getAuthenticatedUser());
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_RESOURCE_COLLECTIONS;
    }

    @WriteableSession
    @Override
    @Action(value="save", results={
            @Result(name="success", type="redirect", location="/collection/{id}"),
            @Result(name="input", location="edit.ftl")
    })
    @PostOnly
    public String execute() {
        return "success";
    }

}

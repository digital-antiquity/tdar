package org.tdar.struts.action.collection.share;

import org.apache.struts2.convention.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts_base.action.TdarActionException;

@ParentPackage("secured")
@Namespace("/collection/share")
@Component
@Scope("prototype")
public class CollectionAdhocShareEditAction extends AbstractPersistableViewableAction<SharedCollection> {
    // we extend APVA because it performes load and checkValidRequest(),  we don't need anything else it offers (in
    // fact adds things we *dont* want, but we'll worry about that later).
    // fixme: refactor APVA by  pulling  out  load() and checkValidRequest() into a parent class (or an interceptor).

    @Override
    public Class<SharedCollection> getPersistableClass() {
        return SharedCollection.class;
    }


    @Override
    public boolean authorize() {
        return getAuthorizationService().canAddToCollection(getPersistable(), getAuthenticatedUser());
    }

    @Override
    public String loadViewMetadata() throws TdarActionException {
        return "success";
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_RESOURCE_COLLECTIONS;
    }


    @Override
    public void validate() {
    }

    @Action(value="edit", results={
            @Result(name="success", location="edit.ftl"),
            @Result(name="input", type="redirect", location="/dashboard")
    })
    public  String execute() {
        return "success";
    }



    public SharedCollection getResourceCollection() {
        return getPersistable();
    }

}

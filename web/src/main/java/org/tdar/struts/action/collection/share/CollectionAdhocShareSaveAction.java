package org.tdar.struts.action.collection.share;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

/**
 *  We extend APVA because it performes load and checkValidRequest(),  we don't need anything else it offers (in
 *  fact adds things we *dont* want, but we'll worry about that later).
 */
// fixme: refactor APVA by  pulling  out  load() and checkValidRequest() into a parent class (or an interceptor).
public class CollectionAdhocShareSaveAction extends AbstractCollectionAdhocShareAction {

    @Override
    public String loadViewMetadata() throws TdarActionException {
        return super.loadViewMetadata();
    }

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        getAdhocShare().setCollectionId(getId());

    }

    @Override
    public boolean authorize() {
        return getAuthorizationService().canAddToCollection(getAuthenticatedUser(),getPersistable());
    }

    @WriteableSession
    @Override
    @Action(value="save", results={
            @Result(name="success", type="redirect", location="/collection/${id}"),
            @Result(name="input", location="edit.ftl")
    })
    @PostOnly
    public String execute() {
        getResourceCollectionService().createShareFromAdhoc(getAdhocShare(), null, getResourceCollection(), null, getAuthenticatedUser());
        return "success";
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_RESOURCE_COLLECTIONS;
    }

}

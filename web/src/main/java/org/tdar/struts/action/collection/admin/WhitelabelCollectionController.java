package org.tdar.struts.action.collection.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/collection/admin/whitelabel")
@HttpsOnly
public class WhitelabelCollectionController extends AbstractAuthenticatableAction implements Preparable, PersistableLoadingAction<SharedCollection> {

    @Autowired
    private transient AuthorizationService authorizationService;
    
    private static final long serialVersionUID = 7148462451707301708L;
    private SharedCollection collection;
    private Long id;

    @SkipValidation
    @Action(value = "{id}/edit", results = {
            @Result(name = SUCCESS, location = "edit.ftl")
    })
    public String edit() throws TdarActionException {
        return SUCCESS;
    }

    
    @WriteableSession
    @PostOnly
    @SkipValidation
    @Action(value = SAVE,
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${collection.detailUrl}"),
                    @Result(name = INPUT, location = "edit.ftl") })
    public String save() throws TdarActionException {
        getGenericService().saveOrUpdate(getCollection());
//        getGenericService().saveOrUpdate(getCollection().getProperties());
        getLogger().trace("{} {} {} ", getCollection().getId(), getCollection().getProperties().getSubtitle(), getCollection().getProperties().getWhitelabel());
        return SUCCESS;
    }
    
    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEdit(getAuthenticatedUser(), getCollection());
    }

    public boolean isEditable() {
        try {
            return authorize();
        } catch (TdarActionException tae) {
            getLogger().debug("authorization exception", tae);
        }
        return false;
    }

    @Override
    public Class<SharedCollection> getPersistableClass() {
        return SharedCollection.class;
    }
    
    @Override
    public Persistable getPersistable() {
        return getCollection();
    }

    @Override
    public void setPersistable(SharedCollection persistable) {
        this.setCollection(persistable);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANYTHING;
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);

    }

    public void setId(Long id) {
        this.id = id;
    }


    public SharedCollection getCollection() {
        return collection;
    }


    public void setCollection(SharedCollection collection) {
        this.collection = collection;
    }

    
}
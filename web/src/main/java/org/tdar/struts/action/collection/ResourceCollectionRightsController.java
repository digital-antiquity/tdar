package org.tdar.struts.action.collection;

import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AbstractRightsController;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespaces(value = { @Namespace("/share"), @Namespace("/collection") })
public class ResourceCollectionRightsController extends AbstractRightsController
        implements Preparable, PersistableLoadingAction<ResourceCollection> {

    private static final long serialVersionUID = 5522048517742464825L;

    private static final String COLLECTION_RIGHTS_FTL = "../collection/rights.ftl";
    private static final String RIGHTS = "rights";
    private static final String RIGHTS_SAVE = "rights-save";

    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
    }

    private ResourceCollection resourceCollection;

    @Override
    public boolean authorize() {
        return authorizationService.canEditCollection(getAuthenticatedUser(), getPersistable());
    }

    public ResourceCollection getResourceCollection() {
        return resourceCollection;
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);
        if (PersistableUtils.isNullOrTransient(getPersistable())) {
            // persistable is null, so the lookup failed (aka not found)
            abort(StatusCode.NOT_FOUND, getText("abstractPersistableController.not_found"));
        }
        super.prepare();

    }

    @Override
    public void validate() {
        super.validate();

    }

    public void indexPersistable() {
        /*
         * if we want to be really "aggressive" we only need to do this if
         * (a) permissions change
         * (b) visibility changes
         */
        if (isAsync()) {
            searchIndexService.indexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            searchIndexService.indexAllResourcesInCollectionSubTree(getPersistable());
        }
    }

    public void setResourceCollection(ResourceCollection rc) {
        setPersistable(rc);
    }

    @SkipValidation
    @Action(value = RIGHTS, results = {
            @Result(name = SUCCESS, location = COLLECTION_RIGHTS_FTL),
    })
    public String edit() throws TdarActionException {
        return super.edit();
    }

    @Override
    public Set<AuthorizedUser> getLocalRightsCollection() {
        return getPersistable().getAuthorizedUsers();
    }

    public void handleLocalSave() {

        resourceCollectionService.saveCollectionForRightsController(getPersistable(), getAuthenticatedUser(), getProxies(), null);

    }

    public void handleCollectionSave() {
        return;
    }

    @SkipValidation
    @Action(value = RIGHTS_SAVE, results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${persistable.detailUrl}"),
            @Result(name = INPUT, location = COLLECTION_RIGHTS_FTL)
    })

    @WriteableSession
    @PostOnly
    public String save() throws TdarActionException {
        return super.save();
    }

    public ResourceCollection getPersistable() {
        return getResourceCollection();
    }

    public void setPersistable(ResourceCollection rc) {
        this.resourceCollection = rc;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_RESOURCE_COLLECTIONS;
    }
}
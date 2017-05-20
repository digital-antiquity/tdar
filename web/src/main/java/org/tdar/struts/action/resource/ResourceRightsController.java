package org.tdar.struts.action.resource;

import java.util.List;

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
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AbstractRightsController;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespaces({
        @Namespace("/document/rights"),
        @Namespace("/image/rights"),
        @Namespace("/dataset/rights"),
        @Namespace("/image/rights"),
        @Namespace("/resource/rights"),
        @Namespace("/geospatial/rights"),
        @Namespace("/sensory-data/rights"),
        @Namespace("/coding-sheet/rights"),
        @Namespace("/ontology/rights")
})
public class ResourceRightsController extends AbstractRightsController implements
        Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = 120530285741022465L;

    private static final String RIGHTS = "{id}";

    private static final String SUCCESS_INVITE = "invite";
    private static final String INVITE = "invite.ftl";

    private Resource resource;

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    @Override
    public boolean authorize() {
        return authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), GeneralPermissions.MODIFY_RECORD);
    }

    @Override
    public Resource getPersistable() {
        return getResource();
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    @Override
    public void setPersistable(Resource persistable) {
        this.setResource(persistable);
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANY_RESOURCE;
    }

    @SkipValidation
    @Action(value = RIGHTS, results = {
            @Result(name = SUCCESS, location = "../rights.ftl")
    })
    public String edit() throws TdarActionException {
        setupEdit();
        return SUCCESS;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        List<GeneralPermissions> permissions = GeneralPermissions.getAvailablePermissionsFor(getPersistableClass());
        return permissions;
    }

    @SkipValidation
    @Action(value = SAVE, results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${resource.detailUrl}"),
            @Result(name = SUCCESS_INVITE, location = INVITE),
            @Result(name = INPUT, location = RIGHTS)
    })
    @PostOnly
    public String save() {
        try {
            getLogger().debug("proxies:{}", getProxies());
            loadEffectiveResourceCollectionsForSave();
            getLogger().debug("retained collections:{}", getRetainedSharedCollections());
            getShares().addAll(getRetainedSharedCollections());
            getLogger().debug("shares:{}", getShares());

            resourceCollectionService.saveResourceCollections(getResource(), getShares(), getResource().getSharedCollections(),
                    getAuthenticatedUser(), true, ErrorHandling.VALIDATE_SKIP_ERRORS, SharedCollection.class);

            if (!authorizationService.canEdit(getAuthenticatedUser(), getResource())) {
                // addActionError("abstractResourceController.cannot_remove_collection");
                getLogger().error("user is trying to remove themselves from the collection that granted them rights");
                addActionMessage("abstractResourceController.collection_rights_remove");
            }

            resourceCollectionService.saveResourceRights(getProxies(), getAuthenticatedUser(), getResource());
        } catch (Exception e) {
            getLogger().error("issue saving", e);
            return INPUT;
        }
        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);
        super.prepare();
    }

    @Override
    public ResourceCollection getLocalRightsCollection() {
        return getResource().getInternalResourceCollection();
    }
    
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void handleCollectionSave() {
        resourceCollectionService.saveResourceCollections(getResource(), getShares(), getResource().getSharedCollections(),
                getAuthenticatedUser(), true, ErrorHandling.VALIDATE_SKIP_ERRORS, SharedCollection.class);
    }
    
    @Override
    public void handleLocalSave() {
        resourceCollectionService.saveResourceRights(getProxies(), getAuthenticatedUser(), getResource());

    }
    
    public void loadEffectiveResourceCollectionsForEdit() {
        getEffectiveShares().addAll(resourceCollectionService.getEffectiveSharesForResource(getResource()));

        getLogger().debug("loadEffective...");
        for (SharedCollection rc : getResource().getSharedResourceCollections()) {
            if (authorizationService.canViewCollection(getAuthenticatedUser(), rc)) {
                getShares().add(rc);
            } else {
                getRetainedSharedCollections().add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
        getLogger().debug("Shares: {}", getShares());
    }

    public void loadEffectiveResourceCollectionsForSave() {
        getLogger().debug("loadEffective...");
        for (SharedCollection rc : getResource().getSharedCollections()) {
            if (!authorizationService.canViewCollection(getAuthenticatedUser(), rc)) {
                getRetainedSharedCollections().add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
    }

    public boolean isRightsPage() {
        return true;
    }

}

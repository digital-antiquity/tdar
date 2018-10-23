package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.List;
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
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ErrorHandling;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AbstractRightsController;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.web.service.ResourceEditControllerService;
import org.tdar.web.service.ResourceSaveControllerService;

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

    private static final String RIGHTS_FTL = "../rights.ftl";

    private static final long serialVersionUID = 120530285741022465L;

    private static final String RIGHTS = "{id}";

    private Resource resource;

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient ResourceSaveControllerService saveService;
    @Autowired
    private transient ResourceEditControllerService editService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    private List<ResourceCollection> shares = new ArrayList<>();
    private List<ResourceCollection> retainedSharedCollections = new ArrayList<>();
    private List<ResourceCollection> effectiveShares = new ArrayList<>();

    private CollectionResourceSection type;

    @Override
    public boolean authorize() {
        return authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), Permissions.MODIFY_RECORD);
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
            @Result(name = SUCCESS, location = RIGHTS_FTL)
    })
    public String edit() throws TdarActionException {
        editService.updateSharesForEdit(getResource(), getAuthenticatedUser(), effectiveShares, new ArrayList<>(),
                shares, new ArrayList<>());

        setupEdit();
        return SUCCESS;
    }

    public List<Permissions> getAvailablePermissions() {
        List<Permissions> permissions = Permissions.getAvailablePermissionsFor(getPersistableClass());
        return permissions;
    }

    @SkipValidation
    @Action(value = SAVE, results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${resource.detailUrl}"),
            @Result(name = INPUT, location = RIGHTS_FTL)
    })
    @PostOnly
    @WriteableSession
    public String save() {
        try {
            getLogger().debug("save proxies:{}", getProxies());
            AuthWrapper<Resource> auth = new AuthWrapper<Resource>(resource, false, getAuthenticatedUser(), false);
            saveService.loadEffectiveResourceCollectionsForSave(auth, retainedSharedCollections, new ArrayList<>());
            getLogger().debug("retained collections:{}", getRetainedSharedCollections());
            getShares().addAll(getRetainedSharedCollections());
            getLogger().debug("shares:{}", getShares());

            resourceCollectionService.saveResourceCollections(getResource(), getShares(), getResource().getManagedResourceCollections(),
                    getAuthenticatedUser(), true, ErrorHandling.VALIDATE_SKIP_ERRORS, type);

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
    public Set<AuthorizedUser> getLocalRightsCollection() {
        return resource.getAuthorizedUsers();
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void handleCollectionSave() {
        resourceCollectionService.saveResourceCollections(getResource(), getShares(), getResource().getManagedResourceCollections(),
                getAuthenticatedUser(), true, ErrorHandling.VALIDATE_SKIP_ERRORS, getType());
    }

    @Override
    public void handleLocalSave() {
        resourceCollectionService.saveResourceRights(getProxies(), getAuthenticatedUser(), getResource());

    }

    public boolean isRightsPage() {
        return true;
    }

    public List<ResourceCollection> getRetainedSharedCollections() {
        return retainedSharedCollections;
    }

    public void setRetainedSharedCollections(List<ResourceCollection> retainedSharedCollections) {
        this.retainedSharedCollections = retainedSharedCollections;
    }

    public List<ResourceCollection> getEffectiveShares() {
        return effectiveShares;
    }

    public void setEffectiveShares(List<ResourceCollection> effectiveShares) {
        this.effectiveShares = effectiveShares;
    }

    public List<ResourceCollection> getShares() {
        return shares;
    }

    public void setShares(List<ResourceCollection> shares) {
        this.shares = shares;
    }

    public CollectionResourceSection getType() {
        return type;
    }

    public void setType(CollectionResourceSection type) {
        this.type = type;
    }

}

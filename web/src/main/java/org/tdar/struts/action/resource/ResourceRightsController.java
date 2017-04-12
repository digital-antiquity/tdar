package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
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
public class ResourceRightsController extends AbstractAuthenticatableAction implements
        Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = 8551222659351457637L;

    private static final String RIGHTS = "{id}";

    private static final String SUCCESS_INVITE = "invite";
    private static final String INVITE = "invite.ftl";

    private Resource resource;
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    private List<SharedCollection> shares = new ArrayList<>();
    private List<SharedCollection> retainedSharedCollections = new ArrayList<>();
    private List<RightsBasedResourceCollection> effectiveShares = new ArrayList<>();

    private List<UserRightsProxy> proxies = new ArrayList<>();

    private Long id;

    @Override
    public boolean authorize() {
        return authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(),GeneralPermissions.MODIFY_RECORD);
    }

    public UserRightsProxy getBlankProxy() {
        return new UserRightsProxy();
    }

    public List<UserRightsProxy> getProxies() {
        return proxies;
    }

    public void setProxies(List<UserRightsProxy> proxies) {
        this.proxies = proxies;
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
    public Long getId() {
        return id;
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
            @Result(name= SUCCESS_INVITE, location = INVITE),
            @Result(name = INPUT, location = RIGHTS)
    })
    @PostOnly
    public String save() {
        try {
            getLogger().debug("proxies:{}",proxies);
            loadEffectiveResourceCollectionsForSave();
            getLogger().debug("retained collections:{}", getRetainedSharedCollections());
            getShares().addAll(getRetainedSharedCollections());
            
                resourceCollectionService.saveResourceCollections(getResource(), getShares(), getResource().getSharedCollections(),
                        getAuthenticatedUser(), true, ErrorHandling.VALIDATE_SKIP_ERRORS, SharedCollection.class);

                if (!authorizationService.canEdit(getAuthenticatedUser(), getResource())) {
//                    addActionError("abstractResourceController.cannot_remove_collection");
                    getLogger().error("user is trying to remove themselves from the collection that granted them rights");
                    addActionMessage("abstractResourceController.collection_rights_remove");
                }

            resourceCollectionService.saveResourceRights(proxies, getAuthenticatedUser(), getResource());
        } catch (Exception e) {
            getLogger().error("issue saving", e);
            return INPUT;
        }
        return SUCCESS;
    }


    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);
    }

    private void setupEdit() {
        InternalCollection internal = getResource().getInternalResourceCollection();
        loadEffectiveResourceCollectionsForEdit();
        getLogger().debug("internal:{}", internal);
        if (internal != null) {
            internal.getAuthorizedUsers().forEach(au -> {
                proxies.add(new UserRightsProxy(au));
            });
        }

        List<UserInvite> invites = resourceCollectionService.findUserInvites(getPersistable());
        if (CollectionUtils.isNotEmpty(invites)) {
            invites.forEach(invite -> {
                proxies.add(new UserRightsProxy(invite));
            });
        }
        getLogger().debug("proxies:{}", proxies);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }


    private void loadEffectiveResourceCollectionsForEdit() {
        getEffectiveShares().addAll(resourceCollectionService.getEffectiveSharesForResource(getResource()));

        getLogger().debug("loadEffective...");
        for (SharedCollection rc : getResource().getSharedResourceCollections()) {
            if (authorizationService.canViewCollection(getAuthenticatedUser(),rc)) {
                getShares().add(rc);
            } else {
                getRetainedSharedCollections().add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
    }

    
    private void loadEffectiveResourceCollectionsForSave() {
        getLogger().debug("loadEffective...");
        for (SharedCollection rc : getResource().getSharedCollections()) {
            if (!authorizationService.canViewCollection(getAuthenticatedUser(),rc)) {
                getRetainedSharedCollections().add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
    }

    public SharedCollection getBlankShare() {
        return new SharedCollection();
    }

    public List<SharedCollection> getRetainedSharedCollections() {
        return retainedSharedCollections;
    }

    public void setRetainedSharedCollections(List<SharedCollection> retainedSharedCollections) {
        this.retainedSharedCollections = retainedSharedCollections;
    }

    public List<RightsBasedResourceCollection> getEffectiveShares() {
        return effectiveShares;
    }

    public void setEffectiveShares(List<RightsBasedResourceCollection> effectiveShares) {
        this.effectiveShares = effectiveShares;
    }

    public List<SharedCollection> getShares() {
        return shares;
    }

    public void setShares(List<SharedCollection> shares) {
        this.shares = shares;
    }
}

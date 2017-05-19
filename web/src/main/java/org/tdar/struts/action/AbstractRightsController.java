package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;

import com.opensymphony.xwork2.Preparable;

@Component
public abstract class AbstractRightsController extends AbstractAuthenticatableAction implements
        Preparable {

    private static final long serialVersionUID = 8551222659351457637L;

    private static final String RIGHTS = "{id}";

    private static final String SUCCESS_INVITE = "invite";
    private static final String INVITE = "invite.ftl";

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    private List<SharedCollection> shares = new ArrayList<>();
    private List<SharedCollection> retainedSharedCollections = new ArrayList<>();
    private List<RightsBasedResourceCollection> effectiveShares = new ArrayList<>();

    private List<UserRightsProxy> proxies = new ArrayList<>();

    private Long id;

    public Long getId() {
        return id;
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
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${persistable.detailUrl}"),
            @Result(name = SUCCESS_INVITE, location = INVITE),
            @Result(name = INPUT, location = RIGHTS)
    })
    @PostOnly
    public String save() {
        try {
            getLogger().debug("proxies:{}", proxies);
            loadEffectiveResourceCollectionsForSave();
            getLogger().debug("retained collections:{}", getRetainedSharedCollections());
            getShares().addAll(getRetainedSharedCollections());
            getLogger().debug("shares:{}", getShares());

            handleCollectionSave();

            if (!authorizationService.canEdit(getAuthenticatedUser(), getPersistable())) {
                // addActionError("abstractResourceController.cannot_remove_collection");
                getLogger().error("user is trying to remove themselves from the collection that granted them rights");
                addActionMessage("abstractResourceController.collection_rights_remove");
            }

            handleLocalSave();
        } catch (Exception e) {
            getLogger().error("issue saving", e);
            return INPUT;
        }
        return SUCCESS;
    }

    public abstract Persistable getPersistable();

    public abstract Class getPersistableClass();

    public abstract void handleLocalSave();

    public abstract void handleCollectionSave();


    protected void setupEdit() {
        RightsBasedResourceCollection internal = getLocalRightsCollection();
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

    public abstract void loadEffectiveResourceCollectionsForEdit();

    public abstract void loadEffectiveResourceCollectionsForSave();

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

    public ListCollection getBlankResourceCollection() {
        return new ListCollection();
    }

    public boolean isRightsPage() {
        return true;
    }

    public abstract RightsBasedResourceCollection getLocalRightsCollection();

}

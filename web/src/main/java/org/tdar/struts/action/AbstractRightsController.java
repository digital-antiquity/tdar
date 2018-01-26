package org.tdar.struts.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.service.UserRightsProxyService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
public abstract class AbstractRightsController extends AbstractAuthenticatableAction implements
        Preparable {

    private static final long serialVersionUID = 8551222659351457637L;

    private static final String RIGHTS = "{id}";
    private boolean asyncSave = true;
    private static final String SUCCESS_INVITE = "invite";
    private static final String INVITE = "invite.ftl";

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient UserRightsProxyService userRightsProxyService;
    @Autowired
    private transient SearchIndexService searchIndexService;

//    private List<SharedCollection> shares = new ArrayList<>();
//    private List<SharedCollection> retainedSharedCollections = new ArrayList<>();
//    private List<SharedCollection> effectiveShares = new ArrayList<>();

    private List<UserRightsProxy> proxies = new ArrayList<>();
    private List<UserRightsProxy> invites = new ArrayList<>();
    private Long id;
    private String ownerProperName;
    private TdarUser owner;

    
    @Override
    public void prepare() throws Exception {
        setupOwnerField();
        if (PersistableUtils.isNotNullOrTransient(getOwner())) {
            TdarUser uploader = getGenericService().find(TdarUser.class, getOwner().getId());
            if (getPersistable() instanceof ResourceCollection) {
                ((ResourceCollection)getPersistable()).setOwner(uploader);
            }
        }
        getProxies().addAll(getInvites());
    }
    
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
    public String save() throws TdarActionException {
        try {
            getLogger().debug("proxies:{}", proxies);

            handleCollectionSave();

            if (!authorizationService.canEdit(getAuthenticatedUser(), getPersistable())) {
                // addActionError("abstractResourceController.cannot_remove_collection");
                getLogger().error("user is trying to remove themselves from the collection that granted them rights");
                addActionMessage("abstractResourceController.collection_rights_remove");
            }

            handleLocalSave();
            indexPersistable();
        } catch (Exception e) {
            addActionErrorWithException(e.getMessage(), e);
            addActionErrorWithException(getText("abstractPersistableController.unable_to_save", getPersistable()), e);
            getLogger().error("issue saving", e);

            return INPUT;
        }
        return SUCCESS;
    }
    
    public void indexPersistable() throws SearchIndexException, IOException {
        if (getPersistable() instanceof Resource) {
            searchIndexService.index((Resource)getPersistable()); 
        } 
        if (getPersistable() instanceof ResourceCollection) {
            searchIndexService.index((ResourceCollection)getPersistable()); 
        } 
    }


    public abstract Persistable getPersistable();

    public abstract Class getPersistableClass();

    public abstract void handleLocalSave();

    public abstract void handleCollectionSave();


    protected void setupEdit() {
        if (getPersistable() instanceof ResourceCollection) {
            setOwner(((ResourceCollection) getPersistable()).getOwner());
        }
        if (getPersistable() instanceof Resource) {
            setOwner(((Resource) getPersistable()).getSubmitter());
        }
        
        setupOwnerField();
        Collection<AuthorizedUser> users  = getLocalRightsCollection();
            users.forEach(au -> {
                proxies.add(new UserRightsProxy(au));
            });

        List<UserInvite> invites = userRightsProxyService.findUserInvites(getPersistable());
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


    public SharedCollection getBlankShare() {
        return new SharedCollection();
    }


    public ListCollection getBlankResourceCollection() {
        return new ListCollection();
    }

    public boolean isRightsPage() {
        return true;
    }

    public abstract Set<AuthorizedUser> getLocalRightsCollection();

    

    protected void setupOwnerField() {
        if (PersistableUtils.isNotNullOrTransient(getOwner()) && StringUtils.isNotBlank(getOwner().getProperName())) {
            if (getOwner().getFirstName() != null && getOwner().getLastName() != null)
                setOwnerProperName(getOwner().getProperName());
        } else {
            setOwnerProperName(getAuthenticatedUser().getProperName());
        }
    }


    public String getOwnerProperName() {
        return ownerProperName;
    }

    public void setOwnerProperName(String ownerProperName) {
        this.ownerProperName = ownerProperName;
    }

    public TdarUser getOwner() {
        return owner;
    }

    public void setOwner(TdarUser owner) {
        this.owner = owner;
    }
    
    public void setAsync(boolean async) {
        this.asyncSave = async;
    }

    public boolean isAsync() {
        return asyncSave;
    }

    public List<UserRightsProxy> getInvites() {
        return invites;
    }

    public void setInvites(List<UserRightsProxy> invites) {
        this.invites = invites;
    }



}

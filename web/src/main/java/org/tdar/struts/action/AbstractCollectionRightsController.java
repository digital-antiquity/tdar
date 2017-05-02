package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

public abstract class AbstractCollectionRightsController<C extends HierarchicalCollection<C>> extends AbstractAuthenticatableAction
        implements Preparable, PersistableLoadingAction<C> {

    private static final String RIGHTS = "rights";

    private static final long serialVersionUID = -8140980937049864587L;

    private static final String RIGHTS_SAVE = "rights-save";

    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private String ownerProperName;
    private TdarUser owner;

    private C resourceCollection;
    private List<AuthorizedUser> authorizedUsers = new ArrayList<>();
    private List<String> authorizedUsersFullNames = new ArrayList<>();
    private Long id;

    private boolean asyncSave;

    @Override
    public boolean authorize() {
        return authorizationService.canEditCollection(getAuthenticatedUser(), getPersistable());
    }


    public C getResourceCollection() {
        return resourceCollection;
    }

    @Override
    public void prepare() throws TdarActionException {
        prepareAndLoad(this, RequestType.EDIT);

        setupOwnerField();
        if (PersistableUtils.isNotNullOrTransient(getOwner())) {
            TdarUser uploader = getGenericService().find(TdarUser.class, getOwner().getId());
            getPersistable().setOwner(uploader);
        }
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

    public void setResourceCollection(C rc) {
        setPersistable(rc);
    }

    public List<SortOption> getSortOptions() {
        return SortOption.getOptionsForResourceCollectionPage();
    }

    public List<DisplayOrientation> getResultsOrientations() {
        List<DisplayOrientation> options = Arrays.asList(DisplayOrientation.values());
        return options;
    }

    @SkipValidation
    @Action(value = RIGHTS, results = {
            @Result(name = SUCCESS, location = "../collection/rights.ftl"),
            @Result(name = INPUT, location = ADD, type = TDAR_REDIRECT)
    })
    public String edit() throws TdarActionException {
        setOwner(getPersistable().getOwner());
        setupOwnerField();
        getAuthorizedUsers().addAll(resourceCollectionService.getAuthorizedUsersForCollection(getPersistable(), getAuthenticatedUser()));
        for (AuthorizedUser au : getAuthorizedUsers()) {
            String name = null;
            if (au != null && au.getUser() != null) {
                name = au.getUser().getProperName();
            }
            getAuthorizedUsersFullNames().add(name);
        }

        return SUCCESS;
    }

    
    @SkipValidation
    @Action(value = RIGHTS_SAVE, results = {
            @Result(name = SUCCESS, type=TDAR_REDIRECT, location = "${persistable.detailUrl}"),
            @Result(name = INPUT, location =  "../collection/rights.ftl")
    })
    @WriteableSession
    @PostOnly
    public String save() throws TdarActionException {
        try {
        resourceCollectionService.saveCollectionForRightsController(getPersistable(), getAuthenticatedUser(), getAuthorizedUsers(), getPersistableClass(), null);
        indexPersistable();
        } catch (Exception e) {
            getLogger().error("error saving righs", e);
            addActionErrorWithException(getText("abstractPersistableController.unable_to_save", getPersistable()), e);
            return INPUT;
        }
        return SUCCESS;
    }

    private void setupOwnerField() {
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

    public C getPersistable() {
        return getResourceCollection();
    }

    public List<AuthorizedUser> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public void setAuthorizedUsers(List<AuthorizedUser> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    public List<String> getAuthorizedUsersFullNames() {
        return authorizedUsersFullNames;
    }

    public void setAuthorizedUsersFullNames(List<String> authorizedUsersFullNames) {
        this.authorizedUsersFullNames = authorizedUsersFullNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAsync(boolean async) {
        this.asyncSave = async;
    }

    public boolean isAsync() {
        return asyncSave;
    }

    public void setPersistable(C rc) {
        this.resourceCollection = rc;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_RESOURCE_COLLECTIONS;
    }


    public AuthorizedUser getBlankAuthorizedUser() {
        AuthorizedUser user = new AuthorizedUser();
        user.setUser(new TdarUser());
        return user;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        List<GeneralPermissions> permissions = GeneralPermissions.getAvailablePermissionsFor(getPersistableClass());
        return permissions;
    }

}

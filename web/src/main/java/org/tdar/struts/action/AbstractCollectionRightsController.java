package org.tdar.struts.action;

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
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractCollectionRightsController<C extends HierarchicalCollection<C>> extends AbstractPersistableController<C> {


    private static final long serialVersionUID = -8140980937049864587L;

    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient AuthorizationService authorizationService;
    
    private String ownerProperName;
    private TdarUser owner;


    @Override
    public boolean authorize() {
        if (isNullOrNew()) {
            return true;
        }
        return authorizationService.canEditCollection(getAuthenticatedUser(), getPersistable());
    }

    /**
     * Returns a list of all resource collections that can act as candidate parents for the current resource collection.
     * 
     * @return
     */
    public abstract List<C> getCandidateParentResourceCollections();

    public abstract <C> C getResourceCollection();

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();


        
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

    @Override
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


    @Override
    public String loadEditMetadata() throws TdarActionException {
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

    @Override
    public String loadAddMetadata() {
        setupOwnerField();
        return SUCCESS;
    }

    @Override
    @SkipValidation
    @Action(value = EDIT, results = {
            @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, location = ADD, type = TDAR_REDIRECT)
    })
    public String edit() throws TdarActionException {
        String result = super.edit();
        return result;
    }

    private void setupOwnerField() {
        if (PersistableUtils.isNotNullOrTransient(getOwner()) && StringUtils.isNotBlank(getOwner().getProperName())) {
            if (getOwner().getFirstName() != null && getOwner().getLastName() != null)
                setOwnerProperName(getOwner().getProperName());
        } else {
            setOwnerProperName(getAuthenticatedUser().getProperName());
        }
    }

    /**
     * A hint to the view-layer that this resource collection is "big". The view-layer may choose to gracefully degrade the presentation to save on bandwidth
     * and/or
     * client resources.
     * 
     * @return
     */
    public abstract boolean isBigCollection();


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

}

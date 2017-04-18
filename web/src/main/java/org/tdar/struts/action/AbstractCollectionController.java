package org.tdar.struts.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

import com.google.common.base.Objects;

public abstract class AbstractCollectionController<C extends HierarchicalCollection<C>> extends AbstractPersistableController<C> implements DataTableResourceDisplay {

    /**
     * Threshold that defines a "big" collection (based on imperieal evidence by highly-trained tDAR staff). This number
     * refers to the combined count of authorized users +the count of resources associated with a collection. Big
     * collections may adversely affect save/load times as well as cause rendering problems on the client, and so the
     * system may choose to mitigate these effects (somehow)
     */
    public static final int BIG_COLLECTION_CHILDREN_COUNT = 3_000;

    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient AuthorizationService authorizationService;
    
    private static final long serialVersionUID = 5710621983240752457L;
    private List<C> allResourceCollections = new LinkedList<>();

    private List<Long> selectedResourceIds = new ArrayList<>();
    private List<Resource> fullUserProjects;
    private List<ResourceCollection> collections = new LinkedList<>();

    private Long viewCount = 0L;
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<ResourceType>();

    private List<Project> allSubmittedProjects;
    private File file;
    private String fileContentType;
    private String fileFileName;
    private String ownerProperName;
    private TdarUser owner;

    private String parentCollectionName;
    private Long parentId;
    private C parentCollection;

    private ArrayList<AuthorizedUser> authorizedUsers;


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

        // Try to lookup parent collection by ID, then by name.  Name lookup must be unambiguous.
        if(PersistableUtils.isNotNullOrTransient(parentId)) {
            setParentCollection(getGenericService().find(getPersistableClass(), parentId));
            getLogger().debug("lookup parent collection by id:{}  result:{}", parentId, getParentCollection());

        }
        else if(StringUtils.isNotBlank(parentCollectionName)) {
            setParentCollection(resourceCollectionService.findCollectionsWithName(getAuthenticatedUser(),
                    parentCollectionName, getPersistableClass()));
            getLogger().debug("lookup parent collection by name:{}  results:{}", parentCollectionName, getParentCollection());

            if(getParentCollection() == null) {
                addActionError(getText("collectionController.ambiguous_parent_name"));
                // Clear the name field or the INPUT form will be primed to fail in the same way upon submit.
                //parentCollectionName = "";
            }
 
        }
        
        setupOwnerField();
        if (PersistableUtils.isNotNullOrTransient(getOwner())) {
            TdarUser uploader = getGenericService().find(TdarUser.class, getOwner().getId());
            getPersistable().setOwner(uploader);
        }

        if(getParentCollection() != null) {
            parentId = getParentCollection().getId();
        }
        if (PersistableUtils.isNotNullOrTransient(parentId) && PersistableUtils.isNullOrTransient((Persistable)getParentCollection())) {
            addActionError(getText("collectionController.type_mismatch"));
        }

    }
    
    @Override
    public void validate() {
        super.validate();
        if(getParentCollection() != null) {
            parentId = getParentCollection().getId();
        }

        if (PersistableUtils.isNotNullOrTransient(getPersistable()) && PersistableUtils.isNotNullOrTransient(getParentCollection())
                && (getParentCollection().getParentIds().contains(getPersistable().getId()) || Objects.equal(parentId, getPersistable().getId()))) {
            addActionError(getText("collectionController.cannot_set_self_parent"));
        }

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
    public List<SortOption> getResourceDatatableSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
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

        prepareDataTableSection();
        setParentId(getPersistable().getParentId());
        if (PersistableUtils.isNotNullOrTransient(getParentId())) {
            parentCollectionName = getPersistable().getParent().getName();
        }
        return SUCCESS;
    }

    @Override
    public String loadAddMetadata() {
        if (PersistableUtils.isNotNullOrTransient(parentId)) {
            HierarchicalCollection parent = getGenericService().find(HierarchicalCollection.class,parentId);
            if (parent != null) {
                parentCollectionName = parent.getName();
            }
        }
        prepareDataTableSection();
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

    public List<Long> getSelectedResourceIds() {
        return selectedResourceIds;
    }

    public void setSelectedResourceIds(List<Long> selectedResourceIds) {
        this.selectedResourceIds = selectedResourceIds;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public List<Project> getAllSubmittedProjects() {
        return allSubmittedProjects;
    }

    private void prepareDataTableSection() {
        allSubmittedProjects = projectService.findBySubmitter(getAuthenticatedUser());
        Collections.sort(allSubmittedProjects);
        boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
        fullUserProjects = new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
        fullUserProjects.removeAll(getAllSubmittedProjects());
        for (C c : resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser(), getPersistableClass())) {
            getAllResourceCollections().add(c);
        }
        // always place current resource collection as the first option
        if (PersistableUtils.isNotTransient(getPersistable())) {
            getAllResourceCollections().remove(getPersistable());
            getAllResourceCollections().add(0, getPersistable());
        }
    }

    public void setFullUserProjects(List<Resource> projects) {
        this.fullUserProjects = projects;
    }

    @Override
    public List<Resource> getFullUserProjects() {
        if (fullUserProjects == null) {
            boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
            fullUserProjects = new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
        }
        return fullUserProjects;
    }

    @Override
    public List<Status> getStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    @Override
    public List<ResourceType> getResourceTypes() {
        return resourceService.getAllResourceTypes();
    }

    public void setCollections(List<ResourceCollection> findAllChildCollections) {
        getLogger().info("child collections: {}", findAllChildCollections);
        this.collections = findAllChildCollections;
    }

    public List<ResourceCollection> getCollections() {
        return this.collections;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }

    public void setParentCollectionName(String parentCollectionName) {
        this.parentCollectionName = parentCollectionName;
    }

    public ArrayList<ResourceType> getSelectedResourceTypes() {
        return selectedResourceTypes;
    }

    public void setSelectedResourceTypes(ArrayList<ResourceType> selectedResourceTypes) {
        this.selectedResourceTypes = selectedResourceTypes;
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

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public List<C> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(List<C> allResourceCollections) {
        this.allResourceCollections = allResourceCollections;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileFileName() {
        return fileFileName;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
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


    public C getParentCollection() {
        return parentCollection;
    }

    public void setParentCollection(C parentCollection) {
        this.parentCollection = parentCollection;
    }

    
    /**
     * @return the authorizedUsers
     */
    public List<AuthorizedUser> getAuthorizedUsers() {
        if (authorizedUsers == null) {
            authorizedUsers = new ArrayList<AuthorizedUser>();
        }
        return authorizedUsers;
    }

    public AuthorizedUser getBlankAuthorizedUser() {
        AuthorizedUser user = new AuthorizedUser();
        user.setUser(new TdarUser());
        return user;
    }

}

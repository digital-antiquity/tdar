package org.tdar.struts.action.collection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.DataTableResourceDisplay;
import org.tdar.struts.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionController extends AbstractPersistableController<SharedCollection> implements DataTableResourceDisplay {

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
    private List<ResourceCollection> allResourceCollections = new LinkedList<>();

    private List<Long> selectedResourceIds = new ArrayList<>();
    private List<Resource> fullUserProjects;
    private List<ResourceCollection> collections = new LinkedList<>();

    private Long viewCount = 0L;
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<ResourceType>();

    private List<Long> toRemove = new ArrayList<>();
    private List<Long> toAdd = new ArrayList<>();
    private List<Long> publicToRemove = new ArrayList<>();
    private List<Long> publicToAdd = new ArrayList<>();
    private List<Project> allSubmittedProjects;
    private File file;
    private String fileContentType;
    private String fileFileName;
    private String ownerProperName;
    private TdarUser owner;

    private String parentCollectionName;
    private Long parentId;
    private SharedCollection parentCollection;


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
    public List<ResourceCollection> getCandidateParentResourceCollections() {
        List<ResourceCollection> publicResourceCollections = resourceCollectionService.findPotentialParentCollections(getAuthenticatedUser(),
                getPersistable());
        return publicResourceCollections;
    }


    @Override
    public void prepare() throws TdarActionException {
        super.prepare();

        // Try to lookup parent collection by ID, then by name.  Name lookup must be unambiguous.
        if(PersistableUtils.isNotNullOrTransient(parentId)) {
            parentCollection = getGenericService().find(SharedCollection.class, parentId);
            getLogger().debug("lookup parent collection by id:{}  result:{}", parentId, parentCollection);

        }
        else if(StringUtils.isNotBlank(parentCollectionName)) {
            List<SharedCollection> results = resourceCollectionService.findCollectionsWithName(getAuthenticatedUser(),
                    parentCollectionName);
            getLogger().debug("lookup parent collection by name:{}  results:{}", parentCollectionName, results.size());

            if(results.size() != 1) {
                addActionError(getText("collectionController.ambiguous_parent_name"));
                // Clear the name field or the INPUT form will be primed to fail in the same way upon submit.
                //parentCollectionName = "";
            } else {
                parentCollection = results.get(0);
            }
        }
    }

    @Override
    protected String save(SharedCollection persistable) {
        // FIXME: may need some potential check for recursive loops here to prevent self-referential parent-child loops
        // FIXME: if persistable's parent is different from current parent; then need to reindex all of the children as well
        
        setupOwnerField();
        if (PersistableUtils.isNotNullOrTransient(getOwner())) {
            TdarUser uploader = getGenericService().find(TdarUser.class, getOwner().getId());
            getPersistable().setOwner(uploader);
        }

//        lookupParent();
        if(parentCollection != null) {
            parentId = parentCollection.getId();
        }

        // FIXME: this section smells like validation.  Consider overriding validate() and moving it there.
        if (PersistableUtils.isNotNullOrTransient(persistable) && PersistableUtils.isNotNullOrTransient(parentCollection)
                && (parentCollection.getParentIds().contains(persistable.getId()) || parentCollection.getId().equals(persistable.getId()))) {
            addActionError(getText("collectionController.cannot_set_self_parent"));
        }

        //FIXME: this section is necessary because our prepare code is here, but we can't put it in prepare() because dozens of our tests will break because they do not correctly mock their controllers and assume that prepare() is never called.
        if(hasActionErrors()) {
            return INPUT;
        }
        resourceCollectionService.saveCollectionForController(getPersistable(), parentId, parentCollection, getAuthenticatedUser(), getAuthorizedUsers(), toAdd,
                toRemove, publicToAdd, publicToRemove, shouldSaveResource(), generateFileProxy(getFileFileName(), getFile()));
        setSaveSuccessPath(getPersistable().getUrlNamespace());
        return SUCCESS;
    }

    @Override
    public void indexPersistable() {
        /*
         * if we want to be really "aggressive" we only need to do this if
         * (a) permissions change
         * (b) visibility changes
         */
        if (isAsync()) {
            searchIndexService.partialIndexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            searchIndexService.partialIndexAllResourcesInCollectionSubTree(getPersistable());
        }
    }

    public SharedCollection getResourceCollection() {
        if (getPersistable() == null) {
            setPersistable(new SharedCollection());
        }
        return getPersistable();
    }

    public void setResourceCollection(SharedCollection rc) {
        setPersistable(rc);
    }

    @Override
    public Class<SharedCollection> getPersistableClass() {
        return SharedCollection.class;
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
            ResourceCollection parent = resourceCollectionService.find(parentId);
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
        getAllResourceCollections().addAll(resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser()));

        // always place current resource collection as the first option
        if (PersistableUtils.isNotTransient(getResourceCollection())) {
            getAllResourceCollections().remove(getResourceCollection());
            getAllResourceCollections().add(0, getResourceCollection());
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
    public boolean isBigCollection() {
        return (getPersistable().getResources().size() + getAuthorizedUsers().size()) > BIG_COLLECTION_CHILDREN_COUNT;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    @Override
    public List<ResourceCollection> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(List<ResourceCollection> allResourceCollections) {
        this.allResourceCollections = allResourceCollections;
    }

    public List<Long> getToAdd() {
        return toAdd;
    }

    public void setToAdd(List<Long> toAdd) {
        this.toAdd = toAdd;
    }

    public List<Long> getToRemove() {
        return toRemove;
    }

    public void setToRemove(List<Long> toRemove) {
        this.toRemove = toRemove;
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

    public List<Long> getPublicToAdd() {
        return publicToAdd;
    }

    public void setPublicToAdd(List<Long> publicToAdd) {
        this.publicToAdd = publicToAdd;
    }

    public List<Long> getPublicToRemove() {
        return publicToRemove;
    }

    public void setPublicToRemove(List<Long> publicToRemove) {
        this.publicToRemove = publicToRemove;
    }

}

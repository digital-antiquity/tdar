package org.tdar.struts.action.collection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.search.SearchIndexService;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.query.FacetGroup;
import org.tdar.search.query.FacetValue;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.DataTableResourceDisplay;
import org.tdar.struts.action.TdarActionException;
import org.tdar.utils.PaginationHelper;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionController extends AbstractPersistableController<ResourceCollection> implements SearchResultHandler<Resource>, DataTableResourceDisplay {

    /**
     * Threshold that defines a "big" collection (based on imperical evidence by highly-trained tDAR staff). This number
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
    private transient SearchService searchService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private static final long serialVersionUID = 5710621983240752457L;
    // private List<Resource> resources = new ArrayList<>();
    private List<ResourceCollection> allResourceCollections = new LinkedList<>();

    private List<Long> selectedResourceIds = new ArrayList<>();
    private Long parentId;
    private List<Resource> fullUserProjects;
    private List<ResourceCollection> collections = new LinkedList<>();
    private ArrayList<FacetValue> resourceTypeFacets = new ArrayList<>();

    private Long viewCount = 0L;
    private int startRecord = DEFAULT_START;
    private int recordsPerPage = getDefaultRecordsPerPage();
    private int totalRecords;
    private List<Resource> results;
    private SortOption secondarySortField;
    private SortOption sortField;
    private String mode = "CollectionBrowse";
    private PaginationHelper paginationHelper;
    private String parentCollectionName;
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<ResourceType>();

    private List<Long> toRemove = new ArrayList<>();
    private List<Long> toAdd = new ArrayList<>();
    private List<Project> allSubmittedProjects;
    private File file;
    private String fileContentType;
    private String fileFileName;

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
    protected String save(ResourceCollection persistable) {
        // FIXME: may need some potential check for recursive loops here to prevent self-referential parent-child loops
        // FIXME: if persistable's parent is different from current parent; then need to reindex all of the children as well
        ResourceCollection parent = resourceCollectionService.find(parentId);
        if (PersistableUtils.isNotNullOrTransient(persistable) && PersistableUtils.isNotNullOrTransient(parent)
                && (parent.getParentIds().contains(persistable.getId()) || parent.getId().equals(persistable.getId()))) {
            addActionError(getText("collectionController.cannot_set_self_parent"));
            return INPUT;
        }

        List<Resource> resourcesToRemove = resourceService.findAll(Resource.class, toRemove);
        List<Resource> resourcesToAdd = resourceService.findAll(Resource.class, toAdd);
        getLogger().debug("toAdd: {}", resourcesToAdd);
        getLogger().debug("toRemove: {}", resourcesToRemove);
        resourceCollectionService.saveCollectionForController(getPersistable(), parentId, parent, getAuthenticatedUser(), getAuthorizedUsers(), resourcesToAdd,
                resourcesToRemove, shouldSaveResource(), generateFileProxy(getFileFileName(), getFile()));
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
            searchIndexService.indexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            searchIndexService.indexAllResourcesInCollectionSubTree(getPersistable());
        }
    }

    public ResourceCollection getResourceCollection() {
        if (getPersistable() == null) {
            setPersistable(new ResourceCollection());
        }
        return getPersistable();
    }

    public void setResourceCollection(ResourceCollection rc) {
        setPersistable(rc);
    }

    @Override
    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
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
        return SUCCESS;
    }

    @Override
    @SkipValidation
    @Action(value = EDIT, results = {
            @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, location = ADD, type = REDIRECT)
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

    @Override
    public SortOption getSortField() {
        return this.sortField;
    }

    @Override
    public SortOption getSecondarySortField() {
        return this.secondarySortField;
    }

    @Override
    public void setTotalRecords(int resultSize) {
        this.totalRecords = resultSize;
    }

    @Override
    public int getStartRecord() {
        return this.startRecord;
    }

    @Override
    public int getRecordsPerPage() {
        return this.recordsPerPage;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public boolean isShowAll() {
        return false;
    }

    @Override
    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public void setCollections(List<ResourceCollection> findAllChildCollections) {
        getLogger().info("child collections: {}", findAllChildCollections);
        this.collections = findAllChildCollections;
    }

    public List<ResourceCollection> getCollections() {
        return this.collections;
    }

    @Override
    public int getTotalRecords() {
        return totalRecords;
    }

    @Override
    public void setResults(List<Resource> toReturn) {
        getLogger().trace("setResults: {}", toReturn);
        this.results = toReturn;
    }

    @Override
    public List<Resource> getResults() {
        return results;
    }

    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

    @Override
    public void setSortField(SortOption sortField) {
        this.sortField = sortField;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.search.query.SearchResultHandler#setMode(java.lang.String)
     */
    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.search.query.SearchResultHandler#getMode()
     */
    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
    }

    @Override
    public int getPrevPageStartRecord() {
        return startRecord - recordsPerPage;
    }

    @Override
    public String getSearchTitle() {
        return String.format("Resources in the %s Collection", getPersistable().getTitle());
    }

    @Override
    public String getSearchDescription() {
        return getSearchTitle();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        List<FacetGroup<? extends Enum>> group = new ArrayList<>();
        // List<FacetGroup<?>> group = new ArrayList<FacetGroup<?>>();
        group.add(new FacetGroup<ResourceType>(ResourceType.class, QueryFieldNames.RESOURCE_TYPE, resourceTypeFacets, ResourceType.DOCUMENT));
        return group;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null) {
            paginationHelper = PaginationHelper.withSearchResults(this);
        }
        return paginationHelper;
    }

    public String getParentCollectionName() {
        return parentCollectionName;

    }

    public ArrayList<FacetValue> getResourceTypeFacets() {
        return resourceTypeFacets;
    }

    public void setResourceTypeFacets(ArrayList<FacetValue> resourceTypeFacets) {
        this.resourceTypeFacets = resourceTypeFacets;
    }

    public ArrayList<ResourceType> getSelectedResourceTypes() {
        return selectedResourceTypes;
    }

    public void setSelectedResourceTypes(ArrayList<ResourceType> selectedResourceTypes) {
        this.selectedResourceTypes = selectedResourceTypes;
    }

    @Override
    public ProjectionModel getProjectionModel() {
        return ProjectionModel.RESOURCE_PROXY;
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

    @Override
    public int getDefaultRecordsPerPage() {
        return 100;
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
}

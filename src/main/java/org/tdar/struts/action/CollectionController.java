package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.struts.data.FacetGroup;
import org.tdar.utils.PaginationHelper;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionController extends AbstractPersistableController<ResourceCollection> implements SearchResultHandler<ResourceCollection> {

    private static final long serialVersionUID = 5710621983240752457L;
    private List<Resource> resources = new ArrayList<Resource>();

    private List<Long> selectedResourceIds = new ArrayList<Long>();
    private Long parentId;
    private List<Resource> fullUserProjects;
    private List<ResourceCollection> collections;

    private int startRecord = DEFAULT_START;
    private int recordsPerPage = 100;
    private int totalRecords;
    private List<ResourceCollection> results;
    private SortOption secondarySortField;
    private SortOption sortField;
    private String mode = "CollectionBrowse";
    private PaginationHelper paginationHelper;

    @Override
    public boolean isEditable() {
        if (isNullOrNew())
            return false;
        return getAuthenticationAndAuthorizationService().canEditCollection(getAuthenticatedUser(), getPersistable());
    }

    /**
     * Returns a list of all resource collections that can act as candidate parents for the current resource collection.
     * 
     * @return
     */
    public List<ResourceCollection> getCandidateParentResourceCollections() {
        List<ResourceCollection> publicResourceCollections = getResourceCollectionService().findPotentialParentCollections(getAuthenticatedUser(),
                getPersistable());
        return publicResourceCollections;
    }

    @Override
    public boolean isViewable() {
        return isEditable() || getAuthenticationAndAuthorizationService().canViewCollection(getResourceCollection(), getAuthenticatedUser());
    }

    @Override
    protected String save(ResourceCollection persistable) {
        if (persistable.getType() == null) {
            persistable.setType(CollectionType.SHARED);
        }
        // FIXME: may need some potential check for recursive loops here to prevent self-referential
        // parent-child loops
        // FIXME: if persistable's parent is different from current parent; then need to reindex all of the children as well
        ResourceCollection parent = getResourceCollectionService().find(parentId);
        if (Persistable.Base.isNotNullOrTransient(persistable) && Persistable.Base.isNotNullOrTransient(parent) && (parent.getParentIdList().contains(persistable.getId()) || parent.getId().equals(persistable.getId()))) {
            addActionError("cannot set a parent collection of self or it's child");
            return INPUT;
        }
        persistable.setParent(parent);
        getGenericService().saveOrUpdate(persistable);
        getResourceCollectionService().saveAuthorizedUsersForResourceCollection(persistable, getAuthorizedUsers(), shouldSaveResource());

        List<Resource> rehydratedIncomingResources = getResourceCollectionService().reconcileIncomingResourcesForCollection(persistable,
                getAuthenticatedUser(), resources);
        logger.trace("{}", rehydratedIncomingResources);
        logger.debug("RESOURCES {}", persistable.getResources());
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
            getSearchIndexService().indexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            getSearchIndexService().indexAllResourcesInCollectionSubTree(getPersistable());
        }
    }

    @Override
    public List<? extends Persistable> getDeleteIssues() {
        List<ResourceCollection> findAllChildCollections = getResourceCollectionService().findAllDirectChildCollections(getId(), null, CollectionType.SHARED);
        logger.info("we still have children: {}", findAllChildCollections);
        return findAllChildCollections;
    }

    @Override
    protected void delete(ResourceCollection persistable) {
        // should I do something special?
        for (Resource resource : persistable.getResources()) {
            resource.getResourceCollections().remove(persistable);
            getGenericService().saveOrUpdate(resource);
        }
        getGenericService().delete(persistable.getAuthorizedUsers());
        // FIXME: need to handle parents and children

        // getSearchIndexService().index(persistable.getResources().toArray(new Resource[0]));
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

    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
    }

    public List<SortOption> getSortOptions() {
        List<SortOption> options = SortOption.getOptionsForContext(Resource.class);
        options.remove(SortOption.RESOURCE_TYPE);
        options.remove(SortOption.RESOURCE_TYPE_REVERSE);
        options.add(0, SortOption.RESOURCE_TYPE);
        options.add(1, SortOption.RESOURCE_TYPE_REVERSE);
        return options;
    }

    public List<DisplayOrientation> getResultsOrientations() {
        List<DisplayOrientation> options = Arrays.asList(DisplayOrientation.values());
        return options;
    }

    public List<SortOption> getResourceDatatableSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    @Override
    public String loadViewMetadata() {
        // getAuthorizedUsers().addAll(getPersistable().getAuthorizedUsers());
        // resources.addAll(getPersistable().getResources());
        // for (Resource resource : getPersistable().getResources()) {
        // getAuthenticationAndAuthorizationService().applyTransientViewableFlag(resource, getAuthenticatedUser());
        // }
        setParentId(getPersistable().getParentId());
        return SUCCESS;
    }

    @Override
    public String loadEditMetadata() throws TdarActionException {
        super.loadEditMetadata();
        getAuthorizedUsers().addAll(getResourceCollectionService().getAuthorizedUsersForCollection(getPersistable(), getAuthenticatedUser()));
        // FIXME: this could be replaced with a load that's a skeleton object (title, resourceType, date)
        resources.addAll(getPersistable().getResources());
        // for (Resource resource : getPersistable().getResources()) {
        // getAuthenticationAndAuthorizationService().applyTransientViewableFlag(resource, getAuthenticatedUser());
        // }
        setParentId(getPersistable().getParentId());
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
        resources.removeAll(getRetainedResources());
        return result;
    }

    private List<Resource> getRetainedResources() {
        List<Resource> retainedResources = new ArrayList<Resource>();
        for (Resource resource : getPersistable().getResources()) {
            boolean canEdit = getAuthenticationAndAuthorizationService().canEditResource(getAuthenticatedUser(), resource);
            if (!canEdit) {
                retainedResources.add(resource);
            }
        }
        return retainedResources;
    }

    public void loadExtraViewMetadata() {
        if (Persistable.Base.isNullOrTransient(getId()))
            return;
        List<ResourceCollection> findAllChildCollections;
        if (isAuthenticated()) {
            findAllChildCollections = getResourceCollectionService().findAllDirectChildCollections(getId(), null, CollectionType.SHARED);
            // FIXME: not needed?
            // boolean granularPermissions = false;
            // if (granularPermissions) {
            // Iterator<ResourceCollection> iterator = findAllChildCollections.iterator();
            // while (iterator.hasNext()) {
            // ResourceCollection nextcollection = iterator.next();
            // if (!nextcollection.getAuthorizedUsers().contains(new AuthorizedUser(getAuthenticatedUser(), GeneralPermissions.VIEW_ALL)) &&
            // !nextcollection.getOwner().equals(getAuthenticatedUser()) &&
            // getAuthenticationAndAuthorizationService().cannot(InternalTdarRights.VIEW_ANYTHING, getAuthenticatedUser())) {
            // iterator.remove();
            // }
            // }
            // }
        } else {
            findAllChildCollections = getResourceCollectionService().findAllDirectChildCollections(getId(), true, CollectionType.SHARED);
        }
        setCollections(findAllChildCollections);
        Collections.sort(collections);

        if (isEditor()) {
            List<Long> collectionIds = Persistable.Base.extractIds(getResourceCollectionService().findAllChildCollectionsRecursive(getPersistable(),
                    CollectionType.SHARED));
            collectionIds.add(getId());
            setUploadedResourceAccessStatistic(getResourceService().getResourceSpaceUsageStatistics(null, null, collectionIds, null,
                    Arrays.asList(Status.ACTIVE, Status.DRAFT)));
        }

        if (getPersistable() != null) {
            // FIXME: logic is right here, but this feels "wrong"

            // if this collection is public, it will appear in a resource's public collection id list, otherwise it'll be in the shared collection id list
            // String collectionListFieldName = getPersistable().isVisible() ? QueryFieldNames.RESOURCE_COLLECTION_PUBLIC_IDS
            // : QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS;

            // the visibilty fence should take care of visible vs. shared above
            ResourceQueryBuilder qb = getSearchService().buildResourceContainedInSearch(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS,
                    getResourceCollection(), getAuthenticatedUser());
            setSortField(getPersistable().getSortBy());
            if (getSortField() != SortOption.RELEVANCE) {
                setSecondarySortField(SortOption.TITLE);
            }
            try {
                getSearchService().handleSearch(qb, this);
            } catch (Exception e) {
                addActionErrorWithException("error occurred while searching for collection contents", e);
            }
        }
    }

    /**
     * @return the resources
     */
    public List<Resource> getResources() {
        return resources;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources(List<Resource> resources) {
        this.resources = resources;
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

    public List<Project> getAllSubmittedProjects() {
        List<Project> allSubmittedProjects = getProjectService().findBySubmitter(getAuthenticatedUser());
        Collections.sort(allSubmittedProjects);
        return allSubmittedProjects;
    }

    public List<Resource> getFullUserProjects() {
        if (fullUserProjects == null) {
            boolean canEditAnything = getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
            fullUserProjects = new ArrayList<Resource>(getProjectService().findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
            fullUserProjects.removeAll(getAllSubmittedProjects());
        }
        return fullUserProjects;
    }

    public List<Status> getStatuses() {
        return new ArrayList<Status>(getAuthenticationAndAuthorizationService().getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    public List<ResourceType> getResourceTypes() {
        List<ResourceType> toReturn = new ArrayList<ResourceType>();
        toReturn.addAll(Arrays.asList(ResourceType.values()));
        return toReturn;
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

    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public void setCollections(List<ResourceCollection> findAllChildCollections) {
        logger.info("child collections: {}", findAllChildCollections);
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
    public void setResults(List<ResourceCollection> toReturn) {
        logger.trace("setResults: {}", toReturn);
        this.results = toReturn;
    }

    @Override
    public List<ResourceCollection> getResults() {
        return results;
    }

    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

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

    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
    }

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

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getProjections() {
        return ListUtils.EMPTY_LIST;
    }

    @Override
    public List<FacetGroup<? extends Facetable>> getFacetFields() {
        return null;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null)
            paginationHelper = PaginationHelper.withSearchResults(this);
        return paginationHelper;
    }

}

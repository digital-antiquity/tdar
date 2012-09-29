package org.tdar.struts.action;

import static org.tdar.core.service.external.auth.InternalTdarRights.SEARCH_FOR_DELETED_RECORDS;
import static org.tdar.core.service.external.auth.InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.hibernate.search.FullTextQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.queryBuilder.ResourceQueryBuilder;
import org.tdar.struts.search.query.SearchResultHandler;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionController extends AbstractPersistableController<ResourceCollection> implements SearchResultHandler<ResourceCollection> {

    private static final long serialVersionUID = 5710621983240752457L;
    private List<Resource> resources = new ArrayList<Resource>();

    private List<Long> selectedResourceIds = new ArrayList<Long>();
    private List<Resource> toReindex = new ArrayList<Resource>();
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

    @Override
    public boolean isEditable() {
        if (isNullOrNew())
            return false;
        return getEntityService().canEditCollection(getAuthenticatedUser(), getPersistable());
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
        return isEditable() || getEntityService().canViewCollection(getResourceCollection(), getAuthenticatedUser());
    }

    @Override
    protected String save(ResourceCollection persistable) {
        if (persistable.getType() == null) {
            persistable.setType(CollectionType.SHARED);
        }
        // FIXME: may need some potential check for recursive loops here to prevent self-referential
        // parent-child loops

        persistable.setParent(getResourceCollectionService().find(parentId));
        getGenericService().saveOrUpdate(persistable);
        getResourceCollectionService().saveAuthorizedUsersForResourceCollection(persistable, getAuthorizedUsers(), shouldSaveResource());

        List<Resource> rehydratedIncomingResources = getGenericService().rehydrateSparseIdBeans(resources, Resource.class);
        for (Resource resource : persistable.getResources()) {
            if (!rehydratedIncomingResources.contains(resource)) {
                resource.getResourceCollections().remove(persistable);
                toReindex.add(resource);
            }
        }
        persistable.getResources().removeAll(toReindex);
        getGenericService().saveOrUpdate(persistable);
        List<Resource> ineligibleResources = new ArrayList<Resource>();
        for (Resource resource : rehydratedIncomingResources) {
            logger.info(getAuthenticatedUser().toString());
            if (!getAuthenticationAndAuthorizationService().canEditResource(getAuthenticatedUser(), resource)) {
                ineligibleResources.add(resource);
                logger.info("{}", resource);
            } else {
                resource.getResourceCollections().add(persistable);
                // getGenericService().saveOrUpdate(resource);
                toReindex.add(resource);
            }
        }
        rehydratedIncomingResources.removeAll(ineligibleResources);
        persistable.getResources().addAll(rehydratedIncomingResources);
        getGenericService().saveOrUpdate(persistable);
        if (ineligibleResources.size() > 0) {
            throw new TdarRecoverableRuntimeException(
                    "the following resources could not be added to the collection because you do not have the rights to add them: " + ineligibleResources);
        }
        logger.trace("{}", rehydratedIncomingResources);
        logger.debug("RESOURCES {}", persistable.getResources());
        return SUCCESS;
    }

    @Override
    public void postSaveCleanup() {
        //This is apparently necessary in order to force indexing of transient fields.
        getSearchIndexService().indexCollection(toReindex);
    }

    @Override
    public List<? extends Persistable> getDeleteIssues() {
        List<ResourceCollection> findAllChildCollections = getResourceCollectionService().findAllChildCollections(getId(), null, CollectionType.SHARED);
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
    
    public List<SortOption> getResourceDatatableSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    @Override
    public String loadMetadata() {
        getAuthorizedUsers().addAll(getPersistable().getAuthorizedUsers());
        resources.addAll(getPersistable().getResources());
        setParentId(getPersistable().getParentId());
        return SUCCESS;
    }

    @Override
    @SkipValidation
    @Action(value = "edit", results = {
            @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, location = "add", type = "redirect")
    })
    public String edit() throws TdarActionException {
        String result = super.edit();
        resources.removeAll(getRetainedResources());
        return result;
    }

    private List<Resource> getRetainedResources() {
        List<Resource> retainedResources = new ArrayList<Resource>();
        for(Resource resource: getPersistable().getResources()) {
            boolean canEdit = getAuthenticationAndAuthorizationService().canEditResource(getAuthenticatedUser(), resource);
            if(!canEdit) {
                retainedResources.add(resource);
            }
        }
        return retainedResources; 
    }
    
    
    public void loadExtraViewMetadata() {
        if (getId() == null || getId() == -1)
            return;
        List<ResourceCollection> findAllChildCollections;
        if (isAuthenticated()) {
            findAllChildCollections = getResourceCollectionService().findAllChildCollections(getId(), null, CollectionType.SHARED);
            // FIXME: not needed?
            boolean granularPermissions = false;
            if (granularPermissions) {
                Iterator<ResourceCollection> iterator = findAllChildCollections.iterator();
                while (iterator.hasNext()) {
                    ResourceCollection nextcollection = iterator.next();
                    if (!nextcollection.getAuthorizedUsers().contains(new AuthorizedUser(getAuthenticatedUser(), GeneralPermissions.VIEW_ALL)) &&
                            !nextcollection.getOwner().equals(getAuthenticatedUser()) &&
                            getAuthenticationAndAuthorizationService().cannot(InternalTdarRights.VIEW_ANYTHING, getAuthenticatedUser())) {
                        iterator.remove();
                    }
                }
            }
        } else {
            findAllChildCollections = getResourceCollectionService().findAllChildCollections(getId(), true, CollectionType.SHARED);
        }
        setCollections(findAllChildCollections);
        Collections.sort(collections);

        if (getPersistable() != null) {
            ResourceQueryBuilder qb = getSearchService().buildResourceContainedInSearch(QueryFieldNames.RESOURCE_COLLECTION_PUBLIC_IDS,
                    getResourceCollection(), getAuthenticatedUser());
            setSortField(getPersistable().getSortBy());
            if (getSortField() != SortOption.RELEVANCE) {
                setSecondarySortField(SortOption.TITLE);
            }
            try {
                getSearchService().handleSearch(qb, this);
            } catch (Exception e) {
                addActionErrorWithException("something happend", e);
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
        List<Status> toReturn = new ArrayList<Status>(getResourceService().findAllStatuses());
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(toReturn, Status.DELETED, SEARCH_FOR_DELETED_RECORDS, getAuthenticatedUser());
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(toReturn, Status.FLAGGED, SEARCH_FOR_FLAGGED_RECORDS, getAuthenticatedUser());
        return toReturn;
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
    public void addFacets(FullTextQuery ftq) {
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

}

package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.hibernate.search.FullTextQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.ResourceQueryBuilder;
import org.tdar.search.query.SortOption;
import org.tdar.struts.search.query.SearchResultHandler;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionController extends AbstractPersistableController<ResourceCollection> implements SearchResultHandler {

    private static final long serialVersionUID = 5710621983240752457L;
    private List<Resource> resources = new ArrayList<Resource>();

    private List<Long> selectedResourceIds = new ArrayList<Long>();
    private List<Resource> toReindex = new ArrayList<Resource>();
    private Long parentId;
    private List<Resource> fullUserProjects;
    private int resultSize;
    private int startRecord = DEFAULT_START;
    private int recordsPerPage = 800;
    private int totalRecords;
    private List<ResourceCollection> collections;
    private List<Indexable> results;
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

    public boolean isViewable() {
        return isAdministrator() || isEditable() || getEntityService().canViewCollection(getResourceCollection(), getAuthenticatedUser());
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
        for (Resource resource : rehydratedIncomingResources) {
            resource.getResourceCollections().add(persistable);
            // getGenericService().saveOrUpdate(resource);
            toReindex.add(resource);
        }
        persistable.getResources().addAll(rehydratedIncomingResources);
        getGenericService().saveOrUpdate(persistable);
        logger.trace("{}", rehydratedIncomingResources);
        logger.debug("RESOURCES {}", persistable.getResources());
        return SUCCESS;
    }

    @Override
    public void postSaveCleanup() {
      //  getSearchIndexService().indexCollection(toReindex);
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

//        getSearchIndexService().index(persistable.getResources().toArray(new Resource[0]));
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

    @Override
    public String loadMetadata() {
        getAuthorizedUsers().addAll(getPersistable().getAuthorizedUsers());
        resources.addAll(getPersistable().getResources());
        setParentId(getPersistable().getParentId());
        return SUCCESS;
    }

    public void loadExtraViewMetadata() {
        if(getId() == null || getId() == -1) return;
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
                            !nextcollection.getOwner().equals(getAuthenticatedUser()) && !isAdministrator()) {
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
            ResourceQueryBuilder qb = new ResourceQueryBuilder();
            QueryPartGroup group = new QueryPartGroup(Operator.OR);
            group.append(new FieldQueryPart(QueryFieldNames.STATUS, Status.ACTIVE.toString()));
            if (getAuthenticatedUser() != null) {
                QueryPartGroup group2 = new QueryPartGroup(Operator.AND);
                group2.append(new FieldQueryPart(QueryFieldNames.STATUS, Status.DRAFT.toString()));
                group2.append(new FieldQueryPart(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, getAuthenticatedUser().getId().toString()));
                group.append(group2);
            }
            qb.append(group);
            qb.append(new FieldQueryPart(QueryFieldNames.RESOURCE_COLLECTION_PUBLIC_IDS, getPersistable().getId().toString()));
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
            fullUserProjects = new ArrayList<Resource>(getProjectService().findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), isAdministrator()));
            fullUserProjects.removeAll(getAllSubmittedProjects());
        }
        return fullUserProjects;
    }

    public List<Status> getStatuses() {
        List<Status> toReturn = new ArrayList<Status>();
        for (Status status : getResourceService().findAllStatuses()) {
            if (!isAdministrator() && (status == Status.FLAGGED ||
                    status == Status.DELETED)) {
                continue;
            }
            toReturn.add(status);

        }
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
        this.setResultSize(resultSize);
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

    public int getResultSize() {
        return resultSize;
    }

    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
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
    public void setResults(List<Indexable> toReturn) {
        logger.info("setResults: {}", toReturn);
        this.results = toReturn;
    }

    @Override
    public List<Indexable> getResults() {
        return results;
    }

    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

    public void setSortField(SortOption sortField) {
        this.sortField = sortField;
    }

    /* (non-Javadoc)
     * @see org.tdar.struts.search.query.SearchResultHandler#setMode(java.lang.String)
     */
    @Override
    public void setMode(String mode) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.tdar.struts.search.query.SearchResultHandler#getMode()
     */
    @Override
    public String getMode() {
        // TODO Auto-generated method stub
        return null;
    }

}

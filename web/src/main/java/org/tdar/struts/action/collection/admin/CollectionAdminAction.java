package org.tdar.struts.action.collection.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.exception.SearchPaginationException;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PaginationHelper;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin")
public class CollectionAdminAction extends AbstractCollectionAdminAction implements Preparable, FacetedResultHandler<Resource> {

    private static final long serialVersionUID = -4060598709570483884L;
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ResourceSearchService resourceSearchService;
    @Autowired
    private BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    private AuthorizationService authorizationService;

    private String term;
    private Set<SharedCollection> allChildCollections;
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;
    private PaginationHelper paginationHelper;
    private FacetWrapper facetWrapper = new FacetWrapper();
    private int recordsPerPage = 1000;
    private int totalRecords;
    private int startRecord = 0;

    private List<Resource> results;
    private SortOption secondarySortField;
    private SortOption sortField = SortOption.TITLE;
    private String mode = "CollectionAdminBrowse";
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<>();
    private ArrayList<Status> selectedResourceStatuses = new ArrayList<>();
    private ArrayList<ResourceAccessType> fileAccessTypes = new ArrayList<>();

    @Override
    public void prepare() throws Exception {
        super.prepare();
        resourceCollectionService.buildCollectionTreeForController(getCollection(), getAuthenticatedUser(), CollectionType.SHARED);
        setAllChildCollections(getCollection().getTransientChildren());

        List<Long> collectionIds = PersistableUtils.extractIds(getCollection().getTransientChildren());
        collectionIds.add(getId());

        setUploadedResourceAccessStatistic(resourceService.getSpaceUsageForCollections(collectionIds, Arrays.asList(Status.ACTIVE, Status.DRAFT)));
        facetWrapper.facetBy(QueryFieldNames.RESOURCE_TYPE, ResourceType.class, getSelectedResourceTypes());
        facetWrapper.facetBy(QueryFieldNames.STATUS, Status.class, getSelectedResourceStatuses());
        facetWrapper.facetBy(QueryFieldNames.RESOURCE_ACCESS_TYPE, ResourceAccessType.class, getFileAccessTypes());

        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }

        try {
            resourceSearchService.buildResourceContainedInSearch(getCollection(), getTerm(), getAuthenticatedUser(), this, this);
            bookmarkedResourceService.applyTransientBookmarked(getResults(), getAuthenticatedUser());
        } catch (SearchPaginationException spe) {
            throw new TdarActionException(StatusCode.BAD_REQUEST, spe);
        } catch (Exception e) {
            addActionErrorWithException(getText("collectionController.error_searching_contents"), e);
        }

    }

    @Override
    @Action(value = "{id}", results = {
            @Result(name = SUCCESS, type = FREEMARKER, location = "index.ftl"),
    })
    public String execute() throws Exception {
        return SUCCESS;
    }

    public ResourceSpaceUsageStatistic getUploadedResourceAccessStatistic() {
        return uploadedResourceAccessStatistic;
    }

    public void setUploadedResourceAccessStatistic(ResourceSpaceUsageStatistic uploadedResourceAccessStatistic) {
        this.uploadedResourceAccessStatistic = uploadedResourceAccessStatistic;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    public List<Resource> getResults() {
        return results;
    }

    public void setResults(List<Resource> results) {
        this.results = results;
    }

    public SortOption getSecondarySortField() {
        return secondarySortField;
    }

    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

    public SortOption getSortField() {
        return sortField;
    }

    public void setSortField(SortOption sortField) {
        this.sortField = sortField;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public ProjectionModel getProjectionModel() {
        return ProjectionModel.LUCENE_EXPERIMENTAL;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public String getSearchTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSearchDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getNextPageStartRecord() {
        return getStartRecord() + getRecordsPerPage();
    }

    @Override
    public int getPrevPageStartRecord() {
        return getStartRecord() - getRecordsPerPage();
    }

    @Override
    public int getDefaultRecordsPerPage() {
        return 1000;
    }

    @Override
    public void setSearchTitle(String description) {
        // TODO Auto-generated method stub

    }

    @Override
    public DisplayOrientation getOrientation() {
        return DisplayOrientation.MAP;
    }

    public FacetWrapper getFacetWrapper() {
        return facetWrapper;
    }

    public void setFacetWrapper(FacetWrapper facetWrapper) {
        this.facetWrapper = facetWrapper;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null) {
            paginationHelper = PaginationHelper.withSearchResults(this);
        }
        return paginationHelper;
    }

    public Set<SharedCollection> getAllChildCollections() {
        return allChildCollections;
    }

    public void setAllChildCollections(Set<SharedCollection> allChildCollections) {
        this.allChildCollections = allChildCollections;
    }

    public List<Facet> getResourceTypeFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.RESOURCE_TYPE);
    }

    public List<Facet> getStatusFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.STATUS);
    }

    public List<Facet> getFileAccessFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.RESOURCE_ACCESS_TYPE);
    }

    public List<Status> getAllStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    public List<SortOption> getSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    public ArrayList<Status> getSelectedResourceStatuses() {
        return selectedResourceStatuses;
    }

    public void setSelectedResourceStatuses(ArrayList<Status> selectedResourceStatuses) {
        this.selectedResourceStatuses = selectedResourceStatuses;
    }

    public ArrayList<ResourceType> getSelectedResourceTypes() {
        return selectedResourceTypes;
    }

    public void setSelectedResourceTypes(ArrayList<ResourceType> selectedResourceTypes) {
        this.selectedResourceTypes = selectedResourceTypes;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public ArrayList<ResourceAccessType> getFileAccessTypes() {
        return fileAccessTypes;
    }

    public void setFileAccessTypes(ArrayList<ResourceAccessType> fileAccessTypes) {
        this.fileAccessTypes = fileAccessTypes;
    }

}

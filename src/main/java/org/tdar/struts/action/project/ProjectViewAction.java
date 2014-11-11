package org.tdar.struts.action.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.SearchService;
import org.tdar.search.query.FacetValue;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.resource.AbstractResourceViewAction;
import org.tdar.struts.data.FacetGroup;
import org.tdar.utils.PaginationHelper;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/project")
public class ProjectViewAction extends AbstractResourceViewAction<Project> implements SearchResultHandler<Resource> {

    private static final long serialVersionUID = 974044619477885680L;
    private ProjectionModel projectionModel = ProjectionModel.RESOURCE_PROXY;
    private int startRecord = DEFAULT_START;
    private int recordsPerPage = 100;
    private int totalRecords;
    private List<Resource> results;
    private SortOption secondarySortField;
    private SortOption sortField;
    private String mode = "ProjectBrowse";
    private PaginationHelper paginationHelper;
    private ArrayList<FacetValue> resourceTypeFacets = new ArrayList<>();
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<>();

    @Autowired
    private transient SearchService searchService;

    @Autowired
    private BookmarkedResourceService bookmarkedResourceService;

    
    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        try {
            handleSearch();
        } catch (Exception e) {
            getLogger().error("error in exception", e);
        }
    }

    private void handleSearch() throws TdarActionException {
        Project project = (Project) getResource();
        ResourceQueryBuilder qb = searchService.buildResourceContainedInSearch(QueryFieldNames.PROJECT_ID, project, getAuthenticatedUser(), this);
        setSortField(project.getSortBy());
        setSecondarySortField(SortOption.TITLE);
        if (project.getSecondarySortBy() != null) {
            setSecondarySortField(project.getSecondarySortBy());
        }
        searchService.addResourceTypeFacetToViewPage(qb, selectedResourceTypes, this);
        Date dateUpdated = project.getDateUpdated();
        if (dateUpdated == null || DateTime.now().minusMinutes(TdarConfiguration.getInstance().getAsyncWaitToTrustCache()).isBefore(dateUpdated.getTime())) {
            projectionModel = ProjectionModel.RESOURCE_PROXY_INVALIDATE_CACHE;
        }
        try {
            searchService.handleSearch(qb, this, this);
            bookmarkedResourceService.applyTransientBookmarked(getResults(), getAuthenticatedUser());

        } catch (SearchPaginationException e) {
            throw new TdarActionException(StatusCode.BAD_REQUEST, e);
        } catch (Exception e) {
            addActionErrorWithException(getText("projectController.something_happened"), e);
        }

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

    @Override
    public int getTotalRecords() {
        return totalRecords;
    }

    @Override
    public void setResults(List<Resource> results) {
        getLogger().trace("setResults: {}", results);
        this.results = results;
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
        return getText("projectController.search_title", getPersistable().getTitle());
    }

    @Override
    public String getSearchDescription() {
        return getSearchTitle();
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
        return projectionModel;
    }

    public void setProjectionModel(ProjectionModel projectionModel) {
        this.projectionModel = projectionModel;
    }

}

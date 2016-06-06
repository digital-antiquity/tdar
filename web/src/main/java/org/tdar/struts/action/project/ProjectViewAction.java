package org.tdar.struts.action.project;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.search.exception.SearchPaginationException;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.ResourceFacetedAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.resource.AbstractResourceViewAction;
import org.tdar.utils.PaginationHelper;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/project")
public class ProjectViewAction extends AbstractResourceViewAction<Project> implements FacetedResultHandler<Resource>, ResourceFacetedAction {

    private static final long serialVersionUID = 974044619477885680L;
    private ProjectionModel projectionModel = ProjectionModel.LUCENE_EXPERIMENTAL;
    private int startRecord = DEFAULT_START;
    private int recordsPerPage = getDefaultRecordsPerPage();
    private int totalRecords;
    private List<Resource> results;
    private SortOption secondarySortField;
    private SortOption sortField;
    private String mode = "ProjectBrowse";
    private PaginationHelper paginationHelper;
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<>();

    @Autowired
    private transient ResourceSearchService resourceSearchService;


    @Autowired
    private BookmarkedResourceService bookmarkedResourceService;
    private FacetWrapper facetWrapper = new FacetWrapper();

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        try {
            handleSearch();
        } catch (Exception e) {
            if (e.getCause() instanceof SearchPaginationException) {
                getLogger().warn("search pagination issue", e);
            } else {
                getLogger().error("error in exception", e);
            }
        }
    }

    private void handleSearch() throws TdarActionException {
        Project project = (Project) getResource();

        setSortField(project.getSortBy());
        setSecondarySortField(SortOption.TITLE);
        if (project.getSecondarySortBy() != null) {
            setSecondarySortField(project.getSecondarySortBy());
        }
        facetWrapper.facetBy(QueryFieldNames.RESOURCE_TYPE,  ResourceType.class, selectedResourceTypes);

        try {
            resourceSearchService.buildResourceContainedInSearch(QueryFieldNames.PROJECT_ID, project, getAuthenticatedUser(), this, this);
            bookmarkedResourceService.applyTransientBookmarked(getResults(), getAuthenticatedUser());
            reSortFacets(this, project);
        } catch (SearchPaginationException e) {
            throw new TdarActionException(StatusCode.BAD_REQUEST, e);
        } catch (Exception e) {
            addActionErrorWithException(getText("projectController.something_happened"), e);
        }

    }

    @Override
    public Class<Project> getPersistableClass() {
        return Project.class;
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

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null) {
            paginationHelper = PaginationHelper.withSearchResults(this);
        }
        return paginationHelper;
    }

    public List<Facet> getResourceTypeFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.RESOURCE_TYPE);
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

    @Override
    public int getDefaultRecordsPerPage() {
        return 100;
    }

    @Override
    public void setSearchTitle(String description) {
        // TODO Auto-generated method stub
        
    }


    public FacetWrapper getFacetWrapper() {
        return facetWrapper;
    }

    public void setFacetWrapper(FacetWrapper facetWrapper) {
        this.facetWrapper = facetWrapper;
    }

	@Override
	public DisplayOrientation getOrientation() {
		return getPersistable().getOrientation();
	}

}

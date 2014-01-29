package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FacetGroup;
import org.tdar.utils.PaginationHelper;

/**
 * $Id$
 * 
 * Manages requests to create/delete/edit a Project and its associated metadata (including Datasets, etc).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/project")
@Component
@Scope("prototype")
public class ProjectController extends AbstractResourceController<Project> implements SearchResultHandler<Resource> {

    private static final long serialVersionUID = -5625084702553576277L;

    private String callback;
    private ProjectionModel projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
    private int startRecord = DEFAULT_START;
    private int recordsPerPage = 100;
    private int totalRecords;
    private List<Resource> results;
    private SortOption secondarySortField;
    private SortOption sortField;
    private String mode = "ProjectBrowse";
    private PaginationHelper paginationHelper;
    private ArrayList<ResourceType> resourceTypeFacets = new ArrayList<ResourceType>();
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<ResourceType>();

    /**
     * Projects contain no additional metadata beyond basic Resource metadata so saveBasicResourceMetadata() should work.
     */
    @Override
    protected String save(Project resource) {
        getLogger().trace("saving a project");
        saveBasicResourceMetadata();
        getLogger().trace("saved metadata -- about to call saveOrUPdate");
        getProjectService().saveOrUpdate(resource);
        getLogger().trace("finished calling saveorupdate");
        return SUCCESS;
    }

    @Override
    public void indexPersistable() {
        if (isAsync()) {
            getSearchIndexService().indexProjectAsync(getPersistable());
        } else {
            getSearchIndexService().indexProject(getPersistable());
        }
    }

    // FIXME: this belongs in the abstractResourcController, and there should be an abstract method that returns gives hints to json() on which fields to
    // serialize
    @Action(value = JSON,
            results = { @Result(
                    name = SUCCESS,
                    location = "json.ftl",
                    params = { "contentType", "application/json" },
                    type = "freemarker"
                    ) }
            )
            @SkipValidation
            public String json() {
        return SUCCESS;
    }

    @Override
    public Collection<? extends Persistable> getDeleteIssues() {
        return getProjectService().findAllResourcesInProject(getProject(), Status.ACTIVE, Status.DRAFT);
    }

    @Override
    protected void loadCustomMetadata() throws TdarActionException {
        if (getPersistable() != null) {
            ResourceQueryBuilder qb = getSearchService().buildResourceContainedInSearch(QueryFieldNames.PROJECT_ID, getProject(), getAuthenticatedUser(),this);
            setSortField(getProject().getSortBy());
            setSecondarySortField(SortOption.TITLE);
            if (getProject().getSecondarySortBy() != null) {
                setSecondarySortField(getProject().getSecondarySortBy());
            }
            getSearchService().addResourceTypeFacetToViewPage(qb, selectedResourceTypes, this);

            try {
                setProjectionModel(ProjectionModel.RESOURCE_PROXY);
                getSearchService().handleSearch(qb, this);
            } catch (SearchPaginationException e) {
                throw new TdarActionException(StatusCode.BAD_REQUEST, e);
            } catch (Exception e) {
                addActionErrorWithException(getText("projectController.something_happened"), e);
            }
        }
    }

    @SkipValidation
    public String getProjectAsJson() {
        getLogger().trace("getprojectasjson called");
        String json = "{}";
        try {
            Project project = getProject();
            if (project == null || project.isTransient()) {
                getLogger().trace("Trying to convert blank or null project to json: " + project);
                return json;
            }
            json = project.toJSON().toString();
        } catch (Exception ex) {
            addActionErrorWithException(getText("projectController.project_json_invalid"), ex);
        }
        getLogger().trace("returning json:" + json);
        return json;
    }

    public Project getProject() {
        return getPersistable();
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void setProject(Project project) {
        setPersistable(project);
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
        logger.trace("setResults: {}", results);
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
    public List<FacetGroup<? extends Facetable>> getFacetFields() {
        List<FacetGroup<? extends Facetable>> group = new ArrayList<>();
        // List<FacetGroup<?>> group = new ArrayList<FacetGroup<?>>();
        group.add(new FacetGroup<ResourceType>(ResourceType.class, QueryFieldNames.RESOURCE_TYPE, resourceTypeFacets, ResourceType.DOCUMENT));
        return group;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null)
            paginationHelper = PaginationHelper.withSearchResults(this);
        return paginationHelper;
    }

    public ArrayList<ResourceType> getResourceTypeFacets() {
        return resourceTypeFacets;
    }

    public void setResourceTypeFacets(ArrayList<ResourceType> resourceTypeFacets) {
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

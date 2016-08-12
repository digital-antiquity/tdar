package org.tdar.struts.action.collection.admin;

import java.util.ArrayList;
import java.util.Collections;
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
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PaginationHelper;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin/batch")
public class CollectionBatchAction extends AbstractCollectionAdminAction implements Preparable, FacetedResultHandler<Resource> {

    private static final long serialVersionUID = 1L;

    private Long accountId;
    private BillingAccount account;
    
    private Long projectId;
    private Project project;

    
    private List<Long> ids = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private List<String> descriptions = new ArrayList<>();
    private List<Integer> dates = new ArrayList<>();
    private List<Resource> fullUserProjects;
    private List<Project> allSubmittedProjects;
    
    @Autowired
    private GenericService genericService;
    @Autowired
    private BillingAccountService billingAccountService;
    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private ResourceService resourceService;
    
    private String term;
    private Set<ResourceCollection> allChildCollections;
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
    }

    @Override
    @Action(value = "{id}", results = {
            @Result(name = SUCCESS, type = FREEMARKER, location = "../batch.ftl"),
    })
    public String execute() throws Exception {
        setAllSubmittedProjects(projectService.findBySubmitter(getAuthenticatedUser()));
        Collections.sort(getAllSubmittedProjects());
        boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
        setFullUserProjects(new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything)));
        getFullUserProjects().removeAll(getAllSubmittedProjects());

        return SUCCESS;
    }

    
    @Action(value = "save", results = {
            @Result(name = SUCCESS, type = FREEMARKER, location = "../index.ftl"),
    })
    @PostOnly
    public String save() throws Exception {
        Project project = genericService.find(Project.class, projectId);
        BillingAccount account = billingAccountService.find(accountId);

        resourceService.updateBatch(project,account, ids, dates, titles, descriptions, getAuthenticatedUser());
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

    public Set<ResourceCollection> getAllChildCollections() {
        return allChildCollections;
    }

    public void setAllChildCollections(Set<ResourceCollection> allChildCollections) {
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<Integer> getDates() {
        return dates;
    }

    public void setDates(List<Integer> dates) {
        this.dates = dates;
    }

    public List<Resource> getFullUserProjects() {
        return fullUserProjects;
    }

    public void setFullUserProjects(List<Resource> fullUserProjects) {
        this.fullUserProjects = fullUserProjects;
    }

    public List<Project> getAllSubmittedProjects() {
        return allSubmittedProjects;
    }

    public void setAllSubmittedProjects(List<Project> allSubmittedProjects) {
        this.allSubmittedProjects = allSubmittedProjects;
    }

}

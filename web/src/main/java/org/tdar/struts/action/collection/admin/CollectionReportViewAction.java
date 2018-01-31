package org.tdar.struts.action.collection.admin;

import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection/admin/report")
@HttpsOnly
public class CollectionReportViewAction extends AbstractAuthenticatableAction
        implements FacetedResultHandler<Resource>, PersistableLoadingAction<ResourceCollection>, Preparable {

    private static final long serialVersionUID = 5515399574166871914L;
    @Autowired
    ResourceSearchService resourceSearchService;

    private FacetWrapper facetWrapper = new FacetWrapper();
    private ResourceCollection resourceCollection;
    private Long id;
    private List<Resource> results;

    @Override
    @Action(value = "{id}", results = { @Result(name = SUCCESS, location = "../report.ftl") })
    public String execute() throws Exception {
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        getAuthenticatedUser();
        asqo.getReservedParams().getShares().add((ResourceCollection) getResourceCollection());
        getFacetWrapper().facetBy("status", Status.class);
        getFacetWrapper().facetBy("resourceType", ResourceType.class);
        getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, CultureKeyword.class);
        getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_INVESTIGATION_TYPES, InvestigationType.class);
        getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS, MaterialKeyword.class);
        getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS, TemporalKeyword.class);
        getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_OTHER_KEYWORDS, OtherKeyword.class);
        getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS, GeographicKeyword.class);
        getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS, SiteTypeKeyword.class);
        getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS, SiteNameKeyword.class);
        for (ResourceCreatorRole role : ResourceCreatorRole.values()) {
            getFacetWrapper().facetBy(role.name(), Creator.class);

        }
        getFacetWrapper().setMaxFacetLimit(100);
        asqo.getReservedParams().setStatuses(Arrays.asList(Status.ACTIVE, Status.DRAFT));
        resourceSearchService.buildAdvancedSearch(asqo, getAuthenticatedUser(), this, this);
        return SUCCESS;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return true;
    }

    @Override
    public Persistable getPersistable() {
        return getResourceCollection();
    }

    @Override
    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.VIEW);
    }

    @Override
    public void setPersistable(ResourceCollection persistable) {
        this.setResourceCollection(persistable);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.VIEW_ANYTHING;
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        // TODO Auto-generated method stub

    }

    @Override
    public SortOption getSortField() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSortField(SortOption sortField) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getStartRecord() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setStartRecord(int startRecord) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getRecordsPerPage() {
        // TODO Auto-generated method stub
        return 10;
    }

    @Override
    public SortOption getSecondarySortField() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTotalRecords(int resultSize) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getTotalRecords() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isDebug() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setResults(List<Resource> toReturn) {
        this.results = toReturn;
    }

    @Override
    public List<Resource> getResults() {
        return results;
    }

    @Override
    public void setMode(String mode) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getMode() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getPrevPageStartRecord() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDefaultRecordsPerPage() {
        return 10;
    }

    @Override
    public void setSearchTitle(String description) {
        // TODO Auto-generated method stub

    }

    @Override
    public DisplayOrientation getOrientation() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceCollection getResourceCollection() {
        return resourceCollection;
    }

    public void setResourceCollection(ResourceCollection resourceCollection) {
        this.resourceCollection = resourceCollection;
    }

    public FacetWrapper getFacetWrapper() {
        return facetWrapper;
    }

    public void setFacetWrapper(FacetWrapper facetWrapper) {
        this.facetWrapper = facetWrapper;
    }

    @Override
    public ProjectionModel getProjectionModel() {
        // TODO Auto-generated method stub
        return null;
    }

}

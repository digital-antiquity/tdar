package org.tdar.struts.action.collection;

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
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection/report")
@HttpsOnly
public class CollectionReportView extends AuthenticationAware.Base implements SearchResultHandler<Resource>, PersistableLoadingAction<ResourceCollection> {

	private static final long serialVersionUID = 5515399574166871914L;
	@Autowired
	ResourceSearchService resourceSearchService;
	
	private FacetWrapper facetWrapper = new FacetWrapper();
	private ResourceCollection resourceCollection;
	private Long id;


	@Override
    @Action(value = "{id}", results = { @Result(name = SUCCESS, location = "../report.ftl") })
	public String execute() throws Exception {
		AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
		asqo.getReservedParams().getCollections().add(getResourceCollection());
		getFacetWrapper().facetBy("people", Person.class);
		getFacetWrapper().facetBy("institutions", Institution.class);
		getFacetWrapper().facetBy("cultureKeyword", CultureKeyword.class);
		getFacetWrapper().facetBy("investigationType", InvestigationType.class);
		getFacetWrapper().facetBy("materialKeyword", MaterialKeyword.class);
		getFacetWrapper().facetBy("temporalKeyword", TemporalKeyword.class);
		getFacetWrapper().facetBy("otherKeyword", OtherKeyword.class);
		getFacetWrapper().facetBy("siteTypeKeyword", SiteTypeKeyword.class);
		getFacetWrapper().facetBy("siteNameKeyword", SiteNameKeyword.class);
		getFacetWrapper().facetBy("p"+ResourceCreatorRole.AUTHOR.name(), Person.class);
		getFacetWrapper().facetBy("i"+ResourceCreatorRole.AUTHOR.name(), Institution.class);
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
		return 0;
	}


	@Override
	public SortOption getSecondarySortField() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public org.tdar.search.query.SearchResultHandler.ProjectionModel getProjectionModel() {
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
		// TODO Auto-generated method stub
		
	}


	@Override
	public List<Resource> getResults() {
		// TODO Auto-generated method stub
		return null;
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
	public TdarUser getAuthenticatedUser() {
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
		// TODO Auto-generated method stub
		return 0;
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

	
}

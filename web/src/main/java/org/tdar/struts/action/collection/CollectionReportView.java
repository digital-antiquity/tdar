package org.tdar.struts.action.collection;

import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
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
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

@ParentPackage("secured")
@Namespace("/collection/usage")
@HttpsOnly
public class CollectionReportView extends TdarActionSupport implements SearchResultHandler<Resource>, PersistableLoadingAction<ResourceCollection> {

	private static final long serialVersionUID = 5515399574166871914L;
	@Autowired
	ResourceSearchService resourceSearchService;
	
	private FacetWrapper facetWrapper = new FacetWrapper();
	private ResourceCollection resourceCollection;
	private Long id;

	@Override
	public String execute() throws Exception {
		AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
		asqo.getReservedParams().getCollections().add(resourceCollection);
		facetWrapper.facetBy("people", Person.class);
		facetWrapper.facetBy("institutions", Institution.class);
		facetWrapper.facetBy("cultureKeyword", CultureKeyword.class);
		facetWrapper.facetBy("investigationType", InvestigationType.class);
		facetWrapper.facetBy("materialKeyword", MaterialKeyword.class);
		facetWrapper.facetBy("temporalKeyword", TemporalKeyword.class);
		facetWrapper.facetBy("otherKeyword", OtherKeyword.class);
		facetWrapper.facetBy("siteTypeKeyword", SiteTypeKeyword.class);
		facetWrapper.facetBy("siteNameKeyword", SiteNameKeyword.class);
		facetWrapper.facetBy("geographicKeyword", GeographicKeyword.class);
		resourceSearchService.buildAdvancedSearch(asqo, getAuthenticatedUser(), this, this);
		return SUCCESS;
	}
	

	@Override
	public boolean authorize() throws TdarActionException {
		return false;
	}

	@Override
	public Persistable getPersistable() {
		return resourceCollection;
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
		this.resourceCollection = persistable;
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

	
}

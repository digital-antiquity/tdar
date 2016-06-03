package org.tdar.search.query;

import java.io.Serializable;

import org.tdar.core.bean.Indexable;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;

public class SearchResult<I extends Indexable> extends BaseSearchResult<I> implements FacetedResultHandler<I>, Serializable {

    private static final long serialVersionUID = 8370261049894410532L;
    private FacetWrapper facetWrapper = new FacetWrapper();
    private ProjectionModel projectionModel = ProjectionModel.HIBERNATE_DEFAULT;    

    public SearchResult() {}
    
    public SearchResult(int i) {
    	setRecordsPerPage(i);
	}


    @Override
    public FacetWrapper getFacetWrapper() {
        return facetWrapper;
    }

    public void setFacetWrapper(FacetWrapper facetWrapper) {
        this.facetWrapper = facetWrapper;
    }


    @Override
    public ProjectionModel getProjectionModel() {
        return projectionModel;
    }

    public void setProjectionModel(ProjectionModel projectionModel) {
        this.projectionModel = projectionModel;
    }

}

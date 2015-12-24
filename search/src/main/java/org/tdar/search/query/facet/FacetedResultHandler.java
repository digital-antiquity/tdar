package org.tdar.search.query.facet;

import org.tdar.core.bean.Indexable;
import org.tdar.search.query.SearchResultHandler;

public interface FacetedResultHandler<I extends Indexable> extends SearchResultHandler<I> {

    
    FacetWrapper getFacetWrapper();

}

package org.tdar.search.query;

import org.tdar.core.bean.Indexable;

public interface FacetedResultHandler<I extends Indexable> extends SearchResultHandler<I> {

    
    FacetWrapper getFacetWrapper();

}

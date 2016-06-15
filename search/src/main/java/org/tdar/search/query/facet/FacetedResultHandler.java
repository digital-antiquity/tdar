package org.tdar.search.query.facet;

import org.tdar.core.bean.Indexable;
import org.tdar.search.query.LuceneSearchResultHandler;

public interface FacetedResultHandler<I extends Indexable> extends LuceneSearchResultHandler<I> {

    
    FacetWrapper getFacetWrapper();

    
}

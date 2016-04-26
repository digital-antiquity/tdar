package org.tdar.search.query;

import org.tdar.core.bean.Indexable;

public interface LuceneSearchResultHandler<I extends Indexable> extends SearchResultHandler<I>{

    ProjectionModel getProjectionModel();

}

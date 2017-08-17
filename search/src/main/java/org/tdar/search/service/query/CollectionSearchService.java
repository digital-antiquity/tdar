package org.tdar.search.service.query;

import java.io.IOException;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;

import com.opensymphony.xwork2.TextProvider;

public interface CollectionSearchService {

    LuceneSearchResultHandler<ResourceCollection> buildResourceCollectionQuery(TdarUser authenticatedUser, CollectionSearchQueryObject query,
            LuceneSearchResultHandler<ResourceCollection> result, TextProvider provider) throws SearchException, IOException;

    LuceneSearchResultHandler<ResourceCollection> lookupCollection(TdarUser authenticatedUser, CollectionSearchQueryObject csqo,
            LuceneSearchResultHandler<ResourceCollection> result, TextProvider provider) throws SearchException, IOException;

}
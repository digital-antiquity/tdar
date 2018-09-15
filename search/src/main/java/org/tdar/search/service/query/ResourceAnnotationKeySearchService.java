package org.tdar.search.service.query;

import java.io.IOException;

import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResultHandler;

import com.opensymphony.xwork2.TextProvider;

public interface ResourceAnnotationKeySearchService {

    SearchResultHandler<ResourceAnnotationKey> buildAnnotationSearch(String term, LuceneSearchResultHandler<ResourceAnnotationKey> result, int min,
            TextProvider provider) throws SearchException, IOException;

}
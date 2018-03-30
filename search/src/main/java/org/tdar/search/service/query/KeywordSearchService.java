package org.tdar.search.service.query;

import java.io.IOException;

import org.tdar.core.bean.keyword.Keyword;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;

import com.opensymphony.xwork2.TextProvider;

public interface KeywordSearchService<I extends Keyword> {

    LuceneSearchResultHandler<I> findKeyword(String term, String keywordType, LuceneSearchResultHandler<I> result, TextProvider provider, int min)
            throws SearchException, IOException;

}
package org.tdar.search;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResult;
import org.tdar.utils.MessageHelper;

public class SearchTitleITCase extends AbstractResourceSearchITCase {

    @Test
    @Rollback
    public void testFacetPivotStats() throws SearchException, SearchIndexException, IOException, ParseException {
        SearchResult<Resource> result = new SearchResult<>();
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        SearchParameters sp = new SearchParameters();
        ArrayList<String> lst = new ArrayList<>();
        lst.add("312");
        lst.add("313");
        sp.getApprovedSiteTypeIdLists().add(lst);
        asqo.getSearchParameters().add(sp);
        LuceneSearchResultHandler<Resource> handler = resourceSearchService.buildAdvancedSearch(asqo, null, result, MessageHelper.getInstance());
        logger.debug("title: {}", handler.getSearchTitle());
        logger.debug("phrase: {}", asqo.getSearchPhrase());
        assertEquals(handler.getSearchTitle(), asqo.getSearchPhrase());
        logger.debug("descr: {}", handler.getSearchDescription());
    }

}

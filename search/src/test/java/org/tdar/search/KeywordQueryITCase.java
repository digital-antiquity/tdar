package org.tdar.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.builder.KeywordQueryBuilder;
import org.tdar.search.service.KeywordSearchService;
import org.tdar.search.service.SearchIndexService;
import org.tdar.search.service.SearchService;
import org.tdar.utils.MessageHelper;

public class KeywordQueryITCase extends AbstractWithIndexIntegrationTestCase{

    
    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    SearchService searchService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Autowired
    KeywordSearchService keywordSearchService;

    public void reindex() {
        searchIndexService.purgeAll(Arrays.asList(Keyword.class));
        searchIndexService.indexAll(getAdminUser(), GeographicKeyword.class, CultureKeyword.class, SiteNameKeyword.class, SiteTypeKeyword.class);
    };
    

    @Test
    public void testKeywordLookup() throws SolrServerException, IOException, ParseException {
        
        SearchResult result = processSearch("Folsom","CultureKeyword",2);
        List<Indexable> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }


    private SearchResult processSearch(String term, String type, int min) throws ParseException, SolrServerException, IOException {
        KeywordQueryBuilder findKeyword = keywordSearchService.findKeyword(term, type, MessageHelper.getInstance(), min);
        SearchResult result = new SearchResult();
        searchService.handleSearch(findKeyword, result, MessageHelper.getInstance());
        return result;
    }

    @Test
    public void testSiteNameKeywordLookup() throws SolrServerException, IOException, ParseException {
        SiteNameKeyword keyword = new SiteNameKeyword();
        keyword.setLabel("18-ST-389");
        genericService.saveOrUpdate(keyword);
        Long id = keyword.getId();
        searchIndexService.indexAll(getAdminUser(), SiteNameKeyword.class);

        SearchResult result = processSearch("18-ST-389","SiteNameKeyword",2);
        List<Indexable> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);

        result = processSearch("18ST389","SiteNameKeyword",2);
        resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);

    }

}

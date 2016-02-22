package org.tdar.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.KeywordSearchService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

import net.fortuna.ical4j.model.property.Geo;

public class KeywordQueryITCase extends AbstractWithIndexIntegrationTestCase{

    
    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Autowired
    KeywordSearchService<Keyword> keywordSearchService;

    public void reindex() {
        searchIndexService.purgeAll(LookupSource.KEYWORD);
        searchIndexService.indexAll(getAdminUser(), LookupSource.KEYWORD);
    };
    
    
    @Test
    public void testMultiWordKeyword() throws ParseException, SolrServerException, IOException {
        GeographicKeyword kwd = new GeographicKeyword();
        kwd.setLabel("Pima County");
        kwd.setStatus(Status.ACTIVE);
        genericService.save(kwd);
        searchIndexService.index(kwd);
        logger.debug("ID:{}", kwd.getId());
        SearchResult<Keyword> result = processSearch("Pima County","GeographicKeyword",2);
        List<Keyword> resources = result.getResults();
        logger.debug("{}", PersistableUtils.extractIds(resources));
        assertTrue("at least one keyword", resources.size() > 0);
        
        result = processSearch("Pima","GeographicKeyword",2);
        resources = result.getResults();
        logger.debug("{}", PersistableUtils.extractIds(resources));
        assertTrue("at least one keyword", resources.size() > 0);

        result = processSearch("Pima Co","GeographicKeyword",2);
        resources = result.getResults();
        logger.debug("{}", PersistableUtils.extractIds(resources));
        assertTrue("at least one keyword", resources.size() > 0);

    }

    @Test
    public void testKeywordLookup() throws SolrServerException, IOException, ParseException {
        
        SearchResult<Keyword> result = processSearch("Folsom","CultureKeyword",2);
        List<Keyword> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }


    private SearchResult<Keyword> processSearch(String term, String type, int min) throws ParseException, SolrServerException, IOException {
        SearchResult<Keyword> result = new SearchResult<>();
        keywordSearchService.findKeyword(term, type,result, MessageHelper.getInstance(), min);
        return result;
    }

    @Test
    public void testSiteNameKeywordLookup() throws SolrServerException, IOException, ParseException {
        SiteNameKeyword keyword = new SiteNameKeyword();
        keyword.setLabel("18-ST-389");
        genericService.saveOrUpdate(keyword);
        searchIndexService.indexAll(getAdminUser(), LookupSource.KEYWORD);

        SearchResult<Keyword> result = processSearch("18-ST-389","SiteNameKeyword",2);
        List<Keyword> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);

        result = processSearch("18ST389","SiteNameKeyword",2);
        resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);

    }

}

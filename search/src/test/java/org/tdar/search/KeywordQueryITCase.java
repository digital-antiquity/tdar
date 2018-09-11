package org.tdar.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.KeywordSearchService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

public class KeywordQueryITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Autowired
    KeywordSearchService<Keyword> keywordSearchService;

    public void reindex() {
        searchIndexService.purgeAll(LookupSource.KEYWORD);
        searchIndexService.indexAll(new QuietIndexReciever(), Arrays.asList(LookupSource.KEYWORD), getAdminUser());
    };

    @Test
    public void testMultiWordKeyword() throws SearchException, SearchIndexException, IOException {
        GeographicKeyword kwd = new GeographicKeyword();
        kwd.setLabel("Pima County");
        kwd.setStatus(Status.ACTIVE);
        genericService.save(kwd);
        searchIndexService.index(kwd);
        logger.debug("ID:{}", kwd.getId());
        SearchResult<Keyword> result = processSearch("Pima County", "GeographicKeyword", 2);
        List<Keyword> resources = result.getResults();
        logger.debug("{}", PersistableUtils.extractIds(resources));
        assertTrue("at least one keyword", resources.size() > 0);

        result = processSearch("Pima", "GeographicKeyword", 2);
        resources = result.getResults();
        logger.debug("{}", PersistableUtils.extractIds(resources));
        assertTrue("at least one keyword", resources.size() > 0);

        result = processSearch("Pima Co", "GeographicKeyword", 2);
        resources = result.getResults();
        logger.debug("{}", PersistableUtils.extractIds(resources));
        assertTrue("at least one keyword", resources.size() > 0);

    }

    @Test
    public void testKeywordLookup() throws SearchException, SearchIndexException, IOException {

        SearchResult<Keyword> result = processSearch("Folsom", "CultureKeyword", 2);
        List<Keyword> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    private SearchResult<Keyword> processSearch(String term, String type, int min) throws SearchException, SearchIndexException, IOException {
        SearchResult<Keyword> result = new SearchResult<>();
        keywordSearchService.findKeyword(term, type, result, MessageHelper.getInstance(), min);
        return result;
    }

    @Test
    public void testSiteNameKeywordLookup() throws SearchException, SearchIndexException, IOException {
        SiteNameKeyword keyword = new SiteNameKeyword();
        keyword.setLabel("18-ST-389");
        genericService.saveOrUpdate(keyword);
        searchIndexService.indexAll(new QuietIndexReciever(), Arrays.asList(LookupSource.KEYWORD), getAdminUser());

        SearchResult<Keyword> result = processSearch("18-ST-389", "SiteNameKeyword", 2);
        List<Keyword> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);

        result = processSearch("18ST389", "SiteNameKeyword", 2);
        resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);

    }

}

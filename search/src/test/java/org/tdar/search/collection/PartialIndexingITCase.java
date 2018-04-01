package org.tdar.search.collection;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.AbstractResourceSearchITCase;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.SearchUtils;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;

public class PartialIndexingITCase extends AbstractResourceSearchITCase {

    @Autowired
    private SolrClient template;

    @Autowired
    ResourceSearchService resourceSearchService;

    @Test
    @Rollback
    public void testPartialIndexing() throws SolrServerException, IOException, ParseException, SearchException, SearchIndexException {
        Dataset ds = createAndSaveNewDataset();
        CultureKeyword hittite = new CultureKeyword("hittite");
        ds.getActiveCultureKeywords().add(hittite);
        genericService.saveOrUpdate(ds.getActiveCultureKeywords());
        ResourceCollection rc = createAndSaveNewResourceCollection("turkey");
        rc.setHidden(false);
        rc.getManagedResources().add(ds);
        ds.getManagedResourceCollections().add(rc);
        searchIndexService.index(ds);
        searchIndexService.index(rc);
        SolrDocument byId = template.getById(SearchUtils.createKey(ds));
        logger.debug("{}", byId);
        SearchResult<Resource> result = new SearchResult<>();
        resourceSearchService.buildKeywordQuery(hittite, KeywordType.CULTURE_KEYWORD, new ReservedSearchParameters(), result, MessageHelper.getInstance(),
                getAdminUser());
        logger.debug("{}", result.getResults());
        assertEquals(1, result.getResults().size());
        searchIndexService.partialIndexAllResourcesInCollectionSubTree(rc);
        result = new SearchResult<>();

        resourceSearchService.buildKeywordQuery(hittite, KeywordType.CULTURE_KEYWORD, new ReservedSearchParameters(), result, MessageHelper.getInstance(),
                getAdminUser());
        logger.debug("{}", result.getResults());
        assertEquals(1, result.getResults().size());
        logger.debug("{}", template.getById(SearchUtils.createKey(ds)));

    }
}

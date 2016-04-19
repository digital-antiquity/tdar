package org.tdar.search.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceAnnotationType;
import org.tdar.core.service.GenericService;
import org.tdar.search.QuietIndexReciever;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.ResourceAnnotationKeySearchService;
import org.tdar.utils.MessageHelper;

public class ResourceAnnotationKeySearchITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    GenericService genericService;
    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    ResourceAnnotationKeySearchService resourceAnnotationKeySearchService;

    @Override
    public void reindex() {
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE_ANNOTATION_KEY), getAdminUser());
    }
    @Test
    public void testAllSearch() throws ParseException, SolrServerException, IOException {
        SearchResult<ResourceAnnotationKey> result = new SearchResult<>();
        resourceAnnotationKeySearchService.buildAnnotationSearch(null, result, 2, MessageHelper.getInstance());
        assertTrue(result.getTotalRecords() > 0);
    }
    
    @Test
    public void testKeySearch() throws ParseException, SolrServerException, IOException {
        ResourceAnnotationKey key = new ResourceAnnotationKey();
        key.setKey("ISSN");
        key.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key);
        ResourceAnnotationKey key2 = new ResourceAnnotationKey();
        key2.setKey("ISBN");
        key2.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key2);

        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE_ANNOTATION_KEY), getAdminUser());
        SearchResult<ResourceAnnotationKey> result = new SearchResult<>();
        resourceAnnotationKeySearchService.buildAnnotationSearch("IS", result, 2, MessageHelper.getInstance());
        List<ResourceAnnotationKey> resources = result.getResults();
        assertTrue("at least one document", resources.size() == 2);

        // FIXME: not properly simulating new page request
        result = new SearchResult<>();
        resourceAnnotationKeySearchService.buildAnnotationSearch("ZZ", result, 2, MessageHelper.getInstance());
        resources = result.getResults();
        assertEquals("ZZ should return no results", 0, resources.size());

    }
}

package org.tdar.search.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;

public class NestedObjectIdexingITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    SearchIndexService searchIndexService;
    
    @Autowired
    ResourceSearchService resourceSearchService;
    
    @Autowired
    GenericService genericService;
    
    @Override
    public void reindex() {
    }
    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    @Ignore("not really a test, but trying to use to bind save of collections...")
    public void testFlush() {
        SharedCollection collection = createAndSaveNewResourceCollection(SPITAL_DB_NAME);
        Dataset dc = createAndSaveNewDataset();
        for (int i=0;i < 10; i++) {
        	Image image = createAndSaveNewInformationResource(Image.class);
        	image.setTitle(i + ":"+ image.getTitle() );
        	collection.getResources().add(image);
        	image.getSharedCollections().add(collection);
        	genericService.saveOrUpdate(image);
        	genericService.saveOrUpdate(collection);
        }
        genericService.synchronize();
        logger.debug("===================");
        collection.getResources().add(dc);
        dc.getSharedCollections().add(collection);
        genericService.synchronize();
        logger.debug("===================");

    }
    
    @SuppressWarnings({ "deprecation", "unchecked" })
    @Test
    @Ignore
    @Rollback(true)
    public void testIndexing() throws SolrServerException, IOException, ParseException {
//    	sessionFactory.getCurrentSession().
        SharedCollection collection = createAndSaveNewResourceCollection(SPITAL_DB_NAME);
        Image image = createAndSaveNewInformationResource(Image.class);
        genericService.synchronize();
        logger.debug("===================");
        collection.getResources().add(image);
        image.getSharedCollections().add(collection);
        logger.debug("{}", image);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(image);
        genericService.synchronize();
        searchIndexService.index(collection, image);
        SearchResult<Resource> result = new SearchResult<>();
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        SearchParameters params = new SearchParameters();
//        params.getCollections().add(collection);
        params.getResourceIds().add(image.getId());
        asqo.getSearchParameters().add(params);
        resourceSearchService.buildAdvancedSearch(asqo, getAdminUser(), result, MessageHelper.getInstance());
        assertTrue(result.getResults().contains(image));

        result = new SearchResult<>();
        asqo = new AdvancedSearchQueryObject();
        params = new SearchParameters();
        params.getShares().add(collection);
        asqo.getSearchParameters().add(params);
        resourceSearchService.buildAdvancedSearch(asqo, getAdminUser(), result, MessageHelper.getInstance());
        assertTrue(result.getResults().contains(image));

        collection.getResources().remove(image);
        image.getSharedCollections().remove(collection);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(image);
        genericService.synchronize();

        result = new SearchResult<>();
        asqo = new AdvancedSearchQueryObject();
        params = new SearchParameters();
        params.getShares().add(collection);
        asqo.getSearchParameters().add(params);
        resourceSearchService.buildAdvancedSearch(asqo, getAdminUser(), result, MessageHelper.getInstance());
        assertFalse(result.getResults().contains(image));

    }
}

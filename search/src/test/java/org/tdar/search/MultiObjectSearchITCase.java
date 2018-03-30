package org.tdar.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;

public class MultiObjectSearchITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    ResourceSearchService resourceSearchService;
    private DataIntegrationWorkflow workflow;
    private Dataset dataset;
    private ResourceCollection collection;

    @Before
    public void setup() {
        dataset = createAndSaveNewDataset();
        collection = createAndSaveNewResourceCollection("test collection");
        workflow = new DataIntegrationWorkflow("test integration", false, getAdminUser());
        workflow.markUpdated(getAdminUser());
        genericService.saveOrUpdate(workflow);
    }

    @Test
    @Rollback
    public void testAllActive() throws SearchException, SearchIndexException, IOException {
        dataset.setStatus(Status.ACTIVE);
        collection.setStatus(Status.ACTIVE);
        collection.setHidden(false);
        genericService.saveOrUpdate(workflow);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(dataset);
        searchIndexService.index(workflow, collection, dataset);
        SearchResult<Resource> result = performSearch("", null, 100);
        logger.debug("results:{}", result.getResults());
        assertTrue("should see collection", result.getResults().contains(collection));
        assertTrue("should see dataset", result.getResults().contains(dataset));
        assertTrue("should see integration", result.getResults().contains(workflow));
    }

    @Test
    @Rollback
    public void testAllHidden() throws SearchException, SearchIndexException, IOException {

        /// change statuses to hidden
        dataset.setStatus(Status.DRAFT);
        workflow.setHidden(true);
        collection.setHidden(true);
        genericService.saveOrUpdate(workflow);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(dataset);
        searchIndexService.index(workflow, collection, dataset);
        SearchResult<Resource> result = performSearch("", null, 100);
        logger.debug("results:{}", result.getResults());
        assertFalse("should see collection", result.getResults().contains(collection));
        assertFalse("should see dataset", result.getResults().contains(dataset));
        assertFalse("should see integration", result.getResults().contains(workflow));
    }

    @Test
    @Rollback
    public void testCollectionDraft() throws SearchException, SearchIndexException, IOException {
        /// change collection to draft, but visible
        collection.setHidden(false);
        collection.setStatus(Status.DRAFT);
        genericService.save(collection);
        searchIndexService.index(workflow, collection, dataset);
        SearchResult<Resource> result = performSearch("", null, 100);
        logger.debug("results:{}", result.getResults());
        assertFalse("should see collection", result.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testCollectionDraftAndHidden() throws SearchException, SearchIndexException, IOException {
        /// change collection to draft, but visible
        collection.setHidden(true);
        collection.setStatus(Status.DRAFT);
        genericService.save(collection);
        searchIndexService.index(workflow, collection, dataset);
        SearchResult<Resource> result = performSearch("", null, 100);
        logger.debug("results:{}", result.getResults());
        assertFalse("should see collection", result.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testCollectionDeleted() throws SearchException, SearchIndexException, IOException {
        /// change collection to draft, but visible
        collection.setHidden(false);
        collection.setStatus(Status.DELETED);
        genericService.save(collection);
        searchIndexService.index(workflow, collection, dataset);
        SearchResult<Resource> result = performSearch("", null, 100);
        logger.debug("results:{}", result.getResults());
        assertFalse("should see collection", result.getResults().contains(collection));
    }

    public SearchResult<Resource> performSearch(String term, TdarUser user, int max) throws IOException, SearchException, SearchIndexException {
        SearchResult<Resource> result = new SearchResult<>(max);
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        asqo.setMultiCore(true);
        asqo.setQuery(term);
        resourceSearchService.buildAdvancedSearch(asqo, user, result, MessageHelper.getInstance());
        // (TdarUser user, ResourceLookupObject look, LuceneSearchResultHandler<Resource> result,
        // TextProvider support) throws SearchException, IOE
        return result;
    }

}

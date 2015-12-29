package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.index.SearchIndexService;

public class AuthenticatedLuceneSearchControllerITCase extends AbstractSearchControllerITCase {

    protected static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    protected static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;
    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    GenericKeywordService genericKeywordService;

    @Before
    @Override
    public void reset() {
        reindex();
        controller = generateNewInitializedController(AdvancedSearchController.class, getUser());
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback(true)
    public void testForInheritedCulturalInformationFromProject1() {
        logger.info("{}", getUser());
        Long imgId = setupImage();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        setResourceTypes(getInheritingTypes());
        List<String> approvedCultureKeywordIds = new ArrayList<String>();
        approvedCultureKeywordIds.add("9");
        setStatuses(Status.DRAFT);
        firstGroup().getApprovedCultureKeywordIdLists().add(approvedCultureKeywordIds);
        doSearch("");
        assertTrue("'Archaic' defined in parent project should be found in information resource", resultsContainId(imgId));
        // fail("Um, actually this test doesn't do anything close to what it says it's doing");
    }

    @Test
    @Rollback(true)
    public void testDeletedMaterialsAreIndexed() {
        controller = generateNewInitializedController(AdvancedSearchController.class, getAdminUser());
        controller.setRecordsPerPage(50);
        Long datasetId = setupDataset();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        setResourceTypes(allResourceTypes);
        setStatuses(Status.DELETED);
        doSearch("precambrian");
        assertTrue(resultsContainId(datasetId));
    }

    @Test
    @Rollback(true)
    public void testDeletedMaterialsAreNotVisible() {
        Long datasetId = setupDataset();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        setResourceTypes(allResourceTypes);
        setStatuses(Status.DELETED);
        setIgnoreActionErrors(true);
        doSearch("precambrian", true);
        assertFalse(resultsContainId(datasetId));
        assertEquals(1, controller.getActionErrors().size());
    }

    @Test
    @Rollback(true)
    public void testDeletedMaterialsAreIndexedButYouCantSee() throws SolrServerException, IOException {
        controller = generateNewInitializedController(AdvancedSearchController.class, getBasicUser());
        setIgnoreActionErrors(true);
        controller.setRecordsPerPage(50);
        Long datasetId = setupDataset();
        searchIndexService.index(genericService.find(Dataset.class, datasetId));
        setResourceTypes(allResourceTypes);
        setStatuses(Status.DELETED);
        doSearch("precambrian", true);
        assertTrue(controller.getActionErrors().size() > 0);
    }

    @Test
    @Rollback(true)
    public void testDraftMaterialsAreIndexed() {
        Long imgId = setupImage();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        setResourceTypes(allResourceTypes);
        setStatuses(Status.DRAFT);
        doSearch("description");
        assertTrue(resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testHierarchicalCultureKeywordsAreIndexed() {
        Long imgId = setupImage();
        logger.info("Created new image: " + imgId);
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        setResourceTypes(allResourceTypes);
        setStatusAll();
        doSearch("PaleoIndian");
        assertTrue(resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testFindAllSearchPhrase() throws ParseException, SolrServerException, IOException {
        doSearch("");
        logger.debug(controller.getSearchDescription());
        logger.debug(controller.getSearchPhrase());
        logger.debug(controller.getSearchSubtitle());
       assertTrue(controller.getSearchPhrase().contains("All"));
    }

    @Test
    @Rollback
    // searching for an specific tdar id should ignore all other filters
    public void testTdarIdSearchOverride() throws Exception {
        Document document = createAndSaveNewInformationResource(Document.class);
        Long expectedId = document.getId();
        assertTrue(expectedId > 0);
        reindex();

        // specify some filters that would normally filter-out the document we just created.
        firstGroup().getTitles().add("thistitleshouldprettymuchfilteroutanyandallresources");
        firstGroup().setOperator(Operator.OR);
        firstGroup().getResourceIds().add(expectedId);
        doSearch("");
        assertEquals("expecting only one result", 1, controller.getResults().size());
        Indexable resource = controller.getResults().iterator().next();
        assertEquals(expectedId, resource.getId());
    }
}

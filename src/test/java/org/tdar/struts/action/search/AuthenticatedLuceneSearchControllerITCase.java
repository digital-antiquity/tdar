package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.action.TdarActionSupport;

public class AuthenticatedLuceneSearchControllerITCase extends AbstractSearchControllerITCase {

    protected static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    protected static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;
    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    public TdarActionSupport getController() {
        return controller;
    }

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    GenericKeywordService genericKeywordService;

    @Before
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
        searchIndexService.indexAll(getAdminUser(), Resource.class);
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
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        setResourceTypes(allResourceTypes);
        setStatuses(Status.DELETED);
        doSearch("precambrian");
        assertTrue(resultsContainId(datasetId));
    }

    @Test
    @Rollback(true)
    public void testDeletedMaterialsAreNotVisible() {
        Long datasetId = setupDataset();
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        setResourceTypes(allResourceTypes);
        setStatuses(Status.DELETED);
        doSearch("precambrian", true);
        assertFalse(resultsContainId(datasetId));
        setIgnoreActionErrors(true);
        assertEquals(1, controller.getActionErrors().size());
    }

    @Test
    @Rollback(true)
    public void testDeletedMaterialsAreIndexedButYouCantSee() {
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
        searchIndexService.indexAll(getAdminUser(), Resource.class);
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
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        setResourceTypes(allResourceTypes);
        setStatusAll();
        doSearch("PaleoIndian");
        assertTrue(resultsContainId(imgId));
    }

}

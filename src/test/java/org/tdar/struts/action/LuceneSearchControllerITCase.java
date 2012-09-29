package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.tdar.core.bean.resource.ResourceType.DOCUMENT;
import static org.tdar.core.bean.resource.ResourceType.IMAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.TestConstants;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.CultureKeywordService;
import org.tdar.core.service.InvestigationTypeService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SiteTypeKeywordService;

@Transactional
public class LuceneSearchControllerITCase extends AbstractSearchControllerITCase {

    private static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    private static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;
    private static final String MESSAGE_RESULTS_NO_HITS = "No records match the query";

    // investigations types that we know are being used by resources in the test db
    private static final Long[] KNOWN_INVESTIGATION_TYPES = { 1L, 2L, 5L, 6L, 7L, 9L, 10L, 12L };

    private static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    public TdarActionSupport getController() {
        return controller;
    }

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    CultureKeywordService cultureKeywordService;
    @Autowired
    SiteTypeKeywordService siteTypeKeywordService;
    @Autowired
    InvestigationTypeService investigationTypeService;

    @Before
    public void reset() {
        reindex();
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback(true)
    public void testFindAllSearchPhrase() {
        doSearch("");
        assertEquals(controller.getSearchPhrase(), LuceneSearchController.SEARCHING_ALL_RESOURCE_TYPES);
        assertEquals(controller.getSearchSubtitle(), LuceneSearchController.ALL_RECORDS);
    }

    @Test
    @Rollback(true)
    public void testFindResourceTypePhrase() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));
        doSearch("");
        assertTrue(controller.getSearchPhrase().contains(LuceneSearchController.SELECTED_RESOURCE_TYPES));
        assertTrue(controller.getSearchPhrase().contains(DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(IMAGE.getLabel()));
        assertEquals(controller.getSearchSubtitle(), LuceneSearchController.ALL_RECORDS);
    }

    @Test
    @Rollback(true)
    public void testFindResourceById() {
        controller.setId(3074);
        controller.setResourceTypes(Arrays.asList(ResourceType.DATASET));
        doSearch("");
        assertTrue(resultsContainId(3074l));
        for (Resource r : controller.getResources()) {
            logger.info(r);
        }

        controller.setId(TestConstants.ADMIN_PROJECT_ID);
        controller.setResourceTypes(Arrays.asList(ResourceType.PROJECT));
        doSearch("");
        for (Resource r : controller.getResources()) {
            logger.info(r);
        }
        assertTrue(resultsContainId((long) TestConstants.ADMIN_PROJECT_ID));
        assertEquals(controller.getResources().size(), 1);

    }

    @Test
    @Rollback(true)
    public void testFindTerm() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));

        doSearch("test");
        assertTrue(controller.getSearchPhrase().contains(LuceneSearchController.SELECTED_RESOURCE_TYPES));
        logger.info(controller.getSearchPhrase());
        assertTrue(controller.getSearchPhrase().contains(DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(IMAGE.getLabel()));
        assertTrue(controller.getSearchPhrase().contains("test"));
        assertEquals(controller.getSearchSubtitle(), "test");
    }

    @Test
    @Rollback(true)
    public void testCultureKeywordSearch() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));
        CultureKeyword label = cultureKeywordService.findByLabel("Folsom");
        CultureKeyword label2 = cultureKeywordService.findByLabel("Early Archaic");
        controller.setApprovedCultureKeywordIds(Arrays.asList(label.getId(), label2.getId()));
        doSearch("test");
        assertTrue(controller.getSearchPhrase().contains(LuceneSearchController.SELECTED_RESOURCE_TYPES + " "));
        assertTrue(controller.getSearchPhrase().contains(" " + DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(" " + IMAGE.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(" Folsom"));
        assertTrue(controller.getSearchPhrase().contains(" Early Archaic"));
        assertTrue(controller.getSearchPhrase().contains(" test"));
        assertEquals(controller.getSearchSubtitle(), "test");
    }

    @Test
    public void abcd() {
        List<Long> ids = new ArrayList<Long>();
        ids.add(19l);
        ids.add(39l);
        ids.add(40l);
        ids.add(8l);
        ids.add(25l);
        controller.setUncontrolledSiteTypeKeywords(Arrays.asList(""));
        controller.setApprovedCultureKeywordIds(ids);
        doSearch("Archeological Survey and Architectural Study of Montezuma Castle National Monument");
        assertTrue(!controller.getResources().isEmpty());

    }

    @Test
    @Rollback(true)
    public void testBadDateSearch() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));
        controller.setYearType("none");
        doSearch("test");
        assertTrue(controller.getSearchPhrase().contains(LuceneSearchController.SELECTED_RESOURCE_TYPES + " "));
        assertTrue(controller.getSearchPhrase().contains(" " + DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(" " + IMAGE.getLabel()));
        assertFalse(controller.getSearchPhrase().contains("null"));
        assertFalse(controller.getSearchPhrase().contains(" between "));
        assertEquals(controller.getSearchSubtitle(), "test");
    }

    @Test
    @Rollback(true)
    public void testCalDateSearch() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));
        controller.setYearType(CoverageType.CALENDAR_DATE.name());
        controller.setFromYear(-1000);
        controller.setToYear(1200);
        doSearch("test");
        assertTrue(controller.getSearchPhrase().contains(LuceneSearchController.SELECTED_RESOURCE_TYPES));
        assertTrue(controller.getSearchPhrase().contains(DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(IMAGE.getLabel()));
        assertFalse(controller.getSearchPhrase().contains("null"));
        assertTrue(controller.getSearchPhrase().contains(" between "));
        assertTrue(controller.getSearchPhrase().contains(" 1000 BC"));
        assertTrue(controller.getSearchPhrase().contains(" 1200 AD"));
        assertEquals(controller.getSearchSubtitle(), "test");
    }

    @Test
    @Rollback(true)
    public void testSpatialSearch() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));
        controller.setMaxx(1d);
        controller.setMinx(-1d);
        controller.setMaxy(1d);
        controller.setMiny(-1d);
        doSearch("test");
        assertTrue(controller.getSearchPhrase().contains(LuceneSearchController.SELECTED_RESOURCE_TYPES + " "));
        assertTrue(controller.getSearchPhrase().contains(DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(IMAGE.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(" " + LuceneSearchController.WITHIN_MAP_CONSTRAINTS));
    }

    @Test
    @Rollback(true)
    public void testForInheritedCulturalInformationFromProject() {
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(getInheritingTypes());
        doSearch("Archaic");
        assertTrue("'Archaic' defined inparent project should be found in information resource", resultsContainId(DOCUMENT_INHERITING_CULTURE_ID));
        assertFalse("A child document that inherits nothing from parent project should not appear in results", resultsContainId(DOCUMENT_INHERITING_NOTHING_ID));
    }

    @Test
    @Rollback(true)
    public void testForInheritedCulturalInformationFromProject1() {
        Long imgId = setupImage();
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(getInheritingTypes());
        List<Long> approvedCultureKeywordIds = new ArrayList<Long>();
        approvedCultureKeywordIds.add(9l);
        setStatuses(Status.DRAFT);
        controller.setApprovedCultureKeywordIds(approvedCultureKeywordIds);
        doSearch("");
        assertTrue("'Archaic' defined in parent project should be found in information resource", resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testDeletedOrDraftMaterialsAreHiddenInDefaultSearch() {
        Long imgId = setupImage();
        Long datasetId = setupDataset();
        Long codingSheetId = setupCodingSheet();

        logger.info("imgId:" + imgId + " datasetId:" + datasetId + " codingSheetId:" + codingSheetId);
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        doSearch("precambrian");
        assertFalse(resultsContainId(datasetId));
        assertTrue(resultsContainId(codingSheetId));
        assertFalse(resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testDeletedMaterialsAreIndexed() {
        Long datasetId = setupDataset();
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        setStatuses(Status.DELETED);
        doSearch("precambrian");
        assertTrue(resultsContainId(datasetId));
    }

    @Test
    @Rollback(true)
    public void testDraftMaterialsAreIndexed() {
        Long imgId = setupImage();
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        setStatuses(Status.DRAFT);
        doSearch("description");
        assertTrue(resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testHierarchicalCultureKeywordsAreIndexed() {
        Long imgId = setupImage();
        logger.info("Created new image: " + imgId);
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        setStatusAll();
        doSearch("PaleoIndian");
        assertTrue(resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testPeopleAndInstitutionsInSearchResults() {
        Long imgId = setupDataset();
        logger.info("Created new image: " + imgId);
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        setStatusAll();
        doSearch(("testabc"));
        assertTrue("expected to find person in keyword style search of firstname", resultsContainId(imgId));
        doSearch("\"" + TestConstants.DEFAULT_FIRST_NAME + "abc " + TestConstants.DEFAULT_LAST_NAME + "abc\"");
        assertTrue("expected to find person in phrase style search of full name", resultsContainId(imgId));

        doSearch("university");
        assertTrue("institutional author expected to find in search", resultsContainId(imgId));
    }
    
    @Test
    @Rollback(true)
    //try a search that will fail the strict parsing pass, but work under lenient parsing.
    public void testLenientParsing() {
        String term = "a term w/ unclosed \" quote and at least one token that will return results: " + TestConstants.DEFAULT_LAST_NAME;
        doSearch(term);
        assertEquals(0, controller.getActionErrors().size());
    }
    
    @Test
    @Rollback(true)
    public void testStrictParsingPass() {
        //perform a quoted search e.g. '"test test"', which should return less results than 'test test'.
 
        String term = "test";
        String quotedTerm = "\"test test\"";
        String unquotedTerm = "test test";
        doSearch(quotedTerm);
        int quotedResultCount = controller.getResources().size();
        controller = generateNewInitializedController(LuceneSearchController.class);
        doSearch(term);
        int resultCount = controller.getResources().size();
        controller = generateNewInitializedController(LuceneSearchController.class);
        doSearch(term);
        int unquotedResultCount = controller.getResources().size();
       
        assertTrue("unquoted search should have at least one result", resultCount >= 1);
        assertTrue("quoted search should have less results than unquoted search", quotedResultCount < resultCount);
        assertEquals("unquoted repeated term should have same resultcount as single term", resultCount,  unquotedResultCount);
    }

    @Test
    @Rollback(true)
    public void testDatedSearch() {
        Long docId = setupDatedDocument();
        logger.info("Created new document: " + docId);
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        controller.setYearType(CoverageType.CALENDAR_DATE.name());

        // test inner range
        controller.setFromYear(-900);
        controller.setToYear(1000);
        doSearch("");
        assertTrue("expected to find document for inner range match", resultsContainId(docId));

        // test overlapping range lower
        controller.setFromYear(-2000);
        controller.setToYear(-1);
        doSearch("");
        assertTrue("expected to find document for overlapping range (lower)", resultsContainId(docId));

        // test overlapping range upper
        controller.setFromYear(1999);
        controller.setToYear(2009);
        doSearch("");
        assertTrue("expected to find document for overlapping range (upper)", resultsContainId(docId));

        // test invalid range
        controller.setFromYear(-7000);
        controller.setToYear(-1001);
        doSearch("");
        assertFalse("expected not to find document in invalid range", resultsContainId(docId));

        // test exact range (query inclusive)
        controller.setFromYear(-1000);
        controller.setToYear(2000);
        doSearch("");
        assertTrue("expected to find document for exact range match", resultsContainId(docId));
    }

    @Test
    @Rollback
    public void testInvestigationTypes() {

        // TODO:dynamically get the list of 'used investigation types' and the resources that use them
        addInvestigationTypes();
        controller.setResourceTypes(allResourceTypes);
        controller.setIncludedStatuses(Arrays.asList(Status.ACTIVE, Status.DELETED, Status.DRAFT, Status.FLAGGED));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResources().isEmpty());
        assertTrue("expected to find document that uses known investigation types", resultsContainId(2420L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(1628L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(3805L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(3738L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(4287L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(262L));
    }

    public void testGeographicBounds() {

    }

    private void addInvestigationTypes() {
        // controller.getInvestigationTypeIds().add(Arrays.<Long>asList(KNOWN_INVESTIGATION_TYPES));

        // screw it, add EVERY investigation type. why won't you work!!?
        controller.getInvestigationTypeIds().add(1L);
        controller.getInvestigationTypeIds().add(2L);
        controller.getInvestigationTypeIds().add(3L);
        controller.getInvestigationTypeIds().add(4L);
        controller.getInvestigationTypeIds().add(5L);
        controller.getInvestigationTypeIds().add(6L);
        controller.getInvestigationTypeIds().add(7L);
        controller.getInvestigationTypeIds().add(8L);
        controller.getInvestigationTypeIds().add(9L);
        controller.getInvestigationTypeIds().add(10L);
        controller.getInvestigationTypeIds().add(11L);
        controller.getInvestigationTypeIds().add(12L);
        controller.getInvestigationTypeIds().add(13L);
        controller.getInvestigationTypeIds().add(14L);
        controller.getInvestigationTypeIds().add(15L);
        controller.getInvestigationTypeIds().add(16L);
        controller.getInvestigationTypeIds().add(17L);
        controller.getInvestigationTypeIds().add(18L);
        controller.getInvestigationTypeIds().add(19L);
        controller.getInvestigationTypeIds().add(20L);
    }

    private void addGeoBounds() {

    }

}

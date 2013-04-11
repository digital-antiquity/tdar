package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.TestConstants;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.junit.TdarAssert;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.DateRange;

@Transactional
public class LuceneSearchControllerITCase extends AbstractSearchControllerITCase {

    protected static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    protected static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;
    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Override
    @Autowired
    public TdarActionSupport getController() {
        return controller;
    }

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    GenericKeywordService genericKeywordService;

    private void setTitle(String title) {
        firstGroup().getTitles().add(title);
    }

    @Override
    @Before
    public void reset() {
        reindex();
        newController();
    }

    private void newController() {
        controller = generateNewInitializedController(AdvancedSearchController.class);
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback(true)
    public void testFindAllSearchPhrase() {
        doSearch("");
        assertEquals(AdvancedSearchController.TITLE_ALL_RECORDS, controller.getSearchSubtitle());
    }

    @Test
    @Rollback(true)
    public void testResourceTypeSearchPhrase() {
        controller.getResourceTypes().add(ResourceType.IMAGE);
        doSearch("");
        for (Resource r : controller.getResults()) {
            assertEquals(ResourceType.IMAGE, r.getResourceType());
        }
    }

    public void setupTestDocuments() throws InstantiationException, IllegalAccessException {
        String[] titles = {
                "Preliminary Archeological Investigation at the Site of a Mid-Nineteenth Century Shop and Yard Complex Associated With the Belvidere and Delaware Railroad, Lambertville, New Jersey",
                "The James Franks Site (41DT97): Excavations at a Mid-Nineteenth Century Farmstead in the South Sulphur River Valley, Cooper Lake Project, Texas",
                "Archeological and Architectural Investigation of Public, Residential, and Hydrological Features at the Mid-Nineteenth Century Quintana Thermal Baths Ponce, Puerto Rico",
                "Final Report On a Phased Archaeological Survey Along the Ohio and Erie Canal Towpath in Cuyahoga Valley NRA, Summit and Cuyahoga Counties, Ohio",
                "Archeological Investigation at the Lock 33 Complex, Chesapeake and Ohio Canal",
                "Arthur Patterson Site, a Mid-Nineteenth Century Site, San Jacinto County" };
        for (String title : titles) {
            Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), title);
            searchIndexService.index(document);
        }

    }

    @Test
    @Rollback(true)
    public void testExactTitleMatchInKeywordSearch() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "Archeological Excavation at Site 33-Cu-314: A Mid-Nineteenth Century Structure on the Ohio and Erie Canal";
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        searchIndexService.index(document);
        setupTestDocuments();
        doSearch(resourceTitle);
        logger.info("results:{}", controller.getResults());
        assertTrue(controller.getResults().contains(document));
        assertTrue(controller.getResults().get(0).equals(document) || controller.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testHyphenatedSearchBasic() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "33-Cu-314";
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        searchIndexService.index(document);
        setupTestDocuments();
        doSearch(resourceTitle);
        logger.info("results:{}", controller.getResults());
        assertTrue(controller.getResults().contains(document));
        assertTrue(controller.getResults().get(0).equals(document) || controller.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testHyphenatedTitleSearch() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "33-Cu-314";
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        searchIndexService.index(document);
        setupTestDocuments();
        firstGroup().getTitles().add(resourceTitle);
        doSearch("");
        logger.info("results:{}", controller.getResults());
        assertTrue(controller.getResults().contains(document));
        assertTrue(controller.getResults().get(0).equals(document) || controller.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testUnHyphenatedTitleSearch() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "33-Cu-314";
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        searchIndexService.index(document);
        setupTestDocuments();
        firstGroup().getTitles().add(resourceTitle.replaceAll("\\-", ""));
        doSearch("");
        logger.info("results:{}", controller.getResults());
        assertTrue(controller.getResults().contains(document));
        assertTrue(controller.getResults().get(0).equals(document) || controller.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testHyphenatedSiteNameSearch() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "what fun";
        SiteNameKeyword snk = new SiteNameKeyword();
        String label = "33-Cu-314";
        snk.setLabel(label);
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        genericService.save(snk);
        document.getSiteNameKeywords().add(snk);
        searchIndexService.index(document);
        setupTestDocuments();
        firstGroup().getSiteNames().add(label);
        doSearch("");
        logger.info("results:{}", controller.getResults());
        assertTrue(controller.getResults().contains(document));
        assertTrue(controller.getResults().get(0).equals(document) || controller.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testHyphenatedSiteNameSearchCombined() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "what fun";
        SiteNameKeyword snk = new SiteNameKeyword();
        String label = "33-Cu-314";
        snk.setLabel(label);
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        genericService.save(snk);
        document.getSiteNameKeywords().add(snk);
        searchIndexService.index(document);
        setupTestDocuments();
        doSearch("what fun 33-Cu-314");
        logger.info("results:{}", controller.getResults());
        assertTrue(controller.getResults().contains(document));
        assertTrue(controller.getResults().get(0).equals(document) || controller.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testFindResourceTypePhrase() {
        setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.IMAGE));
        doSearch("");
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        assertTrue(controller.getSearchPhrase().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(ResourceType.IMAGE.getLabel()));
        assertEquals(controller.getSearchSubtitle(), AdvancedSearchController.TITLE_ALL_RECORDS);
    }

    @Test
    @Rollback(true)
    public void testFindResourceById() {
        controller.setId(Long.valueOf(3074));
        doSearch("");
        assertTrue(resultsContainId(3074l));
        for (Indexable r : controller.getResults()) {
            logger.info("{}", r);
        }
    }

    @Test
    @Rollback(true)
    public void testFindTerm() {
        setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.IMAGE));

        doSearch("test");
        logger.info(controller.getSearchPhrase());
        assertTrue(controller.getSearchPhrase().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(ResourceType.IMAGE.getLabel()));
        assertTrue(controller.getSearchPhrase().contains("test"));
        assertEquals(controller.getSearchSubtitle(), "test");
    }

    @Test
    @Rollback(true)
    public void testCultureKeywordSearch() {
        setResourceTypes(ResourceType.DOCUMENT, ResourceType.IMAGE);
        CultureKeyword keyword1 = genericKeywordService.findByLabel(CultureKeyword.class, "Folsom");
        CultureKeyword keyword2 = genericKeywordService.findByLabel(CultureKeyword.class, "Early Archaic");
        firstGroup().getApprovedCultureKeywordIdLists().add(Arrays.asList(keyword1.getId().toString(), keyword2.getId().toString()));
        firstGroup().getAllFields().add("test");
        doSearch("");
        String searchPhrase = controller.getSearchPhrase();
        assertTrue("search phrase shouldn't be blank:", StringUtils.isNotBlank(searchPhrase));
        logger.debug("search phrase: {}", searchPhrase);
        assertTrue(searchPhrase.contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(searchPhrase.contains(ResourceType.IMAGE.getLabel()));
        assertTrue(searchPhrase.contains(keyword1.getLabel()));
        assertTrue(searchPhrase.contains(keyword2.getLabel()));
        assertTrue(searchPhrase.contains("test"));
    }

    @Test
    @Rollback(true)
    public void testBadDateSearch() {
        setResourceTypes(ResourceType.DOCUMENT, ResourceType.IMAGE);
        CoverageDate cd = new CoverageDate(CoverageType.NONE);
        firstGroup().getCoverageDates().add(cd);
        firstGroup().getAllFields().add("test");
        doSearch("");
        assertTrue(controller.getSearchPhrase().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(ResourceType.IMAGE.getLabel()));
        assertFalse(controller.getSearchPhrase().contains("null"));
        assertFalse(controller.getSearchPhrase().contains(" TO "));
    }

    @Test
    @Rollback(true)
    public void testCalDateSearchPhrase() {
        setResourceTypes(ResourceType.DOCUMENT, ResourceType.IMAGE);
        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 1200);
        firstGroup().getCoverageDates().add(cd);
        firstGroup().getAllFields().add("test");
        doSearch("");
        logger.debug(controller.getSearchPhrase());

        assertTrue(controller.getSearchPhrase().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(ResourceType.IMAGE.getLabel()));
        assertFalse(controller.getSearchPhrase().contains("null"));
        assertTrue(controller.getSearchPhrase().contains("1000"));
        assertTrue(controller.getSearchPhrase().contains("1200"));
        assertTrue(controller.getSearchPhrase().contains(CoverageType.CALENDAR_DATE.getLabel()));
        TdarAssert.assertMatches(controller.getSearchPhrase(), ".+?" + "\\:.+? \\- .+?");
    }

    @Test
    @Rollback(true)
    public void testSpatialSearch() {
        setResourceTypes(ResourceType.DOCUMENT, ResourceType.IMAGE);
        LatitudeLongitudeBox box = new LatitudeLongitudeBox(-1d, -1d, 1d, 1d);
        controller.setMap(box);
        doSearch("test");
        assertTrue(controller.getSearchPhrase().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(ResourceType.IMAGE.getLabel()));
        assertTrue(controller.getSearchPhrase().contains("Resource Located"));
    }

    @Test
    @Rollback(true)
    public void testForInheritedCulturalInformationFromProject() {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        setResourceTypes(getInheritingTypes());
        doSearch("Archaic");
        assertTrue("'Archaic' defined inparent project should be found in information resource", resultsContainId(DOCUMENT_INHERITING_CULTURE_ID));
        assertFalse("A child document that inherits nothing from parent project should not appear in results", resultsContainId(DOCUMENT_INHERITING_NOTHING_ID));
    }

    @Test
    @Rollback(true)
    public void testDeletedOrDraftMaterialsAreHiddenInDefaultSearch() {
        Long imgId = setupImage();
        Long datasetId = setupDataset();
        Long codingSheetId = setupCodingSheet();

        logger.info("imgId:" + imgId + " datasetId:" + datasetId + " codingSheetId:" + codingSheetId);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        setResourceTypes(allResourceTypes);
        doSearch("precambrian");
        assertFalse(resultsContainId(datasetId));
        assertTrue(resultsContainId(codingSheetId));
        assertFalse(resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testGeneratedAreHidden() {
        Long codingSheetId = setupCodingSheet();
        CodingSheet sheet = genericService.find(CodingSheet.class, codingSheetId);
        sheet.setGenerated(true);
        genericService.save(sheet);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        setResourceTypes(ResourceType.CODING_SHEET);
        controller.setRecordsPerPage(10000);
        assertFalse(resultsContainId(codingSheetId));
    }

    @Test
    @Rollback(true)
    public void testPeopleAndInstitutionsInSearchResults() {
        Long imgId = setupDataset(Status.ACTIVE);
        logger.info("Created new image: " + imgId);
        searchIndexService.index(resourceService.find(imgId));
        setResourceTypes(allResourceTypes);

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
    // try a search that will fail the strict parsing pass, but work under lenient parsing.
    public void testLenientParsing() {
        String term = "a term w/ unclosed \" quote and at least one token that will return results: " + TestConstants.DEFAULT_LAST_NAME;
        doSearch(term);
        assertEquals(0, controller.getActionErrors().size());
    }

    @Test
    @Rollback(true)
    public void testDatedSearch() {
        Long docId = setupDatedDocument();
        logger.info("Created new document: " + docId);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        setResourceTypes(allResourceTypes);

        // test inner range
        setCoverageDate(CoverageType.CALENDAR_DATE, -900, 1000);
        doSearch("");
        assertTrue("expected to find document for inner range match", resultsContainId(docId));

        // test overlapping range lower
        controller = generateNewInitializedController(AdvancedSearchController.class);
        setResourceTypes(allResourceTypes);
        setCoverageDate(CoverageType.CALENDAR_DATE, -2000, -1);
        doSearch("");
        assertTrue("expected to find document for overlapping range (lower)", resultsContainId(docId));

        // test overlapping range upper
        controller = generateNewInitializedController(AdvancedSearchController.class);
        setResourceTypes(allResourceTypes);
        setCoverageDate(CoverageType.CALENDAR_DATE, 1999, 2009);
        doSearch("");
        assertTrue("expected to find document for overlapping range (upper)", resultsContainId(docId));

        // test invalid range
        controller = generateNewInitializedController(AdvancedSearchController.class);
        setResourceTypes(allResourceTypes);
        setCoverageDate(CoverageType.CALENDAR_DATE, -7000, -1001);
        doSearch("");
        assertFalse("expected not to find document in invalid range", resultsContainId(docId));

        // test exact range (query inclusive)
        controller = generateNewInitializedController(AdvancedSearchController.class);
        setResourceTypes(allResourceTypes);
        setCoverageDate(CoverageType.CALENDAR_DATE, -1000, 2000);
        doSearch("");
        assertTrue("expected to find document for exact range match", resultsContainId(docId));
    }

    private void setCoverageDate(CoverageType ct, int start, int end) {
        CoverageDate cd = new CoverageDate(ct, start, end);
        firstGroup().getCoverageDates().clear();
        firstGroup().getCoverageDates().add(cd);
    }

    @Test
    @Rollback
    public void testInvestigationTypes() {

        // TODO:dynamically get the list of 'used investigation types' and the resources that use them
        addInvestigationTypes();
        setResourceTypes(allResourceTypes);
        setStatuses(Status.ACTIVE, Status.DELETED, Status.DRAFT, Status.FLAGGED);
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known investigation types", resultsContainId(2420L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(1628L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(3805L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(3738L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(4287L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(262L));
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
        setResourceTypes(Arrays.asList(ResourceType.ONTOLOGY));
        setTitle("thistitleshouldprettymuchfilteroutanyandallresources");

        controller.setId(expectedId);
        controller.search();
        assertEquals("expecting only one result", 1, controller.getResults().size());
        Resource resource = (Resource) controller.getResults().iterator().next();
        assertEquals(expectedId, resource.getId());
    }

    // add all investigation types... for some reason
    private void addInvestigationTypes() {
        List<InvestigationType> investigationTypes = genericService.findAll(InvestigationType.class);
        List<String> ids = new ArrayList<String>();
        for (InvestigationType type : investigationTypes) {
            ids.add(type.getId().toString());
        }
        firstGroup().getInvestigationTypeIdLists().add(ids);
    }

    @Test
    @Rollback
    public void testLookupResourceWithDateRegisteredRange() throws InstantiationException, IllegalAccessException {
        // From the Hibernate documentation:
        // "The default Date bridge uses Lucene's DateTools to convert from and to String. This means that all dates are expressed in GMT time."
        // The Joda DateMidnight defaults to DateTimeZone.getDefault(). Which is probably *not* GMT
        // So for the tests below to work in, say, Australia, we need to force the DateMidnight to the GMT time zone...
        // ie:
        // DateTimeZone dtz = DateTimeZone.forID("Australia/Melbourne");
        // will break this test.
        DateTimeZone dtz = DateTimeZone.forID("GMT");

        // first create two documents with two separate create dates
        Document document1 = createAndSaveNewInformationResource(Document.class, createAndSaveNewPerson("lookuptest1@mailinator.com", ""));
        DateMidnight dm1 = new DateMidnight(2001, 2, 16, dtz);
        document1.setDateCreated(dm1.toDate());

        Document document2 = createAndSaveNewInformationResource(Document.class, createAndSaveNewPerson("lookuptest2@mailinator.com", ""));
        DateMidnight dm2 = new DateMidnight(2002, 11, 1, dtz);
        document2.setDateCreated(dm2.toDate());

        genericService.saveOrUpdate(document1, document2);
        searchIndexService.index(document1, document2);

        // okay, lets start with a search that should contain both of our newly created documents
        DateRange dateRange = new DateRange();
        dateRange.setStart(dm1.minusDays(1).toDate());
        dateRange.setEnd(dm2.plusDays(1).toDate());
        firstGroup().getRegisteredDates().add(dateRange);

        doSearch("");

        assertTrue(controller.getResults().contains(document1));
        assertTrue(controller.getResults().contains(document2));

        // now lets refine the search so that the document2 is filtered out.
        reset();
        dateRange.setEnd(dm2.minusDays(1).toDate());
        firstGroup().getRegisteredDates().add(dateRange);

        doSearch("");

        assertTrue(controller.getResults().contains(document1));
        assertFalse(controller.getResults().contains(document2));
    }

    @Test
    public void testSearchPhraseWithQuote() {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("\"test");
    }

    @Test
    public void testSearchPhraseWithColon() {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("\"test : abc ");
    }

    @Test
    public void testSearchPhraseWithLuceneSyntax() {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("title:abc");
    }

    @Test
    public void testSearchPhraseWithUnbalancedParenthesis() {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("\"test ( abc ");
    }

    @Test
    @Rollback(true)
    public void testAttachedFileSearch() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "33-Cu-314";
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        addFileToResource(document, new File(TestConstants.TEST_DOCUMENT_DIR + "test-file.rtf"));
        searchIndexService.index(document);
        firstGroup().setContents(Arrays.asList("fun"));
        doSearch("");
        logger.info("results:{}", controller.getResults());
        assertTrue(controller.getResults().contains(document));
        reset();
        firstGroup().setContents(Arrays.asList("have fun digging"));
        doSearch("");
        logger.info("results:{}", controller.getResults());
        assertTrue(controller.getResults().contains(document));

    }

    @Test
    @Rollback(true)
    public void testConfidentialFileSearch() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "33-Cu-314";
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        addFileToResource(document, new File(TestConstants.TEST_DOCUMENT_DIR + "test-file.rtf"), FileAccessRestriction.CONFIDENTIAL);
        searchIndexService.index(document);
        controller = generateNewController(AdvancedSearchController.class);
        initAnonymousUser(controller); // Anonymous user cannot find text in contents
        firstGroup().setContents(Arrays.asList("fun"));
        doSearch("");
        logger.info("results:{}", controller.getResults());
        assertFalse(controller.getResults().contains(document));
        reset(); // user who uploaded cannot see resource with full-text either
        firstGroup().setContents(Arrays.asList("have fun digging"));
        doSearch("");
        logger.info("results:{}", controller.getResults());
        assertFalse(controller.getResults().contains(document));

    }

}

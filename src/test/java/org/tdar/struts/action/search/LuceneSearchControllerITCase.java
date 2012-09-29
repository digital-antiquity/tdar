package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.tdar.core.bean.resource.ResourceType.DOCUMENT;
import static org.tdar.core.bean.resource.ResourceType.IMAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.joda.time.DateMidnight;
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
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.search.query.QueryDescriptionBuilder;
import org.tdar.search.query.SpatialQueryPart;
import org.tdar.struts.action.TdarActionSupport;

@Transactional
public class LuceneSearchControllerITCase extends AbstractSearchControllerITCase {

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
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback(true)
    public void testFindAllSearchPhrase() {
        doSearch("");
        assertEquals(QueryDescriptionBuilder.TITLE_ALL_RECORDS, controller.getSearchSubtitle());
    }

    public void setupTestDocuments() throws InstantiationException, IllegalAccessException {
        String[] titles = {"Preliminary Archeological Investigation at the Site of a Mid-Nineteenth Century Shop and Yard Complex Associated With the Belvidere and Delaware Railroad, Lambertville, New Jersey", "The James Franks Site (41DT97): Excavations at a Mid-Nineteenth Century Farmstead in the South Sulphur River Valley, Cooper Lake Project, Texas", "Archeological and Architectural Investigation of Public, Residential, and Hydrological Features at the Mid-Nineteenth Century Quintana Thermal Baths Ponce, Puerto Rico","Final Report On a Phased Archaeological Survey Along the Ohio and Erie Canal Towpath in Cuyahoga Valley NRA, Summit and Cuyahoga Counties, Ohio","Archeological Investigation at the Lock 33 Complex, Chesapeake and Ohio Canal", "Arthur Patterson Site, a Mid-Nineteenth Century Site, San Jacinto County"};
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
        logger.info("results:{}",controller.getResults());
        assertTrue(controller.getResults().contains(document));
        assertTrue(controller.getResults().get(0).equals(document) || controller.getResults().get(1).equals(document) );
    }

    @Test
    @Rollback(true)
    public void testExactTitleMatchInTitleSearch() throws InstantiationException, IllegalAccessException {
        String resourceTitle = "Archeological Excavation at Site 33-Cu-314: A Mid-Nineteenth Century Structure on the Ohio and Erie Canal";
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        setupTestDocuments();
        searchIndexService.index(document);
        controller.setTitle(resourceTitle);
        doSearch();
        logger.info("results:{}",controller.getResults());
        assertTrue(controller.getResults().contains(document));
        assertTrue(controller.getResults().get(0).equals(document) || controller.getResults().get(1).equals(document) );
    }

    @Test
    @Rollback(true)
    public void testFindResourceTypePhrase() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));
        doSearch("");
        assertTrue(controller.getSearchPhrase().contains(QueryDescriptionBuilder.SELECTED_RESOURCE_TYPES));
        assertTrue(controller.getSearchPhrase().contains(DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(IMAGE.getLabel()));
        assertEquals(controller.getSearchSubtitle(), QueryDescriptionBuilder.TITLE_ALL_RECORDS);
    }

    @Test
    @Rollback(true)
    public void testFindResourceById() {
        controller.setId(Long.valueOf(3074));
        controller.setResourceTypes(Arrays.asList(ResourceType.DATASET));
        doSearch("");
        assertTrue(resultsContainId(3074l));
        for (Indexable r : controller.getResults()) {
            logger.info("{}", r);
        }
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setId(TestConstants.ADMIN_PROJECT_ID);
        controller.setResourceTypes(Arrays.asList(ResourceType.PROJECT));
        doSearch("");
        for (Indexable r : controller.getResults()) {
            logger.info("{}", r);
        }
        assertTrue(resultsContainId((long) TestConstants.ADMIN_PROJECT_ID));
        assertEquals(controller.getResults().size(), 1);

    }

    @Test
    @Rollback(true)
    public void testFindTerm() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));

        doSearch("test");
        assertTrue(controller.getSearchPhrase().contains(QueryDescriptionBuilder.SELECTED_RESOURCE_TYPES));
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
        CultureKeyword label = genericKeywordService.findByLabel(CultureKeyword.class, "Folsom");
        CultureKeyword label2 = genericKeywordService.findByLabel(CultureKeyword.class, "Early Archaic");
        controller.setApprovedCultureKeywordIds(Arrays.asList(label.getId(), label2.getId()));
        doSearch("test");
        String searchPhrase = controller.getSearchPhrase();
        assertTrue("search phrase shouldn't be blank:", StringUtils.isNotBlank(searchPhrase));
        logger.debug("search phrase: {}", searchPhrase);
        assertTrue(searchPhrase.contains(QueryDescriptionBuilder.SELECTED_RESOURCE_TYPES));
        assertTrue(searchPhrase.contains(DOCUMENT.getLabel()));
        assertTrue(searchPhrase.contains(IMAGE.getLabel()));
        assertTrue(searchPhrase.contains("Folsom"));
        assertTrue(searchPhrase.contains("Early Archaic"));
        assertTrue(searchPhrase.contains("test"));
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
        assertTrue(!controller.getResults().isEmpty());

    }

    @Test
    @Rollback(true)
    public void testBadDateSearch() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));
        doSearch("test");
        CoverageDate cd = new CoverageDate(CoverageType.NONE);
        assertTrue(controller.getSearchPhrase().contains(QueryDescriptionBuilder.SELECTED_RESOURCE_TYPES));
        assertTrue(controller.getSearchPhrase().contains(DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(IMAGE.getLabel()));
        assertFalse(controller.getSearchPhrase().contains("null"));
        assertFalse(controller.getSearchPhrase().contains(" between "));
        assertEquals(controller.getSearchSubtitle(), "test");
    }

    @Test
    @Rollback(true)
    public void testCalDateSearch() {
        controller.setResourceTypes(Arrays.asList(DOCUMENT, IMAGE));
        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 1200);
        controller.getCoverageDates().add(cd);

        doSearch("test");
        logger.debug(controller.getSearchPhrase());
        assertTrue(controller.getSearchPhrase().contains(QueryDescriptionBuilder.SELECTED_RESOURCE_TYPES));
        assertTrue(controller.getSearchPhrase().contains(DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(IMAGE.getLabel()));
        assertFalse(controller.getSearchPhrase().contains("null"));
        assertTrue(controller.getSearchPhrase().contains("Between: "));
        assertTrue(controller.getSearchPhrase().contains("1000 BC"));
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
        assertTrue(controller.getSearchPhrase().contains(QueryDescriptionBuilder.SELECTED_RESOURCE_TYPES));
        assertTrue(controller.getSearchPhrase().contains(DOCUMENT.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(IMAGE.getLabel()));
        assertTrue(controller.getSearchPhrase().contains(QueryDescriptionBuilder.WITHIN_MAP_CONSTRAINTS));
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
    public void testPeopleAndInstitutionsInSearchResults() {
        Long imgId = setupDataset(Status.ACTIVE);
        logger.info("Created new image: " + imgId);
        searchIndexService.index(resourceService.find(imgId));
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
    // try a search that will fail the strict parsing pass, but work under lenient parsing.
    public void testLenientParsing() {
        String term = "a term w/ unclosed \" quote and at least one token that will return results: " + TestConstants.DEFAULT_LAST_NAME;
        doSearch(term);
        assertEquals(0, controller.getActionErrors().size());
    }

    @Test
    @Rollback(true)
    public void testStrictParsingPass() {
        // perform a quoted search e.g. '"test test"', which should return less results than 'test test'.

        String term = "test";
        String quotedTerm = "\"test test\"";
        doSearch(quotedTerm);
        int quotedResultCount = controller.getResults().size();
        controller = generateNewInitializedController(LuceneSearchController.class);
        doSearch(term);
        int resultCount = controller.getResults().size();
        controller = generateNewInitializedController(LuceneSearchController.class);
        doSearch(term);
        int unquotedResultCount = controller.getResults().size();

        assertTrue("unquoted search should have at least one result", resultCount >= 1);
        assertTrue("quoted search should have less results than unquoted search", quotedResultCount < resultCount);
        assertEquals("unquoted repeated term should have same resultcount as single term", resultCount, unquotedResultCount);
    }

    @Test
    @Rollback(true)
    public void testDatedSearch() {
        Long docId = setupDatedDocument();
        logger.info("Created new document: " + docId);
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);

        // test inner range
        setCoverageDate(CoverageType.CALENDAR_DATE, -900, 1000);
        doSearch("");
        assertTrue("expected to find document for inner range match", resultsContainId(docId));
        
        // test overlapping range lower
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setResourceTypes(allResourceTypes);
        setCoverageDate(CoverageType.CALENDAR_DATE, -2000, -1);
        doSearch("");
        assertTrue("expected to find document for overlapping range (lower)", resultsContainId(docId));
        
        // test overlapping range upper
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setResourceTypes(allResourceTypes);
        setCoverageDate(CoverageType.CALENDAR_DATE, 1999, 2009);
        doSearch("");
        assertTrue("expected to find document for overlapping range (upper)", resultsContainId(docId));
        
        // test invalid range
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setResourceTypes(allResourceTypes);
        setCoverageDate(CoverageType.CALENDAR_DATE, -7000, -1001);
        doSearch("");
        assertFalse("expected not to find document in invalid range", resultsContainId(docId));

        // test exact range (query inclusive)
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setResourceTypes(allResourceTypes);
        setCoverageDate(CoverageType.CALENDAR_DATE, -1000, 2000);
        doSearch("");
        assertTrue("expected to find document for exact range match", resultsContainId(docId));
    }
    
    private void setCoverageDate(CoverageType ct, int start, int end) {
    	CoverageDate cd = new CoverageDate(ct, start, end);
    	controller.getCoverageDates().clear();
    	controller.getCoverageDates().add(cd);
    }

    @Test
    @Rollback
    public void testInvestigationTypes() {

        // TODO:dynamically get the list of 'used investigation types' and the resources that use them
        addInvestigationTypes();
        controller.setResourceTypes(allResourceTypes);
        controller.setIncludedStatuses(new ArrayList<Status>(Arrays.asList(Status.ACTIVE, Status.DELETED, Status.DRAFT, Status.FLAGGED)));
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
        controller.setResourceTypes(Arrays.asList(ResourceType.ONTOLOGY));
        controller.setTitle("thistitleshouldprettymuchfilteroutanyandallresources");

        controller.setId(expectedId);
        controller.performSearch();
        assertEquals("expecting only one result", 1, controller.getResults().size());
        Resource resource = (Resource) controller.getResults().iterator().next();
        assertEquals(expectedId, resource.getId());
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

    @Test
    @Rollback
    public void testLookupResourceWithDateRegisteredRange() throws InstantiationException, IllegalAccessException {
        // first create two documents with two separate create dates
        Document document1 = createAndSaveNewInformationResource(Document.class, createAndSaveNewPerson("lookuptest1@mailinator.com", ""));
        Document document2 = createAndSaveNewInformationResource(Document.class, createAndSaveNewPerson("lookuptest2@mailinator.com", ""));
        DateMidnight dm1 = new DateMidnight(2001, 2, 16);
        DateMidnight dm2 = new DateMidnight(2002, 11, 1);
        document1.setDateCreated(dm1.toDate());
        document2.setDateCreated(dm2.toDate());
        genericService.saveOrUpdate(document1, document2);
        searchIndexService.indexAll(Resource.class);

        // okay, lets start with a search that should contain both of our newly created documents
        controller.setDateRegisteredStart(dm1.minusDays(1).toDate());
        controller.setDateRegisteredEnd(dm2.plusDays(1).toDate());

        doSearch();
        assertTrue(controller.getResults().contains(document1));
        assertTrue(controller.getResults().contains(document2));

        // now lets refine the search so that the document2 is filtered out.
        controller.setDateRegisteredEnd(dm2.minusDays(1).toDate());

        doSearch();
        assertFalse(controller.getResults().contains(document2));
    }
    
    @Test
    public void testSearchPhraseWithQuote() {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(Resource.class);
        doSearch("\"test");
    }


    @Test
    public void testSearchPhraseWithColon() {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(Resource.class);
        doSearch("\"test : abc ");
    }

    @Test
    public void testSearchPhraseWithLuceneSyntax() {
        searchIndexService.indexAll(Resource.class);
        doSearch("title:abc");
    }

    @Test
    public void testSearchPhraseWithUnbalancedParenthesis() {
        searchIndexService.indexAll(Resource.class);
        doSearch("\"test ( abc ");
    }
    
    @Test
    @Rollback
    public void testTitleSearchInAllFields() throws InstantiationException, IllegalAccessException, ParseException {
        String title = "the archaeology of class and war";
        Set<String> wordsInTitle = new HashSet<String>(Arrays.asList(title.split(" ")));
        wordsInTitle.removeAll(TdarConfiguration.getInstance().getStopWords());
        
        Document doc = createDocumentWithContributorAndSubmitter();
        doc.setTitle(title);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        controller.setQuery(title);
        controller.performSearch();
        
        //since we are sorting by relevance, we expect the exact match to be topmost,  and all other results to contain at least one matching term
        assertTrue("at least one result expected", controller.getResults().size() > 1);
        
        assertEquals("exact match should be most relevant", title, controller.getResults().get(0).getTitle());
        for(Resource r : controller.getResults()) {
            Set<String> wordsInResult = new HashSet<String>(Arrays.asList(r.getTitle().toLowerCase().split(" ")));
            wordsInResult.removeAll(TdarConfiguration.getInstance().getStopWords());
            wordsInResult.retainAll(wordsInTitle);  //intersection should yield at least one word
            
            String msg = String.format("expecting at least one term in title - title:'%s'  terms: [%s]", r.getTitle(), wordsInTitle);
            assertFalse(msg, wordsInResult.isEmpty());
            
        }
    }
    
    @Test
    public void testEscapedBoolean() {
        //we don't care whether we get search results back.  However,  we need to make sure that the query does not throw action errors
        String unsafeQuery = "Cultural Resource Management, Inc., Eugene, OR";
        String safeQuery = searchService.sanitize(unsafeQuery);
        logger.debug("unsafe query:\t{}", unsafeQuery);
        logger.debug("safe query:\t{}", safeQuery);
        assertFalse("unsafe query should have been modified", unsafeQuery.equals(safeQuery));
        
        assertQueryGetsSanitized(unsafeQuery);
    }
    
    @Test
    public void testSafeAndUnsafeQueries() {
        //queries that will cause parse errors (even if escaped)
        String[] unsafeQueries = {
                "Eugene, OR",
                "OR OR OR ",
                "AND(OR)",
                "AND PROTOHISTORIC, HISTORIC NON-INDIAN"
        };
        
        //stuff that shouldn't be modified by SearchService.sanitize()
        String [] safeQueries = {
                "EUGENE, OREGON",
                "this or that",
                "OREGENO",
                "ANDY RICHTER CONTROLS THE UNIVERSE",
                "title:foo and (allfields:foo allfields:bar)" 
        };
        
        String fmt = "%s:: Original:'%s'\t sanitized:'%s'";
        for(String query : unsafeQueries)  {
            String sanitized = searchService.sanitize(query);
            assertFalse(String.format(fmt, "query should have been sanitized", query, sanitized), query.equals(sanitized));
        }
        
        for(String query : safeQueries)  {
            String sanitized = searchService.sanitize(query);
            assertTrue(String.format(fmt, "query should not have been sanitized", query, sanitized), query.equals(sanitized));
        }
        
        //now pass everything through a parser to make sure it's valid after we escape/sanitize it.
        //we don't care about getting valid results. we just want to determine if string parses as valid lucene syntax
        QueryParser parser = new QueryParser(Version.LUCENE_31, "ignored", new StandardAnalyzer(Version.LUCENE_31));
        
        List<String> allQueries = new ArrayList<String>(Arrays.asList(unsafeQueries));
        allQueries.addAll(Arrays.asList(safeQueries));
        for(String query : allQueries) {
            String escapedSanitized = searchService.sanitize(QueryParser.escape(query));
            try {
                parser.parse(escapedSanitized);
            } catch (ParseException pex) {
                fmt = "%s:: Original:'%s'\t sanitizedEscaped:'%s'";
                fail(String.format(fmt, "Could not parse query", query, escapedSanitized));
            }
        }
        
    }
    
    
    
    //assert query doesn't cause errors in any field where we use escaped-but-unquoted values
    public void assertQueryGetsSanitized(String query) {
        setIgnoreActionErrors(true);
        
        //all fields
        controller.setQuery(query);
        controller.search();
        if(controller.hasActionErrors()) {
            fail("the following query caused errors on the ALLFIELDS  field:" + query);
        }
            
            
        //title 
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setTitle(query);
        controller.search();
        if(controller.hasActionErrors()) {
            fail("the following query caused errors on the title  field:" + query);
        }
        
    }
    
    
}



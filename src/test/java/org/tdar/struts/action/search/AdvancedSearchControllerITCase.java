package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.query.QueryDescriptionBuilder;
import org.tdar.search.query.SortOption;

@Transactional
public class AdvancedSearchControllerITCase extends AbstractSearchControllerITCase {

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Autowired
    EntityService entityService;

    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    @Before
    public void reset() {
        reindex();
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback
    public void testSiteNameKeywords() {
        controller.setSiteNameKeywords(Arrays.asList("Atsinna"));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known site name kwd", resultsContainId(2420L));
    }

    @Test
    @Rollback
    public void testApprovedSiteTypeKeywords() {
        controller.setApprovedSiteTypeKeywordIds(Arrays.asList(256l));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known site type kwd", resultsContainId(262L));
    }

    @Test
    @Rollback
    public void testMaterialKeywords() {
        controller.setMaterialKeywordIds(Arrays.asList(2L));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known material kwd", resultsContainId(3805L));
    }

    @Test
    @Rollback
    public void testCulture() {
        controller.setUncontrolledCultureKeywords(Arrays.asList("Sinagua"));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        // looking for project that has document that has known culture
        assertTrue("expected to find project that uses known culture", resultsContainId(4279L));
    }

    @Test
    @Rollback
    public void testApprovedCulture() {
        controller.setApprovedCultureKeywordIds(Arrays.asList(19L));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known material kwd", resultsContainId(2420L));
    }

    @Test
    @Rollback
    public void testLatLong() {
        controller = generateNewInitializedController(LuceneSearchController.class,getAdminUser());
        controller.setRecordsPerPage(50);
        Long imgId = setupImage();
        controller.setMaxx(-112.0330810546875);
        controller.setMaxy(33.465816745730024);
        controller.setMinx(-112.11273193359375);
        controller.setMiny(33.42571077612917);
        searchIndexService.index(genericService.find(Image.class, imgId));
        setStatuses(Status.DRAFT);
        doSearch("");
        assertFalse("we should get back at least one hit", !controller.getResults().isEmpty());
        assertFalse("expected to find document that uses known material kwd", resultsContainId(imgId));
    }

    @Test
    @Rollback
    public void testLatLongDraftFail() {
        setIgnoreActionErrors(true);
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(50);
        Long imgId = setupImage();
        controller.setMaxx(-112.0330810546875);
        controller.setMaxy(33.465816745730024);
        controller.setMinx(-112.11273193359375);
        controller.setMiny(33.42571077612917);
        searchIndexService.index(genericService.find(Image.class, imgId));
        setStatuses(Status.DRAFT);
        doSearch("");
        assertTrue("we should get back at least one hit", controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known material kwd", !resultsContainId(imgId));
        assertTrue(controller.getActionErrors().size() > 0);
    }

    @Test
    @Rollback
    public void testProjectIds() {
        Long projectId = 139L;
        controller.setProjectIds(Arrays.asList(projectId));
        controller.setResourceTypes(Arrays.asList(ResourceType.PROJECT));
        doSearch("");
        resultsContainId(projectId);
    }

    @Test
    @Rollback
    public void testTitle() {
        Long projectId = 139L;
        String projectTitle = "Rudd Creek Archaeological Project";
        controller.setTitle(projectTitle);
        doSearch("");
        resultsContainId(projectId);
    }

    // @Test
    // TODO: currently covered in SearchITCase webtest, may want to add as a controller test later
    @Rollback
    public void testUncontrolledSiteTypeKeywords() {
        fail("not implemented");
    }

    @Test
    @Rollback
    public void testSearchSubmitterIds() {
        Long submitterId = 6L;
        String resourceTitle = "Durrington Walls Humerus Dataset";
        Long resourceId = 3074L;
        setResourceTypes(ResourceType.DATASET);
        controller.setSearchSubmitterIds(Arrays.asList(submitterId));
        doSearch("");
        resultsContainId(resourceId);
    }

    @Test
    @Rollback
    public void testSearchContributorIds() {
        Long contributorId = 100L;
        // FIXME: pull these out into constants
        Long projectId = 139L;
        String projectTitle = "Rudd Creek Archaeological Project";
        setResourceTypes(ResourceType.PROJECT);
        controller.setSearchContributorIds(Arrays.asList(contributorId));
        doSearch("");
        resultsContainId(projectId);
    }

    @Test
    @Rollback
    public void testResultCounts() {
        setIgnoreActionErrors(true);
        for (ResourceType type : ResourceType.values()) {
            for (Status status : Status.values()) {
                assertResultCount(type, status, null);
            }
        }
    }

    @Test
    @Rollback
    public void testResultCountsAdmin() {
        for (ResourceType type : ResourceType.values()) {
            for (Status status : Status.values()) {
                assertResultCount(type, status, getAdminUser());
            }
        }
    }

    // compare the counts returned from searchController against the counts we get from the database
    private void assertResultCount(ResourceType resourceType, Status status, Person user) {
        logger.info(String.format("testing %s , %s for %s", resourceType, status, user));
        long expectedCount = resourceService.getResourceCount(resourceType, status);
        controller = generateNewInitializedController(LuceneSearchController.class, user);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        setResourceTypes(resourceType);
        setStatuses(status);
        if (status == Status.DELETED && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, user) ||
                status == Status.FLAGGED && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, user) ||
                status == Status.DRAFT && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, user)) {
            logger.debug("expecting exception");
            doSearch("");
            assertTrue(controller.getActionErrors().size() > 0);
        } else {
            Object[] msg_ = { user, resourceType, status, expectedCount, controller.getTotalRecords() };
            String msg = String.format("User: %s ResourceType:%s  Status:%s  expected:%s actual: %s", msg_);
            logger.info(msg);
            doSearch("");
            Assert.assertEquals(msg, expectedCount, controller.getTotalRecords());
        }
    }

    private void setSortThenCheckFirstResult(String message, SortOption sortField, Long projectId, Long expectedId) {
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setSortField(sortField);
        controller.setProjectIds(Arrays.asList(projectId));
        doSearch("");
        logger.info("{}", controller.getResults());
        Resource found = (Resource) controller.getResults().iterator().next();
        logger.info("{}", found);
        Assert.assertEquals(message, expectedId, found.getId());
    }

    // note: relevance sort broken out into SearchRelevancyITCase
    @Test
    @Rollback
    public void testSortFieldTitle() {
        Long alphaId = -1L;
        Long omegaId = -1L;
        Project p = new Project();
        p.setTitle("test project");
        p.setStatus(Status.ACTIVE);
        p.markUpdated(getUser());
        List<String> titleList = Arrays.asList(new String[] { "a", "b", "c", "d" });
        genericService.save(p);
        for (String title : titleList) {
            Document doc = new Document();
            doc.markUpdated(getUser());
            doc.setTitle(title);
            doc.setProject(p);
            doc.setStatus(Status.ACTIVE);
            genericService.save(doc);
            if (alphaId == -1) {
                alphaId = doc.getId();
            }
            omegaId = doc.getId();
        }
        reindex();
        setSortThenCheckFirstResult("sorting by title asc", SortOption.TITLE, p.getId(), alphaId);
        setSortThenCheckFirstResult("sorting by title desc", SortOption.TITLE_REVERSE, p.getId(), omegaId);
    }

    @Test
    @Rollback
    public void testSortFieldDateCreated() {
        Long alphaId = -1L;
        Long omegaId = -1L;
        Project p = new Project();
        p.setTitle("test project");
        p.markUpdated(getUser());
        List<Integer> dateList = Arrays.asList(new Integer[] { null, 1, 2, 3, 4, 5, 19, 39 });
        genericService.save(p);
        for (Integer date : dateList) {
            Document doc = new Document();
            doc.markUpdated(getUser());
            doc.setDate(date);
            doc.setTitle("hello" + date);
            doc.setProject(p);
            genericService.save(doc);
            if (alphaId == -1) {
                alphaId = doc.getId();
            }
            omegaId = doc.getId();
        }
        reindex();

        setSortThenCheckFirstResult("sorting by datecreated asc", SortOption.DATE, p.getId(), alphaId);
        setSortThenCheckFirstResult("sorting by datecreated desc", SortOption.DATE_REVERSE, p.getId(), omegaId);
    }

    @Test
    @Rollback
    public void testResourceCount() {
        // fixme: remove this query. it's only temporary to ensure that my named query is working
        long count = resourceService.getResourceCount(ResourceType.PROJECT, Status.ACTIVE);
        assertTrue(count > 0);
    }

    @Test
    @Rollback
    public void testOtherKeywords() throws InstantiationException, IllegalAccessException, ParseException {
        // Create a document w/ some other keywords, then try to find that document in a search
        OtherKeyword ok = new OtherKeyword();
        ok.setLabel("testotherkeyword");
        assertNull("this label already taken. need a unique label", genericKeywordService.findByLabel(OtherKeyword.class, ok.getLabel()));
        Document document = createAndSaveNewInformationResource(Document.class);
        document.setTitle("otherkeywordtest");
        document.getOtherKeywords().add(ok);
        genericService.save(ok);
        genericService.save(document);
        reindex();
        Long documentId = document.getId();
        assertNotNull(documentId);
        controller.getOtherKeywords().add(ok.getLabel());
        controller.getResourceTypes().add(ResourceType.DOCUMENT);
        controller.performSearch();
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(controller.getResults());
        assertEquals("only expecting one result", 1L, controller.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(QueryDescriptionBuilder.WITH_OTHER_KEYWORDS);
        assertSearchPhrase(ok.getLabel());
    }

    @Test
    @Rollback
    public void testTemporalKeywords() throws ParseException, InstantiationException, IllegalAccessException {
        // Create a document w/ some temporal keywords, then try to find that document in a search
        TemporalKeyword tk = new TemporalKeyword();
        tk.setLabel("testtemporalkeyword");
        assertNull("this label already taken. need a unique label", genericKeywordService.findByLabel(TemporalKeyword.class, tk.getLabel()));
        Document document = createAndSaveNewInformationResource(Document.class);
        document.setTitle("temporal keyword test");
        document.getTemporalKeywords().add(tk);
        genericService.save(tk);
        genericService.save(document);
        reindex();
        Long documentId = document.getId();
        assertNotNull(documentId);
        controller.getTemporalKeywords().add(tk.getLabel());
        controller.getResourceTypes().add(ResourceType.DOCUMENT);
        controller.performSearch();
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(controller.getResults());
        assertEquals("only expecting one result", 1L, controller.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(QueryDescriptionBuilder.WITH_TEMPORAL_KEYWORDS);
        assertSearchPhrase(tk.getLabel());
    }

    @Test
    @Rollback
    public void testGeoKeywords() throws InstantiationException, IllegalAccessException, ParseException {
        // Create a document w/ some temporal keywords, then try to find that document in a search
        GeographicKeyword gk = new GeographicKeyword();
        gk.setLabel("testgeographickeyword");
        assertNull("this label already taken. need a unique label", genericKeywordService.findByLabel(GeographicKeyword.class, gk.getLabel()));
        Document document = createAndSaveNewInformationResource(Document.class);
        document.setTitle("geographic keyword test");
        document.getGeographicKeywords().add(gk);
        genericService.save(gk);
        genericService.save(document);
        reindex();
        Long documentId = document.getId();
        assertNotNull(documentId);
        controller.getGeographicKeywords().add(gk.getLabel());
        controller.getResourceTypes().add(ResourceType.DOCUMENT);
        controller.performSearch();
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(controller.getResults());
        assertEquals("only expecting one result", 1L, controller.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(QueryDescriptionBuilder.WITH_GEOGRAPHIC_KEYWORDS);
        assertSearchPhrase(gk.getLabel());
    }

    private Document createDocumentWithContributorAndSubmitter() throws InstantiationException, IllegalAccessException {
        Document doc = createAndSaveNewInformationResource(Document.class);
        Person contributor = new Person("Kelly", "deVos", "kellyd@mailinator.com");
        genericService.save(contributor);
        ResourceCreator rc = new ResourceCreator(doc, contributor, ResourceCreatorRole.AUTHOR);
        Person submitter = new Person("Evelyn", "deVos", "ecd@mailinator.com");
        genericService.save(submitter);
        doc.setSubmitter(submitter);
        genericService.saveOrUpdate(rc);
        doc.getResourceCreators().add(rc);
        genericService.saveOrUpdate(doc);
        reindex();
        return doc;
    }

    @Test
    @Rollback
    public void testSearchBySubmitterIds() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = createDocumentWithContributorAndSubmitter();
        Long submitterId = doc.getSubmitter().getId();
        controller.getSearchSubmitterIds().add(submitterId);
        controller.performSearch();
        assertEquals("only one result expected", 1L, controller.getResults().size());
        assertEquals(doc, controller.getResults().iterator().next());

    }

    @Test
    @Rollback
    public void testSearchByContributorIds() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = createDocumentWithContributorAndSubmitter();
        Long contributorId = doc.getResourceCreators().iterator().next().getCreator().getId();
        controller.getSearchContributorIds().add(contributorId);
        controller.performSearch();
        assertEquals("only one result expected", 1L, controller.getResults().size());
        assertEquals(doc, controller.getResults().iterator().next());
    }

    @Test
    @Rollback
    public void testSearchByContributorName() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = createDocumentWithContributorAndSubmitter();
        Person contributor = (Person) doc.getResourceCreators().iterator().next().getCreator();
        controller.getSearchContributor().setFirstName(contributor.getFirstName());
        controller.getSearchContributor().setLastName(contributor.getLastName());
        controller.performSearch();
        assertEquals("only one result expected", 1L, controller.getResults().size());
        assertEquals(doc, controller.getResults().iterator().next());
    }

    @Test
    @Rollback
    public void testSearchBySubmitterName() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = createDocumentWithContributorAndSubmitter();
        Person submitter = (Person) doc.getSubmitter();
        controller.getSearchSubmitter().setFirstName(submitter.getFirstName());
        controller.getSearchSubmitter().setLastName(submitter.getLastName());
        controller.getSearchSubmitter().setEmail(submitter.getEmail());
        controller.performSearch();
        assertEquals("only one result expected", 1L, controller.getResults().size());
        assertEquals(doc, controller.getResults().iterator().next());
    }

    // TODO: need tests for search subtitle, search phrases

    public void assertSearchPhrase(String term) {
        assertTrue("looking for '" + term + "' in \"" + controller.getSearchPhrase() + "\"", true);
    }

    @Test
    @Rollback
    public void testTitleSearch() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = createDocumentWithContributorAndSubmitter();
        doc.setTitle("the archaeology of class and war");
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        controller.setTitle("the archaeology of class and war");
        controller.performSearch();
        assertEquals("only one result expected", 1L, controller.getResults().size());
        assertEquals(doc, controller.getResults().iterator().next());
    }
    
}

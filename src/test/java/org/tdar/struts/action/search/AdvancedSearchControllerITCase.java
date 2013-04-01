package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.TestConstants;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.ResourceCreatorProxy;

@Transactional
public class AdvancedSearchControllerITCase extends AbstractControllerITCase {

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    ResourceService resourceServicek;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Autowired
    EntityService entityService;

    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    private AdvancedSearchController controller;

    public void AdvancedSearchController() {
        controller = generateNewInitializedController(AdvancedSearchController.class);
    }

    private void resetController() {
        controller = generateNewInitializedController(AdvancedSearchController.class);
    }

    // we assume here that struts performs similar actions when you reference an element index that may not yet exist.
    private SearchParameters firstGroup() {
        if (controller.getG().isEmpty()) {
            controller.getG().add(new SearchParameters());
        }
        return controller.getG().get(0);
    }

    @Before
    public void reset() {
        reindex();
        resetController();
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback
    public void testSiteNameKeywords() {
        SiteNameKeyword snk = genericKeywordService.findByLabel(SiteNameKeyword.class, "Atsinna");
        Document doc = createAndSaveNewResource(Document.class);
        doc.getSiteNameKeywords().add(snk);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        firstGroup().getSiteNames().add(snk.getLabel());
        doSearch();
        assertFalse("we should get back at least one hit", controller.getResults().isEmpty());
        for (Resource resource : controller.getResults()) {
            assertTrue("expecting site name for resource", resource.getSiteNameKeywords().contains(snk));
        }
    }

    @Test
    @Rollback
    public void testComplexGeographicKeywords() {
        GeographicKeyword snk = genericKeywordService.findOrCreateByLabel(GeographicKeyword.class, "propylon, Athens, Greece, Mnesicles");
        Document doc = createAndSaveNewResource(Document.class);
        doc.getGeographicKeywords().add(snk);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        firstGroup().getGeographicKeywords().add("Greece");
        doSearch();
        assertFalse("we should get back at least one hit", controller.getResults().isEmpty());
        for (Resource resource : controller.getResults()) {
            assertTrue("expecting site name for resource", resource.getGeographicKeywords().contains(snk));
        }
    }

    @Test
    @Rollback
    public void testPersonSearchWithoutAutocomplete() {
        String lastName = "Watts";
        Person person = new Person(null, lastName, null);
        lookForCreatorNameInResult(lastName, person);
    }

    @Test
    @Rollback
    public void testMultiplePersonSearch() {
        Long peopleIds[] = { 8044L, 8344L, 8393L, 8608L, 8009L };
        List<Person> people = genericService.findAll(Person.class, Arrays.asList(peopleIds));
        assertEquals(4, people.size());
        logger.info("{}", people);
        List<String> names = new ArrayList<String>();
        for (Person person : people) {
            names.add(person.getProperName());
            Person p = new Person();
            p.setId(person.getId());
            ResourceCreator rc = new ResourceCreator(p, null);
            firstGroup().getResourceCreatorProxies().add(new ResourceCreatorProxy(rc));
        }
        doSearch();
        logger.info(controller.getSearchPhrase());
        for (String name : names) {
            assertTrue(controller.getSearchPhrase().contains(name));
        }
        // lookForCreatorNameInResult(lastName, person);
    }

    @Test
    @Rollback
    public void testInstitutionSearchWithoutAutocomplete() {
        String name = "Digital Antiquity";
        Institution institution = new Institution(name);
        lookForCreatorNameInResult(name, institution);
    }

    private void lookForCreatorNameInResult(String namePart, Creator creator_) {
        firstGroup().getResourceCreatorProxies().add(new ResourceCreatorProxy(new ResourceCreator(creator_, null)));
        doSearch();
        assertFalse("we should get back at least one hit", controller.getResults().isEmpty());
        for (Resource resource : controller.getResults()) {
            logger.info("{}", resource);
            boolean seen = false;
            if (resource.getSubmitter().getProperName().contains(namePart) || resource.getUpdatedBy().getProperName().contains(namePart)) {
                seen = true;
            }
            if (resource instanceof InformationResource) {
                Institution institution = ((InformationResource) resource).getResourceProviderInstitution();
                if (institution != null && institution.getName().contains(namePart)) {
                    seen = true;
                }
            }
            for (ResourceCreator creator : resource.getResourceCreators()) {
                if (creator.getCreator().getProperName().contains(namePart)) {
                    seen = true;
                }
            }
            assertTrue("should have seen term somwehere", seen);
        }
    }

    @Test
    @Rollback
    public void testSearchDecade() {
        Document doc = createAndSaveNewResource(Document.class);
        doc.setDate(4000);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        firstGroup().getCreationDecades().add(4000);
        doSearch();
        assertFalse("we should get back at least one hit", controller.getResults().isEmpty());
        for (Resource resource : controller.getResults()) {
            assertEquals("expecting resource", 4000, ((InformationResource) resource).getDateNormalized().intValue());
        }

    }

    @Test
    @Rollback
    public void testApprovedSiteTypeKeywords() {
        final Long keywordId = 256L;
        Keyword keyword = genericService.find(SiteTypeKeyword.class, keywordId);
        List<String> keywordIds = new ArrayList<String>();
        keywordIds.add(keywordId.toString());
        firstGroup().getApprovedSiteTypeIdLists().add(keywordIds);
        doSearch();
        assertFalse("we should get back at least one hit", controller.getResults().isEmpty());
        assertTrue(resultsContainId(262L));
        controller.getIncludedStatuses().add(Status.ACTIVE);
        for (Resource resource : controller.getResults()) {
            // if it's a project, the keyword should be found in either it's own keyword list, or the keyword list
            // of one of the projects informationResources
            if (resource instanceof Project) {
                // put all of the keywords in a superset
                Project project = (Project) resource;
                Set<Keyword> keywords = new HashSet<Keyword>(project.getActiveSiteTypeKeywords());
                Set<InformationResource> projectInformationResources = projectService.findAllResourcesInProject(project, Status.ACTIVE);
                for (InformationResource informationResource : projectInformationResources) {
                    keywords.addAll(informationResource.getActiveSiteTypeKeywords());
                }
                assertTrue("keyword should be found in project, or project's informationResources",
                        keywords.contains(keyword));

            } else {
                logger.debug("resourceid:{} contents of resource:", resource.getId(), resource.getActiveSiteTypeKeywords());
                assertTrue("expecting site type for resource:", resource.getActiveSiteTypeKeywords().contains(keyword));
            }
        }
    }

    @Test
    @Rollback
    public void testMaterialKeywords() {
        // FIXME: magic numbers
        Keyword keyword = genericKeywordService.find(MaterialKeyword.class, 2L);
        firstGroup().getMaterialKeywordIdLists().add(Arrays.asList(keyword.getId().toString()));
        doSearch();
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        // every resource in results should have that material keyword (or should have at least one informationResource that has that keyword)
        for (Resource resource : controller.getResults()) {
            Set<Keyword> keywords = new HashSet<Keyword>(resource.getActiveMaterialKeywords());
            if (resource instanceof Project) {
                // check that at least one child uses this keyword
                Project project = (Project) resource;
                for (InformationResource informationResource : projectService.findAllResourcesInProject(project)) {
                    keywords.addAll(informationResource.getMaterialKeywords());
                }
            }
            assertTrue(String.format("Expected to find material keyword %s in %s", keyword, resource), keywords.contains(keyword));
        }
    }

    @Test
    @Rollback
    public void testCulture() {
        // FIXME: this test is brittle/incomplete
        String label = "Sinagua";
        firstGroup().getUncontrolledCultureKeywords().add(label);
        Keyword keyword = genericKeywordService.findByLabel(CultureKeyword.class, label);

        doSearch();
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());

        for (Resource resource : controller.getResults()) {
            // if it's a project, the keyword should be found in either it's own keyword list, or the keyword list
            // of one of the projects informationResources
            if (resource instanceof Project) {
                // put all of the keywords in a superset
                Project project = (Project) resource;
                Set<Keyword> keywords = new HashSet<Keyword>(project.getActiveCultureKeywords());
                Set<InformationResource> projectInformationResources = projectService.findAllResourcesInProject(project, Status.ACTIVE);
                for (InformationResource informationResource : projectInformationResources) {
                    keywords.addAll(informationResource.getActiveCultureKeywords());
                }
                assertTrue("keyword should be found in project, or project's informationResources",
                        keywords.contains(keyword));

            } else {
                logger.debug("resourceid:{} contents of resource:", resource.getId(), resource.getActiveCultureKeywords());
                assertTrue("expecting site type for resource:", resource.getActiveCultureKeywords().contains(keyword));
            }
        }

    }

    @Test
    @Rollback
    public void testApprovedCulture() {
        // FIXME: pull this ID from db or generate/save new keyword+resource that uses it
        Long keywordId = 19L;
        Keyword keyword = genericService.find(CultureKeyword.class, keywordId);
        firstGroup().getApprovedCultureKeywordIdLists().add(Arrays.asList(keywordId.toString()));
        doSearch();
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        for (Resource resource : controller.getResults()) {
            // if it's a project, the keyword should be found in either it's own keyword list, or the keyword list
            // of one of the projects informationResources
            if (resource instanceof Project) {
                // put all of the keywords in a superset
                Project project = (Project) resource;
                Set<Keyword> keywords = new HashSet<Keyword>(project.getActiveCultureKeywords());
                Set<InformationResource> projectInformationResources = projectService.findAllResourcesInProject(project, Status.ACTIVE);
                for (InformationResource informationResource : projectInformationResources) {
                    keywords.addAll(informationResource.getActiveCultureKeywords());
                }
                assertTrue("keyword should be found in project, or project's informationResources",
                        keywords.contains(keyword));

            } else {
                logger.debug("resourceid:{} contents of resource:", resource.getId(), resource.getActiveCultureKeywords());
                assertTrue("expecting site type for resource:", resource.getActiveCultureKeywords().contains(keyword));
            }
        }
    }

    @Test
    @Rollback
    public void testLatLong() throws Exception {
        // create a document that we expect to find w/ geo search, and a bounding box big enough to find it
        Document doc = createAndSaveNewInformationResource(Document.class);
        LatitudeLongitudeBox region = new LatitudeLongitudeBox(-100d, 30d, -90d, 40d);
        LatitudeLongitudeBox selectionRegion = new LatitudeLongitudeBox(-101d, 29d, -89d, 41d);
        LatitudeLongitudeBox elsewhere = new LatitudeLongitudeBox(100d, 10d, 110d, 20d);

        doc.getLatitudeLongitudeBoxes().add(region);

        genericService.save(doc);
        reindex();

        controller = generateNewInitializedController(AdvancedSearchController.class, getAdminUser());
        controller.setRecordsPerPage(50);
        controller.setMap(selectionRegion);
        doSearch();
        assertTrue("expected to find document within selection region", controller.getResults().contains(doc));

        // now do another search with bounding boxes outside of doc's region
        controller = generateNewInitializedController(AdvancedSearchController.class, getAdminUser());
        controller.setRecordsPerPage(50);
        controller.setMap(elsewhere);
        assertFalse("document shouldn't not be found within provided bounding box.", controller.getResults().contains(doc));
        doSearch();

    }

    @Test
    @Rollback
    public void testProjectIds() {
        // FIXME: magic numbers
        Long projectId = 3805L;
        firstGroup().getProjects().add(sparseProject(projectId));
        controller.getResourceTypes().clear(); // select all resource types
        doSearch();
        int resourceCount = 0;
        for (Resource resource : controller.getResults()) {
            if (resource instanceof InformationResource) {
                resourceCount++;
                InformationResource informationResource = (InformationResource) resource;
                assertEquals("informationResource should belong to project we just searched for", projectId, informationResource.getProjectId());
            }
        }
        assertTrue("search should have at least 1 result", resourceCount > 0);
    }

    private Project sparseProject(Long id) {
        Project project = new Project(id, "sparse");
        return project;
    }

    private ResourceCollection sparseCollection(Long id) {
        ResourceCollection collection = new ResourceCollection();
        collection.setId(id);
        return collection;
    }

    @Test
    @Rollback
    public void testTitle() {
        // FIXME: magic numbers
        Long projectId = 139L;
        Project project = genericService.find(Project.class, projectId);
        String projectTitle = project.getTitle();
        firstGroup().getTitles().add(projectTitle);
        doSearch();
        controller.getResults().contains(project);
    }

    @Test
    @Rollback
    public void testSearchSubmitterIds() {
        // FIXME: magic numbers
        Person person = genericService.find(Person.class, 6L);
        firstGroup().getResourceCreatorProxies().add(new ResourceCreatorProxy(person, ResourceCreatorRole.SUBMITTER));
        doSearch();

        // make sure every resource has that submitter
        for (Resource resource : controller.getResults()) {
            assertEquals("Expecting same submitterId", person.getId(), resource.getSubmitter().getId());
        }
    }

    @Test
    @Rollback(true)
    public void testResultCountsAsUnauthenticatedUser() {
        setIgnoreActionErrors(true);
        testResourceCounts(null);
    }

    @Test
    @Rollback(true)
    public void testResultCountsAsBasicUser() {
        // testing as a user who did not create their own stuff
        setIgnoreActionErrors(true);
        Person p = new Person("a", "test", "anoter@test.user.com");
        p.setRegistered(true);
        genericService.saveOrUpdate(p);
        testResourceCounts(p);
    }

    @Test
    @Rollback(true)
    public void testResultCountsAsBasicContributor() {
        // testing as a user who did create their own stuff
        setIgnoreActionErrors(true);
        testResourceCounts(getBasicUser());
    }

    @Test
    @Rollback(true)
    public void testResultCountsAdmin() {
        testResourceCounts(getAdminUser());
    }

    @Test
    @Rollback(true)
    public void testGeographicKeywordIndexedAndFound() {
        Document doc = createAndSaveNewResource(Document.class, getBasicUser(), "testing doc");
        GeographicKeyword kwd = new GeographicKeyword();
        kwd.setLabel("Casa NonGrande");
        genericService.save(kwd);
        doc.getGeographicKeywords().add(kwd);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        firstGroup().getGeographicKeywords().add("Casa NonGrande");
        doSearch();
        boolean seen = false;
        for (Resource res : controller.getResults()) {
            logger.info("{}", res);
            if (res.getGeographicKeywords().contains(kwd)) {
                seen = true;
            } else {
                fail("found resource without keyword");
            }
        }
        assertTrue(seen);
    }

    
    @Test
    @Rollback(true)
    public void testFilenameFound() throws InstantiationException, IllegalAccessException {
        Document doc = generateInformationResourceWithFileAndUser();
        searchIndexService.index(doc);
        firstGroup().getFilenames().add(TestConstants.TEST_DOCUMENT_NAME);
        doSearch();
        boolean seen = false;
        for (Resource res : controller.getResults()) {
            if (res.getId().equals(doc.getId())) {
                seen = true;
            } else {
                fail("found resource without keyword");
            }
        }
        assertTrue(seen);
    }

    private void testResourceCounts(Person user) {
        for (ResourceType type : ResourceType.values()) {
            Resource resource = createAndSaveNewResource(type.getResourceClass());
            for (Status status : Status.values()) {
                if (Status.DUPLICATE == status || Status.FLAGGED_ACCOUNT_BALANCE == status) {
                    continue;
                }
                resource.setStatus(status);
                genericService.saveOrUpdate(resource);
                searchIndexService.index(resource);
                assertResultCount(type, status, user);
            }
        }
    }

    // compare the counts returned from searchController against the counts we get from the database
    private void assertResultCount(ResourceType resourceType, Status status, Person user) {
        String stat = String.format("testing %s , %s for %s", resourceType, status, user);
        logger.info(stat);
        long expectedCount = resourceService.getResourceCount(resourceType, status);
        controller = generateNewController(AdvancedSearchController.class);
        init(controller, user);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        controller.getResourceTypes().add(resourceType);
        controller.getIncludedStatuses().add(status);
        if (status == Status.DELETED && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, user) ||
                status == Status.FLAGGED && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, user)) {
            logger.debug("expecting exception");
            doSearch();
            assertTrue(String.format("expected action errors %s", stat), controller.getActionErrors().size() > 0);
        } else if (status == Status.DRAFT && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, user)) {
            // this was in the test, but with the new status search I think this is more accurate to be commented out as
            doSearch();
            for (Resource res : controller.getResults()) {
                if (res.isDraft() && !res.getSubmitter().equals(user)) {
                    fail("we should only see our own drafts here");
                }

            }
            // assertEquals(String.format("expecting results to be empty %s",stat),0, controller.getTotalRecords());
        } else {
            doSearch();
            Object[] msg_ = { user, resourceType, status, expectedCount, controller.getTotalRecords() };
            String msg = String.format("User: %s ResourceType:%s  Status:%s  expected:%s actual: %s", msg_);
            logger.info(msg);
            Assert.assertEquals(msg, expectedCount, controller.getTotalRecords());
        }
    }

    private void setSortThenCheckFirstResult(String message, SortOption sortField, Long projectId, Long expectedId) {
        resetController();
        controller.setSortField(sortField);
        firstGroup().getProjects().add(sparseProject(projectId));
        doSearch();
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
        p.setDescription("test descr");
        p.setStatus(Status.ACTIVE);
        p.markUpdated(getUser());
        List<String> titleList = Arrays.asList(new String[] { "a", "b", "c", "d" });
        genericService.save(p);
        for (String title : titleList) {
            Document doc = new Document();
            doc.markUpdated(getUser());
            doc.setTitle(title);
            doc.setDescription(title);
            doc.setDate(2341);
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
        p.setDescription("test description");
        p.markUpdated(getUser());
        List<Integer> dateList = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 19, 39 });
        genericService.save(p);
        for (Integer date : dateList) {
            Document doc = new Document();
            doc.markUpdated(getUser());
            doc.setDate(date);
            doc.setTitle("hello" + date);
            doc.setDescription(doc.getTitle());
            doc.setProject(p);
            genericService.save(doc);
            if (alphaId == -1) {
                logger.debug("setting id for doc:{}", doc.getId());
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
        searchIndexService.index(document);
        searchIndexService.index(ok);
        Long documentId = document.getId();
        assertNotNull(documentId);
        firstGroup().getOtherKeywords().add(ok.getLabel());
        controller.getResourceTypes().add(ResourceType.DOCUMENT);
        controller.search();
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(controller.getResults());
        assertEquals("only expecting one result", 1L, controller.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
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
        searchIndexService.index(tk);
        searchIndexService.index(document);
        Long documentId = document.getId();
        assertNotNull(documentId);
        firstGroup().getTemporalKeywords().add(tk.getLabel());
        controller.getResourceTypes().add(ResourceType.DOCUMENT);
        controller.search();
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(controller.getResults());
        assertEquals("only expecting one result", 1L, controller.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
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
        searchIndexService.index(gk);
        searchIndexService.index(document);
        Long documentId = document.getId();
        assertNotNull(documentId);
        firstGroup().getGeographicKeywords().add(gk.getLabel());
        controller.getResourceTypes().add(ResourceType.DOCUMENT);
        controller.search();
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(controller.getResults());
        assertEquals("only expecting one result", 1L, controller.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(gk.getLabel());
    }

    private Document createDocumentWithContributorAndSubmitter() throws InstantiationException, IllegalAccessException {
        Person submitter = new Person("Evelyn", "deVos", "ecd@mailinator.com");
        genericService.save(submitter);
        Document doc = createAndSaveNewInformationResource(Document.class, submitter);
        ResourceCreator rc = new ResourceCreator(new Person("Kelly", "deVos", "kellyd@mailinator.com"), ResourceCreatorRole.AUTHOR);
        genericService.save(rc.getCreator());
        // genericService.save(rc);
        doc.getResourceCreators().add(rc);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        return doc;
    }

    @Test
    @Rollback
    public void testSearchBySubmitterIds() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = createDocumentWithContributorAndSubmitter();
        Long submitterId = doc.getSubmitter().getId();
        assertFalse(submitterId == -1);
        firstGroup().getResourceCreatorProxies().add(new ResourceCreatorProxy(doc.getSubmitter(), ResourceCreatorRole.SUBMITTER));
        controller.search();
        assertTrue("only one result expected", 1 <= controller.getResults().size());
        assertTrue(controller.getResults().contains(doc));
    }

    @Test
    @Rollback
    public void testSearchContributorIds2() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = createDocumentWithContributorAndSubmitter();
        ResourceCreator contributor = doc.getResourceCreators().iterator().next();
        firstGroup().getResourceCreatorProxies().add(new ResourceCreatorProxy(contributor.getCreator(), contributor.getRole()));
        controller.search();
        assertEquals("only one result expected", 1L, controller.getResults().size());
        assertEquals(doc, controller.getResults().iterator().next());
    }

    public void assertSearchPhrase(String term) {
        logger.debug("term:{}\t search phrase:{}", term, controller.getSearchPhrase());
        assertTrue(String.format("looking for string '%s' in search phrase '%s'", term, controller.getSearchPhrase()),
                controller.getSearchPhrase().toLowerCase().contains(term.toLowerCase()));
    }

    @Test
    @Rollback
    public void testTitleSearch() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = createDocumentWithContributorAndSubmitter();
        String title = "the archaeology of class and war";
        doc.setTitle(title);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        firstGroup().getTitles().add(title);
        controller.search();
        logger.info("{}", controller.getResults());
        assertEquals("only one result expected", 1L, controller.getResults().size());
        assertEquals(doc, controller.getResults().iterator().next());
    }

    @Test
    @Rollback
    public void testResourceCreatorPerson() {
        Person person = new Person("Bob", "Loblaw", null);
        genericService.save(person);
        Resource resource = constructActiveResourceWithCreator(person, ResourceCreatorRole.AUTHOR);
        reindex();
        logger.debug("user:{}   id:{}", person, person.getId());
        assertTrue("person id should be set - id:" + person.getId(), person.getId() != 1L);

        firstGroup().getResourceCreatorProxies().add(new ResourceCreatorProxy(person, ResourceCreatorRole.AUTHOR));

        doSearch();

        assertTrue(String.format("expecting %s in results", resource), controller.getResults().contains(resource));
        assertEquals("should be one and only one result", 1, controller.getResults().size());
    }

    @Test
    @Rollback
    public void testResourceCreatorWithAnyRole() {
        Person person = new Person("Bob", "Loblaw", null);
        genericService.save(person);
        Resource resource = constructActiveResourceWithCreator(person, ResourceCreatorRole.AUTHOR);
        reindex();
        logger.debug("user:{}   id:{}", person, person.getId());
        assertTrue("person id should be set - id:" + person.getId(), person.getId() != 1L);
        firstGroup().getResourceCreatorProxies().add(new ResourceCreatorProxy(person, null));

        doSearch();
        assertTrue(String.format("expecting %s in results", resource), controller.getResults().contains(resource));
    }

    @Test
    @Rollback(true)
    public void testCalDateSearch() throws InstantiationException, IllegalAccessException {
        Document exact = createDocumentWithDates(-1000, 1200);
        Document interior = createDocumentWithDates(-500, 1000);
        Document start = createDocumentWithDates(-1500, 1000);
        Document end = createDocumentWithDates(-500, 2000);
        Document before = createDocumentWithDates(-1300, -1100);
        Document after = createDocumentWithDates(1300, 2000);
        genericService.saveOrUpdate(start, end, interior, exact, after, before);
        searchIndexService.index(exact, interior, start, end, after, before);

        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 1200);
        firstGroup().getCoverageDates().add(cd);
        doSearch();

        assertFalse("expecting multiple results", controller.getResults().isEmpty());
        assertTrue(controller.getResults().contains(start));
        assertTrue(controller.getResults().contains(end));
        assertTrue(controller.getResults().contains(interior));
        assertTrue(controller.getResults().contains(exact));
        assertFalse(controller.getResults().contains(before));
        assertFalse(controller.getResults().contains(after));
    }

    private Document createDocumentWithDates(int i, int j) throws InstantiationException, IllegalAccessException {
        Document document = createAndSaveNewInformationResource(Document.class);
        CoverageDate date = new CoverageDate(CoverageType.CALENDAR_DATE, i, j);
        document.getCoverageDates().add(date);
        genericService.saveOrUpdate(date);
        return document;
    }

    @Test
    @Rollback(true)
    public void testLegacyKeywordSearch() throws Exception {
        Document doc = createAndSaveNewInformationResource(Document.class);
        Set<CultureKeyword> cultureKeywords = genericKeywordService.findOrCreateByLabels(CultureKeyword.class, Arrays.asList("iamaculturekeyword"));
        Set<SiteNameKeyword> siteNames = genericKeywordService.findOrCreateByLabels(SiteNameKeyword.class, Arrays.asList("thisisasitename"));
        Set<SiteTypeKeyword> siteTypes = genericKeywordService.findOrCreateByLabels(SiteTypeKeyword.class, Arrays.asList("asitetypekeyword"));

        doc.setCultureKeywords(cultureKeywords);
        doc.setSiteNameKeywords(siteNames);
        doc.setSiteTypeKeywords(siteTypes);
        genericService.saveOrUpdate(doc);
        reindex();

        controller.getUncontrolledCultureKeywords().add(cultureKeywords.iterator().next().getLabel());
        assertOnlyOneResult(doc);
        resetController();

        controller.getUncontrolledSiteTypeKeywords().add(siteTypes.iterator().next().getLabel());
        assertOnlyOneResult(doc);
        resetController();

        controller.getSiteNameKeywords().add(siteNames.iterator().next().getLabel());
        assertOnlyOneResult(doc);
    }

    @Test
    @Rollback(true)
    // TODO: modify this test to do additional checks on what we define as "good grammar", right now it only tests for a one-off bug (repetition)
    public void testAllFieldsSearchDescriptionGrammar() {
        String TEST_VALUE = "spam"; // damn vikings!
        controller.setQuery(TEST_VALUE);
        controller.search();
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        logger.debug("search phrase:{}", controller.getSearchPhrase());
        int occurances = controller.getSearchPhrase().split(TEST_VALUE).length;
        assertTrue("search description should have gooder english than it currently does", occurances <= 2);
    }

    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testSparseObjectLoading() {
        String colname = "my fancy collection";
        Project proj = createAndSaveNewResource(Project.class);
        ResourceCollection coll = createAndSaveNewResourceCollection(colname);
        searchIndexService.index(coll);
        searchIndexService.index(proj);

        // simulate searchParamerters that represents a project at [0] and collection at [1]
        firstGroup().getProjects().add(sparseProject(proj.getId()));
        firstGroup().getCollections().add(null); // [0]
        firstGroup().getCollections().add(sparseCollection(coll.getId())); // [1]

        controller.search();

        // skeleton lists should have been loaded w/ sparse records...
        assertEquals(proj.getTitle(), firstGroup().getProjects().get(0).getTitle());
        assertEquals(colname, firstGroup().getCollections().get(1).getName());
    }

    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testSparseObjectNameLoading() {
        String colname = "my fancy collection";
        Project proj = createAndSaveNewResource(Project.class);
        ResourceCollection coll = createAndSaveNewResourceCollection(colname);
        searchIndexService.index(coll);
        proj.getResourceCollections().add(coll);
        searchIndexService.index(proj);

        // simulate searchParamerters that represents a project at [0] and collection at [1]
        // firstGroup().getProjects().add(new Project(null,proj.getName()));
        // firstGroup().getCollections().add(null); // [0]
        firstGroup().getCollections().add(new ResourceCollection(colname, null, null, null, true, null)); // [1]

        controller.search();

        // skeleton lists should have been loaded w/ sparse records...
        // assertEquals(proj.getTitle(), firstGroup().getProjects().get(0).getTitle());
        assertEquals(colname, firstGroup().getCollections().get(0).getName());
        assertTrue(controller.getResults().contains(proj));
        // assertEquals(proj.getId(), firstGroup().getProjects().get(0).getId());
        // assertEquals(coll.getId(), firstGroup().getCollections().get(1).getId());
    }

    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testLookupObjectLoading() {
        String colname = "my fancy collection";
        Project proj = createAndSaveNewResource(Project.class);
        proj.setTitle(colname);
        Document doc1 = createAndSaveNewResource(Document.class);
        doc1.setProject(proj);
        genericService.saveOrUpdate(doc1);
        genericService.saveOrUpdate(proj);
        ResourceCollection coll = createAndSaveNewResourceCollection(colname);
        searchIndexService.index(doc1, proj);

        // simulate searchParamerters that represents a project at [0] and collection at [1]
        firstGroup().getProjects().add(new Project(-1L, colname));

        controller.search();

        // skeleton lists should have been loaded w/ sparse records...
        assertEquals(proj.getTitle(), firstGroup().getProjects().get(0).getTitle());
        assertTrue(controller.getResults().contains(doc1));
    }

    @Test
    @Rollback
    public void testPersonRoles() {
        assertTrue(controller.getRelevantPersonRoles().contains(ResourceCreatorRole.SUBMITTER));
        assertTrue(controller.getRelevantPersonRoles().contains(ResourceCreatorRole.UPDATER));
        assertFalse(controller.getRelevantPersonRoles().contains(ResourceCreatorRole.RESOURCE_PROVIDER));
    }

    @Test
    @Rollback
    public void testInstitutionRoles() {
        assertFalse(controller.getRelevantInstitutionRoles().contains(ResourceCreatorRole.SUBMITTER));
        assertFalse(controller.getRelevantInstitutionRoles().contains(ResourceCreatorRole.UPDATER));
        assertTrue(controller.getRelevantInstitutionRoles().contains(ResourceCreatorRole.RESOURCE_PROVIDER));
    }

    @Test
    public void testRefineSimpleSearch() {
        // simulate /search?query=this is a test. We expect the form to pre-populate with this search term
        String query = "this is a test";
        controller.setQuery(query);
        controller.advanced();
        assertTrue("first group should have one term", firstGroup().getAllFields().size() > 0);
        assertEquals("query should appear on first term", query, firstGroup().getAllFields().get(0));
    }

    @Test
    // if user gets to the results page via clicking on persons name from resource view page, querystring only contains person.id field. So before
    // rendering the 'refine your search' version of the search form the controller must inflate query components.
    public void testRefineSearchWithSparseProject() {
        Project persisted = createAndSaveNewProject();
        Project sparse = new Project();
        // ensure the project is in
        genericService.synchronize();
        sparse.setId(persisted.getId());
        firstGroup().getProjects().add(sparse);
        controller.advanced();

        assertEquals("sparse project should have been inflated", persisted.getTitle(), firstGroup().getProjects().get(0).getTitle());
    }

    private void assertOnlyOneResult(InformationResource informationResource) {
        doSearch();
        assertEquals("expecting two results: doc and project", 2, controller.getResults().size());
        assertTrue("expecting resource in results", controller.getResults().contains(informationResource));
        assertTrue("expecting resource's project in results", controller.getResults().contains(informationResource.getProject()));
    }

    private Resource constructActiveResourceWithCreator(Creator creator, ResourceCreatorRole role) {
        try {
            Document doc = createAndSaveNewInformationResource(Document.class);
            ResourceCreator resourceCreator = new ResourceCreator(creator, role);
            doc.getResourceCreators().add(resourceCreator);
            return doc;
        } catch (Exception ignored) {
        }
        fail();
        return null;
    }

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    protected boolean resultsContainId(Long id) {
        boolean found = false;
        for (Resource r_ : controller.getResults()) {
            Resource r = (Resource) r_;
            logger.trace(r.getId() + " " + r.getResourceType());
            if (id.equals(r.getId()))
                found = true;
        }
        return found;
    }

    protected void reindex() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), Resource.class, Person.class, Institution.class, ResourceCollection.class);
    }

    protected void doSearch() {
        controller.search();
        logger.info("search found: " + controller.getTotalRecords());
    }

    protected Long setupImage() {
        return setupImage(getUser());
    }

    protected Long setupImage(Person user) {
        Image img = new Image();
        img.setTitle("precambrian Test");
        img.setDescription("image description");
        img.markUpdated(user);
        CultureKeyword label = genericKeywordService.findByLabel(CultureKeyword.class, "Folsom");
        CultureKeyword label2 = genericKeywordService.findByLabel(CultureKeyword.class, "Early Archaic");
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox(-117.101, 33.354, -117.124, 35.791);
        img.setLatitudeLongitudeBox(latLong);
        assertNotNull(label.getId());
        img.getCultureKeywords().add(label);
        img.getCultureKeywords().add(label2);
        img.setStatus(Status.DRAFT);
        genericService.save(img);
        genericService.save(latLong);
        Long imgId = img.getId();
        return imgId;
    }

}

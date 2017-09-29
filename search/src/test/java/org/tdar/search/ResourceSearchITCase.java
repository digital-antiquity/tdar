package org.tdar.search;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.junit.TdarAssert;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.ResourceLookupObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.CreatorSearchService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.range.DateRange;

public class ResourceSearchITCase  extends AbstractResourceSearchITCase {



    @Autowired
    CreatorSearchService<Creator<?>> creatorSearchService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    EntityService entityService;

    @Test
    public void testInvalidPhrase() throws ParseException, SearchException, SearchIndexException, IOException,SearchException, SearchIndexException {
        ResourceLookupObject look = new ResourceLookupObject();
        look.setTerm("he operation and evolution of an irrigation system\"");
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
        resourceSearchService.lookupResource(getAdminUser(), look, result , MessageHelper.getInstance());
    }

    @Test
    public void testInvalidWithColon() throws ParseException, SearchException, SearchIndexException, IOException,SearchException, SearchIndexException {
        ResourceLookupObject look = new ResourceLookupObject();
        look.setTerm("Temporal Control in the Southern North Coast Ranges of California: The Application of Obsidian Hydration Analysis");
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
        resourceSearchService.lookupResource(getAdminUser(), look, result , MessageHelper.getInstance());
    }

    @Test
    public void testInvalidWithColon2() throws ParseException, SearchException, SearchIndexException, IOException {
        SearchParameters sp = new SearchParameters();
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        asqo.getSearchParameters().add(sp);
        sp.getAllFields().add("Temporal Control in the Southern North Coast Ranges of California: The Application of Obsidian Hydration Analysis");
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
        resourceSearchService.buildAdvancedSearch(asqo, getAdminUser(), result, MessageHelper.getInstance());
    }
    
    @Test
    @Rollback
    public void testSiteNameKeywords() throws SearchException, SearchIndexException, IOException, ParseException {
        SiteNameKeyword snk = genericKeywordService.findByLabel(SiteNameKeyword.class, "Atsinna");
        Document doc = createAndSaveNewResource(Document.class);
        doc.getSiteNameKeywords().add(snk);
        genericService.saveOrUpdate(doc);
        SearchParameters sp = new SearchParameters();
        sp.getSiteNames().add(snk.getLabel());
        SearchResult<Resource> result = doSearch(null,null,sp,null);
        assertFalse("we should get back at least one hit", result.getResults().isEmpty());
        for (Indexable resource : result.getResults()) {
            assertTrue("expecting site name for resource", ((Resource)resource).getSiteNameKeywords().contains(snk));
        }
    }
    
    @Test
    @Rollback
    public void testSiteCode() throws SearchException, SearchIndexException, IOException, ParseException {
        SiteNameKeyword snk = new SiteNameKeyword("38-AK-500");
        genericService.saveOrUpdate(snk);
        Document doc = createAndSaveNewResource(Document.class);
        doc.getSiteNameKeywords().add(snk);
        genericService.saveOrUpdate(doc);
        SearchParameters sp = new SearchParameters();
        sp.getAllFields().add("38ak500");
        SearchResult<Resource> result = doSearch(null,null,sp,null);
        assertFalse("we should get back at least one hit", result.getResults().isEmpty());
        for (Indexable resource : result.getResults()) {
            assertTrue("expecting site name for resource", ((Resource)resource).getSiteNameKeywords().contains(snk));
        }
    }


    @Test
    @Rollback
    public void testComplexGeographicKeywords() throws SearchException, SearchIndexException, IOException, ParseException {
        GeographicKeyword snk = genericKeywordService.findOrCreateByLabel(GeographicKeyword.class, "propylon, Athens, Greece, Mnesicles");
        Document doc = createAndSaveNewResource(Document.class);
        doc.getGeographicKeywords().add(snk);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getGeographicKeywords().add("Greece");
        SearchResult<Resource> result = doSearch(null,null, sp,null);
        assertFalse("we should get back at least one hit", result.getResults().isEmpty());
        for (Resource resource : result.getResults()) {
            assertTrue("expecting site name for resource", ((Resource)resource).getGeographicKeywords().contains(snk));
        }
    }

    @Test
    @Rollback
    public void testMultiplePersonSearch() throws ParseException, SearchException, SearchIndexException, IOException {
        Long peopleIds[] = { 8044L, 8344L, 8393L, 8608L, 8009L };
        List<Person> people = genericService.findAll(Person.class, Arrays.asList(peopleIds));
        assertEquals(4, people.size());
        logger.info("{}", people);
        List<String> names = new ArrayList<String>();
        SearchParameters sp = new SearchParameters();
        for (Person person : people) {
            names.add(person.getProperName());
            Person p = new Person();
            // this will likely fail because skeleton people are being put into a set further down the chain...
            p.setId(person.getId());
            ResourceCreator rc = new ResourceCreator(p, null);
            sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(rc));
        }
        SearchResult<Resource> result = doSearch(null,null,sp,null);
        logger.info(result.getSearchTitle());
        for (String name : names) {
            assertTrue(result.getSearchTitle().contains(name));
        }
        // lookForCreatorNameInResult(lastName, person);
    }


    @Test
    @Rollback
    public void testSearchDecade() throws SearchException, SearchIndexException, IOException, ParseException {
        Document doc = createAndSaveNewResource(Document.class);
        doc.setDate(4000);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getCreationDecades().add(4000);
        SearchResult<Resource> result = doSearch(null, null, sp, null);
        assertFalse("we should get back at least one hit", result.getResults().isEmpty());
        for (Indexable resource : result.getResults()) {
            assertEquals("expecting resource", 4000, ((InformationResource) resource).getDateNormalized().intValue());
        }

    }

    @Test
    @Rollback
    public void testApprovedSiteTypeKeywords() throws ParseException, SearchException, SearchIndexException, IOException {
        final Long keywordId = 256L;
        Keyword keyword = genericService.find(SiteTypeKeyword.class, keywordId);
        List<String> keywordIds = new ArrayList<String>();
        keywordIds.add(keywordId.toString());
        SearchParameters sp = new SearchParameters();
        sp.getApprovedSiteTypeIdLists().add(keywordIds);
        SearchResult<Resource> result = doSearch(null, null, sp, null);
        assertFalse("we should get back at least one hit", result.getResults().isEmpty());
        assertTrue(resultsContainId(result, 262L));
//        result.getIncludedStatuses().add(Status.ACTIVE);
        for (Indexable resource : result.getResults()) {
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
                logger.debug("resourceid:{} contents of resource:", resource.getId(), ((Resource)resource).getActiveSiteTypeKeywords());
                assertTrue("expecting site type for resource:", ((Resource)resource).getActiveSiteTypeKeywords().contains(keyword));
            }
        }
    }

    @Test
    @Rollback
    public void testMaterialKeywords() throws ParseException, SearchException, SearchIndexException, IOException {
        // FIXME: magic numbers
        Keyword keyword = genericService.find(MaterialKeyword.class, 2L);
        SearchParameters sp = new SearchParameters();
        sp.getMaterialKeywordIdLists().add(Arrays.asList(keyword.getId().toString()));
        SearchResult<Resource> result = doSearch(null,null, sp, null);
        assertTrue("we should get back at least one hit", !result.getResults().isEmpty());
        // every resource in results should have that material keyword (or should have at least one informationResource that has that keyword)
        for (Indexable resource : result.getResults()) {
            Set<Keyword> keywords = new HashSet<Keyword>(((Resource)resource).getActiveMaterialKeywords());
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
    public void testCulture() throws ParseException, SearchException, SearchIndexException, IOException {
        // FIXME: this test is brittle/incomplete
        String label = "Sinagua";
        SearchParameters sp = new SearchParameters();
        sp.getUncontrolledCultureKeywords().add(label);
        Keyword keyword = genericKeywordService.findByLabel(CultureKeyword.class, label);

        SearchResult<Resource> result = doSearch(null, null,sp ,null);
        assertTrue("we should get back at least one hit", !result.getResults().isEmpty());

        for (Indexable resource : result.getResults()) {
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
                logger.debug("resourceid:{} contents of resource:", resource.getId(), ((Resource)resource).getActiveCultureKeywords());
                assertTrue("expecting site type for resource:", ((Resource)resource).getActiveCultureKeywords().contains(keyword));
            }
        }

    }

    @Test
    @Rollback
    public void testApprovedCulture() throws ParseException, SearchException, SearchIndexException, IOException {
        // FIXME: pull this ID from db or generate/save new keyword+resource that uses it
        Long keywordId = 19L;
        Keyword keyword = genericService.find(CultureKeyword.class, keywordId);
        SearchParameters sp = new SearchParameters();
        sp.getApprovedCultureKeywordIdLists().add(Arrays.asList(keywordId.toString()));
        SearchResult<Resource> result = doSearch(null,null,sp ,null);
        assertTrue("we should get back at least one hit", !result.getResults().isEmpty());
        for (Indexable resource : result.getResults()) {
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
                logger.debug("resourceid:{} contents of resource:", resource.getId(), ((Resource)resource).getActiveCultureKeywords());
                assertTrue("expecting site type for resource:", ((Resource)resource).getActiveCultureKeywords().contains(keyword));
            }
        }
    }
    


    @Test
    @Rollback
    public void testProjectIds() throws ParseException, SearchException, SearchIndexException, IOException {
        // FIXME: magic numbers
        Long projectId = 3805L;
        SearchParameters sp = new SearchParameters();
        sp.getProjects().add(sparseProject(projectId));
        ReservedSearchParameters rsp = new ReservedSearchParameters();
        rsp.getObjectTypes().clear(); // select all resource types
        SearchResult<Resource> result = doSearch(null,null,sp, null);
        int resourceCount = 0;
        for (Indexable resource : result.getResults()) {
            if (resource instanceof InformationResource) {
                resourceCount++;
                InformationResource informationResource = (InformationResource) resource;
                assertEquals("informationResource should belong to project we just searched for", projectId, informationResource.getProjectId());
            }
        }
        assertTrue("search should have at least 1 result", resourceCount > 0);
    }
    


    @Test
    @Rollback
    public void testSearchSubmitterIds() throws ParseException, SearchException, SearchIndexException, IOException {
        // FIXME: magic numbers
        Person person = genericService.find(Person.class, 6L);
        SearchParameters sp = new SearchParameters();
        sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(person, ResourceCreatorRole.SUBMITTER));
        SearchResult<Resource> result = doSearch(null,null,sp, null);

        // make sure every resource has that submitter
        for (Indexable resource : result.getResults()) {
            assertEquals("Expecting same submitterId", person.getId(), ((Resource)resource).getSubmitter().getId());
        }
    }


    @Test
    @Rollback(true)
    public void testGeographicKeywordIndexedAndFound() throws SearchException, SearchIndexException, IOException, ParseException {
        Document doc = createAndSaveNewResource(Document.class, getBasicUser(), "testing doc");
        GeographicKeyword kwd = new GeographicKeyword();
        kwd.setLabel("Casa NonGrande");
        genericService.save(kwd);
        doc.getGeographicKeywords().add(kwd);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getGeographicKeywords().add("Casa NonGrande");
        SearchResult<Resource> result = doSearch(null, null, sp, null);
        boolean seen = false;
        for (Indexable res : result.getResults()) {
            logger.info("{}", res);
            if (((Resource)res).getGeographicKeywords().contains(kwd)) {
                seen = true;
            } else {
                fail("found resource without keyword");
            }
        }
        assertTrue(seen);
    }


    @Test
    @Rollback
    public void testResourceCount() {
        // fixme: remove this query. it's only temporary to ensure that my named query is working
        long count = resourceService.getResourceCount(ResourceType.PROJECT, Status.ACTIVE);
        assertTrue(count > 0);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testResourceUpdated() throws java.text.ParseException, ParseException, SearchException, SearchIndexException, IOException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Document document = new Document();
        document.setTitle("just before");
        document.setDescription("just before");
        document.markUpdated(getAdminUser());
        document.setDateUpdated(format.parse("2010-03-04"));
        genericService.saveOrUpdate(document);
        Document documentAfter = new Document();
        documentAfter.setTitle("just after");
        documentAfter.setDescription("just after");
        documentAfter.markUpdated(getAdminUser());
        documentAfter.setDateUpdated(format.parse("2010-07-23"));
        genericService.saveOrUpdate(documentAfter);
        genericService.synchronize();

        SearchParameters params = new SearchParameters();
        params.getUpdatedDates().add(new DateRange(format.parse("2010-03-05"), format.parse("2010-07-22")));
        SearchResult<Resource> result = doSearch(null,null, params, null, SortOption.DATE_UPDATED);
        for (Indexable r : result.getResults()) {
            logger.debug("{} - {} - {}", r.getId(), ((Resource)r).getDateUpdated(), ((Resource)r).getTitle());
        }
        assertFalse(result.getResults().contains(documentAfter));
        assertFalse(result.getResults().contains(document));
    }

    /**
     * lucene translates dates to utc prior to indexing. When performing a search the system must similarly transform the begin/end
     * dates in a daterange
     * @throws IOException 
     * @throws SearchException, SearchIndexException 
     * @throws ParseException 
     */
    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testTimezoneEdgeCase() throws ParseException, SearchException, SearchIndexException, IOException {
        Resource doc = createAndSaveNewInformationResource(Document.class);
        DateTime createDateTime = new DateTime(2005, 3, 26, 23, 0, 0, 0);
        DateTime searchDateTime = new DateTime(2005, 3, 26, 0, 0, 0, 0);
        doc.setDateCreated(createDateTime.toDate());
        doc.setDateUpdated(createDateTime.toDate());
        genericService.saveOrUpdate(doc);
        genericService.synchronize();
        searchIndexService.index(doc);

        // converstion from MST to UTC date advances registration date by one day.
        DateRange dateRange = new DateRange();
        dateRange.setStart(searchDateTime.toDate());
        dateRange.setEnd(searchDateTime.plusDays(1).toDate());

        SearchParameters sp = new SearchParameters();
        sp.getRegisteredDates().add(dateRange);
        SearchResult<Resource> result = doSearch(null,null,sp, null);
        Long docId = doc.getId();
        assertThat(PersistableUtils.extractIds(result.getResults()), contains(docId));

        // if we advance the search begin/end by one day, we should not see it in search results
        dateRange.setStart(searchDateTime.plusDays(1).toDate());
        dateRange.setEnd(searchDateTime.plusDays(2).toDate());
        sp.getRegisteredDates().add(dateRange);
        result = doSearch(null,null,sp, null);
        assertThat(PersistableUtils.extractIds(result.getResults()), not(contains(docId)));

        // if we decrement the search begin/end by one day, we should not see it in search results
        dateRange.setStart(searchDateTime.minusDays(1).toDate());
        dateRange.setEnd(searchDateTime.toDate());
        sp.getRegisteredDates().add(dateRange);
        result = doSearch(null,null,sp, null);
        assertThat(PersistableUtils.extractIds(result.getResults()), not(contains(docId)));
    }

    @Test
    @Rollback
    public void testOtherKeywords() throws InstantiationException, IllegalAccessException, ParseException, SearchException, SearchIndexException, IOException {
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
        SearchParameters sp = new SearchParameters();
        ReservedSearchParameters rsp = new ReservedSearchParameters();

        sp.getOtherKeywords().add(ok.getLabel());
        rsp.getObjectTypes().add(ObjectType.DOCUMENT);
        SearchResult<Resource> result = doSearch(null,null,sp, rsp);
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(result.getResults());
        assertEquals("only expecting one result", 1L, result.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(result, ok.getLabel());
    }

    @Test
    @Rollback
    public void testTemporalKeywords() throws ParseException, InstantiationException, IllegalAccessException, SearchException, SearchIndexException, IOException {
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
        SearchParameters sp = new SearchParameters();
        ReservedSearchParameters rsp = new ReservedSearchParameters();

        sp.getTemporalKeywords().add(tk.getLabel());
        rsp.getObjectTypes().add(ObjectType.DOCUMENT);
        SearchResult<Resource> result = doSearch(null,null,sp, rsp);
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(result.getResults());
        assertEquals("only expecting one result", 1L, result.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(result, tk.getLabel());
    }

    @Test
    @Rollback
    public void testGeoKeywords() throws InstantiationException, IllegalAccessException, ParseException, SearchException, SearchIndexException, IOException {
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
        SearchParameters sp = new SearchParameters();
        sp.getGeographicKeywords().add(gk.getLabel());
        ReservedSearchParameters rsp = new ReservedSearchParameters();
        rsp.getObjectTypes().add(ObjectType.DOCUMENT);
        SearchResult<Resource> result = doSearch(null,null,sp, rsp);
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(result.getResults());
        assertEquals("only expecting one result", 1L, result.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(result, gk.getLabel());
    }

    @Test
    @Rollback
    public void testSearchBySubmitterIds() throws InstantiationException, IllegalAccessException, ParseException, SearchException, SearchIndexException, IOException {
        Document doc = createDocumentWithContributorAndSubmitter();
        Long submitterId = doc.getSubmitter().getId();
        assertFalse(submitterId == -1);
        SearchParameters sp = new SearchParameters();
        sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(doc.getSubmitter(), ResourceCreatorRole.SUBMITTER));
        SearchResult<Resource> result = doSearch(null,null,sp, null);

        assertTrue("only one result expected", 1 <= result.getResults().size());
        assertTrue(result.getResults().contains(doc));
    }

    @Test
    @Rollback
    public void testSearchContributorIds2() throws InstantiationException, IllegalAccessException, ParseException, SearchException, SearchIndexException, IOException {
        Document doc = createDocumentWithContributorAndSubmitter();
        ResourceCreator contributor = doc.getResourceCreators().iterator().next();
        SearchParameters sp = new SearchParameters();
        sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(contributor.getCreator(), contributor.getRole()));
        
        SearchResult<Resource> result = doSearch(null,null,sp, null);

        assertEquals("only one result expected", 1L, result.getResults().size());
        assertEquals(doc, result.getResults().iterator().next());
    }

    public void assertSearchPhrase(SearchResult<Resource> result, String term) {
        logger.debug("term:{}\t search phrase:{}", term, result.getSearchTitle());
        assertTrue(String.format("looking for string '%s' in search phrase '%s'", term, result.getSearchTitle()),
                result.getSearchTitle().toLowerCase().contains(term.toLowerCase()));
    }


    @Test
    @Rollback
    public void testResourceCreatorPerson() throws ParseException, SearchException, SearchIndexException, IOException {
        Person person = new Person("Bob", "Loblaw", null);
        genericService.save(person);
        Resource resource = constructActiveResourceWithCreator(person, ResourceCreatorRole.AUTHOR);
        logger.info("resource: {}", resource);
        reindex();
        logger.debug("user:{}   id:{}", person, person.getId());
        assertTrue("person id should be set - id:" + person.getId(), person.getId() != 1L);

        SearchParameters sp = new SearchParameters();
        sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(person, ResourceCreatorRole.AUTHOR));

        SearchResult<Resource> result = doSearch(null,null,sp, null);

        logger.info("{}", result.getResults());
        assertTrue(String.format("expecting %s in results", resource), result.getResults().contains(resource));
        assertEquals("should be one and only one result", 1, result.getResults().size());
    }

    @Test
    @Rollback
    public void testResourceCreatorWithAnyRole() throws ParseException, SearchException, SearchIndexException, IOException {
        Person person = new Person("Bob", "Loblaw", null);
        genericService.save(person);
        Resource resource = constructActiveResourceWithCreator(person, ResourceCreatorRole.AUTHOR);
        reindex();
        logger.debug("user:{}   id:{}", person, person.getId());
        assertTrue("person id should be set - id:" + person.getId(), person.getId() != 1L);
        SearchParameters sp = new SearchParameters();
        sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(person, null));

        SearchResult<Resource> result = doSearch(null,null,sp, null);
        assertTrue(String.format("expecting %s in results", resource), result.getResults().contains(resource));
    }

    @Test
    @Rollback
    public void testBooleanSearch() throws InstantiationException, IllegalAccessException, SearchException, SearchIndexException, IOException, ParseException {
        Document doc1 = generateDocumentWithUser();
        Document doc2 = generateDocumentWithUser();
        GeographicKeyword istanbul = new GeographicKeyword();
        istanbul.setLabel(ISTANBUL);
        GeographicKeyword constantinople = new GeographicKeyword();
        constantinople.setLabel(CONSTANTINOPLE);
        genericService.save(istanbul);
        genericService.save(constantinople);
        doc1.getGeographicKeywords().add(istanbul);
        doc2.getGeographicKeywords().add(constantinople);
        genericService.saveOrUpdate(doc1);
        genericService.saveOrUpdate(doc2);
        evictCache();
        searchIndexService.index(doc1, doc2);

        SearchParameters params = new SearchParameters();
        params.setAllFields(Arrays.asList(ISTANBUL, CONSTANTINOPLE));
        params.setOperator(Operator.OR);
        
        SearchResult<Resource> result = doSearch(null,null,params, null);
        assertTrue(result.getResults().contains(doc1));
        assertTrue(result.getResults().contains(doc2));
        logger.debug("results:{}", result.getResults());

        params.setOperator(Operator.AND);
        result = doSearch(null,null,params, null);
        logger.debug("results:{}", result.getResults());
        assertFalse(result.getResults().contains(doc1));
        assertFalse(result.getResults().contains(doc2));
    }

    @Test
    @Rollback(true)
    public void testCalDateSearch() throws InstantiationException, IllegalAccessException, SearchException, SearchIndexException, IOException, ParseException {
        Document exact = createDocumentWithDates(-1000, 1200);
        Document interior = createDocumentWithDates(-500, 1000);
        Document start = createDocumentWithDates(-1500, 1000);
        Document end = createDocumentWithDates(-500, 2000);
        Document before = createDocumentWithDates(-1300, -1100);
        Document after = createDocumentWithDates(1300, 2000);
        genericService.saveOrUpdate(start, end, interior, exact, after, before);
        searchIndexService.index(exact, interior, start, end, after, before);

        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 1200);
        SearchParameters sp = new SearchParameters();
        sp.getCoverageDates().add(cd);
        SearchResult<Resource> result = doSearch(null,null,sp, null);

        assertFalse("expecting multiple results", result.getResults().isEmpty());
        assertTrue(result.getResults().contains(start));
        assertTrue(result.getResults().contains(end));
        assertTrue(result.getResults().contains(interior));
        assertTrue(result.getResults().contains(exact));
        assertFalse(result.getResults().contains(before));
        assertFalse(result.getResults().contains(after));
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testLegacyKeywordSearch() throws Exception {
        Document doc = createAndSaveNewInformationResource(Document.class);
        Project proj = createAndSaveNewProject("parent");
        doc.setProject(proj);
        Set<CultureKeyword> cultureKeywords = genericKeywordService.findOrCreateByLabels(CultureKeyword.class, Arrays.asList("iamaculturekeyword"));
        Set<SiteNameKeyword> siteNames = genericKeywordService.findOrCreateByLabels(SiteNameKeyword.class, Arrays.asList("thisisasitename"));
        Set<SiteTypeKeyword> siteTypes = genericKeywordService.findOrCreateByLabels(SiteTypeKeyword.class, Arrays.asList("asitetypekeyword"));

        doc.setCultureKeywords(cultureKeywords);
        doc.setSiteNameKeywords(siteNames);
        doc.setSiteTypeKeywords(siteTypes);
        genericService.saveOrUpdate(doc);
        genericService.synchronize();
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());

        SearchParameters sp = new SearchParameters();
        sp.getUncontrolledCultureKeywords().add(cultureKeywords.iterator().next().getLabel());
        SearchResult<Resource> result = doSearch(null, null, sp, null);
        assertOnlyResultAndProject(result, doc);

        sp = new SearchParameters();
        sp.getUncontrolledSiteTypes().add(siteTypes.iterator().next().getLabel());
        result = doSearch(null, null, sp, null);
        assertOnlyResultAndProject(result, doc);

        sp = new SearchParameters();
        sp.getSiteNames().add(siteNames.iterator().next().getLabel());
        result = doSearch(null, null, sp, null);
        assertOnlyResultAndProject(result, doc);
    }

    @Test
    @Rollback(true)
    // TODO: modify this test to do additional checks on what we define as "good grammar", right now it only tests for a one-off bug (repetition)
    public void testAllFieldsSearchDescriptionGrammar() throws ParseException, SearchException, SearchIndexException, IOException {
        String TEST_VALUE = "spam"; // damn vikings!

        SearchResult<Resource> result = doSearch(TEST_VALUE, null, null, null);

        for (int i = 0; i < 10; i++) {
            logger.debug("search phrase:{}", result.getSearchTitle());
        }
        int occurances = result.getSearchTitle().split(TEST_VALUE).length;
        assertTrue("search description should have gooder english than it currently does", occurances <= 2);
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testSparseObjectLoading() throws SearchException, SearchIndexException, IOException, ParseException {
        String colname = "my fancy collection";
        Project proj = createAndSaveNewResource(Project.class);
        ResourceCollection coll = createAndSaveNewResourceCollection(colname);
        searchIndexService.index(coll);
        searchIndexService.index(proj);

        SearchParameters sp = new SearchParameters();
        // simulate searchParamerters that represents a project at [0] and collection at [1]
        sp.getProjects().add(sparseProject(proj.getId()));
        sp.getShares().add(null); // [0]
        sp.getShares().add(sparseCollection(coll.getId())); // [1]

        SearchResult<Resource> result = doSearch(null, null, sp, null);

        // skeleton lists should have been loaded w/ sparse records...
        assertEquals(proj.getTitle(), sp.getProjects().get(0).getTitle());
        logger.debug("c's:{}",sp.getCollections());
        assertEquals(colname, ((ResourceCollection)sp.getShares().get(1)).getName());
    }

    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testSparseObjectNameLoading() throws SearchException, SearchIndexException, IOException, ParseException {
        String colname = "my fancy collection";
        logger.debug("self assignable to self? {}",ResourceCollection.class.isAssignableFrom(ResourceCollection.class));
        Project proj = createAndSaveNewResource(Project.class);
        ResourceCollection coll = createAndSaveNewResourceCollection(colname);
        searchIndexService.index(coll);
        proj.getManagedResourceCollections().add(coll);
        coll.getManagedResources().add(proj);
        genericService.saveOrUpdate(proj);
        genericService.saveOrUpdate(coll);
        searchIndexService.index(proj);

        // simulate searchParamerters that represents a project at [0] and collection at [1]
        // sp.getProjects().add(new Project(null,proj.getName()));
        // sp.getCollections().add(null); // [0]
        SearchParameters sp = new SearchParameters();
        sp.getShares().add(new ResourceCollection(colname,null, null)); // [1]

        SearchResult<Resource> result = doSearch(null, null, sp, null);

        
        // skeleton lists should have been loaded w/ sparse records...
        // assertEquals(proj.getTitle(), sp.getProjects().get(0).getTitle());
        assertEquals(colname, ((ResourceCollection)sp.getShares().get(0)).getName());
        assertTrue(result.getResults().contains(proj));
        // assertEquals(proj.getId(), sp.getProjects().get(0).getId());
        // assertEquals(coll.getId(), sp.getCollections().get(1).getId());
    }

    @SuppressWarnings({  "unused" })
    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testLookupObjectLoading() throws SearchException, SearchIndexException, IOException, ParseException {
        String colname = "my fancy collection";
        Project proj = createAndSaveNewResource(Project.class);
        proj.setTitle(colname);
        Document doc1 = createAndSaveNewResource(Document.class);
        doc1.setProject(proj);
        genericService.saveOrUpdate(doc1);
        genericService.saveOrUpdate(proj);
//        SharedCollection coll = createAndSaveNewResourceCollection(colname);
        searchIndexService.index(doc1, proj);

        // simulate searchParamerters that represents a project at [0] and collection at [1]
        SearchParameters sp = new SearchParameters();
        sp.getProjects().add(new Project(-1L, colname));
        SearchResult<Resource> result = doSearch(null, null, sp, null);

        // skeleton lists should have been loaded w/ sparse records...
        assertEquals(proj.getTitle(), sp.getProjects().get(0).getTitle());
        assertTrue(PersistableUtils.extractIds(result.getResults()).contains(doc1.getId()));
    }



    @SuppressWarnings("unused")
    @Test
    // if user gets to the results page via clicking on persons name from resource view page, querystring only contains person.id field. So before
    // rendering the 'refine your search' version of the search form the controller must inflate query components.
    public void testRefineSearchWithSparseProject() throws ParseException, SearchException, SearchIndexException, IOException {
        Project persisted = createAndSaveNewProject("PROJECT TEST TITLE");
        Project sparse = new Project();
        // ensure the project is in
        evictCache();
        sparse.setId(persisted.getId());
        SearchParameters sp = new SearchParameters();
        sp.getProjects().add(sparse);
        SearchResult<Resource> result = doSearch(null, null, sp, null);

        assertEquals("sparse project should have been inflated", persisted.getTitle(), sp.getProjects().get(0).getTitle());
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testRefineSearchWithSparseCollection() throws ParseException, SearchException, SearchIndexException, IOException {

        ResourceCollection rc = createAndSaveNewResourceCollection("Mega Collection");
        ResourceCollection sparseCollection = new ResourceCollection();
        evictCache();
        long collectionId = rc.getId();
        assertThat(collectionId, greaterThan(0L));
        sparseCollection.setId(collectionId);
        SearchParameters sp = new SearchParameters();
        sp.getCollections().add(sparseCollection);
        SearchResult<Resource> result = doSearch(null, null, sp, null);

        assertThat((sp.getCollections().get(0)).getTitle(), is("Mega Collection"));
    }

    private void assertOnlyResultAndProject(SearchResult<Resource> result, InformationResource informationResource) {
        assertEquals("expecting two results: doc and project", 2, result.getResults().size());
        assertTrue("expecting resource in results", result.getResults().contains(informationResource));
        assertTrue("expecting resource's project in results", result.getResults().contains(informationResource.getProject()));
    }

    private Resource constructActiveResourceWithCreator(Creator<?> creator, ResourceCreatorRole role) {
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

    protected boolean resultsContainId(SearchResult<Resource> result, Long id) {
        boolean found = false;
        for (Indexable r_ : result.getResults()) {
            Resource r = (Resource)r_;
            logger.trace(r.getId() + " " + r.getResourceType());
            if (id.equals(r.getId())) {
                found = true;
            }
        }
        return found;
    }

    
    @Test
    @Rollback(true)
    public void testCreatorOwnerQueryPart() throws ParseException, SearchException, SearchIndexException, IOException {
        
        Document authorDocument = new Document();
        authorDocument.setTitle("author");
        authorDocument.setDescription(REASON);
        authorDocument.markUpdated(getBasicUser());
        genericService.saveOrUpdate(authorDocument);
        authorDocument.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.AUTHOR));
        genericService.saveOrUpdate(authorDocument);
        searchIndexService.index(authorDocument);

        Document contribDocument = new Document();
        contribDocument.setTitle("contrib");
        contribDocument.setDescription(REASON);
        contribDocument.markUpdated(getBasicUser());
        genericService.saveOrUpdate(contribDocument);
        contribDocument.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.CONTACT));
        genericService.saveOrUpdate(contribDocument);
        searchIndexService.index(contribDocument);

        Document ownerDocument = new Document();
        ownerDocument.setTitle("owner");
        ownerDocument.setDescription(REASON);
        ownerDocument.markUpdated(getAdminUser());
        genericService.saveOrUpdate(ownerDocument);
        searchIndexService.index(ownerDocument);

        Document hiddenDocument = new Document();
        hiddenDocument.setTitle("hidden");
        hiddenDocument.setDescription(REASON);
        hiddenDocument.markUpdated(getAdminUser());
        genericService.saveOrUpdate(hiddenDocument);
        hiddenDocument.getResourceCreators().add(new ResourceCreator(getBasicUser(), ResourceCreatorRole.AUTHOR));
        genericService.saveOrUpdate(authorDocument);
        searchIndexService.index(hiddenDocument);

        FacetedResultHandler<Resource> result = new SearchResult<>(Integer.MAX_VALUE);
        result.setSortField(SortOption.RELEVANCE);
        resourceSearchService.generateQueryForRelatedResources(getAdminUser(), null, result, MessageHelper.getInstance());
        for (Resource r : result.getResults()) {
            List<Long> authorIds = new ArrayList<Long>();
            for (ResourceCreator cr : r.getContentOwners()) {
                authorIds.add(cr.getCreator().getId());
            }
            logger.debug("result: {} id:{} [s:{} | {}]", r.getTitle(), r.getId(), r.getSubmitter().getId(), authorIds);
        }
        assertFalse(result.getResults().contains(hiddenDocument));
        assertFalse(result.getResults().contains(contribDocument));
        assertTrue(result.getResults().contains(authorDocument));
        assertTrue(result.getResults().contains(ownerDocument));
    }



    @Test
    @Rollback(true)
    public void testSelectedResourceLookup() throws SearchException, SearchIndexException, IOException, ParseException {
        ResourceCollection collection = new ResourceCollection("test", "test", getUser());
        collection.markUpdated(getUser());
        Ontology ont = createAndSaveNewInformationResource(Ontology.class);
        genericService.saveOrUpdate(collection);
        collection.getManagedResources().add(ont);
        // babysitting bidirectional relationshi[
        genericService.saveOrUpdate(collection);
        ont.getManagedResourceCollections().add(collection);
        genericService.saveOrUpdate(ont);
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setObjectTypes(Arrays.asList(ObjectType.ONTOLOGY));
        SearchResult<Resource> result = performSearch("", null, collection.getId(), null, null, null, params, 100);
        assertFalse(result.getResults().isEmpty());
        assertTrue(result.getResults().contains(ont));
    }
    
    @Override
    public void reindex() {
        searchIndexService.purgeAll(LookupSource.RESOURCE);
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
    }

    @Test
    @Rollback(true)
    public void testModifyEditor() throws SearchException, SearchIndexException, IOException, ParseException {
        ReservedSearchParameters params = new ReservedSearchParameters();

        SearchResult<Resource> result = performSearch("", null, null, null, null, getEditorUser(), params, Permissions.MODIFY_METADATA, 1000);
        logger.debug("results:{}", result.getResults());
        List<Long> ids = PersistableUtils.extractIds(result.getResults());

        result = performSearch("", null, null, null, null, getAdminUser(), params, Permissions.MODIFY_METADATA, 1000);
        logger.debug("results:{}", result.getResults());
        List<Long> ids2 = PersistableUtils.extractIds(result.getResults());
        Assert.assertArrayEquals(ids.toArray(), ids2.toArray());
    }

    @Test
    public void testResourceLookupByType() throws SearchException, SearchIndexException, IOException, ParseException {
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
        // get back all documents
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT));
        SearchResult<Resource> result = performSearch("", null, null, null, null, getEditorUser(), params, 1000);

        List<Resource> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByTdarId() throws SearchException, SearchIndexException, IOException, ParseException {
        // get back all documents
        SearchResult<Resource> result = performSearch(TestConstants.TEST_DOCUMENT_ID, null, null, null, null, null, null, 1000);

        List<Resource> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByProjectId() throws SearchException, SearchIndexException, IOException, ParseException {
        SearchResult<Resource> result = performSearch("", 3073L, null, null, null, null, null, 1000);

        List<Resource> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    @Rollback(value = true)
    public void testDeletedResourceFilteredForNonAdmins() throws Exception {
        Project proj = createProject("project to be deleted");

        searchIndexService.index(proj);
        SearchResult<Resource> result = performSearch("", null, null, true, null, null, null, 1000);

        List<Resource> results = result.getResults();

        List<Long> ids = PersistableUtils.extractIds(results);

        logger.debug("list type:{}  contents:{}", results.getClass(), results);
        Long projectId = proj.getId();
        assertTrue(ids.contains(projectId));

        // now delete the resource and makes sure it doesn't show up for the common rabble
        logger.debug("result contents before delete: {}", result.getResults());
        proj.setStatus(Status.DELETED);
        genericService.saveOrUpdate(proj);
        searchIndexService.index(proj);

        result = performSearch("", null, null, true, null, null, null, 1000);

        ids = PersistableUtils.extractIds(result.getResults());

        logger.debug("result contents after delete: {}", result.getResults());
        assertFalse(ids.contains(projectId));

        // now pretend that it's an admin visiting the dashboard. Even though they can view/edit everything, deleted items
        // won't show up in their dashboard unless they are the submitter or have explicitly been given access rights, so we update the project's submitter
        proj.setSubmitter(getAdminUser());
        genericService.saveOrUpdate(proj);
        searchIndexService.index(proj);

        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setStatuses(new ArrayList<Status>(Arrays.asList(Status.values())));
        result = performSearch("", null, null, null, null, getAdminUser(), params, 1000);
        ids = PersistableUtils.extractIds(result.getResults());
        assertTrue(ids.contains(projectId));
    }

    // more accurately model how struts will create a project by having the controller do it
    private Project createProject(String title) {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(title);
        project.markUpdated(getAdminUser());
        genericService.save(project);
        Assert.assertNotNull(project.getId());
        assertTrue(project.getId() != -1L);
        return project;
    }

    // TODO: need filtered test (e.g. only ontologies in a certain project)

    public ReservedSearchParameters initControllerFields() {
        searchIndexService.indexAll(getAdminUser());
        List<String> types = new ArrayList<String>();
        types.add("DOCUMENT");
        types.add("ONTOLOGY");
        types.add("CODING_SHEET");
        types.add("IMAGE");
        types.add("DATASET");
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT,
                ObjectType.ONTOLOGY, ObjectType.IMAGE, ObjectType.DATASET,
                ObjectType.CODING_SHEET));
        return params;
    }

    @Test
    public void testResourceLookup() throws IOException, SearchException, SearchIndexException, ParseException {
        ReservedSearchParameters params = initControllerFields();
        SearchResult<Resource> result = performSearch("HARP", null, null, null, null, null, params, 1000);

        boolean seen = true;
        for (Indexable idx : result.getResults()) {
            if (!StringUtils.containsIgnoreCase( ((Resource) idx).getTitle(),"HARP")) {
                seen = false;
            }
        }
        assertFalse(result.getResults().size() == 0);
        assertTrue(seen);
    }

    
    @Test
    public void testResourceLookupById() throws IOException, SearchException, SearchIndexException, ParseException {
        ReservedSearchParameters params = initControllerFields();
        SearchResult<Resource> result = performSearch(TestConstants.TEST_DOCUMENT_ID, null, null, null, null, null, params, 1000);

        Long id = Long.parseLong(TestConstants.TEST_DOCUMENT_ID);
        for (Indexable idx : result.getResults()) {
            assertEquals(id, idx.getId());
        }
        assertFalse(result.getResults().size() == 0);
    }

    @Test
    @Rollback(value = true)
    public void testAdminDashboardAnyStatus() throws Exception {
        // have a regular user create a document in each status (except deleted)that should be visible when an admin looks for document with "any" status
        Document activeDoc = createAndSaveNewInformationResource(Document.class, getUser());
        activeDoc.setTitle("testActiveDoc");
        activeDoc.setStatus(Status.ACTIVE); // probably unnecessary
        Document draftDoc = createAndSaveNewInformationResource(Document.class, getUser());
        draftDoc.setTitle("testDraftDoc");
        draftDoc.setStatus(Status.DRAFT);
        Document flaggedDoc = createAndSaveNewInformationResource(Document.class, getUser());
        flaggedDoc.setTitle("testFlaggedaDoc");
        flaggedDoc.setStatus(Status.FLAGGED);
        List<Document> docs = Arrays.asList(activeDoc, draftDoc, flaggedDoc);
        genericService.saveOrUpdate(docs);
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());

        // login as an admin
        for (Document doc : docs) {
            SearchResult<Resource> result = performSearch(doc.getTitle(), getAdminUser(), Integer.MAX_VALUE);
            if (doc.isActive() || doc.isDraft()) {
                assertTrue(String.format("looking for '%s' when filtering ", doc),
                        result.getResults().contains(doc));
            } else {
                assertFalse(String.format("looking for '%s' when filtering ", doc), result.getResults().contains(doc));

            }
        }

        for (Document doc : docs) {
            ReservedSearchParameters params = new ReservedSearchParameters();
            params.setStatuses(Arrays.asList(Status.values()));
            SearchResult<Resource> result = performSearch(doc.getTitle(), null, null, null, null, getAdminUser(), params, Integer.MAX_VALUE);
            assertTrue(String.format("looking for '%s' when filtering", doc), result.getResults().contains(doc));
        }

    }


    protected static List<ObjectType> allResourceTypes = Arrays.asList(ObjectType.values());

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    GenericKeywordService genericKeywordService;
    


    @Test
    @Rollback(true)
    public void testResourceTypeSearchPhrase() throws ParseException, SearchException, SearchIndexException, IOException {
        ReservedSearchParameters reserved = new ReservedSearchParameters();
        reserved.getObjectTypes().add(ObjectType.IMAGE);
        SearchResult<Resource> result = doSearch("", null, null, reserved);
        for (Indexable r : result.getResults()) {
            assertEquals(ResourceType.IMAGE, ((Resource)r).getResourceType());
        }
    }

    @Test
    @Rollback(true)
    public void testExactTitleMatchInKeywordSearch() throws InstantiationException, IllegalAccessException, SearchException, SearchIndexException, IOException, ParseException {
        String resourceTitle = "Archeological Excavation at Site 33-Cu-314: A Mid-Nineteenth Century Structure on the Ohio and Erie Canal";
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        searchIndexService.index(document);
        setupTestDocuments();
        SearchResult<Resource> result = doSearch(resourceTitle);
        logger.info("results:{}", result.getResults());
        assertTrue(result.getResults().contains(document));
        assertTrue(result.getResults().get(0).equals(document) || result.getResults().get(1).equals(document));
    }


    @Test
    @Rollback(true)
    public void testFindResourceTypePhrase() throws ParseException, SearchException, SearchIndexException, IOException {
        ReservedSearchParameters reserved = new ReservedSearchParameters();
        reserved.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT, ObjectType.IMAGE));
        SearchResult<Resource> result = doSearch("", null, null, reserved);
        logger.debug("search phrase:{}", result.getSearchTitle());
        assertTrue(result.getSearchTitle().contains(ObjectType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ObjectType.IMAGE.getLabel()));
    }

    @Test
    @Rollback(true)
    public void testFindResourceById() throws ParseException, SearchException, SearchIndexException, IOException {
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.getResourceIds().add(Long.valueOf(3074));
        SearchResult<Resource> result = doSearch("", null, null, params);
        assertTrue(resultsContainId(result,3074l));
        for (Indexable r : result.getResults()) {
            logger.info("{}", r);
        }
    }

    @Test
    @Rollback(true)
    public void testFindTerm() throws ParseException, SearchException, SearchIndexException, IOException {
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT, ObjectType.IMAGE));

        SearchResult<Resource> result = doSearch("test", null, null, params);
        logger.info(result.getSearchTitle());
        assertTrue(result.getSearchTitle().contains(ObjectType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ObjectType.IMAGE.getLabel()));
        assertTrue(result.getSearchTitle().contains("test"));
    }

    @Test
    @Rollback(true)
    public void testCultureKeywordSearch() throws ParseException, SearchException, SearchIndexException, IOException {
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT, ObjectType.IMAGE));

        CultureKeyword keyword1 = genericKeywordService.findByLabel(CultureKeyword.class, "Folsom");
        CultureKeyword keyword2 = genericKeywordService.findByLabel(CultureKeyword.class, "Early Archaic");
        logger.info(keyword1.getLabel());
        logger.info(keyword2.getLabel());
        // this test is failing because the "Skeleton" versions of these fields just have IDs, and thus, when they're put into a set
        // they fall in on themselves, thus, bad.
        
        SearchParameters params = new SearchParameters();
        params.getApprovedCultureKeywordIdLists().add(Arrays.asList(keyword1.getId().toString(), keyword2.getId().toString()));
        params.getAllFields().add("test");
        SearchResult<Resource> result = doSearch("", null, params, rparams);
        String searchPhrase = result.getSearchTitle();
        assertTrue("search phrase shouldn't be blank:", StringUtils.isNotBlank(searchPhrase));
        logger.debug("search phrase: {}", searchPhrase);
        logger.debug("keyword1:      {}", keyword1.getLabel());
        logger.debug("keyword2:      {}", keyword2.getLabel());
        assertTrue(searchPhrase.contains(ObjectType.DOCUMENT.getLabel()));
        assertTrue(searchPhrase.contains(ObjectType.IMAGE.getLabel()));
        assertTrue(searchPhrase.contains(keyword1.getLabel()));
        assertTrue(searchPhrase.contains(keyword2.getLabel()));
        assertTrue(searchPhrase.contains("test"));
    }

    @Test
    @Rollback(true)
    public void testBadDateSearch() throws ParseException, SearchException, SearchIndexException, IOException {
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT, ObjectType.IMAGE));
        CoverageDate cd = new CoverageDate(CoverageType.NONE);
        SearchParameters params = new SearchParameters();
        params.getCoverageDates().add(cd);
        params.getAllFields().add("test");
        SearchResult<Resource> result = doSearch("", null, params, rparams);
        assertTrue(result.getSearchTitle().contains(ObjectType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ObjectType.IMAGE.getLabel()));
        assertFalse(result.getSearchTitle().contains("null"));
        assertFalse(result.getSearchTitle().contains(" TO "));
    }

    @Test
    @Rollback(true)
    public void testCalDateSearchPhrase() throws ParseException, SearchException, SearchIndexException, IOException {
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT, ObjectType.IMAGE));
        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 1200);
        SearchParameters params = new SearchParameters();
        params.getCoverageDates().add(cd);
        params.getAllFields().add("test");
        SearchResult<Resource> result = doSearch("", null, params, rparams);
        logger.debug(result.getSearchTitle());

        assertTrue(result.getSearchTitle().contains(ObjectType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ObjectType.IMAGE.getLabel()));
        assertFalse(result.getSearchTitle().contains("null"));
        assertTrue(result.getSearchTitle().contains("1000"));
        assertTrue(result.getSearchTitle().contains("1200"));
        assertTrue(result.getSearchTitle().contains(CoverageType.CALENDAR_DATE.getLabel()));
        TdarAssert.assertMatches(result.getSearchTitle(), ".+?" + "\\:.+? \\- .+?");
    }

    @Test
    @Rollback(true)
    public void testSpatialSearch() throws ParseException, SearchException, SearchIndexException, IOException {
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT, ObjectType.IMAGE));
        LatitudeLongitudeBox box = new LatitudeLongitudeBox(-1d, -1d, 1d, 1d);
        rparams.getLatitudeLongitudeBoxes().add(box);
        SearchResult<Resource> result = doSearch("test",null, null, rparams);
        assertTrue(result.getSearchTitle().contains(ObjectType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ObjectType.IMAGE.getLabel()));
        assertTrue(result.getSearchTitle().contains("Resource Located"));
    }


    @Test
    @Rollback(true)
    public void testDeletedOrDraftMaterialsAreHiddenInDefaultSearch() throws ParseException, SearchException, SearchIndexException, IOException {
        Long imgId = setupImage();
        Long datasetId = setupDataset();
        Long codingSheetId = setupCodingSheet();

        logger.info("imgId:" + imgId + " datasetId:" + datasetId + " codingSheetId:" + codingSheetId);
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(allResourceTypes);
        SearchResult<Resource> result = doSearch("precambrian",null, null, rparams);
        assertFalse(resultsContainId(result,datasetId));
        assertTrue(resultsContainId(result,codingSheetId));
        assertFalse(resultsContainId(result,imgId));
    }

    @Test
    @Rollback(true)
    @Ignore
    public void testGeneratedAreHidden() throws ParseException, SearchException, SearchIndexException, IOException {
        Long codingSheetId = setupCodingSheet();
        CodingSheet sheet = genericService.find(CodingSheet.class, codingSheetId);
        sheet.setGenerated(true);
        genericService.save(sheet);
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.getObjectTypes().add(ObjectType.CODING_SHEET);
        SearchResult<Resource> result = doSearch("", null, null, rparams);
        assertFalse(resultsContainId(result,codingSheetId));
    }

    @Test
    @Rollback(true)
    public void testPeopleAndInstitutionsInSearchResults() throws SearchException, SearchIndexException, IOException, ParseException {
        Long imgId = setupDataset(Status.ACTIVE);
        logger.info("Created new image: " + imgId);
        searchIndexService.index(resourceService.find(imgId));
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(allResourceTypes);
        rparams.getStatuses().addAll(Arrays.asList(Status.values()));
        SearchResult<Resource> result = doSearch("testabc", null, null, rparams);
        assertTrue("expected to find person in keyword style search of firstname", resultsContainId(result,imgId));
        result = doSearch("\"" + TestConstants.DEFAULT_FIRST_NAME + "abc " + TestConstants.DEFAULT_LAST_NAME + "abc\"");
        assertTrue("expected to find person in phrase style search of full name", resultsContainId(result,imgId));

        result = doSearch("university");
        assertTrue("institutional author expected to find in search", resultsContainId(result,imgId));
    }

    
    
    
    
    
    @Test
    @Rollback(true)
    // try a search that will fail the strict parsing pass, but work under lenient parsing.
    public void testLenientParsing() throws ParseException, SearchException, SearchIndexException, IOException {
        String term = "a term w/ unclosed \" quote and at least one token that will return results: " + TestConstants.DEFAULT_LAST_NAME;
        doSearch(term);
    }

    @Test
    @Rollback(true)
    public void testDatedSearch() throws ParseException, SearchException, SearchIndexException, IOException {
        Long docId = setupDatedDocument();
        logger.info("Created new document: " + docId);
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(allResourceTypes);

        // test inner range
        SearchParameters params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, -900, 1000));
        SearchResult<Resource> result = doSearch("", null, params, rparams);
        assertTrue("expected to find document "+docId+" for inner range match", resultsContainId(result,docId));

        rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(allResourceTypes);
        params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, -2000, -1));
        result = doSearch("", null, params, rparams);
        assertTrue("expected to find document for overlapping range (lower)", resultsContainId(result,docId));

        rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(allResourceTypes);
        params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1999, 2009));
        result = doSearch("", null, params, rparams);
        assertTrue("expected to find document for overlapping range (upper)", resultsContainId(result,docId));

        // test invalid range
        rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(allResourceTypes);
        params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, -7000, -1001));
        result = doSearch("", null, params, rparams);
        assertFalse("expected not to find document in invalid range", resultsContainId(result,docId));

        // test exact range (query inclusive)
        rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(allResourceTypes);
        params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 2000));
        result = doSearch("", null, params, rparams);
        assertTrue("expected to find document for exact range match", resultsContainId(result,docId));
    }

    @Test
    @Rollback
    public void testInvestigationTypes() throws ParseException, SearchException, SearchIndexException, IOException {

        // TODO:dynamically get the list of 'used investigation types' and the resources that use them
        SearchParameters params = addInvestigationTypes(new SearchParameters());
        ReservedSearchParameters reserved = new ReservedSearchParameters();
        
        // this fails because all of the Skeleton Investigation Types with IDs get put into a set, and thus fold into each other
        // because equality based on label[NULL]
        reserved.setObjectTypes(allResourceTypes);
        reserved.getStatuses().addAll(Arrays.asList(Status.ACTIVE, Status.DELETED, Status.DRAFT, Status.FLAGGED));
        SearchResult<Resource> result = doSearch("",null, params, reserved);
        assertTrue("we should get back at least one hit", !result.getResults().isEmpty());
        assertTrue("expected to find document that uses known investigation types", resultsContainId(result,2420L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(result,1628L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(result,3805L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(result,3738L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(result,4287L));
        assertTrue("expected to find document that uses known investigation types", resultsContainId(result,262L));
    }


    // add all investigation types... for some reason
    private SearchParameters addInvestigationTypes(SearchParameters params) {
        List<InvestigationType> investigationTypes = genericService.findAll(InvestigationType.class);
        List<String> ids = new ArrayList<String>();
        for (InvestigationType type : investigationTypes) {
            ids.add(type.getId().toString());
        }
        params.getInvestigationTypeIdLists().add(ids);
        return params;
    }

    @Test
    @Rollback
    public void testLookupResourceWithDateRegisteredRange() throws InstantiationException, IllegalAccessException, SearchException, SearchIndexException, IOException, ParseException {
        // From the Hibernate documentation:
        // "The default Date bridge uses Lucene's DateTools to convert from and to String. This means that all dates are expressed in GMT time."
        // The Joda DateMidnight defaults to DateTimeZone.getDefault(). Which is probably *not* GMT
        // So for the tests below to work in, say, Australia, we need to force the DateMidnight to the GMT time zone...
        // ie:
        // DateTimeZone dtz = DateTimeZone.forID("Australia/Melbourne");
        // will break this test.
        DateTimeZone dtz = DateTimeZone.forID("GMT");

        // first create two documents with two separate create dates
        Document document1 = createAndSaveNewInformationResource(Document.class, createAndSaveNewPerson("lookuptest1@tdar.net", ""));
        DateMidnight dm1 = new DateMidnight(2001, 2, 16, dtz);
        document1.setDateCreated(dm1.toDate());

        Document document2 = createAndSaveNewInformationResource(Document.class, createAndSaveNewPerson("lookuptest2@tdar.net", ""));
        DateMidnight dm2 = new DateMidnight(2002, 11, 1, dtz);
        document2.setDateCreated(dm2.toDate());

        genericService.saveOrUpdate(document1, document2);
        searchIndexService.index(document1, document2);

        // okay, lets start with a search that should contain both of our newly created documents
        DateRange dateRange = new DateRange();
        dateRange.setStart(dm1.minusDays(1).toDate());
        dateRange.setEnd(dm2.plusDays(1).toDate());
        SearchParameters params = new SearchParameters();
        params.getRegisteredDates().add(dateRange);
        SearchResult<Resource> result = doSearch("",null, params,null);

        doSearch("", null, params, null);

        assertTrue(result.getResults().contains(document1));
        assertTrue(result.getResults().contains(document2));

        // now lets refine the search so that the document2 is filtered out.
        dateRange.setEnd(dm2.minusDays(1).toDate());
        params = new SearchParameters();
        params.getRegisteredDates().add(dateRange);
        result = doSearch("",null, params,null);

        assertTrue(result.getResults().contains(document1));
        assertFalse(result.getResults().contains(document2));
    }


    @Test
    @Rollback
    public void testScholarLookup() throws InstantiationException, IllegalAccessException, SearchException, SearchIndexException, IOException, ParseException {
        // From the Hibernate documentation:
        // "The default Date bridge uses Lucene's DateTools to convert from and to String. This means that all dates are expressed in GMT time."
        // The Joda DateMidnight defaults to DateTimeZone.getDefault(). Which is probably *not* GMT
        // So for the tests below to work in, say, Australia, we need to force the DateMidnight to the GMT time zone...
        // ie:
        // DateTimeZone dtz = DateTimeZone.forID("Australia/Melbourne");
        // will break this test.
        DateTimeZone dtz = DateTimeZone.forID("GMT");

        // first create two documents with two separate create dates
        Document document1 = createAndSaveNewInformationResource(Document.class, createAndSaveNewPerson("lookuptest1@tdar.net", ""));
        DateMidnight dm1 = new DateMidnight(2001, 2, 16, dtz);
        document1.setDateCreated(dm1.toDate());
        document1.setDate(2017);
        Document document2 = createAndSaveNewInformationResource(Document.class, createAndSaveNewPerson("lookuptest2@tdar.net", ""));
        DateMidnight dm2 = new DateMidnight(2002, 11, 1, dtz);
        document2.setDateCreated(dm2.toDate());
        document1.setDate(2012);

        genericService.saveOrUpdate(document1, document2);
        searchIndexService.index(document1, document2);
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
        resourceSearchService.findByTdarYear(2001, result, MessageHelper.getInstance());

        assertTrue(result.getResults().contains(document1));
        assertFalse(result.getResults().contains(document2));
        
        result = new SearchResult<>();
        resourceSearchService.findByTdarYear(2017, result, MessageHelper.getInstance());

        assertFalse(result.getResults().contains(document1));
        assertFalse(result.getResults().contains(document2));
    }


    
}

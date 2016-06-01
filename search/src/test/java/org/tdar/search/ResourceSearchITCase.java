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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
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
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.junit.TdarAssert;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.ResourceLookupObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.CreatorSearchService;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.range.DateRange;

public class ResourceSearchITCase  extends AbstractResourceSearchITCase {

    private static final String _33_CU_314 = "33-Cu-314";

    @Autowired
    CreatorSearchService<Creator<?>> creatorSearchService;

    public static final String REASON = "because";



    private static final String CONSTANTINOPLE = "Constantinople";

    private static final String ISTANBUL = "Istanbul";

    private static final String L_BL_AW = "l[]bl aw\\";

    @Autowired
    ResourceService resourceService;

    @Autowired
    EntityService entityService;

    @Test
    public void testInvalidPhrase() throws ParseException, SolrServerException, IOException {
        ResourceLookupObject look = new ResourceLookupObject();
        look.setTerm("he operation and evolution of an irrigation system\"");
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
        resourceSearchService.lookupResource(getAdminUser(), look, result , MessageHelper.getInstance());
    }

    @Test
    public void testInvalidWithColon() throws ParseException, SolrServerException, IOException {
        ResourceLookupObject look = new ResourceLookupObject();
        look.setTerm("Temporal Control in the Southern North Coast Ranges of California: The Application of Obsidian Hydration Analysis");
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
        resourceSearchService.lookupResource(getAdminUser(), look, result , MessageHelper.getInstance());
    }

    @Test
    public void testInvalidWithColon2() throws ParseException, SolrServerException, IOException {
        SearchParameters sp = new SearchParameters();
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        asqo.getSearchParameters().add(sp);
        sp.getAllFields().add("Temporal Control in the Southern North Coast Ranges of California: The Application of Obsidian Hydration Analysis");
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
        resourceSearchService.buildAdvancedSearch(asqo, getAdminUser(), result, MessageHelper.getInstance());
    }

    @Test
    @Rollback
    public void testResourceAnnotationSearch() throws SolrServerException, IOException, ParseException {
        Document doc = createAndSaveNewResource(Document.class);
        ResourceAnnotationKey key = new ResourceAnnotationKey("MAC Lab Lot Number");
        genericService.saveOrUpdate(key);
        String code = "18ST659/158";
        ResourceAnnotation ann = new ResourceAnnotation(key, code);
        ResourceAnnotation ann2 = new ResourceAnnotation(key, "18ST659/143");
        doc.getResourceAnnotations().add(ann);
        doc.getResourceAnnotations().add(ann2);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchResult<Resource> result = doSearch(code,null,null,null);
        assertFalse("we should get back at least one hit", result.getResults().isEmpty());
        assertTrue(result.getResults().contains(doc));
    }

    
    @Test
    @Rollback
    public void testSiteNameKeywords() throws SolrServerException, IOException, ParseException {
        SiteNameKeyword snk = genericKeywordService.findByLabel(SiteNameKeyword.class, "Atsinna");
        Document doc = createAndSaveNewResource(Document.class);
        doc.getSiteNameKeywords().add(snk);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getSiteNames().add(snk.getLabel());
        SearchResult<Resource> result = doSearch(null,null,sp,null);
        assertFalse("we should get back at least one hit", result.getResults().isEmpty());
        for (Indexable resource : result.getResults()) {
            assertTrue("expecting site name for resource", ((Resource)resource).getSiteNameKeywords().contains(snk));
        }
    }

    @Test
    public void testTitleCaseSensitivity() throws SolrServerException, IOException, ParseException {
        Document doc = createAndSaveNewResource(Document.class);
        doc.setTitle("usaf");
        updateAndIndex(doc);
        SearchParameters sp = new SearchParameters();
        sp.setTitles(Arrays.asList("USAF"));
        SearchResult<Resource> result = doSearch(null,null,sp,null);
        
        assertTrue(result.getResults().contains(doc));
        doc.setTitle("USAF");
        updateAndIndex(doc);
        sp = new SearchParameters();
        sp.setTitles(Arrays.asList("usaf"));
        result = doSearch(null,null,sp,null);
        assertTrue(result.getResults().contains(doc));
    }


    @Test
    @Rollback
    public void testComplexGeographicKeywords() throws SolrServerException, IOException, ParseException {
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
    public void testMultiplePersonSearch() throws ParseException, SolrServerException, IOException {
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
    public void testSearchDecade() throws SolrServerException, IOException, ParseException {
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
    public void testApprovedSiteTypeKeywords() throws ParseException, SolrServerException, IOException {
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
    public void testMaterialKeywords() throws ParseException, SolrServerException, IOException {
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
    public void testCulture() throws ParseException, SolrServerException, IOException {
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
    public void testApprovedCulture() throws ParseException, SolrServerException, IOException {
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
    public void testProjectIds() throws ParseException, SolrServerException, IOException {
        // FIXME: magic numbers
        Long projectId = 3805L;
        SearchParameters sp = new SearchParameters();
        sp.getProjects().add(sparseProject(projectId));
        ReservedSearchParameters rsp = new ReservedSearchParameters();
        rsp.getResourceTypes().clear(); // select all resource types
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

    @SuppressWarnings("deprecation")
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
    public void testTitle() throws ParseException, SolrServerException, IOException {
        // FIXME: magic numbers
        Long projectId = 139L;
        Project project = genericService.find(Project.class, projectId);
        String projectTitle = project.getTitle();
        SearchParameters sp = new SearchParameters();
        sp.getTitles().add(projectTitle);
        SearchResult<Resource> result = doSearch(null,null,sp, null);
        result.getResults().contains(project);
    }

    @Test
    @Rollback
    public void testSearchSubmitterIds() throws ParseException, SolrServerException, IOException {
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
    @Rollback
    public void testInvalidPersonSearch() throws ParseException, SolrServerException, IOException {
        // FIXME: magic numbers
        SearchParameters sp = new SearchParameters();
        sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(new Person("Colin", "Renfrew", null),null));
        SearchResult<Resource> result = null;
        try {
            result = doSearch(null,null,sp, null);
        } catch (SearchException se) {
            assertTrue(se.getMessage().contains("Renfrew"));
        }
        assertNull("should be null (expecting exception)",result);
    }

    @Test
    @Rollback(true)
    public void testTitleSiteCodeMatching() throws SolrServerException, IOException, ParseException {
        List<String> titles = Arrays
                .asList("1. Pueblo Grande (AZ U:9:1(ASM)): Unit 12, Gateway and 44th Streets: SSI Kitchell Testing, Photography Log (PHOTO) Data (1997)",
                        "2. Archaeological Testing at Pueblo Grande (AZ U:9:1(ASM)): Unit 15, The Former Maricopa County Sheriff's Substation, Washington and 48th Streets, Phoenix, Arizona -- DRAFT REPORT (1999)",
                        "3. Phase 2 Archaeological Testing at Pueblo Grande (AZ U:9:1(ASM)): Unit 15, the Former Maricopa County Sheriff’s Substation, Washington and 48th Streets, Phoenix, Arizona -- DRAFT REPORT (1999)",
                        "4. Final Data Recovery And Burial Removal At Pueblo Grande (AZ U:9:1(ASM)): Unit 15, The Former Maricopa Counry Sheriff's Substation, Washington And 48th Streets, Phoenix, Arizona (2008)",
                        "5. Pueblo Grande (AZ U:9:1(ASM)): Unit 15, Washington and 48th Streets: Soil Systems, Inc. Kitchell Development Testing and Data Recovery (The Former Maricopa County Sheriff's Substation) ",
                        "6. Archaeological Testing of Unit 13 at Pueblo Grande, AZ U:9:1(ASM), Arizona Federal Credit Union Property, 44th and Van Buren Streets, Phoenix, Maricopa County, Arizona (1998)",
                        "7. Archaeological Testing And Burial Removal Of Unit 11 At Pueblo Grande, AZ U:9:1(ASM), DMB Property, 44th And Van Buren Streets, Phoenix, Maricopa County, Arizona -- DRAFT REPORT (1998)",
                        "8. Pueblo Grande (AZ U:9:1(ASM)): Unit 13, Northeast Corner of Van Buren and 44th Streets: Soil Systems, Inc. AZ Federal Credit Union Testing and Data Recovery Project ",
                        "9. POLLEN AND MACROFLORAL ANAYSIS AT THE WATER USERS SITE, AZ U:6:23(ASM), ARIZONA (1990)",
                        "10. Partial Data Recovery and Burial Removal at Pueblo Grande (AZ U:9:1(ASM)): Unit 15, The Former Maricopa County Sheriff's Substation, Washington and 48th Streets, Phoenix, Arizona -- DRAFT REPORT (2002)",
                        "11. MACROFLORAL AND PROTEIN RESIDUE ANALYSIS AT SITE AZ U:15:18(ASM), CENTRAL ARIZONA (1996)",
                        "12. Pueblo Grande (AZ U:9:1(ASM)) Soil Systems, Inc. Master Provenience Table: Projects, Unit Numbers, and Feature Numbers (2008)");

        List<Document> docs = new ArrayList<>();
        List<Document> badMatches = new ArrayList<>();
        for (String title : titles) {
            Document doc = new Document();
            doc.setTitle(title);
            doc.setDescription(title);
            doc.markUpdated(getBasicUser());
            genericService.saveOrUpdate(doc);
            if (title.contains("MACROFLORAL")) {
                badMatches.add(doc);
            }
            docs.add(doc);
        }
        genericService.synchronize();
        searchIndexService.indexCollection(docs);
        searchIndexService.flushToIndexes();
        SearchResult<Resource> result = doSearch("AZ U:9:1(ASM)");
        List<Resource> results = result.getResults();
        for (Resource r : result.getResults()) {
            logger.debug("results: {}", r);
        }
        
        assertTrue("controller should not contain titles with MACROFLORAL", CollectionUtils.containsAny(results, badMatches));
        assertTrue("controller should not contain titles with MACROFLORAL",
                CollectionUtils.containsAll(results.subList(results.size() - 3, results.size()), badMatches));

    }



    @Test
    @Rollback(true)
    public void testGeographicKeywordIndexedAndFound() throws SolrServerException, IOException, ParseException {
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
    @Rollback(true)
    public void testFilenameFound() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        Document doc = generateDocumentWithFileAndUseDefaultUser();
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getFilenames().add(TestConstants.TEST_DOCUMENT_NAME);
        SearchResult<Resource> result = doSearch(null, null,sp, null);
        boolean seen = false;
        for (Indexable res : result.getResults()) {
            if (res.getId().equals(doc.getId())) {
                seen = true;
            }
        }
        assertTrue(seen);
    }


    private void setSortThenCheckFirstResult(String message, SortOption sortField, Long projectId, Long expectedId) throws ParseException, SolrServerException, IOException {
        SearchParameters sp = new SearchParameters();
        sp.getProjects().add(sparseProject(projectId));
        SearchResult<Resource> result = doSearch(null, null, sp, null, sortField);
//        logger.info("{}", result.getResults());
        for (Resource d: result.getResults()) {
            InformationResource ir = (InformationResource)d;
            logger.debug("{} {} {}", ir.getDate(),ir.getId(), ir);
        }
        Indexable found = result.getResults().iterator().next();
        logger.info("{}", found);
        Assert.assertEquals(message, expectedId, found.getId());
    }

    // note: relevance sort broken out into SearchRelevancyITCase
    @Test
    @Rollback
    public void testSortFieldTitle() throws ParseException, SolrServerException, IOException {
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
    public void testSortFieldProject() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
        searchIndexService.purgeAll();
        Project project = createAndSaveNewProject("my project");
        Project project2 = createAndSaveNewProject("my project 2");
        Image a = createAndSaveNewInformationResource(Image.class, project, getBasicUser(), "a");
        Image b = createAndSaveNewInformationResource(Image.class, project, getBasicUser(), "b");
        Image c = createAndSaveNewInformationResource(Image.class, project, getBasicUser(), "c");

        Image d = createAndSaveNewInformationResource(Image.class, project2, getBasicUser(), "d");
        Image e = createAndSaveNewInformationResource(Image.class, project2, getBasicUser(), "e");
        Image aa = createAndSaveNewInformationResource(Image.class, project2, getBasicUser(), "a");
        List<Resource> res = Arrays.asList(project, project2, a, b, c, d, e, aa);
        searchIndexService.indexCollection(res);

        SearchResult<Resource> result = doSearch("", null, null, null, SortOption.PROJECT);
        List<Resource> results = result.getResults();
        for (Resource r : results) {
            if (r instanceof InformationResource) {
                InformationResource ir = (InformationResource)r;
                logger.debug("{} {} {}", r.getId(), r.getName(), ir.getProjectId());
            } else {
                logger.debug("{} {}", r.getId(), r.getName());
            }
        }
        int i = results.indexOf(project);
        assertEquals(i + 1, results.indexOf(a));
        assertEquals(i + 2, results.indexOf(b));
        assertEquals(i + 3, results.indexOf(c));
        assertEquals(i + 4, results.indexOf(project2));
        assertEquals(i + 5, results.indexOf(aa));
        assertEquals(i + 6, results.indexOf(d));
        assertEquals(i + 7, results.indexOf(e));
    }

    @Test
    @Rollback
    public void testSortFieldDate() throws ParseException, SolrServerException, IOException {
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
    public void testResourceUpdated() throws java.text.ParseException, ParseException, SolrServerException, IOException {
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
        searchIndexService.flushToIndexes();
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
     * @throws SolrServerException 
     * @throws ParseException 
     */
    @Test
    @Rollback
    public void testTimezoneEdgeCase() throws ParseException, SolrServerException, IOException {
        Resource doc = createAndSaveNewInformationResource(Document.class);
        DateTime createDateTime = new DateTime(2005, 3, 26, 23, 0, 0, 0);
        DateTime searchDateTime = new DateTime(2005, 3, 26, 0, 0, 0, 0);
        doc.setDateCreated(createDateTime.toDate());
        doc.setDateUpdated(createDateTime.toDate());
        genericService.saveOrUpdate(doc);
        genericService.synchronize();
        searchIndexService.index(doc);

        // converstion from MST to UTC date advances registration date by one day.
        searchIndexService.flushToIndexes();
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
    public void testOtherKeywords() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
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
        rsp.getResourceTypes().add(ResourceType.DOCUMENT);
        SearchResult<Resource> result = doSearch(null,null,sp, rsp);
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(result.getResults());
        assertEquals("only expecting one result", 1L, result.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(result, ok.getLabel());
    }

    @Test
    @Rollback
    public void testTemporalKeywords() throws ParseException, InstantiationException, IllegalAccessException, SolrServerException, IOException {
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
        rsp.getResourceTypes().add(ResourceType.DOCUMENT);
        SearchResult<Resource> result = doSearch(null,null,sp, rsp);
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(result.getResults());
        assertEquals("only expecting one result", 1L, result.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(result, tk.getLabel());
    }

    @Test
    @Rollback
    public void testGeoKeywords() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
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
        rsp.getResourceTypes().add(ResourceType.DOCUMENT);
        SearchResult<Resource> result = doSearch(null,null,sp, rsp);
        Set<Indexable> results = new HashSet<Indexable>();
        results.addAll(result.getResults());
        assertEquals("only expecting one result", 1L, result.getResults().size());
        assertTrue("document containig our test keyword should be in results", results.contains(document));
        assertSearchPhrase(result, gk.getLabel());
    }

    private Document createDocumentWithContributorAndSubmitter() throws InstantiationException, IllegalAccessException, SolrServerException, IOException {
        TdarUser submitter = new TdarUser("E", "deVos", "ecd@tdar.net");
        genericService.save(submitter);
        Document doc = createAndSaveNewInformationResource(Document.class, submitter);
        ResourceCreator rc = new ResourceCreator(new Person("K", "deVos", "kellyd@tdar.net"), ResourceCreatorRole.AUTHOR);
        genericService.save(rc.getCreator());
        // genericService.save(rc);
        doc.getResourceCreators().add(rc);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        return doc;
    }

    @Test
    @Rollback
    public void testSearchBySubmitterIds() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
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
    public void testSearchContributorIds2() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
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
    public void testTitleSearch() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
        Document doc = createDocumentWithContributorAndSubmitter();
        String title = "the archaeology of class and war";
        doc.setTitle(title);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getTitles().add(title);
        SearchResult<Resource> result = doSearch(null,null,sp, null);

        logger.info("{}", result.getResults());
        assertEquals("only one result expected", 1L, result.getResults().size());
        assertEquals(doc, result.getResults().iterator().next());
    }

    @Test
    @Rollback
    public void testLuceneOperatorInSearch() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
        Document doc = createDocumentWithContributorAndSubmitter();
        String title = "the archaeology of class ( AND ) war";
        doc.setTitle(title);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getAllFields().add(title);
        SearchResult<Resource> result = doSearch(null,null,sp, null);
        logger.info("{}", result.getResults());
        assertEquals("only one result expected", 1L, result.getResults().size());
        assertEquals(doc, result.getResults().iterator().next());
    }

    @Test
    @Rollback
    public void testResourceCreatorPerson() throws ParseException, SolrServerException, IOException {
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
    public void testResourceCreatorWithAnyRole() throws ParseException, SolrServerException, IOException {
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
    public void testBooleanSearch() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
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
        searchIndexService.flushToIndexes();
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
    public void testCalDateSearch() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
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
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        searchIndexService.flushToIndexes();
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
    public void testAllFieldsSearchDescriptionGrammar() throws ParseException, SolrServerException, IOException {
        String TEST_VALUE = "spam"; // damn vikings!

        SearchResult<Resource> result = doSearch(TEST_VALUE, null, null, null);

        for (int i = 0; i < 10; i++) {
            logger.debug("search phrase:{}", result.getSearchTitle());
        }
        int occurances = result.getSearchTitle().split(TEST_VALUE).length;
        assertTrue("search description should have gooder english than it currently does", occurances <= 2);
    }

    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testSparseObjectLoading() throws SolrServerException, IOException, ParseException {
        String colname = "my fancy collection";
        Project proj = createAndSaveNewResource(Project.class);
        ResourceCollection coll = createAndSaveNewResourceCollection(colname);
        searchIndexService.index(coll);
        searchIndexService.index(proj);

        SearchParameters sp = new SearchParameters();
        // simulate searchParamerters that represents a project at [0] and collection at [1]
        sp.getProjects().add(sparseProject(proj.getId()));
        sp.getCollections().add(null); // [0]
        sp.getCollections().add(sparseCollection(coll.getId())); // [1]

        SearchResult<Resource> result = doSearch(null, null, sp, null);

        // skeleton lists should have been loaded w/ sparse records...
        assertEquals(proj.getTitle(), sp.getProjects().get(0).getTitle());
        assertEquals(colname, sp.getCollections().get(1).getName());
    }

    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testSparseObjectNameLoading() throws SolrServerException, IOException, ParseException {
        String colname = "my fancy collection";
        Project proj = createAndSaveNewResource(Project.class);
        ResourceCollection coll = createAndSaveNewResourceCollection(colname);
        searchIndexService.index(coll);
        proj.getResourceCollections().add(coll);
        searchIndexService.index(proj);

        // simulate searchParamerters that represents a project at [0] and collection at [1]
        // sp.getProjects().add(new Project(null,proj.getName()));
        // sp.getCollections().add(null); // [0]
        SearchParameters sp = new SearchParameters();
        sp.getCollections().add(new ResourceCollection(colname, null, null, null, true, null)); // [1]

        SearchResult<Resource> result = doSearch(null, null, sp, null);

        
        // skeleton lists should have been loaded w/ sparse records...
        // assertEquals(proj.getTitle(), sp.getProjects().get(0).getTitle());
        assertEquals(colname, sp.getCollections().get(0).getName());
        assertTrue(result.getResults().contains(proj));
        // assertEquals(proj.getId(), sp.getProjects().get(0).getId());
        // assertEquals(coll.getId(), sp.getCollections().get(1).getId());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback()
    // sparse collections like projects and collections should get partially hydrated when rendering the "refine" page
    public void testLookupObjectLoading() throws SolrServerException, IOException, ParseException {
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
        SearchParameters sp = new SearchParameters();
        sp.getProjects().add(new Project(-1L, colname));
        SearchResult<Resource> result = doSearch(null, null, sp, null);

        // skeleton lists should have been loaded w/ sparse records...
        assertEquals(proj.getTitle(), sp.getProjects().get(0).getTitle());
        assertTrue(PersistableUtils.extractIds(result.getResults()).contains(doc1.getId()));
    }



    @Test
    // if user gets to the results page via clicking on persons name from resource view page, querystring only contains person.id field. So before
    // rendering the 'refine your search' version of the search form the controller must inflate query components.
    public void testRefineSearchWithSparseProject() throws ParseException, SolrServerException, IOException {
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

    @Test
    @Rollback
    public void testRefineSearchWithSparseCollection() throws ParseException, SolrServerException, IOException {

        ResourceCollection rc = createAndSaveNewResourceCollection("Mega Collection");
        ResourceCollection sparseCollection = new ResourceCollection();
        evictCache();
        long collectionId = rc.getId();
        assertThat(collectionId, greaterThan(0L));
        sparseCollection.setId(collectionId);
        SearchParameters sp = new SearchParameters();
        sp.getCollections().add(sparseCollection);
        SearchResult<Resource> result = doSearch(null, null, sp, null);

        assertThat(sp.getCollections().get(0).getTitle(), is("Mega Collection"));
    }

    private void assertOnlyResultAndProject(SearchResult<Resource> result, InformationResource informationResource) {
        assertEquals("expecting two results: doc and project", 2, result.getResults().size());
        assertTrue("expecting resource in results", result.getResults().contains(informationResource));
        assertTrue("expecting resource's project in results", result.getResults().contains(informationResource.getProject()));
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
    public void testCreatorOwnerQueryPart() throws ParseException, SolrServerException, IOException {
        
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
    public void testSelectedResourceLookup() throws SolrServerException, IOException, ParseException {
        ResourceCollection collection = new ResourceCollection("test", "test", SortOption.TITLE, CollectionType.SHARED, true, getUser());
        collection.markUpdated(getUser());
        Ontology ont = createAndSaveNewInformationResource(Ontology.class);
        genericService.saveOrUpdate(collection);
        collection.getResources().add(ont);
        // babysitting bidirectional relationshi[
        genericService.saveOrUpdate(collection);
        ont.getResourceCollections().add(collection);
        genericService.saveOrUpdate(ont);
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setResourceTypes(Arrays.asList(ResourceType.ONTOLOGY));
        SearchResult<Resource> result = performSearch("", null, collection.getId(), null, null, null, params, 100);
        assertFalse(result.getResults().isEmpty());
        assertTrue(result.getResults().contains(ont));
    }
    
    @Override
    public void reindex() {
        searchIndexService.purgeAll(LookupSource.RESOURCE);
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
    }

    @Test
    @Rollback(true)
    public void testModifyEditor() throws SolrServerException, IOException, ParseException {
        ReservedSearchParameters params = new ReservedSearchParameters();

        SearchResult<Resource> result = performSearch("", null, null, null, null, getEditorUser(), params, GeneralPermissions.MODIFY_METADATA, 1000);
        logger.debug("results:{}", result.getResults());
        List<Long> ids = PersistableUtils.extractIds(result.getResults());

        result = performSearch("", null, null, null, null, getAdminUser(), params, GeneralPermissions.MODIFY_METADATA, 1000);
        logger.debug("results:{}", result.getResults());
        List<Long> ids2 = PersistableUtils.extractIds(result.getResults());
        Assert.assertArrayEquals(ids.toArray(), ids2.toArray());
    }

    @Test
    @Rollback(true)
    public void testLookupByTitle() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        String[] titles = new String[] { "CARP Fauna Side or Symmetry", "CARP Fauna Completeness (Condition)", "CARP Fauna Origin of Fragmentation",
                "CARP Fauna Proximal-Distal", " CARP Fauna Dorsal-Ventral", "CARP Fauna Fusion", "CARP Fauna Burning", "CARP Fauna Bone Artifacts",
                "CARP Fauna Gnawing", "CARP Fauna Natural Modification", "CARP Fauna Element", "CARP Fauna Butchering",
                "CARP Fauna Species Alternate Ontology - Scientific Name", "Carp Elements", "CARP Condition", "HARP Fauna Condition Coding Sheet",
                "HARP Fauna Element Coding Sheet", "HARP Fauna Species Coding Sheet", "HARP Fauna Side Coding Sheet", "EMAP_fauna_taxon", "EMAP_fauna_taxa",
                "EMAP_fauna_taxa", "EMAP_fauna_element", "Powell_coding_mammal_taxa", "Powell_coding_nonmammal_taxa", "Powell_coding_symmetry",
                "Powell_coding_side", "Powell_coding_sex", "EMAP_breakage", "EMAP_fauna_element", "Region Coding Sheet (Valley of Mexico Project)",
                "Valley of Mexico Region Coding Sheet V. 2", "HARP Fauna Burning Coding Sheet", "HARP Fauna Butchering Coding Sheet",
                "HARP Fauna Post-depositional Processes Coding Sheet", "EMAP fauna breakage codes", "EMAP fauna class codes", "EMAP fauna element codes",
                "EMAP fauna modification codes", "EMAP fauna period codes", "EMAP fauna taxon codes", "Koster Site Fauna Burning Coding Sheet",
                "HARP Fauna Dorsal/Ventral Coding Sheet", "HARP Fauna Proximal/Distal Coding Sheet", "Koster Site Fauna Certainty Coding Sheet",
                "Koster Site Fauna Analyst Coding Sheet", "Koster Site Fauna Species Coding Sheet (Test)", "Koster Site Fauna Side Coding Sheet",
                "Koster Site Fauna Integrity Coding Sheet", "Koster Site Fauna Portion Coding Sheet", "Koster Site Fauna Element Coding Sheet",
                "Koster Site Fauna Feature/Midden Coding Sheet", "Koster Site Fauna Horizon Feature/Midden Coding Sheet",
                "Koster Site Fauna Other Integ Coding Sheet", "Koster Site Species Coding Sheet", "Durrington Walls - Coding Sheet - Fauna -  Fusion  ",
                "Knowth - Coding Sheet - Fauna - Fusion", "Knowth - Coding Sheet - Fauna - Species", "GQ burning coding sheet", "Koster burning",
                "Koster Burning test2", "HARP Fauna Fusion Coding Sheet", "HARP Fauna Modification Coding Sheet",
                "CARP Fauna Species Alternate Ontology - Common Name", "CARP Fauna Species Scientifc (Common)", "Species Coding Sheet (TAG Workpackage 2)",
                "Bone Coding Sheet  (TAG workpackage 2)", "Chew type Coding Sheet (TAG Workpackage 2)", "Condition Coding Sheet (TAG Workpackage 2)",
                "Erosion Coding Sheet (TAG Workshop Package 2)", "Size Coding Sheet (TAG Workpackage 2)", "Zone Coding Sheet (TAG Workpackage 2)",
                "RCAP Coding Sheet - Context", "GQ butchering coding sheet", "GQ dorsal-ventral coding key", "GQ Element coding key", "GQ Fusion coding key",
                "GQ origin fragmentation coding key", "GQ sex coding key", "GQ Modification coding key", "GQ Proximal-distal coding key", "GQ side coding key",
                "GQ Time period coding key", "GQ species coding key", "GQ condition coding key", "Preservation-Lookup", "Pueblo Blanco Temporal Codes",
                "Pueblo Blanco Species codes", "Pueblo Colorado Temporal Periods", "OLD Taxon coding sheet for CCAC - needs to be deleted",
                "String Code Coding Sheet - Text Box", "String Code Test Coding Sheet from CSV", "CCAC Taxon Coding Sheet",
                "OUTDATED CCAC element coding sheet - needs deletion", "OUTDATED Part coding sheet for CCAC - needs to be deleted", "Site Coding Sheet",
                "New Bridge & Carlin Sites Taxon Coding Sheet SMDraft", "Subperiod I & II Coding Sheet (Valley of Mexico Project)",
                "Occupation Coding Sheet (Valley of Mexico)", "Survey Code Coding Sheet (Valley of Mexico)", "Region Coding Sheet (Valley of Mexico)",
                "Period Coding Sheet (Valley of Mexico Project)", "Phase/Period codes for Taraco Archaeological Survey",
                "Environmental Zones for Taraco Peninsula Site Database", "sutype", "sutype", "Spitalfields Project Periods Coding Sheet",
                "Museum of London Archaeology fauna bone part coding sheet", "Museum of London Archaeology fauna bone modification codes",
                "Kitchell Mortuary Vessel Data Coding Sheet", "Alexandria Period Pre/Post 1680 Aggregation Coding Sheet",
                "Side coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Fusion coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Breakage coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Modification coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Length coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Thickness coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "FAT coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "FAP coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "SUDesc coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "SUType coding sheet for Crow Canyon Archaeological Center fauna through 2008", "Albert Porter ComponID coding sheet, CCAC fauna through 2008",
                "Albert Porter ComponID coding sheet, CCAC fauna", "Albert Porter ComponID coding sheet, CCAC fauna",
                "Woods Canyon ComponID coding sheet, CCAC fauna", "Castle Rock ComponID coding sheet, CCAC fauna",
                "Shields Pueblo ComponID coding sheet, CCAC fauna", "Yellow Jacket Pueblo ComponID coding sheet, CCAC fauna",
                "Sand Canyon ComponID coding sheet, CCAC fauna", "FeTyp coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Length coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Modification coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Side coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "SUDesc coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "SUType coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Thickness coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Thickness coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Breakage coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "FAP coding sheet for Crow Canyon Archaeological Center fauna through 2008", "Woods Canyon ComponID coding sheet, CCAC fauna",
                "Albert Porter ComponID coding sheet, CCAC fauna through 2008", "FAT coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Fusion coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Element coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Part coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Taxon coding sheet for Crow Canyon Archaeological Center fauna through 2008", "DAI - CTYPE",
                "Sand Canyon locality testing project ComponID coding sheet, CCAC fauna", "DAI - SIZE", "TUTORIAL Color Coding Sheet",
                "TUTORIAL Element Coding Sheet", "TUTORIAL Element Coding Sheet", "TUTORIAL Element Coding Sheet", "TUTORIAL Screen Size",
                "TUTORIAL Coding Sheet Size", "TUTORIAL Coding Sheet Context", "TUTORIAL Coding Sheet Context", "DAI - SHAPE", "NSF2002 - Date codes",
                "DAI - DATES", "DAI - PART", "DAI - TSG", "DAI -TT", "NSF2002 - Temper type", "NSF2002 - Part codes", "NSF2002 - Size codes",
                "NSF2002 - Shape codes", "Soil Systems, Inc. General Artifact Coding Sheet", "Soil Systems, Inc. Ceramic Temper Coding Sheet",
                "Soil Systems, Inc. Ceramic Ware and Type Coding Sheet", "Soil Systems, Inc .Cremation Interment Type Coding Sheet",
                "Soil Systems, Inc. Vessel Form Coding Sheet", "Soil Systems, Inc. Vessel Rim Angle Coding Sheet",
                "Soil Systems, Inc. Vessel Rim Fillet Coding Sheet", "Soil Systems, Inc. Vessel Rim Lip Shape Coding Sheet",
                "Soil Systems, Inc. Sherd Temper Coding Sheet", "Soil Systems, Inc. Structural Unit Type Coding Sheet",
                "Soil Systems, Inc. Feature Type Coding Sheet", "Soil Systems, Inc. Collection Unit Size Coding Sheet",
                "Soil Systems, Inc. Collection Unit Type Coding Sheet", "Soil Systems, Inc. Collection Unit Method Coding Sheet",
                "Soil Systems, Inc. Provenience Elevation Reference Coding Sheet", "Soil Systems, Inc. Context Coding Sheet",
                "Soil Systems, Inc. Provenience Integrity Coding Sheet", "asd", "Soil Systems, Inc. Inhumation Alcove Position Coding Sheet",
                "Soil Systems, Inc. Inhumation Arm Position Coding Sheet", "Soil Systems, Inc. Inhumation Body Position Coding Sheet",
                "Body Position Codes from SSI Inhumation Form", "Soil Systems, Inc. Inhumation Burial Pit Integrity Coding Sheet",
                "Soil Systems, Inc. Inhumation Burning Coding Sheet", "Soil Systems, Inc. Cremation Fill Type Coding Sheet",
                "Soil Systems, Inc. Cremation Pit Burning Coding Sheet", "Soil Systems, Inc. Cremation Pit Integrity Coding Sheet",
                "Soil Systems, Inc. Cremation Grave Orientation Coding Sheet", "Soil Systems, Inc. Inhumation Grave Planview Shape Coding Sheet",
                "Soil Systems, Inc. Inhumation Grave Profile Shape Coding Sheet", "Soil Systems, Inc. Cremation Grave Type Coding Sheet",
                "Soil Systems, Inc. Inhumation Head Facing Coding Sheet", "Soil Systems, Inc. Inhumation Head Location Coding Sheet",
                "Soil Systems, Inc. Inhumation Impressions Coding Sheet", "Soil Systems, Inc. Inhumation Grave Fill Type Coding Sheet",
                "Soil Systems, Inc. Inhumation Pit Integrity Coding Sheet", "Soil Systems, Inc. Inhumation Leg Positions Coding Sheet",
                "Soil Systems, Inc. Cremation Location for Remains Coding Sheet", "Soil Systems, Inc. Inhumation Color of Minerals & Staining Coding Sheet",
                "Soil Systems, Inc. Inhumation Location of Minerals & Staining on the Body Coding Sheet",
                "Soil Systems, Inc. Inhumation Minerals & Staining Coding Sheets", "Soil Systems, Inc. Inhumation & Cremation Multiple Burial Coding Sheet",
                "Soil Systems, Inc. Inhuamtion Skeletal Disturbance Type Coding Sheet", "Soil Systems, Inc. Inhumation Pit Disturbance Type Coding Sheet",
                "Pit Disturbance Type Codes from SSI Inhumation Form", "Soil Systems, Inc. Inhumation Skeletal Preservation Coding Sheet",
                "Soil Systems, Inc. Inhumation Superstructure Position Coding Sheet", "Soil Systems, Inc. Inhumation Superstructure Type Coding Sheet",
                "Soil Systems, Inc. Inhumation Surrounding Fill Coding Sheet", "tet", "Soil Systems, Inc. Ornament Type Coding Sheet",
                "Soil Systems, Inc. Ornament Material Type Coding Sheet", "Soil Systems, Inc. Ornament Shape Coding Sheet",
                "Soil Systems, Inc. Ornament Condition Coding Sheet", "Soil Systems, Inc. Ornament Burning Coding Sheet",
                "Soil Systems, Inc. Shell Ornament Umbo Shape Coding Sheet", "Soil Systems, Inc. Ornament Decoration (other than shell umbo) Coding Sheet",
                "Soil Systems, Inc. Shell Ornament Drilling Method Coding Sheet", "Soil Systems, Inc. Faunal Species Coding Sheet",
                "Soil Systems, Inc. Faunal Elements Coding Sheet", "Soil Systems, Inc. Faunal Bone Portion Coding Sheet",
                "Soil Systems, Inc. Faunal Front/Hind Coding Sheet", "Soil Systems, Inc. Faunal Proximal/Distal Coding Sheet",
                "Soil Systems, Inc. Faunal Anterior/Posterior Coding Sheet", "Soil Systems, Inc. Faunal Medial/Lateral Coding Sheet",
                "Soil Systems, Inc. Faunal Dorsal/Ventral Coding Sheet", "Soil Systems, Inc. Faunal Superior/Inferior Coding Sheet",
                "Soil Systems, Inc. Faunal Upper/Lower Coding Sheet", "Soil Systems, Inc. Faunal Element With Teeth Coding Sheet",
                "Soil Systems, Inc. Faunal Bone Side Coding Sheet", "Soil Systems, Inc. Faunal Sex Coding Sheet",
                "Soil Systems, Inc. Faunal Element Size Coding Sheet", "Soil Systems, Inc. Fauna Age Coding Sheet",
                "Soil Systems, Inc. Faunal Remains Condition (Completeness) Coding Sheet", "Soil Systems, Inc. Faunal Remains Burning Coding Sheet",
                "Soil Systems, Inc. Faunal Remains Modification Coding Sheet", "Soil Systems, Inc. Faunal Artifact Type Coding Sheet",
                "Soil Systems, Inc. Faunal Historic Period Coding Sheet", "Soil Systems, Inc. Lithic Material Type Coding Sheet",
                "Soil Systems, Inc. Lithic Rough Sort Artifact Type Coding Sheet", "Soil Systems, Inc. Projectile Point Analysis Basal Edge Form Coding Sheet",
                "Soil Systems, Inc. Projectile Point Basal Grinding Coding Sheet", "Soil Systems, Inc. Projectile Point Analysis Basal Thinning Coding Sheet",
                "Soil Systems, Inc. Projectile Point Analysis Blade Shape Coding Sheet", "Soil Systems, Inc. Projectile Point Analysis Condition Coding Sheet",
                "Soil Systems, Inc. Projectile Point Cross-Section Coding Sheet", "Proj Point General Form Codes from SSI",
                "Proj Point Grain Size Codes from SSI", "Proj Point Notch Codes from SSI", "Proj Point Retouch Pattern Codes from SSI",
                "Proj Point Retouch Type Codes from SSI", "Proj Point Serrations Codes from SSI",
                "Soil Systems, Inc. Projectile Point Stem Shape Coding Sheet", "Soil Systems, Inc. Projectile Point Fracture Type Coding Sheet",
                "Soil Systems, Inc. Projectile Point Resharpening Coding Sheet", "Soil Systems, Inc. Artifact Type Coding Sheet",
                "Motif Classification and Attributes", "Soil Systems, Inc. Pueblo Grande Burial Time Period Assignments Coding Sheet",
                "Soil Systems, Inc. Pueblo Grande Age at death coding sheet", "Soil Systems, Inc. Pueblo Grande Sex Identification Coding Sheet",
                "Soil Systems, Inc. Pueblo Grande Burial Types Coding Sheet", "HARP Fauna Element Coding Sheet",
                "Soil Systems, Inc. Lithic Condition Coding Sheet", "Soil Systems, Inc. Flotation/Botanical Taxon Coding Sheet",
                "Soil Systems, Inc. Flotation/Botanical Part Coding Sheet", "Soil Systems, Inc. Flotation/Botanical Condition Coding Sheet",
                "Soil Systems, Inc. Flotation/Botanical Specimen Completeness Coding Sheet",
                "Soil Systems, Inc. Flotation/Botanical Analysis Type Coding Sheet", "Raw Material Guide", "Soil Systems, Inc. Presence/Absence Coding Sheet",
                "Soil Systems, Inc. True/False Coding Sheet", "Soil Systems, Inc. Cremation Grave Shape Coding Sheet",
                "Soil Systems, Inc. Inhumation Skeletal Completeness Codes", "EMAP - Ceramics Data Sheet", "EMAP - Analytic Unit Coding Sheet",
                "EMAP - Projectile Points - Material Coding Sheet", "EMAP - Projectile Points - Form Coding Sheet", "Tosawihi Bifaces Material Color Codes",
                "Taxonomic Level 1" };
        Integer[] cats = new Integer[] { 83, 67, 79, 81, 72, 75, 70, 63, 76, 79, 73, 70, 85, 73, 67, 78, 73, 85, 83, 6, 6, 85, 6, 85, 85, 83, 83, 6, 64, 73,
                null, null, 70, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 198, null,
                null, 75, 75, 85, 70, 70, 70, 75, 70, 85, 85, 85, 73, 76, 79, null, 67, null, 191, 70, 72, 73, 75, 64, 82, 78, 81, 83, 192, 85, 67, 78, null,
                85, 192, 85, null, null, null, 73, 81, null, 85, 192, 192, 191, 191, 192, null, null, null, null, 61, 67, 78, 39, 192, 83, 75, 64, 78, 77,
                null, 196, 196, 191, 191, 192, 192, 192, 192, 192, 192, 192, 192, 196, 77, 78, 83, 196, 191, null, null, 64, 191, 192, 192, 191, 75, 73, 81,
                85, 49, 192, null, 63, 73, null, null, null, null, 191, 191, null, null, 192, 42, null, 238, 238, 42, 238, 39, 11, 238, 49, 11, 39, 39, 39, 39,
                238, 198, 196, 198, 198, 198, 214, 191, 78, null, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
                11, 11, 11, 11, 11, 11, 11, 11, null, null, null, null, null, null, 223, null, 223, 85, 73, 81, 198, 81, 62, 198, 72, 214, 214, 73, 83, 82, 73,
                61, 67, 70, 78, 63, 6, 53, 56, 52, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 166, null, 11, 11, 11, 11, 73, 52, 170, 250, 250, 250, 250, null,
                null, null, 11, 11, 36, null, 53, 52, 56, 85, 85 };

        List<CodingSheet> sheets = new ArrayList<CodingSheet>();

        List<CodingSheet> allSheets = new ArrayList<CodingSheet>();
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            Integer cat = cats[i];
            CodingSheet cs = createAndSaveNewInformationResource(CodingSheet.class, getUser(), title);
            allSheets.add(cs);
            if (cat != null) {
                cs.setCategoryVariable(genericService.find(CategoryVariable.class, (long) cat));
            }
            if (title.contains("Taxonomic Level")) {
                logger.info("{} {}", cs, cs.getCategoryVariable().getId());
                sheets.add(cs);
            }
            cs = null;

        }
        genericService.saveOrUpdate(allSheets);
        genericService.synchronize();
        List<Long> sheetIds = PersistableUtils.extractIds(sheets);
        sheets = null;
        genericService.synchronize();
//        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setResourceTypes(Arrays.asList(ResourceType.CODING_SHEET));
        SearchResult<Resource> result = performSearch("Taxonomic Level", null, null, null, null, null, params, 10);
        logger.info("{}", result.getResults());
        logger.info("{}", sheetIds);
        assertTrue(PersistableUtils.extractIds(result.getResults()).containsAll(sheetIds));

        result = performSearch("Taxonomic Level", null, null, null, 85l, getBasicUser(), params, 10);
        logger.info("{}", result.getResults());
        assertTrue(PersistableUtils.extractIds(result.getResults()).containsAll(sheetIds));
        Resource col = ((Resource) result.getResults().get(0));
        assertEquals("Taxonomic Level 1", col.getName());

        result = performSearch(null, null, null, null, 85l, getBasicUser(), params, 1000);
        logger.info("{}", result.getResults());
        assertTrue(PersistableUtils.extractIds(result.getResults()).containsAll(sheetIds));
        genericService.synchronize();

    }

    @Test
    public void testResourceLookupByType() throws SolrServerException, IOException, ParseException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        // get back all documents
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT));
        SearchResult<Resource> result = performSearch("", null, null, null, null, getEditorUser(), params, 1000);

        List<Resource> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByTdarId() throws SolrServerException, IOException, ParseException {
        // get back all documents
        SearchResult<Resource> result = performSearch(TestConstants.TEST_DOCUMENT_ID, null, null, null, null, null, null, 1000);

        List<Resource> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByProjectId() throws SolrServerException, IOException, ParseException {
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
        params.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT,
                ResourceType.ONTOLOGY, ResourceType.IMAGE, ResourceType.DATASET,
                ResourceType.CODING_SHEET));
        return params;
    }

    @Test
    public void testResourceLookup() throws IOException, SolrServerException, ParseException {
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
    public void testResourceLookupById() throws IOException, SolrServerException, ParseException {
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
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);

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

    @Autowired
    private ResourceSearchService resourceSearchService;

    public SearchResult<Resource> performSearch(String term, TdarUser user, int max) throws ParseException, SolrServerException, IOException {
        return performSearch(term, null, null, null, null, user, null, null, max);
    }

    public SearchResult<Resource> performSearch(String term, Long projectId, Long collectionId, Boolean includeParent, Long categoryId, TdarUser user,
            ReservedSearchParameters reservedSearchParameters, int max) throws ParseException, SolrServerException, IOException {
        return performSearch(term, projectId, collectionId, includeParent, categoryId, user, reservedSearchParameters, null, max);
    }

    public SearchResult<Resource> performSearch(String term, Long projectId, Long collectionId, Boolean includeParent, Long categoryId, TdarUser user,
            ReservedSearchParameters reservedSearchParameters, GeneralPermissions permission, int max) throws ParseException, SolrServerException, IOException {
        SearchResult<Resource> result = new SearchResult<>(max);
        logger.debug("{}, {}", resourceSearchService, MessageHelper.getInstance());
        ResourceLookupObject rl = new ResourceLookupObject(term, projectId, includeParent, collectionId, categoryId, permission, reservedSearchParameters);
        resourceSearchService.lookupResource(user, rl, result, MessageHelper.getInstance());
        return result;
    }



    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    GenericKeywordService genericKeywordService;
    


    @Test
    @Rollback(true)
    public void testResourceTypeSearchPhrase() throws ParseException, SolrServerException, IOException {
        ReservedSearchParameters reserved = new ReservedSearchParameters();
        reserved.getResourceTypes().add(ResourceType.IMAGE);
        SearchResult<Resource> result = doSearch("", null, null, reserved);
        for (Indexable r : result.getResults()) {
            assertEquals(ResourceType.IMAGE, ((Resource)r).getResourceType());
        }
    }

    public void setupTestDocuments() throws InstantiationException, IllegalAccessException, SolrServerException, IOException {
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
    public void testExactTitleMatchInKeywordSearch() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
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
    public void testHyphenatedSearchBasic() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        String resourceTitle = _33_CU_314;
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
    public void testHyphenatedTitleSearch() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        String resourceTitle = _33_CU_314;
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        searchIndexService.index(document);
        setupTestDocuments();
        SearchParameters params = new SearchParameters();
        params.getTitles().add(resourceTitle);
        SearchResult<Resource> result = doSearch("", null,params,null);
        logger.info("results:{}", result.getResults());
        assertTrue(result.getResults().contains(document));
        assertTrue(result.getResults().get(0).equals(document) || result.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testUnHyphenatedTitleSearch() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        String resourceTitle = _33_CU_314;
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        searchIndexService.index(document);
        setupTestDocuments();
        SearchParameters params = new SearchParameters();
        params.getTitles().add(resourceTitle.replaceAll("\\-", ""));
        SearchResult<Resource> result = doSearch("", null,params,null);
        logger.info("results:{}", result.getResults());
        assertTrue(result.getResults().contains(document));
        assertTrue(result.getResults().get(0).equals(document) || result.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testHyphenatedSiteNameSearch() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        String resourceTitle = "what fun";
        SiteNameKeyword snk = new SiteNameKeyword();
        String label = _33_CU_314;
        snk.setLabel(label);
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        genericService.save(snk);
        Long id = document.getId();
        document.getSiteNameKeywords().add(snk);
        searchIndexService.index(document);
        setupTestDocuments();
        SearchParameters params = new SearchParameters();
        params.getSiteNames().add(label);
        SearchResult<Resource> result = doSearch("", null, params, null);
        List<Resource> results = result.getResults();
        List<Long> ids = PersistableUtils.extractIds(results);
        logger.info("results:{}", results);
        assertTrue(ids.contains(id));
        assertTrue(ids.get(0).equals(id) || ids.get(1).equals(id));
    }

    @Test
    @Rollback(true)
    public void testHyphenatedSiteNameSearchCombined() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        String resourceTitle = "what fun";
        SiteNameKeyword snk = new SiteNameKeyword();
        String label = _33_CU_314;
        snk.setLabel(label);
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        genericService.save(snk);
        document.getSiteNameKeywords().add(snk);
        searchIndexService.index(document);
        setupTestDocuments();
        SearchResult<Resource> result = doSearch("what fun 33-Cu-314");
        logger.info("results:{}", result.getResults());
        assertTrue(result.getResults().contains(document));
        assertTrue(result.getResults().get(0).equals(document) || result.getResults().get(1).equals(document));
    }

    @Test
    @Rollback(true)
    public void testFindResourceTypePhrase() throws ParseException, SolrServerException, IOException {
        ReservedSearchParameters reserved = new ReservedSearchParameters();
        reserved.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.IMAGE));
        SearchResult<Resource> result = doSearch("", null, null, reserved);
        logger.debug("search phrase:{}", result.getSearchTitle());
        assertTrue(result.getSearchTitle().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ResourceType.IMAGE.getLabel()));
    }

    @Test
    @Rollback(true)
    public void testFindResourceById() throws ParseException, SolrServerException, IOException {
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
    public void testFindTerm() throws ParseException, SolrServerException, IOException {
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.IMAGE));

        SearchResult<Resource> result = doSearch("test", null, null, params);
        logger.info(result.getSearchTitle());
        assertTrue(result.getSearchTitle().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ResourceType.IMAGE.getLabel()));
        assertTrue(result.getSearchTitle().contains("test"));
    }

    @Test
    @Rollback(true)
    public void testCultureKeywordSearch() throws ParseException, SolrServerException, IOException {
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.IMAGE));

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
        assertTrue(searchPhrase.contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(searchPhrase.contains(ResourceType.IMAGE.getLabel()));
        assertTrue(searchPhrase.contains(keyword1.getLabel()));
        assertTrue(searchPhrase.contains(keyword2.getLabel()));
        assertTrue(searchPhrase.contains("test"));
    }

    @Test
    @Rollback(true)
    public void testBadDateSearch() throws ParseException, SolrServerException, IOException {
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.IMAGE));
        CoverageDate cd = new CoverageDate(CoverageType.NONE);
        SearchParameters params = new SearchParameters();
        params.getCoverageDates().add(cd);
        params.getAllFields().add("test");
        SearchResult<Resource> result = doSearch("", null, params, rparams);
        assertTrue(result.getSearchTitle().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ResourceType.IMAGE.getLabel()));
        assertFalse(result.getSearchTitle().contains("null"));
        assertFalse(result.getSearchTitle().contains(" TO "));
    }

    @Test
    @Rollback(true)
    public void testCalDateSearchPhrase() throws ParseException, SolrServerException, IOException {
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.IMAGE));
        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 1200);
        SearchParameters params = new SearchParameters();
        params.getCoverageDates().add(cd);
        params.getAllFields().add("test");
        SearchResult<Resource> result = doSearch("", null, params, rparams);
        logger.debug(result.getSearchTitle());

        assertTrue(result.getSearchTitle().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ResourceType.IMAGE.getLabel()));
        assertFalse(result.getSearchTitle().contains("null"));
        assertTrue(result.getSearchTitle().contains("1000"));
        assertTrue(result.getSearchTitle().contains("1200"));
        assertTrue(result.getSearchTitle().contains(CoverageType.CALENDAR_DATE.getLabel()));
        TdarAssert.assertMatches(result.getSearchTitle(), ".+?" + "\\:.+? \\- .+?");
    }

    @Test
    @Rollback(true)
    public void testSpatialSearch() throws ParseException, SolrServerException, IOException {
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.IMAGE));
        LatitudeLongitudeBox box = new LatitudeLongitudeBox(-1d, -1d, 1d, 1d);
        rparams.getLatitudeLongitudeBoxes().add(box);
        SearchResult<Resource> result = doSearch("test",null, null, rparams);
        assertTrue(result.getSearchTitle().contains(ResourceType.DOCUMENT.getLabel()));
        assertTrue(result.getSearchTitle().contains(ResourceType.IMAGE.getLabel()));
        assertTrue(result.getSearchTitle().contains("Resource Located"));
    }


    @Test
    @Rollback(true)
    public void testDeletedOrDraftMaterialsAreHiddenInDefaultSearch() throws ParseException, SolrServerException, IOException {
        Long imgId = setupImage();
        Long datasetId = setupDataset();
        Long codingSheetId = setupCodingSheet();

        logger.info("imgId:" + imgId + " datasetId:" + datasetId + " codingSheetId:" + codingSheetId);
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(allResourceTypes);
        SearchResult<Resource> result = doSearch("precambrian",null, null, rparams);
        assertFalse(resultsContainId(result,datasetId));
        assertTrue(resultsContainId(result,codingSheetId));
        assertFalse(resultsContainId(result,imgId));
    }

    @Test
    @Rollback(true)
    @Ignore
    public void testGeneratedAreHidden() throws ParseException, SolrServerException, IOException {
        Long codingSheetId = setupCodingSheet();
        CodingSheet sheet = genericService.find(CodingSheet.class, codingSheetId);
        sheet.setGenerated(true);
        genericService.save(sheet);
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.getResourceTypes().add(ResourceType.CODING_SHEET);
        SearchResult<Resource> result = doSearch("", null, null, rparams);
        assertFalse(resultsContainId(result,codingSheetId));
    }

    @Test
    @Rollback(true)
    public void testPeopleAndInstitutionsInSearchResults() throws SolrServerException, IOException, ParseException {
        Long imgId = setupDataset(Status.ACTIVE);
        logger.info("Created new image: " + imgId);
        searchIndexService.index(resourceService.find(imgId));
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(allResourceTypes);
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
    public void testLenientParsing() throws ParseException, SolrServerException, IOException {
        String term = "a term w/ unclosed \" quote and at least one token that will return results: " + TestConstants.DEFAULT_LAST_NAME;
        doSearch(term);
    }

    @Test
    @Rollback(true)
    public void testDatedSearch() throws ParseException, SolrServerException, IOException {
        Long docId = setupDatedDocument();
        logger.info("Created new document: " + docId);
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(allResourceTypes);

        // test inner range
        SearchParameters params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, -900, 1000));
        SearchResult<Resource> result = doSearch("", null, params, rparams);
        assertTrue("expected to find document "+docId+" for inner range match", resultsContainId(result,docId));

        rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(allResourceTypes);
        params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, -2000, -1));
        result = doSearch("", null, params, rparams);
        assertTrue("expected to find document for overlapping range (lower)", resultsContainId(result,docId));

        rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(allResourceTypes);
        params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1999, 2009));
        result = doSearch("", null, params, rparams);
        assertTrue("expected to find document for overlapping range (upper)", resultsContainId(result,docId));

        // test invalid range
        rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(allResourceTypes);
        params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, -7000, -1001));
        result = doSearch("", null, params, rparams);
        assertFalse("expected not to find document in invalid range", resultsContainId(result,docId));

        // test exact range (query inclusive)
        rparams = new ReservedSearchParameters();
        rparams.setResourceTypes(allResourceTypes);
        params = new SearchParameters();
        params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 2000));
        result = doSearch("", null, params, rparams);
        assertTrue("expected to find document for exact range match", resultsContainId(result,docId));
    }

    @Test
    @Rollback
    public void testInvestigationTypes() throws ParseException, SolrServerException, IOException {

        // TODO:dynamically get the list of 'used investigation types' and the resources that use them
        SearchParameters params = addInvestigationTypes(new SearchParameters());
        ReservedSearchParameters reserved = new ReservedSearchParameters();
        
        // this fails because all of the Skeleton Investigation Types with IDs get put into a set, and thus fold into each other
        // because equality based on label[NULL]
        reserved.setResourceTypes(allResourceTypes);
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

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testLookupResourceWithDateRegisteredRange() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
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
    public void testSearchPhraseWithQuote() throws ParseException, SolrServerException, IOException {
        doSearch("\"test");
    }

    @Test
    public void testSearchPhraseWithColon() throws ParseException, SolrServerException, IOException {
        doSearch("\"test : abc ");
    }

    @Test
    public void testSearchPhraseWithLuceneSyntax() throws ParseException, SolrServerException, IOException {
        doSearch("title:abc");
    }

    @Test
    public void testSearchPhraseWithUnbalancedParenthesis() throws ParseException, SolrServerException, IOException {
        doSearch("\"test ( abc ");
    }

    @Test
    @Rollback(true)
    public void testAttachedFileSearch() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), _33_CU_314);
        addFileToResource(document, new File(TestConstants.TEST_DOCUMENT_DIR + "test-file.rtf"));
        searchIndexService.index(document);
        SearchParameters params = new SearchParameters();
        params.getContents().add("fun'");
        SearchResult<Resource> result = doSearch("",null, params,null);
        Long id = document.getId();
        List<Long> ids = PersistableUtils.extractIds(result.getResults());
        logger.info("results:{}", result.getResults());
        assertTrue(ids.contains(id));
        params = new SearchParameters();
        params.getContents().add("have fun digging");
        result = doSearch("",null, params,null);
        logger.info("results:{}", result.getResults());
        ids = PersistableUtils.extractIds(result.getResults());
        assertTrue(ids.contains(id));

    }

    @Test
    @Rollback(true)
    public void testConfidentialFileSearch() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        String resourceTitle = _33_CU_314;
        Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), resourceTitle);
        addFileToResource(document, new File(TestConstants.TEST_DOCUMENT_DIR + "test-file.rtf"), FileAccessRestriction.CONFIDENTIAL);
        searchIndexService.index(document);
        SearchParameters params = new SearchParameters();
        params.getContents().add("fun");
        SearchResult<Resource> result = doSearch("",null, params,null);
        logger.info("results:{}", result.getResults());
        assertFalse(result.getResults().contains(document));
        params = new SearchParameters();
        params.getContents().add("have fun digging");
        result = doSearch("",null, params,null);
        logger.info("results:{}", result.getResults());
        assertFalse(result.getResults().contains(document));

    }

    @Test
    @Rollback
    public void testTitleRelevancy() throws SolrServerException, IOException, ParseException {
    	//(11R5)-1
    	//1/4
    	//4\"
		String exact = "Modoc Rock Shelter, IL (11R5)-1984 Fauna dataset Main Trench 1/4\" Screen";
		//Modoc Rock Shelter, IL (11R5)-1984 Fauna dataset Main Trench 1/4\" Screen
		List<String> titles = Arrays.asList(
				"Coding sheet for Element ("+exact+")",
				"Coding sheet for Recovery ("+exact+")",
				"Coding sheet for CulturalAffiliation ("+exact+")",
				"Coding sheet for Resource Type ("+exact+")",
				"Coding sheet for Taxon ("+exact+")",
				"Coding sheet for Portion3 ("+exact+")",
				"Coding sheet for Resource Type ("+exact+")",
				"Coding sheet for ContextType ("+exact+")",
				"Coding sheet for LevelType ("+exact+")",
				"Coding sheet for Site ("+exact+")",
				"Coding sheet for LevelType ("+exact+")",
				exact,
				"Coding sheet for Taxon (Modoc Rock Shelter (11R5), Randolph County, IL-1984, Main Trench 1/4\" screen fauna)");


		List<Resource> docs = new ArrayList<>();
        for (String title : titles) {
            Resource doc = new CodingSheet();
            if (title.equals(exact)) {
            	doc = new Dataset();
            	doc.setDescription("a");
            } else {
            	doc.setDescription(exact);
            }
            doc.setTitle(title);
            doc.markUpdated(getBasicUser());
            genericService.saveOrUpdate(doc);
            docs.add(doc);
        }
        genericService.synchronize();
        searchIndexService.indexCollection(docs);
        searchIndexService.flushToIndexes();
        SearchResult<Resource> result = doSearch(exact);
        List<Resource> results = result.getResults();
        for (Resource r : result.getResults()) {
            logger.debug("results: {}", r);
        }
        assertEquals(exact,result.getResults().get(0).getTitle());
    }

}

package org.tdar.struts.action.resource;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.HibernateSearchDao;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.keyword.SiteNameKeywordService;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.search.LuceneSearchController;

public class SearchRelevancyITCase extends AbstractResourceControllerITCase {

    @Autowired
    private LuceneSearchController controller;
    @Autowired
    private SiteNameKeywordService siteNameKeywordService;
    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    HibernateSearchDao hibernateSearchDao;

    Document resourceWithTitleMatch;
    Document resourceWithKeywordMatch;
    Document resourceWithAttachmentMatch;

    // a word/phrase that we only expect to see in the resources we create for this test
    private static final String SEMI_UNIQUE_NAME = "semiuniquename";

    @Override
    protected String getTestFilePath() {
        return super.getTestFilePath() + "/relevancy_tests";
    }

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    // fixme: generics pointless here?
    private <T extends InformationResource> T prepareResource(T iResource, String title, String description) {
        Project project = Project.NULL;
        Person submitter = getUser();

        iResource.setTitle(title);
        iResource.setDescription(description);

        iResource.markUpdated(submitter);
        iResource.setProject(project);
        informationResourceService.save(iResource);
        return iResource;
    }

    private void runIndex() {
        searchIndexService.indexAll(Resource.class);
    }

    @Before
    // create the necessary information resources needed for our test
    public void prepareInformationResources() {

        // prep the search controller
        searchIndexService.purgeAll();
        controller.setRecordsPerPage(50);
        // prep the doc that matches on title (most relevant)
        resourceWithTitleMatch = prepareResource(new Document(), SEMI_UNIQUE_NAME, "desc");
        logger.debug("resourceWithTitleMatch:" + resourceWithTitleMatch.getId());

        // now init the document that matches on keywords only
        resourceWithKeywordMatch = new Document();
        Set<SiteNameKeyword> siteNameKeywords = new HashSet<SiteNameKeyword>();
        siteNameKeywords.add(siteNameKeywordService.findOrCreateByLabel(SEMI_UNIQUE_NAME));
        resourceWithKeywordMatch.setSiteNameKeywords(siteNameKeywords);
        prepareResource(resourceWithKeywordMatch, "doc with match on keywords", "desc");
        logger.debug("resourceWithKeywordMatch:" + resourceWithKeywordMatch.getId());

        // finally create a document that only matches the semiunique name in an attachment;
        resourceWithAttachmentMatch = setupAndLoadResource("reltest2.pdf", Document.class);
        logger.debug("resourceWithAttachmentMatch:" + resourceWithAttachmentMatch.getId());
        logger.debug("done with prep");

        Assert.assertFalse(resourceWithAttachmentMatch.getInformationResourceFiles().isEmpty());
        Assert.assertEquals(1, resourceWithAttachmentMatch.getInformationResourceFiles().size());
        Assert.assertEquals(5, resourceWithAttachmentMatch.getInformationResourceFiles().iterator().next().getInformationResourceFileVersions().size());
    }

    // assert title match appears before keywordmatch, and keywordmatch before docmatch and docmatch
    @Test
    @Rollback
    // FIXME: Throw more results into the mix so they won't just happen to be sorted even when sorting is turned off (even a broken clock is right twice a day).
    public void testLocationRelevancy() throws IOException {
        prepareInformationResources();
        runIndex();
        controller.setQuery(SEMI_UNIQUE_NAME);
        controller.setSortField(SortOption.RELEVANCE);
        logger.debug("about to perform search");
        controller.performSearch();

        // we should get back **something**
        Assert.assertTrue("search results should not be empty", controller.getResults().size() > 0);
        logger.debug("result count for " + SEMI_UNIQUE_NAME + ":" + controller.getResults().size());
        logger.debug("pdf indexablecontent:" + resourceWithAttachmentMatch.getContent());

        int indexOfTitleMatch = controller.getResults().indexOf(resourceWithTitleMatch);
        int indexOfKeywordMatch = controller.getResults().indexOf(resourceWithKeywordMatch);
        int indexOfAttachmentMatch = controller.getResults().indexOf(resourceWithAttachmentMatch);

        // make sure we got back at least the resources we just added.
        Assert.assertTrue("expecting test resource in results", indexOfTitleMatch > -1);
        Assert.assertTrue("expecting test resource in results", indexOfKeywordMatch > -1);
        Assert.assertTrue("expecting test resource in results", indexOfAttachmentMatch > -1);
        logger.debug("indexOfTitleMatch:" + indexOfTitleMatch);
        logger.debug("indexOfKeywordMatch:" + indexOfKeywordMatch);
        logger.debug("indexOfAttachmentMatch:" + indexOfAttachmentMatch);

        // make sure they're in the right order...
        Assert.assertTrue("expecting test resource in results", indexOfTitleMatch < indexOfKeywordMatch);
        Assert.assertTrue("expecting test resource in results", indexOfKeywordMatch < indexOfAttachmentMatch);
        Assert.assertTrue("expecting test resource in results", indexOfKeywordMatch < indexOfAttachmentMatch);

        Assert.assertTrue("indexOfTitleMatch < indexOfKeywordMatch.  indexOfTitleMatch:" + indexOfTitleMatch + " indexOfKeywordMatch:" + indexOfKeywordMatch
                , indexOfTitleMatch < indexOfKeywordMatch);
        Assert.assertTrue("expecting indexOfKeywordMatch < indexOfAttachmentMatch. indexOfKeywordMatch:" + indexOfKeywordMatch +
                " indexOfAttachmentMatch:" + indexOfAttachmentMatch, indexOfKeywordMatch < indexOfAttachmentMatch);

    }

    // given resource1 and resource2, where both have same title/desc/keywords, assert resource1 is more relevant because it has an attachment
    @Test
    @Rollback
    public void testInheritanceInSearching() throws InstantiationException, IllegalAccessException {
        Project p = new Project();
        p.setTitle("test project");
        p.markUpdated(getUser());
        p.setDescription("test 1234");
        p.setStatus(Status.ACTIVE);
        genericService.saveOrUpdate(p);
        Document document = createAndSaveNewInformationResource(Document.class);
        document.setProject(p);
        document.setStatus(Status.ACTIVE);
        document.setInheritingMaterialInformation(true);
        genericService.saveOrUpdate(document);

        List<MaterialKeyword> materialKeywords = genericService.findRandom(MaterialKeyword.class, 3);
        p.getMaterialKeywords().addAll(materialKeywords);
        genericService.saveOrUpdate(p);
        searchIndexService.index(p);
        controller.setMaterialKeywordIds(Arrays.asList(materialKeywords.get(0).getId()));
        controller.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT, ResourceType.PROJECT));
        controller.performSearch();

        logger.debug("{}", controller.getResults());
        assertTrue(controller.getResults().contains(p));
        assertTrue(controller.getResults().contains(document));
    }

    // @Test
    // public void testLuceneInternals() throws IOException, ParseException {
    // DirectoryProvider clientProvider = hibernateSearchDao.getFullTextSession().getSearchFactory().getDirectoryProviders(Resource.class)[0];
    // logger.info("hi");
    // ReaderProvider readerProvider = hibernateSearchDao.getFullTextSession().getSearchFactory().getReaderProvider();
    // IndexReader reader = readerProvider.openReader(clientProvider);
    // IndexSearcher searcher = new IndexSearcher(reader);
    // // Query query = QueryBuilder.;
    // QueryParser parser = new QueryParser(Version.LUCENE_31, "projectId", new TdarStandardAnalyzer());
    // Query query = parser.parse("projectId:3805");
    // TopDocs hits = searcher.search(query, null, 1000);
    // for (int i = 0; i < hits.scoreDocs.length; i++) {
    // org.apache.lucene.document.Document hitDoc = searcher.doc(hits.scoreDocs[i].doc);
    // logger.info(hitDoc.toString());
    // for (Fieldable field : hitDoc.getFields()) {
    // // field.
    // }
    // }
    // }
}

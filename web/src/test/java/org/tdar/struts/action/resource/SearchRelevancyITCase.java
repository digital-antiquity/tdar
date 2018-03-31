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
import org.tdar.TestConstants;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.search.AdvancedSearchController;
import org.tdar.struts_base.action.TdarActionException;

public class SearchRelevancyITCase extends AbstractControllerITCase {

    private static final String TEST_RELEVANCY_DIR = TestConstants.TEST_ROOT_DIR + "relevancy_tests/";

    @Autowired
    private GenericKeywordService genericKeywordService;
    @Autowired
    SearchIndexService searchIndexService;

    Document resourceWithTitleMatch;
    Document resourceWithKeywordMatch;
    Document resourceWithAttachmentMatch;

    // a word/phrase that we only expect to see in the resources we create for this test
    private static final String SEMI_UNIQUE_NAME = "semiuniquename";

    @Override
    public String getTestFilePath() {
        return super.getTestFilePath() + "/relevancy_tests";
    }

    // fixme: generics pointless here?
    private <T extends InformationResource> T prepareResource(T iResource, String title, String description) {
        Project project = Project.NULL;
        TdarUser submitter = getUser();

        iResource.setTitle(title);
        iResource.setDescription(description);

        iResource.markUpdated(submitter);
        iResource.setProject(project);
        genericService.save(iResource);
        return iResource;
    }

    private void runIndex() {
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
    }

    @Before
    // create the necessary information resources needed for our test
    public void prepareInformationResources() throws TdarActionException {

        // prep the search controller
        searchIndexService.purgeAll();
        // prep the doc that matches on title (most relevant)
        resourceWithTitleMatch = prepareResource(new Document(), SEMI_UNIQUE_NAME, "desc");
        logger.debug("resourceWithTitleMatch:" + resourceWithTitleMatch.getId());

        // now init the document that matches on keywords only
        resourceWithKeywordMatch = new Document();
        Set<SiteNameKeyword> siteNameKeywords = new HashSet<SiteNameKeyword>();
        siteNameKeywords.add(genericKeywordService.findOrCreateByLabel(SiteNameKeyword.class, SEMI_UNIQUE_NAME));
        resourceWithKeywordMatch.setSiteNameKeywords(siteNameKeywords);
        prepareResource(resourceWithKeywordMatch, "doc with match on keywords", "desc");
        logger.debug("resourceWithKeywordMatch:" + resourceWithKeywordMatch.getId());

        // finally create a document that only matches the semiunique name in an attachment;
        resourceWithAttachmentMatch = setupAndLoadResource(TEST_RELEVANCY_DIR + "reltest2.pdf", Document.class);
        logger.debug("resourceWithAttachmentMatch:" + resourceWithAttachmentMatch.getId());
        logger.debug("done with prep");

        Assert.assertFalse(resourceWithAttachmentMatch.getInformationResourceFiles().isEmpty());
        Assert.assertEquals(1, resourceWithAttachmentMatch.getInformationResourceFiles().size());
        int size = resourceWithAttachmentMatch.getInformationResourceFiles().iterator().next().getInformationResourceFileVersions().size();
        if ((size != 3) && (size != 6)) {
            Assert.fail("wrong number of derivatives found");
        }
    }

    // assert title match appears before keywordmatch, and keywordmatch before docmatch and docmatch
    @Test
    @Rollback
    public void testLocationOfPhraseRelevancy() throws IOException, TdarActionException {
        prepareInformationResources();
        runIndex();
        AdvancedSearchController controller = generateNewInitializedController(AdvancedSearchController.class);
        controller.setRecordsPerPage(50);
        controller.setServletRequest(getServletRequest());

        controller.setQuery(SEMI_UNIQUE_NAME);
        controller.setSortField(SortOption.RELEVANCE);
        logger.debug("about to perform search");
        controller.search();

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
        logger.debug("{} indexOfTitleMatch:{}", resourceWithTitleMatch.getId(), indexOfTitleMatch);
        logger.debug("{} indexOfKeywordMatch:{} ", resourceWithKeywordMatch.getId(), indexOfKeywordMatch);
        logger.debug("{} indexOfAttachmentMatch: {}", resourceWithAttachmentMatch.getId(), indexOfAttachmentMatch);

        // make sure they're in the right order...
        Assert.assertTrue("expecting test resource in results", indexOfTitleMatch < indexOfKeywordMatch);
        Assert.assertTrue("expecting test resource in results", indexOfKeywordMatch < indexOfAttachmentMatch);

        Assert.assertTrue("indexOfTitleMatch < indexOfKeywordMatch.  indexOfTitleMatch:" + indexOfTitleMatch + " indexOfKeywordMatch:" + indexOfKeywordMatch,
                indexOfTitleMatch < indexOfKeywordMatch);
        Assert.assertTrue("expecting indexOfKeywordMatch < indexOfAttachmentMatch. indexOfKeywordMatch:" + indexOfKeywordMatch +
                " indexOfAttachmentMatch:" + indexOfAttachmentMatch, indexOfKeywordMatch < indexOfAttachmentMatch);

    }

    // given resource1 and resource2, where both have same title/desc/keywords, assert resource1 is more relevant because it has an attachment
    @Test
    @Rollback
    public void testInheritanceInSearching() throws InstantiationException, IllegalAccessException, TdarActionException, SearchIndexException, IOException {
        AdvancedSearchController controller = generateNewInitializedController(AdvancedSearchController.class);
        controller.setRecordsPerPage(50);
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
        searchIndexService.index(p, document);
        SearchParameters sp = new SearchParameters();
        controller.getGroups().add(sp);
        sp.getMaterialKeywordIdLists().add(Arrays.asList(materialKeywords.get(0).getId().toString()));
        controller.getResourceTypes().addAll((Arrays.asList(ResourceType.DOCUMENT, ResourceType.PROJECT)));
        controller.search();

        logger.debug("{}", controller.getResults());
        assertTrue(controller.getResults().contains(p));
        assertTrue(controller.getResults().contains(document));
    }

}

package org.tdar.struts.action.search;

import static org.junit.Assert.*;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.collection.WhiteLabelCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResultHandler.ProjectionModel;
import org.tdar.search.service.SearchFieldType;
import org.tdar.search.service.SearchIndexService;
import org.tdar.search.service.SearchParameters;
import org.tdar.struts.action.AbstractControllerITCase;

@Transactional
public class AdvancedSearchControllerITCase extends AbstractControllerITCase {

    private static final String USAF_LOWER_CASE = "us air force archaeology and cultural resources archive";

    private static final String USAF_TITLE_CASE = "US Air Force Archaeology and Cultural Resources Archive";

    private static final String CONSTANTINOPLE = "Constantinople";

    private static final String ISTANBUL = "Istanbul";

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    ResourceService resourceServicek;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Autowired
    EntityService entityService;

    @Autowired
    private AuthorizationService authenticationAndAuthorizationService;

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
    @Rollback(true)
    public void testResultCountsAsUnauthenticatedUser() throws SolrServerException, IOException {
        evictCache();

        setIgnoreActionErrors(true);
        testResourceCounts(null);
    }

    @Test
    @Rollback(true)
    public void testResultCountsAsBasicUser() throws SolrServerException, IOException {
        evictCache();

        // testing as a user who did not create their own stuff
        setIgnoreActionErrors(true);
        TdarUser p = new TdarUser("a", "test", "anoter@test.user.com");
        p.setContributor(true);
        genericService.saveOrUpdate(p);
        testResourceCounts(p);
    }

    @Test
    @Rollback(true)
    public void testResultCountsAsBasicContributor() throws SolrServerException, IOException {
        // testing as a user who did create their own stuff
        evictCache();
        setIgnoreActionErrors(true);
        testResourceCounts(getBasicUser());
    }

    @Test
    @Rollback(true)
    public void testResultCountsAdmin() throws SolrServerException, IOException {
        evictCache();
        testResourceCounts(getAdminUser());
    }
    

    private void testResourceCounts(TdarUser user) throws SolrServerException, IOException {
        for (ResourceType type : ResourceType.values()) {
            Resource resource = createAndSaveNewResource(type.getResourceClass());
            for (Status status : Status.values()) {
                if ((Status.DUPLICATE == status) || (Status.FLAGGED_ACCOUNT_BALANCE == status)) {
                    continue;
                }
                resource.setStatus(status);
                genericService.saveOrUpdate(resource);
                searchIndexService.index(resource);
                assertResultCount(type, status, user);
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


    // compare the counts returned from searchController against the counts we get from the database
    private void assertResultCount(ResourceType resourceType, Status status, TdarUser user) {
        String stat = String.format("testing %s , %s for %s", resourceType, status, user);
        logger.info(stat);
        long expectedCount = resourceService.getResourceCount(resourceType, status);
        controller = generateNewController(AdvancedSearchController.class);
        init(controller, user);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        controller.getResourceTypes().add(resourceType);
        controller.getIncludedStatuses().add(status);
        if (((status == Status.DELETED) && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, user)) ||
                ((status == Status.FLAGGED) && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, user))) {
            logger.debug("expecting exception");
            doSearch(true);
            assertTrue(String.format("expected action errors %s", stat), controller.getActionErrors().size() > 0);
        } else if ((status == Status.DRAFT) && authenticationAndAuthorizationService.cannot(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, user)) {
            // this was in the test, but with the new status search I think this is more accurate to be commented out as
            doSearch(null);
            for (Resource res : controller.getResults()) {
                if (res.isDraft() && !res.getSubmitter().equals(user)) {
                    fail("we should only see our own drafts here");
                }

            }
            // assertEquals(String.format("expecting results to be empty %s",stat),0, result.getTotalRecords());
        } else {
            doSearch();
            Object[] msg_ = { user, resourceType, status, expectedCount, controller.getTotalRecords() };
            String msg = String.format("User: %s ResourceType:%s  Status:%s  expected:%s actual: %s", msg_);
            logger.info(msg);
            Assert.assertEquals(msg, expectedCount, controller.getTotalRecords());
        }
    }

    @Test
    public void testResourceCaseSensitivity() throws SolrServerException, IOException {
        Document doc = createAndSaveNewResource(Document.class);
        ResourceCollection titleCase = new ResourceCollection(USAF_TITLE_CASE, "test", SortOption.RELEVANCE, CollectionType.SHARED, false, getAdminUser());
        titleCase.markUpdated(getAdminUser());
        ResourceCollection lowerCase = new ResourceCollection(USAF_LOWER_CASE, "test", SortOption.RELEVANCE, CollectionType.SHARED, false, getAdminUser());
        lowerCase.markUpdated(getAdminUser());
        ResourceCollection upperCase = new ResourceCollection("USAF", "test", SortOption.RELEVANCE, CollectionType.SHARED, false, getAdminUser());
        upperCase.markUpdated(getAdminUser());
        ResourceCollection usafLowerCase = new ResourceCollection("usaf", "test", SortOption.RELEVANCE, CollectionType.SHARED, false, getAdminUser());
        usafLowerCase.markUpdated(getAdminUser());
        doc.setTitle("USAF");
        updateAndIndex(doc);
        usafLowerCase.getResources().add(doc);
        titleCase.getResources().add(doc);
        lowerCase.getResources().add(doc);
        upperCase.getResources().add(doc);
        updateAndIndex(titleCase);
        updateAndIndex(lowerCase);
        updateAndIndex(upperCase);
        updateAndIndex(usafLowerCase);

        // search lowercase one word
        controller.setQuery("usaf");
        doSearch();
        assertTrue(controller.getResults().contains(doc));
        assertTrue(controller.getCollectionResults().contains(usafLowerCase));
        assertTrue(controller.getCollectionResults().contains(upperCase));
        doc.setTitle("usaf");
        resetController();
        updateAndIndex(doc);

        // search uppercase one word
        controller.setQuery("USAF");
        doSearch();
        assertTrue(controller.getCollectionResults().contains(usafLowerCase));
        assertTrue(controller.getCollectionResults().contains(upperCase));
        assertTrue(controller.getResults().contains(doc));

        resetController();
        // search lowercase phrase
        controller.setQuery("us air");
        doSearch();
        assertTrue(controller.getCollectionResults().contains(titleCase));
        assertTrue(controller.getCollectionResults().contains(lowerCase));

        resetController();
        // search titlecase phrase
        controller.setQuery("US Air");
        doSearch();
        assertTrue(controller.getCollectionResults().contains(titleCase));
        assertTrue(controller.getCollectionResults().contains(lowerCase));

        resetController();
        // search uppercase phrase
        controller.setQuery("US AIR");
        doSearch();
        assertTrue(controller.getCollectionResults().contains(titleCase));
        assertTrue(controller.getCollectionResults().contains(lowerCase));

        // search lowercase middle word
        controller.setQuery("force");
        doSearch();
        assertTrue(controller.getCollectionResults().contains(titleCase));
        assertTrue(controller.getCollectionResults().contains(lowerCase));

        // search uppercase middle word
        controller.setQuery("FORCE");
        doSearch();
        assertTrue(controller.getCollectionResults().contains(titleCase));
        assertTrue(controller.getCollectionResults().contains(lowerCase));
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
    @Rollback
    public void testWhitelabelAdvancedSearch() {
        String collectionTitle = "The History Channel Presents: Ancient Ceramic Bowls That Resemble Elvis";
        WhiteLabelCollection rc = createAndSaveNewWhiteLabelCollection(collectionTitle);

        getLogger().debug("collection saved. Id:{}  obj:{}", rc.getId(), rc);
        controller.setCollectionId(rc.getId());
        getLogger().debug("controller collectionId:{}", controller.getCollectionId());
        controller.advanced();

        // We should now have two terms: within-collection, and all-fields
        assertThat(firstGroup().getFieldTypes(), contains(SearchFieldType.COLLECTION, SearchFieldType.ALL_FIELDS));
        assertThat(firstGroup().getCollections().get(0).getTitle(), is(collectionTitle));
    }

    private void updateAndIndex(Indexable doc) throws SolrServerException, IOException {
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
    }

    @Override
    protected void reindex() {
        evictCache();
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), Resource.class, Person.class, Institution.class, ResourceCollection.class);
    }

    protected void doSearch() {
        doSearch(false);
    }

    protected void doSearch(Boolean b) {
        controller.setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        AbstractSearchControllerITCase.doSearch(controller, LookupSource.RESOURCE, b);
        logger.info("search found: " + controller.getTotalRecords());
    }

}

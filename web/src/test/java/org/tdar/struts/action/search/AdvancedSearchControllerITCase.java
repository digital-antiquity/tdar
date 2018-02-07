package org.tdar.struts.action.search;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
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
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.bean.SearchFieldType;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts_base.action.TdarActionException;

@Transactional
public class AdvancedSearchControllerITCase extends AbstractControllerITCase {

    private static final String USAF_LOWER_CASE = "us air force archaeology and cultural resources archive";

    private static final String USAF_TITLE_CASE = "US Air Force Archaeology and Cultural Resources Archive";

    public static final String CONSTANTINOPLE = "Constantinople";

    public static final String ISTANBUL = "Istanbul";

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
    public void testSorting() {
        controller.execute();
        logger.debug("sort:{}", controller.getSortOptions());
        assertNotEmpty("should have sort options", controller.getSortOptions());
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
    public void testResultCountsAsUnauthenticatedUser() throws SearchIndexException, IOException {
        evictCache();

        setIgnoreActionErrors(true);
        testResourceCounts(null);
    }

    @Test
    @Rollback(true)
    public void testResultCountsAsBasicUser() throws SearchIndexException, IOException {
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
    public void testResultCountsAsBasicContributor() throws SearchIndexException, IOException {
        // testing as a user who did create their own stuff
        evictCache();
        setIgnoreActionErrors(true);
        testResourceCounts(getBasicUser());
    }

    @Test
    @Rollback(true)
    public void testResultCountsAdmin() throws SearchIndexException, IOException {
        evictCache();
        testResourceCounts(getAdminUser());
    }
    

    private void testResourceCounts(TdarUser user) throws SearchIndexException, IOException {
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
    public void testResourceCaseSensitivity() throws SearchIndexException, IOException {
        Document doc = createAndSaveNewResource(Document.class);
        SharedCollection titleCase = new SharedCollection(USAF_TITLE_CASE, "test",  getAdminUser());
        titleCase.markUpdated(getAdminUser());
        SharedCollection lowerCase = new SharedCollection(USAF_LOWER_CASE, "test",  getAdminUser());
        lowerCase.markUpdated(getAdminUser());
        SharedCollection upperCase = new SharedCollection("USAF", "test",  getAdminUser());
        upperCase.markUpdated(getAdminUser());
        SharedCollection usafLowerCase = new SharedCollection("usaf", "test", getAdminUser());
        usafLowerCase.markUpdated(getAdminUser());
        doc.setTitle("USAF");
        usafLowerCase.getResources().add(doc);
        titleCase.getResources().add(doc);
        lowerCase.getResources().add(doc);
        upperCase.getResources().add(doc);
        genericService.saveOrUpdate(usafLowerCase, titleCase, lowerCase, upperCase);
        reindex();

        // search lowercase one word
        controller.setQuery("usaf");
        doSearch();
        assertTrue(controller.getResults().contains(doc));
//        assertTrue(controller.getCollectionResults().contains(usafLowerCase));
//        assertTrue(controller.getCollectionResults().contains(upperCase));
        doc.setTitle("usaf");
        resetController();
        updateAndIndex(doc);

        // search uppercase one word
        controller.setQuery("USAF");
        doSearch();
//        assertTrue(controller.getCollectionResults().contains(usafLowerCase));
//        assertTrue(controller.getCollectionResults().contains(upperCase));
        assertTrue(controller.getResults().contains(doc));

        resetController();
        // search lowercase phrase
        controller.setQuery("us air");
        doSearch();
//        assertTrue(controller.getCollectionResults().contains(titleCase));
//        assertTrue(controller.getCollectionResults().contains(lowerCase));

        resetController();
        // search titlecase phrase
        controller.setQuery("US Air");
        doSearch();
//        assertTrue(controller.getCollectionResults().contains(titleCase));
//        assertTrue(controller.getCollectionResults().contains(lowerCase));

        resetController();
        // search uppercase phrase
        controller.setQuery("US AIR");
        doSearch();
//        assertTrue(controller.getCollectionResults().contains(titleCase));
//        assertTrue(controller.getCollectionResults().contains(lowerCase));

        // search lowercase middle word
        controller.setQuery("force");
        doSearch();
//        assertTrue(controller.getCollectionResults().contains(titleCase));
//        assertTrue(controller.getCollectionResults().contains(lowerCase));

        // search uppercase middle word
        controller.setQuery("FORCE");
        doSearch();
//        assertTrue(controller.getCollectionResults().contains(titleCase));
//        assertTrue(controller.getCollectionResults().contains(lowerCase));
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
        ListCollection rc = createAndSaveNewWhiteLabelCollection(collectionTitle);

        getLogger().debug("collection saved. Id:{}  obj:{}", rc.getId(), rc);
        controller.setCollectionId(rc.getId());
        getLogger().debug("controller collectionId:{}", controller.getCollectionId());
        controller.advanced();

        // We should now have two terms: within-collection, and all-fields
        assertThat(firstGroup().getFieldTypes(), contains(SearchFieldType.COLLECTION, SearchFieldType.ALL_FIELDS));
        assertThat(((VisibleCollection)firstGroup().getCollections().get(0)).getTitle(), is(collectionTitle));
    }

    private void updateAndIndex(Indexable doc) throws SearchIndexException, IOException {
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
    }

    @Override
    protected void reindex() {
        evictCache();
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON, LookupSource.COLLECTION, LookupSource.INSTITUTION, LookupSource.KEYWORD,LookupSource.RESOURCE);
    }

    protected void doSearch() {
        doSearch(false);
    }

    protected void doSearch(Boolean b) {
        controller.setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        AbstractSearchControllerITCase.doSearch(controller, LookupSource.RESOURCE, b);
        logger.info("search found: " + controller.getTotalRecords());
    }

    @Test
    @Rollback
    public void testPersonSearchWithoutAutocomplete() throws SearchIndexException, IOException {
        String lastName = "Watts";
        Person person = new Person(null, lastName, null);
        controller.setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        lookForCreatorNameInResult(lastName, person);
    }


    @Test
    @Rollback
    public void testInstitutionSearchWithoutAutocomplete() throws SearchIndexException, IOException {
        controller.setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        String name = "Digital Antiquity";
        Institution institution = new Institution(name);
        lookForCreatorNameInResult(name, institution);
    }


    @Test
    @Rollback
    public void testCollectionSearchLabelText() throws SearchIndexException, IOException, TdarActionException {
        ResourceCollection collection = createAndSaveNewResourceCollection("abecd");
        controller.setCollectionId(collection.getId());
        controller.setQuery("ab");
        controller.search();
        logger.debug(controller.getSearchPhrase());
        assertFalse(StringUtils.contains(controller.getSearchPhrase(), "null"));
    }


    private void lookForCreatorNameInResult(String namePart, Creator<?> creator_) {
        firstGroup().getResourceCreatorProxies().add(new ResourceCreatorProxy(new ResourceCreator(creator_, null)));
        doSearch();
        assertFalse("we should get back at least one hit", controller.getResults().isEmpty());
        for (Resource resource : controller.getResults()) {
            logger.info("{}", resource);
            boolean seen = checkResourceForValue(namePart, resource);
            if (resource instanceof Project) {
                for (Resource r : projectService.findAllResourcesInProject((Project) resource, Status.values())) {
                    if (seen) {
                        break;
                    }
                    seen = checkResourceForValue(namePart, r);
                }

            }
            assertTrue("should have seen term somwehere", seen);
        }
    }

    private boolean checkResourceForValue(String namePart, Resource resource) {
        boolean seen = false;
        if (resource.getSubmitter().getProperName().contains(namePart) || resource.getUpdatedBy().getProperName().contains(namePart)) {
            logger.debug("seen submitter or updater");
            seen = true;
        }
        if (resource instanceof InformationResource) {
            Institution institution = ((InformationResource) resource).getResourceProviderInstitution();
            if ((institution != null) && institution.getName().contains(namePart)) {
                logger.debug("seen in institution");
                seen = true;
            }
        }
        for (ResourceCreator creator : resource.getActiveResourceCreators()) {
            if (creator.getCreator().getProperName().contains(namePart)) {
                logger.debug("seen in resource creator");
                seen = true;
            }
        }
        return seen;
    }



    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testSynonymPersonSearch() throws SearchIndexException, IOException {
        // setup test
        // create image
        Image image = new Image();
        image.setTitle("precambrian Test");
        image.setDescription("image description");
        image.markUpdated(getBasicUser());
        image.setStatus(Status.ACTIVE);

        // create primary creator
        TdarUser person = createAndSaveNewPerson("adelphi@tdar.org", "delphi");

        // create dup
        Person dup = new Person("Delphi", "Person", "d@aoracl.adb");
        dup.setInstitution(new Institution("a test 13asd as"));
        genericService.saveOrUpdate(person.getInstitution());
        dup.setStatus(Status.DUPLICATE);
        dup.setInstitution(new Institution("test 123ad as"));
        genericService.saveOrUpdate(dup.getInstitution());
        genericService.saveOrUpdate(dup);
        person.getSynonyms().add(dup);
        genericService.saveOrUpdate(person);
        genericService.saveOrUpdate(dup);
        ResourceCreator rc = new ResourceCreator(dup, ResourceCreatorRole.CREATOR);
        image.getResourceCreators().add(rc);
        genericService.saveOrUpdate(image);

        genericService.synchronize();
        // flush, detach (important for test), setup
        searchIndexService.index(image);
        genericService.detachFromSession(person);
        genericService.detachFromSession(dup);
        SearchParameters sp = new SearchParameters();

        // transient version of person
        Person p = new Person(person.getFirstName(), person.getLastName(), person.getEmail());
        p.setInstitution(person.getInstitution());
        genericService.detachFromSession(p);
        p.setId(person.getId());

        // test finding dup from parent
        sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(p, ResourceCreatorRole.CREATOR));
        controller.getGroups().add(sp);
        doSearch();
        logger.debug("resutls: {}", controller.getResults());
        assertTrue(controller.getResults().contains(image));

        resetController();
        // test finding parent from dup
        sp = new SearchParameters();
        sp.getResourceCreatorProxies().add(new ResourceCreatorProxy(dup, ResourceCreatorRole.CREATOR));
        controller.getGroups().add(sp);
        doSearch();
        logger.debug("resutls: {}", controller.getResults());
        assertTrue(controller.getResults().contains(image));

    }

}
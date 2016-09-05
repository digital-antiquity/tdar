package org.tdar.core.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.utils.PersistableUtils;

public class ResourceCollectionITCase extends AbstractIntegrationTestCase {

    @Autowired
    ResourceCollectionDao resourceCollectionDao;

    private static final String TEST_TITLE = "Brookville Reservoir Survey 1991-1992";

    @Test
    @Rollback(true)
    public void testSetupCorrect() {
        ResourceCollection collection = resourceCollectionService.find(1575l);
        assertFalse(((VisibleCollection) collection).isHidden());
    }

    @Test
    @Rollback(true)
    public void testSparseResource() throws Exception {
        SharedCollection collection = new SharedCollection("test", "test", true, getAdminUser());
        collection.markUpdated(getAdminUser());
        collection.setResources(new HashSet<>(genericService.findRandom(Resource.class, 20)));
        genericService.saveOrUpdate(collection);
        Long collectionId = collection.getId();
        collection = null;

        collection = genericService.findAll(SharedCollection.class, Arrays.asList(collectionId)).get(0);

        for (Resource resource : collection.getResources()) {
            logger.info("{} {} ", resource, resource.getSubmitter());
        }

    }

    @Test
    @Rollback
    public void testMakeActive() throws Exception {
        SharedCollection collection = new SharedCollection("test", "test", true, getAdminUser());
        collection.markUpdated(getAdminUser());
        boolean seen = false;
        genericService.saveOrUpdate(collection);
        for (Resource r : genericService.findRandom(Resource.class, 20)) {
            r.setStatus(Status.ACTIVE);
            if (seen == false) {
                r.setStatus(Status.DRAFT);
            }
            r.getSharedCollections().add(collection);
            genericService.saveOrUpdate(r);
            collection.getResources().add(r);
        }
        Long collectionId = collection.getId();
        collection = null;
        collection = genericService.find(SharedCollection.class, collectionId);
        resourceCollectionService.makeResourcesInCollectionActive(collection, getAdminUser());
        for (Resource r : collection.getResources()) {
            assertEquals(Status.ACTIVE, r.getStatus());
        }
    }

    /**
     * Make sure that case in-sensitive queries return the same thing
     */
    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testUniqueFind() {
        SharedCollection test = new SharedCollection();
        test.setName("test");
        test.markUpdated(getAdminUser());
        genericService.saveOrUpdate(test);

        SharedCollection c1 = new SharedCollection();
        c1.setName(" TEST ");
        boolean isAdmin = authenticationAndAuthorizationService.can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, getAdminUser());
        SharedCollection withName = resourceCollectionDao.findCollectionWithName(getAdminUser(), true, c1.getName(), SharedCollection.class);
        assertEquals(withName, test);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testFindInSaveForResource() {
        Image image = new Image();
        image.setStatus(Status.ACTIVE);
        image.setTitle("test");
        image.setDescription("test");
        image.setDate(2014);
        image.markUpdated(getBasicUser());
        genericService.saveOrUpdate(image);

        SharedCollection test = new SharedCollection();
        test.setName(TEST_TITLE);
        test.markUpdated(getAdminUser());
        genericService.saveOrUpdate(test);
        genericService.synchronize();
        List<SharedCollection> list = new ArrayList<>();
        SharedCollection trns = new SharedCollection();
        trns.setName(TEST_TITLE);
        trns.setId(-1L);
        list.add(trns);
        resourceCollectionService.saveResourceCollections(image, list, image.getSharedCollections(), getBasicUser(), true,
                ErrorHandling.VALIDATE_SKIP_ERRORS, SharedCollection.class);
        logger.debug("collections: {}", image.getSharedCollections());

        List<Long> extractIds = PersistableUtils.extractIds(image.getSharedResourceCollections());
        assertFalse(extractIds.contains(test.getId()));
        image.getSharedCollections().clear();
        resourceCollectionService.saveResourceCollections(image, list, image.getSharedCollections(), getEditorUser(), true,
                ErrorHandling.VALIDATE_SKIP_ERRORS, SharedCollection.class);
        logger.debug("collections: {}", image.getSharedCollections());
        extractIds = PersistableUtils.extractIds(image.getSharedResourceCollections());
        logger.debug("{} -> {}", test.getId(), extractIds);
        assertTrue(extractIds.contains(test.getId()));
    }

    @Test
    @Rollback
    public void testFindShareWithRights() {
        SharedCollection test = new SharedCollection();
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getBillingUser(), GeneralPermissions.ADMINISTER_SHARE));
        test.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(test);

        SharedCollection c1 = new SharedCollection();
        c1.setName(" TEST ");
        SharedCollection withName = resourceCollectionDao.findCollectionWithName(getBillingUser(), false, c1.getName(), SharedCollection.class);
        assertEquals(withName, test);

        withName = resourceCollectionDao.findCollectionWithName(getBasicUser(), false, c1.getName(), SharedCollection.class);
        assertNotEquals(withName, test);
    }

    @Test
    @Rollback
    public void testFindCollectionWithRights() {
        ListCollection test = new ListCollection();
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP));
        genericService.saveOrUpdate(test);

        ListCollection c1 = new ListCollection();
        c1.setName(" TEST ");
        ListCollection withName = resourceCollectionDao.findCollectionWithName(getBasicUser(), false, c1.getName(), ListCollection.class);
        assertEquals(withName, test);
    }

    @Test
    @Rollback(true)
    public void testDashboardQueries() {
        SharedCollection parent = new SharedCollection("parent", "parent", false, getBasicUser());
        SharedCollection parent2 = new SharedCollection("parent2", "parent", false, getAdminUser());
        SharedCollection parent3 = new SharedCollection("parent3", "parent", false, getAdminUser());
        SharedCollection child1 = new SharedCollection("child", "child", false, getBasicUser());
        SharedCollection child11 = new SharedCollection("child1", "child1", false, getBasicUser());
        SharedCollection access = new SharedCollection("access", "access", false, getAdminUser());
        SharedCollection child2 = new SharedCollection("child2", "child2", false, getBasicUser());
        SharedCollection child22 = new SharedCollection("child22", "child2", false, getBasicUser());
        ListCollection list = new ListCollection("child22", "child2", SortOption.TITLE, false, getBasicUser());

        parent.markUpdated(getAdminUser());
        parent2.markUpdated(getAdminUser());
        parent3.markUpdated(getAdminUser());
        child1.markUpdated(getAdminUser());
        child11.markUpdated(getAdminUser());
        access.markUpdated(getAdminUser());
        child2.markUpdated(getAdminUser());
        child22.markUpdated(getAdminUser());
        list.markUpdated(getAdminUser());
        genericService.saveOrUpdate(parent, child1, child2, child11, child22, parent2, parent3, list, access);
        access.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child1, parent, SharedCollection.class);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child2, parent, SharedCollection.class);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child11, child1, SharedCollection.class);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child22, parent2, SharedCollection.class);
//        genericService.saveOrUpdate(parent, child1, child2, child11, child22, parent2, parent3, access);
        genericService.synchronize();
        List<SharedCollection> parentCollections = resourceCollectionService.findParentOwnerCollections(getBasicUser(), SharedCollection.class);
        logger.debug("parents:{}", parentCollections);
        assertFalse("should not contain admin owner collection", parentCollections.contains(parent3));
        assertFalse("should not contain hidden collection", parentCollections.contains(parent2));
        assertFalse("should not contain admin owned collection", parentCollections.contains(access));
        assertTrue("should contain visbile child", parentCollections.contains(child2));
        assertTrue("should contain normal child", parentCollections.contains(child1));
        assertTrue("should contain visbile child ofhidden parent", parentCollections.contains(child22));
        assertFalse(parentCollections.contains(list));
        getLogger().trace("accessible collections");
        List<ResourceCollection> accessibleResourceCollections = entityService.findAccessibleResourceCollections(getBasicUser());
        logger.debug("accessible:{}", accessibleResourceCollections);
        assertTrue("should contain accesible collection", accessibleResourceCollections.contains(access));
        
        List<Long> collectionIds = PersistableUtils.extractIds(parentCollections);
        collectionIds.addAll(PersistableUtils.extractIds(accessibleResourceCollections));
        resourceCollectionService.reconcileCollectionTree(parentCollections, getBasicUser(), collectionIds, SharedCollection.class);
        parentCollections.forEach(c -> {
            logger.debug("{}", c);
            logger.debug(" {}", c.getTransientChildren());
        });
        assertTrue("parent has child1", parent.getTransientChildren().contains(child1));
        assertTrue("parent has child2", parent.getTransientChildren().contains(child2));
        assertTrue("child1 has subchild", child1.getTransientChildren().contains(child11));
        assertFalse("child2 has subchild", child2.getTransientChildren().size() > 0);
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testDraftResourceIssue() throws Exception {
        String email = "a243@basda.com";
        entityService.delete(entityService.findByEmail(email));

        final TdarUser testPerson = createAndSaveNewPerson(email, "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource normal = generateDocumentWithUser();
        InformationResource draft = generateDocumentWithUser();
        final Long normalId = normal.getId();
        final Long draftId = draft.getId();
        draft.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(draft);
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_SHARE),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        SharedCollection collection = new SharedCollection(name, description, false, getBasicUser());
        collection.markUpdated(getBasicUser());
        resourceCollectionService.saveCollectionForController(collection, null, null, getBasicUser(), users, PersistableUtils.extractIds(resources), null, true,
                null, SharedCollection.class,-1L);
        genericService.synchronize();

        final Long id = collection.getId();
        String slug = collection.getSlug();
        collection = null;
        collection = genericService.find(SharedCollection.class, id);
        List<AuthorizedUser> aus = new ArrayList<>(users);
        aus.add(new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD));
        resourceCollectionService.saveCollectionForController(collection, null, null, getBasicUser(), aus, null, null, true, null, SharedCollection.class, -1l);
        genericService.synchronize();
        logger.debug("au: {}", collection.getAuthorizedUsers());
        logger.debug("no: {}", normal.getSharedCollections());
        logger.debug("df: {}", draft.getSharedCollections());
        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, normal, GeneralPermissions.MODIFY_METADATA));
        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, draft, GeneralPermissions.MODIFY_METADATA));
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), draft, GeneralPermissions.MODIFY_METADATA));
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), normal, GeneralPermissions.MODIFY_METADATA));

        assertTrue(authenticationAndAuthorizationService.canViewResource(getBasicUser(), draft));
        assertTrue(authenticationAndAuthorizationService.canViewResource(getBasicUser(), normal));

    }

}

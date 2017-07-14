package org.tdar.core.bean;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.CollectionSaveObject;
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
        SharedCollection collection = new SharedCollection("test", "test", getAdminUser());
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
        SharedCollection collection = new SharedCollection("test", "test", getAdminUser());
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
        test.setName("test123");
        test.markUpdated(getAdminUser());
        genericService.saveOrUpdate(test);

        SharedCollection c1 = new SharedCollection();
        c1.setName(" TEST123 ");
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
    @Rollback(true)
    public void testFindShareWithRights() {
        SharedCollection test = new SharedCollection();
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.ADMINISTER_SHARE));
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(test);
        genericService.saveOrUpdate(test.getAuthorizedUsers());
        SharedCollection c1 = new SharedCollection();
        c1.setName(" TEST ");
        genericService.synchronize();
        SharedCollection withName = resourceCollectionDao.findCollectionWithName(getBillingUser(), false, c1.getName(), SharedCollection.class);
        assertEquals(test, withName);

        withName = resourceCollectionDao.findCollectionWithName(getBasicUser(), false, c1.getName(), SharedCollection.class);
        assertNotEquals(withName, test);
    }

    @Test
    @Rollback(true)
    public void testFindDeletedShareWithRights() {
        SharedCollection test = new SharedCollection();
        test.setStatus(Status.DELETED);
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getAdminUser(), GeneralPermissions.ADMINISTER_SHARE));
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.ADMINISTER_SHARE));
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(test);
        genericService.saveOrUpdate(test.getAuthorizedUsers());
        genericService.synchronize();
        SharedCollection withName = resourceCollectionDao.findCollectionWithName(getBillingUser(), false, "test", SharedCollection.class);
        assertNull("should be null for deleted collection",withName);
        List<SharedCollection> findCollectionsSharedWith = resourceCollectionDao.findCollectionsSharedWith(getAdminUser(), getBillingUser(), SharedCollection.class, GeneralPermissions.MODIFY_METADATA, false);
        assertTrue("result should be empty", CollectionUtils.isEmpty(findCollectionsSharedWith));
    }

    @Test
    @Rollback
    public void testFindCollectionWithRights() {
        ListCollection test = new ListCollection();
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_GROUP));
        genericService.saveOrUpdate(test);

        ListCollection c1 = new ListCollection();
        c1.setName(" TEST ");
        ListCollection withName = resourceCollectionDao.findCollectionWithName(getBasicUser(), false, c1.getName(), ListCollection.class);
        assertEquals(withName, test);
    }

    
    @Test
    @Rollback
    public void testFindSharedResources() {
        Dataset dataset = createAndSaveNewDataset();
        dataset.getAuthorizedUsers().add(new AuthorizedUser(getBillingUser(), getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(dataset);
        SharedCollection collection = createAndSaveNewResourceCollection("test collection");
        collection.getAuthorizedUsers().clear();
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBillingUser(), getBillingUser(), GeneralPermissions.MODIFY_RECORD));
        collection.getResources().add(dataset);
        genericService.saveOrUpdate(collection);
        dataset.getSharedCollections().add(collection);
        genericService.saveOrUpdate(dataset);
        List<SharedCollection> list = resourceCollectionService.findCollectionsSharedWith(getBillingUser(), getBasicUser(), SharedCollection.class);
        List<Resource> resources = resourceCollectionService.findResourcesSharedWith(getBillingUser(), getBasicUser());
        logger.debug("c:{}", list);
        logger.debug("r:{}", resources);
        assertTrue("should not have shared any collections with user", CollectionUtils.isEmpty(list));
        assertTrue("should have at least one resource", CollectionUtils.isNotEmpty(resources));
    }
    
    
    @Test
    @Rollback(true)
    public void testDashboardQueries() {
        SharedCollection parent = new SharedCollection("parent", "parent", getBasicUser());
        process(parent);
        SharedCollection parent2 = new SharedCollection("parent2", "parent", getAdminUser());
        process(parent2);
        SharedCollection parent3 = new SharedCollection("parent3", "parent", getAdminUser());
        process(parent3);
        SharedCollection child1 = new SharedCollection("child", "child", getBasicUser());
        process(child1);
        SharedCollection child11 = new SharedCollection("child1", "child1", getBasicUser());
        process(child11);
        SharedCollection access = new SharedCollection("access", "access", getAdminUser());
        process(access);
        SharedCollection child2 = new SharedCollection("child2", "child2",  getBasicUser());
        process(child2);
        SharedCollection child22 = new SharedCollection("child22", "child2", getBasicUser());
        process(child22);
        ListCollection list = new ListCollection("child22", "child2", SortOption.TITLE, false, getBasicUser());
        process(list);

        genericService.saveOrUpdate(parent, child1, child2, child11, child22, parent2, parent3, list, access);
        access.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.MODIFY_RECORD));
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

    private void process(ResourceCollection list) {
        list.markUpdated(getAdminUser());
        if (list instanceof SharedCollection) {
            list.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),list.getOwner(),GeneralPermissions.ADMINISTER_SHARE));
        } else {
            list.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),list.getOwner(),GeneralPermissions.ADMINISTER_GROUP));
        }
        genericService.saveOrUpdate(list);
        genericService.saveOrUpdate(list.getAuthorizedUsers());
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    /**
     * Check that draft and normal rights can be applied properly
     * @throws Exception
     */
    public void testDraftResourceIssue() throws Exception {
        String email = System.currentTimeMillis() + "a243@basda.com";
        entityService.delete(entityService.findByEmail(email));

        // create a person
        final TdarUser testPerson = createAndSaveNewPerson(email, "1234");
        String name = "test collection";
        String description = "test description";

        // create a normal and draft resource giving basic users permissions
        InformationResource normal = generateDocumentWithUser();
        InformationResource draft = generateDocumentWithUser();
        normal.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(),getBasicUser(),GeneralPermissions.MODIFY_RECORD));
        draft.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(),getBasicUser(),GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(normal);
        genericService.saveOrUpdate(draft);
        final Long normalId = normal.getId();
        final Long draftId = draft.getId();
        draft.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(draft);
        
        // create a shared collection and add basic user, admin user as authorized users, owner is basic user
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        SharedCollection collection = new SharedCollection(name, description, getBasicUser());
        collection.markUpdated(getBasicUser());
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE));
        genericService.saveOrUpdate(collection);
        genericService.synchronize();
        CollectionSaveObject<SharedCollection> cso = new CollectionSaveObject<SharedCollection>(collection, getBasicUser(), -1L, SharedCollection.class);
        cso.setToAdd(PersistableUtils.extractIds(resources));
        resourceCollectionService.saveCollectionForController(cso);
        
        // do it again, just to make a no-op
        cso = new CollectionSaveObject<SharedCollection>(collection, getBasicUser(), -1L, SharedCollection.class);
        resourceCollectionService.saveCollectionForController(cso);
        final Long id = collection.getId();
        collection = null;

        genericService.synchronize();

//        setVerifyTransactionCallback(new TransactionCallback<SharedCollection>() {
//
//            @Override
//            public SharedCollection doInTransaction(TransactionStatus arg0) {
                List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE),
                        new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
                
                SharedCollection myCollection = genericService.find(SharedCollection.class, id);
                List<UserRightsProxy> aus = new ArrayList<>();
                aus.add(new UserRightsProxy(new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.MODIFY_RECORD)));
                for (AuthorizedUser user : users) {
                    aus.add(new UserRightsProxy(user));
                }
                
                myCollection.getAuthorizedUsers().forEach(au -> {
                    logger.debug(" au: {}", au);
                });

                resourceCollectionService.saveCollectionForRightsController(myCollection, getBasicUser(), aus, SharedCollection.class, -1L);
                genericService.synchronize();
                logger.debug("au: {}", myCollection.getAuthorizedUsers());
                logger.debug("no: {}", normal.getSharedCollections());
                logger.debug("df: {}", draft.getSharedCollections());
                assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, normal, GeneralPermissions.MODIFY_METADATA));
                assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, draft, GeneralPermissions.MODIFY_METADATA));
                assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), draft, GeneralPermissions.MODIFY_METADATA));
                assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), normal, GeneralPermissions.MODIFY_METADATA));

                assertTrue(authenticationAndAuthorizationService.canViewResource(getBasicUser(), draft));
                assertTrue(authenticationAndAuthorizationService.canViewResource(getBasicUser(), normal));
                
//                return null;
//            }
//         });

    }

    @Test
    @Rollback(true)
    //make a collection w/ three authusers, and confirm those users found via findUsersSharedWith()
    public void testFindUsersSharedWith() {
        final String collectionName = "the best collection ever";
        List<TdarUser> users = new ArrayList<>(Arrays.asList(getBasicUser(), getEditorUser(), getBillingUser(), getAdminUser()));

        SharedCollection collection = createAndSaveNewResourceCollection(collectionName, SharedCollection.class);
        users.remove(collection.getOwner());

        // sanity checks
//        assertThat("collection should have no authusers", collection.getAuthorizedUsers(), is( empty()));
        assertThat("test requires at least one user that is not the same as the current user", users, not( empty()));

        // now add some authusers
        collection.getAuthorizedUsers().addAll(
                users.stream().map(user -> new AuthorizedUser(getAdminUser(), user, GeneralPermissions.MODIFY_RECORD)).collect(Collectors.toList()));

        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(collection.getAuthorizedUsers());
        genericService.synchronize();
        List<TdarUser> grantees = resourceCollectionDao.findUsersSharedWith(collection.getOwner());
        for (TdarUser grantee : users) {
            assertTrue(String.format("grantees should contain: %s", grantee),grantees.contains(grantee));
        }
    }
}

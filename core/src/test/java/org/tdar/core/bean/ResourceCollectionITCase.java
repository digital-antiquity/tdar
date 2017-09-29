package org.tdar.core.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
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
import org.tdar.core.service.resource.ErrorHandling;
import org.tdar.utils.PersistableUtils;

public class ResourceCollectionITCase extends AbstractIntegrationTestCase {

    @Autowired
    ResourceCollectionDao resourceCollectionDao;


    private static final String TEST_TITLE = "Brookville Reservoir Survey 1991-1992";

    @Test
    @Rollback(true)
    public void testSetupCorrect() {
        ResourceCollection collection = resourceCollectionService.find(1575l);
        assertFalse(( collection).isHidden());
    }

    @Test
    @Rollback(true)
    public void testSparseResource() throws Exception {
        ResourceCollection collection = new ResourceCollection("test", "test", getAdminUser());
        collection.markUpdated(getAdminUser());
        collection.setManagedResources(new HashSet<>(genericService.findRandom(Resource.class, 20)));
        genericService.saveOrUpdate(collection);
        Long collectionId = collection.getId();
        collection = null;

        collection = genericService.findAll(ResourceCollection.class, Arrays.asList(collectionId)).get(0);

        for (Resource resource : collection.getManagedResources()) {
            logger.info("{} {} ", resource, resource.getSubmitter());
        }

    }

    @Test
    @Rollback
    public void testMakeActive() throws Exception {
        ResourceCollection collection = new ResourceCollection("test", "test", getAdminUser());
        collection.markUpdated(getAdminUser());
        boolean seen = false;
        genericService.saveOrUpdate(collection);
        for (Resource r : genericService.findRandom(Resource.class, 20)) {
            r.setStatus(Status.ACTIVE);
            if (seen == false) {
                r.setStatus(Status.DRAFT);
            }
            r.getManagedResourceCollections().add(collection);
            genericService.saveOrUpdate(r);
            collection.getManagedResources().add(r);
        }
        Long collectionId = collection.getId();
        collection = null;
        collection = genericService.find(ResourceCollection.class, collectionId);
        resourceCollectionService.makeResourcesInCollectionActive(collection, getAdminUser());
        for (Resource r : collection.getManagedResources()) {
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
        ResourceCollection test = new ResourceCollection();
        test.setName("test123");
        test.markUpdated(getAdminUser());
        genericService.saveOrUpdate(test);

        ResourceCollection c1 = new ResourceCollection();
        c1.setName(" TEST123 ");
        boolean isAdmin = authenticationAndAuthorizationService.can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, getAdminUser());
        ResourceCollection withName = resourceCollectionDao.findCollectionWithName(getAdminUser(), true, c1.getName());
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

        ResourceCollection test = new ResourceCollection();
        test.setName(TEST_TITLE);
        test.markUpdated(getAdminUser());
        genericService.saveOrUpdate(test);
        genericService.synchronize();
        List<ResourceCollection> list = new ArrayList<>();
        ResourceCollection trns = new ResourceCollection();
        trns.setName(TEST_TITLE);
        trns.setId(-1L);
        list.add(trns);
        resourceCollectionService.saveResourceCollections(image, list, image.getManagedResourceCollections(), getBasicUser(), true,
                ErrorHandling.VALIDATE_SKIP_ERRORS, CollectionResourceSection.MANAGED);
        logger.debug("collections: {}", image.getManagedResourceCollections());

        List<Long> extractIds = PersistableUtils.extractIds(image.getManagedResourceCollections());
        assertFalse(extractIds.contains(test.getId()));
        image.getManagedResourceCollections().clear();
        resourceCollectionService.saveResourceCollections(image, list, image.getManagedResourceCollections(), getEditorUser(), true,
                ErrorHandling.VALIDATE_SKIP_ERRORS, CollectionResourceSection.MANAGED);
        logger.debug("collections: {}", image.getManagedResourceCollections());
        extractIds = PersistableUtils.extractIds(image.getManagedResourceCollections());
        logger.debug("{} -> {}", test.getId(), extractIds);
        assertTrue(extractIds.contains(test.getId()));
    }

    @Test
    @Rollback(true)
    public void testFindShareWithRights() {
        ResourceCollection test = new ResourceCollection();
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.ADMINISTER_SHARE));
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(test);
        genericService.saveOrUpdate(test.getAuthorizedUsers());
        ResourceCollection c1 = new ResourceCollection();
        c1.setName(" TEST ");
        genericService.synchronize();
        ResourceCollection withName = resourceCollectionDao.findCollectionWithName(getBillingUser(), false, c1.getName());
        assertEquals(test, withName);

        withName = resourceCollectionDao.findCollectionWithName(getBasicUser(), false, c1.getName());
        assertNotEquals(withName, test);
    }

    @Test
    @Rollback(true)
    public void testFindDeletedShareWithRights() {
        ResourceCollection test = new ResourceCollection();
        test.setStatus(Status.DELETED);
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getAdminUser(), GeneralPermissions.ADMINISTER_SHARE));
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.ADMINISTER_SHARE));
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(test);
        genericService.saveOrUpdate(test.getAuthorizedUsers());
        genericService.synchronize();
        ResourceCollection withName = resourceCollectionDao.findCollectionWithName(getBillingUser(), false, "test");
        assertNull("should be null for deleted collection",withName);
        List<ResourceCollection> findCollectionsSharedWith = resourceCollectionDao.findCollectionsSharedWith(getAdminUser(), getBillingUser(), GeneralPermissions.MODIFY_METADATA, false);
        assertTrue("result should be empty", CollectionUtils.isEmpty(findCollectionsSharedWith));
    }

    @Test
    @Rollback
    public void testFindCollectionWithRights() {
        ResourceCollection test = new ResourceCollection();
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE));
        genericService.saveOrUpdate(test);

        ResourceCollection c1 = new ResourceCollection();
        c1.setName(" TEST ");
        ResourceCollection withName = resourceCollectionDao.findCollectionWithName(getBasicUser(), false, c1.getName());
        assertEquals(withName, test);
    }

    
    @Test
    @Rollback
    public void testFindSharedResources() {
        TdarUser user = createAndSaveNewPerson("rightsUser@asba.asd", "rights-user");
        Dataset dataset = createAndSaveNewDataset();
        dataset.getAuthorizedUsers().add(new AuthorizedUser(getBillingUser(), user, GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(dataset);
        ResourceCollection collection = createAndSaveNewResourceCollection("test collection");
        collection.getAuthorizedUsers().clear();
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBillingUser(), getBillingUser(), GeneralPermissions.MODIFY_RECORD));
        collection.getManagedResources().add(dataset);
        genericService.saveOrUpdate(collection);
        dataset.getManagedResourceCollections().add(collection);
        genericService.saveOrUpdate(dataset);
        genericService.synchronize();
        List<ResourceCollection> list = resourceCollectionService.findCollectionsSharedWith(getBillingUser(), user);
        List<Resource> resources = resourceCollectionService.findResourcesSharedWith(getBillingUser(), user);
//        logger.debug("c:{}", list);
        list.forEach(c -> {
            logger.debug("c: {}", c);
            logger.debug("  au: {}", c.getAuthorizedUsers());
        });
        logger.debug("r:{}", resources);
        assertTrue("should not have shared any collections with user", CollectionUtils.isEmpty(list));
        assertTrue("should have at least one resource", CollectionUtils.isNotEmpty(resources));
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBillingUser(), user, GeneralPermissions.MODIFY_RECORD));
        list = resourceCollectionService.findCollectionsSharedWith(getBillingUser(), user);
        assertTrue("should have 1 shared any collections with user", CollectionUtils.isNotEmpty(list));
    }
    
    
    @Test
    @Rollback(true)
    public void testDashboardQueries() {
        ResourceCollection parent = new ResourceCollection("parent", "parent", getBasicUser());
        process(parent);
        ResourceCollection parent2 = new ResourceCollection("parent2", "parent", getAdminUser());
        process(parent2);
        ResourceCollection parent3 = new ResourceCollection("parent3", "parent", getAdminUser());
        process(parent3);
        ResourceCollection child1 = new ResourceCollection("child", "child", getBasicUser());
        process(child1);
        ResourceCollection child11 = new ResourceCollection("child1", "child1", getBasicUser());
        process(child11);
        ResourceCollection access = new ResourceCollection("access", "access", getAdminUser());
        process(access);
        ResourceCollection child2 = new ResourceCollection("child2", "child2",  getBasicUser());
        process(child2);
        ResourceCollection child22 = new ResourceCollection("child22", "child2", getBasicUser());
        process(child22);

        genericService.saveOrUpdate(parent, child1, child2, child11, child22, parent2, parent3, access);
        access.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child1, parent);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child2, parent);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child11, child1);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child22, parent2);
//        genericService.saveOrUpdate(parent, child1, child2, child11, child22, parent2, parent3, access);
        genericService.synchronize();
        List<ResourceCollection> parentCollections = resourceCollectionService.findParentOwnerCollections(getBasicUser());
        logger.debug("parents:{}", parentCollections);
        assertFalse("should not contain admin owner collection", parentCollections.contains(parent3));
        assertFalse("should not contain hidden collection", parentCollections.contains(parent2));
        assertFalse("should not contain admin owned collection", parentCollections.contains(access));
        assertTrue("should contain visbile child", parentCollections.contains(child2));
        assertTrue("should contain normal child", parentCollections.contains(child1));
        assertTrue("should contain visbile child ofhidden parent", parentCollections.contains(child22));
        getLogger().trace("accessible collections");
        List<ResourceCollection> accessibleResourceCollections = entityService.findAccessibleResourceCollections(getBasicUser());
        logger.debug("accessible:{}", accessibleResourceCollections);
        assertTrue("should contain accesible collection", accessibleResourceCollections.contains(access));
        
        List<Long> collectionIds = PersistableUtils.extractIds(parentCollections);
        collectionIds.addAll(PersistableUtils.extractIds(accessibleResourceCollections));
        resourceCollectionService.reconcileCollectionTree(parentCollections, getBasicUser(), collectionIds);
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
        list.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),list.getOwner(),GeneralPermissions.ADMINISTER_SHARE));
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
        ResourceCollection collection = new ResourceCollection(name, description, getBasicUser());
        collection.markUpdated(getBasicUser());
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE));
        genericService.saveOrUpdate(collection);
        genericService.synchronize();
        CollectionSaveObject cso = new CollectionSaveObject(collection, getBasicUser(), -1L);
        cso.setToAdd(PersistableUtils.extractIds(resources));
        resourceCollectionService.saveCollectionForController(cso);
        
        // do it again, just to make a no-op
        cso = new CollectionSaveObject(collection, getBasicUser(), -1L);
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
                
                ResourceCollection myCollection = genericService.find(ResourceCollection.class, id);
                List<UserRightsProxy> aus = new ArrayList<>();
                aus.add(new UserRightsProxy(new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.MODIFY_RECORD)));
                for (AuthorizedUser user : users) {
                    aus.add(new UserRightsProxy(user));
                }
                
                myCollection.getAuthorizedUsers().forEach(au -> {
                    logger.debug(" au: {}", au);
                });

                resourceCollectionService.saveCollectionForRightsController(myCollection, getBasicUser(), aus, -1L);
                genericService.synchronize();
                logger.debug("au: {}", myCollection.getAuthorizedUsers());
                logger.debug("no: {}", normal.getManagedResourceCollections());
                logger.debug("df: {}", draft.getManagedResourceCollections());
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
    @Rollback
    /**
     * make sure that the collection tree has all children alternate and not including grandchildren in alternate tree
     */
    public void testAlternateChildrenInTree() {
        TdarUser user = getBasicUser();
        // setup beans
        ResourceCollection parent = createAndSaveNewResourceCollection("parent");
        ResourceCollection alternate = createAndSaveNewResourceCollection("alternate");
        ResourceCollection child = createAndSaveNewResourceCollection("child");
        ResourceCollection second = createAndSaveNewResourceCollection("second child");
        ResourceCollection grantChild = createAndSaveNewResourceCollection("actual");
        genericService.saveOrUpdate(grantChild);
        genericService.synchronize();
        // set alternate parent
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), grantChild, child);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), second,alternate);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child, parent);
        resourceCollectionService.updateAlternateCollectionParentTo(getAdminUser(), child, alternate);
        genericService.saveOrUpdate(grantChild);
        genericService.saveOrUpdate(alternate);
        genericService.saveOrUpdate(child);
        genericService.saveOrUpdate(second);
        logger.debug("alternate: {}", alternate);
        logger.debug("   parent: {}", parent);
        logger.debug("   second: {}", second);
        logger.debug("    child: {} ap: {} p: {}", child, child.getAlternateParentId(), child.getParentId());
        Long childId = child.getId();
        Long parentId = parent.getId();
        Long altenrateId = alternate.getId();
        Long grandchildId = grantChild.getId();
        Long secondChildId = second.getId();
        child = null;
        parent = null;
        alternate = null;
        second = null;
        genericService.synchronize();
        alternate = genericService.find(ResourceCollection.class, altenrateId);
        resourceCollectionService.buildCollectionTreeForController(alternate, user);
        logger.debug(" children: {}", PersistableUtils.extractIds(alternate.getTransientChildren()));
        for (ResourceCollection _child : alternate.getTransientChildren()) {
            logger.debug("    _child: {}", _child);
            if (childId == _child.getId()) {
                child = _child;
            }
            logger.debug(" _children: {}", PersistableUtils.extractIds(_child.getTransientChildren()));
        };

        assertEquals("should have two children", 2, alternate.getTransientChildren().size());
        assertTrue("child is still in transient children", alternate.getTransientChildren().contains(child));
        assertTrue("child has grandchild", PersistableUtils.extractIds(child.getTransientChildren()).contains(grandchildId));
        
        
    }
    
}

package org.tdar.core.bean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.WhiteLabelCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.utils.PersistableUtils;

import com.hp.hpl.jena.sparql.pfunction.library.alt;

public class ResourceCollectionITCase extends AbstractIntegrationTestCase {

    @Autowired
    ResourceCollectionDao resourceCollectionDao;


    private static final String TEST_TITLE = "Brookville Reservoir Survey 1991-1992";


    @Test
    @Rollback
    public void testSetupCorrect() {
        ResourceCollection collection = resourceCollectionService.find(1575l);
        assertFalse(collection.isHidden());
    }

    @Test
    @Rollback
    public void testSparseResource() throws Exception {
        ResourceCollection collection = new ResourceCollection("test", "test", SortOption.TITLE, CollectionType.SHARED, true, getAdminUser());
        collection.markUpdated(getAdminUser());
        collection.setResources(new HashSet<>(genericService.findRandom(Resource.class, 20)));
        genericService.saveOrUpdate(collection);
        Long collectionId = collection.getId();
        collection = null;
        collection = genericService.find(ResourceCollection.class, collectionId);
        for (Resource resource : collection.getResources()) {
            logger.info("{} {} ", resource, resource.getSubmitter());
        }

    }

    
    @Test
    @Rollback
    public void testMakeActive() throws Exception {
        ResourceCollection collection = new ResourceCollection("test", "test", SortOption.TITLE, CollectionType.SHARED, true, getAdminUser());
        collection.markUpdated(getAdminUser());
        boolean seen = false;
        genericService.saveOrUpdate(collection);
        for (Resource r  : genericService.findRandom(Resource.class, 20)) {
            r.setStatus(Status.ACTIVE);
            if (seen == false) {
                r.setStatus(Status.DRAFT);
            }
            r.getResourceCollections().add(collection);
            genericService.saveOrUpdate(r);
            collection.getResources().add(r);
        }
        Long collectionId = collection.getId();
        collection = null;
        collection = genericService.find(ResourceCollection.class, collectionId);
        resourceCollectionService.makeResourcesInCollectionActive(collection, getAdminUser());
        for (Resource r : collection.getResources()) {
            assertEquals(Status.ACTIVE,r.getStatus());
        }
    }

    
    /**
     * Make sure that case in-sensitive queries return the same thing
     */
    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testUniqueFind() {
        ResourceCollection test = new ResourceCollection(CollectionType.SHARED);
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.setSortBy(SortOption.COLLECTION_TITLE);
        genericService.saveOrUpdate(test);

        ResourceCollection c1 = new ResourceCollection(CollectionType.SHARED);
        c1.setName(" TEST ");
        boolean isAdmin = authenticationAndAuthorizationService.can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, getAdminUser());
        ResourceCollection withName = resourceCollectionDao.findCollectionWithName(getAdminUser(), true, c1);
        assertEquals(withName, test);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testFindInSaveForResource() {
        Image image = new Image();
        image.setStatus(Status.ACTIVE);
        image.setTitle("test");
        image.setDescription("test");
        image.setDate(2014);
        image.markUpdated(getBasicUser());
        genericService.saveOrUpdate(image);

        ResourceCollection test = new ResourceCollection(CollectionType.SHARED);
        test.setName(TEST_TITLE);
        test.markUpdated(getAdminUser());
        test.setSortBy(SortOption.COLLECTION_TITLE);
        genericService.saveOrUpdate(test);
        genericService.synchronize();
        List<ResourceCollection> list = new ArrayList<>();
        ResourceCollection trns = new ResourceCollection();
        trns.setName(TEST_TITLE);
        trns.setId(-1L);
        list.add(trns);
        resourceCollectionService.saveSharedResourceCollections(image, list, image.getResourceCollections(), getBasicUser(), true,
                ErrorHandling.VALIDATE_SKIP_ERRORS);
        logger.debug("collections: {}", image.getResourceCollections());

        List<Long> extractIds = PersistableUtils.extractIds(image.getSharedResourceCollections());
        assertFalse(extractIds.contains(test.getId()));
        image.getResourceCollections().clear();
        resourceCollectionService.saveSharedResourceCollections(image, list, image.getResourceCollections(), getEditorUser(), true,
                ErrorHandling.VALIDATE_SKIP_ERRORS);
        logger.debug("collections: {}", image.getResourceCollections());
        extractIds = PersistableUtils.extractIds(image.getSharedResourceCollections());
        assertTrue(extractIds.contains(test.getId()));
    }

    @Test
    @Rollback
    public void testFindWithRights() {
        ResourceCollection test = new ResourceCollection(CollectionType.SHARED);
        test.setName("test");
        test.markUpdated(getAdminUser());
        test.getAuthorizedUsers().add(new AuthorizedUser(getBillingUser(), GeneralPermissions.ADMINISTER_GROUP));
        test.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        test.setSortBy(SortOption.COLLECTION_TITLE);
        genericService.saveOrUpdate(test);

        ResourceCollection c1 = new ResourceCollection(CollectionType.SHARED);
        c1.setName(" TEST ");
        ResourceCollection withName = resourceCollectionDao.findCollectionWithName(getBillingUser(), false, c1);
        assertEquals(withName, test);

        withName = resourceCollectionDao.findCollectionWithName(getBasicUser(), false, c1);
        assertNotEquals(withName, test);
    }

    @Test
    @Rollback
    public void testConvertToWhitelabelCollection() {
        ResourceCollection resourceCollection = createAndSaveNewResourceCollection("normal collection");
        WhiteLabelCollection whitelabelCollection = resourceCollectionDao.convertToWhitelabelCollection(resourceCollection);

        assertThat(whitelabelCollection, is( not( nullValue())));
        assertThat(resourceCollection.getId(), is(whitelabelCollection.getId()));
        assertThat(resourceCollection.getTitle(), is(whitelabelCollection.getTitle()));
    }

    @Test
    @Rollback
    public void testConvertToResourceCollection() {
        WhiteLabelCollection wlc = createAndSaveNewWhiteLabelCollection("fancy collection");
        ResourceCollection rc = resourceCollectionDao.convertToResourceCollection(wlc);

        assertThat(rc, is( not( nullValue())));
        assertThat(rc, hasProperty("title", is("fancy collection")));
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
        resourceCollectionService.buildCollectionTreeForController(alternate, user, CollectionType.SHARED);
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

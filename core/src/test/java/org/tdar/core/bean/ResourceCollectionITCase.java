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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.WhiteLabelCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
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
        collection = genericService.findAll(ResourceCollection.class, Arrays.asList(collectionId)).get(0);
        for (Resource resource : collection.getResources()) {
            logger.info("{} {} ", resource, resource.getSubmitter());
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

}

package org.tdar.core.service;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.service.collection.ResourceCollectionService;

@Ignore
public class SharingITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    
    @Test
    @Rollback(true)
    public void testResource() {
        Dataset dataset = createAndSaveNewDataset();
        dataset.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(dataset);
        genericService.synchronize();
        List<TdarUser> findUsersSharedWith = resourceCollectionService.findUsersSharedWith(getUser());
        logger.debug("{}", findUsersSharedWith);
        assertTrue("should contain billing user", CollectionUtils.containsAll(findUsersSharedWith, Arrays.asList(getBillingUser())));
    }

    
    @Test
    @Rollback(true)
    public void testResourceInCollection() {
        Dataset dataset = createAndSaveNewDataset();
        SharedCollection collection = createAndSaveNewResourceCollection("test collection");
        collection.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(collection);
        collection.getResources().add(dataset);
        dataset.getSharedCollections().add(collection);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(dataset);
        genericService.synchronize();
        List<TdarUser> findUsersSharedWith = resourceCollectionService.findUsersSharedWith(getUser());
        logger.debug("{}", findUsersSharedWith);
        assertTrue("should contain billing user", CollectionUtils.containsAll(findUsersSharedWith, Arrays.asList(getBillingUser())));
    }
    
    @Test
    @Rollback(true)
    public void testResourceInNestedCollection() {
        Dataset dataset = createAndSaveNewDataset();
        SharedCollection collection = createAndSaveNewResourceCollection("test collection");
        SharedCollection child = createAndSaveNewResourceCollection("test collection");
        collection.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(collection);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child, collection, SharedCollection.class);
        child.getResources().add(dataset);
        dataset.getSharedCollections().add(child);
        genericService.saveOrUpdate(child);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(dataset);
        genericService.synchronize();
        List<TdarUser> findUsersSharedWith = resourceCollectionService.findUsersSharedWith(getUser());
        logger.debug("{}", findUsersSharedWith);
        assertTrue("should contain billing user", CollectionUtils.containsAll(findUsersSharedWith, Arrays.asList(getBillingUser())));
    }

    @Test
    @Rollback(false)
    public void testNewUser() {
        TdarUser user = createAndSaveNewUser();
        genericService.synchronize();
        List<TdarUser> findUsersSharedWith = resourceCollectionService.findUsersSharedWith(user);
        logger.debug("{}", findUsersSharedWith);
        assertEmpty("should be empty", findUsersSharedWith);
    }
    
    
}

package org.tdar.core.service;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.collection.ResourceCollectionService;

//@Ignore
public class SharingITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Test
    @Rollback(true)
    public void testResource() {
        // test that a direct resource share exists (BillingUser -> reosurce -> basicUser)
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
        // test that a direct collection share works
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
        // test that a child collection share works, billing user is in child collection
        Dataset dataset = createAndSaveNewDataset();
        SharedCollection collection = createAndSaveNewResourceCollection("test collection");
        SharedCollection child = createAndSaveNewResourceCollection("test collection");
        collection.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(collection);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child, collection);
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
        // tests that a new user has notihng
        TdarUser user = createAndSaveNewUser();
        genericService.synchronize();
        List<TdarUser> findUsersSharedWith = resourceCollectionService.findUsersSharedWith(user);
        logger.debug("{}", findUsersSharedWith);
        assertEmpty("should be empty", findUsersSharedWith);
    }

    @Test
    @Rollback(true)
    // make a collection w/ three authusers, and confirm those users found via findUsersSharedWith()
    public void testFindUsersSharedWith() {
        final String collectionName = "the best collection ever";
        List<TdarUser> users = new ArrayList<>(Arrays.asList(getBasicUser(), getEditorUser(), getBillingUser(), getAdminUser()));

        SharedCollection collection = createAndSaveNewResourceCollection(collectionName, SharedCollection.class);
        users.remove(collection.getOwner());

        // sanity checks
        // assertThat("collection should have no authusers", collection.getAuthorizedUsers(), is( empty()));
        assertThat("test requires at least one user that is not the same as the current user", users, not(empty()));

        // now add some authusers
        collection.getAuthorizedUsers().addAll(
                users.stream().map(user -> new AuthorizedUser(getAdminUser(), user, GeneralPermissions.MODIFY_RECORD)).collect(Collectors.toList()));

        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(collection.getAuthorizedUsers());
        genericService.synchronize();
        List<TdarUser> grantees = resourceCollectionDao.findUsersSharedWith(collection.getOwner());
        for (TdarUser grantee : users) {
            assertTrue(String.format("grantees should contain: %s", grantee), grantees.contains(grantee));
        }
    }

}

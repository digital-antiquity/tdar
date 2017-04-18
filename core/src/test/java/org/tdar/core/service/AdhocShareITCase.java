package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.jena.ext.com.google.common.base.Objects;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.TdarAuthorizationException;
import org.tdar.core.service.collection.AdhocShare;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.utils.PersistableUtils;

public class AdhocShareITCase extends AbstractIntegrationTestCase {

    @Autowired
    ResourceCollectionService ressourceCollectionService;

    @Test
    @Rollback(true)
    public void testShareWithResources() {
        AdhocShare adhoc = new AdhocShare();
        adhoc.getResourceIds().add(TestConstants.DOCUMENT_INHERITING_NOTHING_ID);
        adhoc.getResourceIds().add(TestConstants.DOCUMENT_INHERITING_CULTURE_ID);
        adhoc.setUserId(getBasicUserId());

        List<Resource> resources = genericService.findAll(Resource.class, adhoc.getResourceIds());
        SharedCollection shareFromAdhoc = (SharedCollection) resourceCollectionService.createShareFromAdhoc(adhoc, resources, null, null, getAdminUser());
        assertTrue(shareFromAdhoc instanceof SharedCollection);
        assertTrue(shareFromAdhoc.getResources().containsAll(resources));
        assertEquals(2, shareFromAdhoc.getResources().size());
        assertEquals(getAdminUser(), shareFromAdhoc.getOwner());
        logger.debug(shareFromAdhoc.getName());
    }

    @Test
    @Rollback(true)
    public void testShareWithResource() {
        AdhocShare adhoc = new AdhocShare();
        adhoc.getResourceIds().add(TestConstants.DOCUMENT_INHERITING_NOTHING_ID);
        adhoc.setUserId(getBasicUserId());

        List<Resource> resources = genericService.findAll(Resource.class, adhoc.getResourceIds());
        RightsBasedResourceCollection shareFromAdhoc = resourceCollectionService.createShareFromAdhoc(adhoc, resources, null, null, getAdminUser());
        assertTrue(shareFromAdhoc instanceof InternalCollection);
        assertTrue(shareFromAdhoc.getResources().containsAll(resources));
        assertEquals(1, shareFromAdhoc.getResources().size());
        assertEquals(getAdminUser(), shareFromAdhoc.getOwner());
    }

    @Test
    @Rollback(true)
    public void testShareWithListCollection() {
        ListCollection list = new ListCollection("test", "test", SortOption.COLLECTION_TITLE, false, getBasicUser());
        list.getUnmanagedResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_NOTHING_ID));
        list.getUnmanagedResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_CULTURE_ID));
        list.markUpdated(getAdminUser());
        genericService.saveOrUpdate(list);
        assertTrue(PersistableUtils.isNotNullOrTransient(list));
        AdhocShare adhoc = new AdhocShare();
        adhoc.setCollectionId(list.getId());
        adhoc.setUserId(getBasicUserId());

        RightsBasedResourceCollection shareFromAdhoc = resourceCollectionService.createShareFromAdhoc(adhoc, null, list, null, getAdminUser());
        assertTrue(shareFromAdhoc instanceof SharedCollection);
        assertTrue(shareFromAdhoc.getResources().containsAll(list.getUnmanagedResources()));
        assertEquals(2, shareFromAdhoc.getResources().size());
        assertEquals(getAdminUser(), shareFromAdhoc.getOwner());
    }

    @Test
    @Rollback(true)
    public void testShareWithSharedCollection() {
        SharedCollection list = new SharedCollection("test", "test", getBasicUser());
        list.getResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_NOTHING_ID));
        list.getResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_CULTURE_ID));
        list.markUpdated(getBasicUser());
        genericService.saveOrUpdate(list);
        assertTrue(PersistableUtils.isNotNullOrTransient(list));
        AdhocShare adhoc = new AdhocShare();
        adhoc.setCollectionId(list.getId());
        adhoc.setUserId(getBasicUserId());

        SharedCollection shareFromAdhoc = (SharedCollection) resourceCollectionService.createShareFromAdhoc(adhoc, null, list, null, getAdminUser());
        assertTrue(shareFromAdhoc instanceof SharedCollection);
        assertTrue(shareFromAdhoc.getResources().containsAll(list.getResources()));
        assertEquals(2, shareFromAdhoc.getResources().size());
        assertEquals(getBasicUser(), shareFromAdhoc.getOwner());
        assertEquals(list, shareFromAdhoc);
    }

    @Test
    @Rollback(true)
    public void testShareWithAccount() {
        BillingAccount account = new BillingAccount();
        account.getResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_NOTHING_ID));
        account.getResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_CULTURE_ID));
        account.markUpdated(getBasicUser());
        genericService.saveOrUpdate(account);
        assertTrue(PersistableUtils.isNotNullOrTransient(account));
        AdhocShare adhoc = new AdhocShare();
        adhoc.setCollectionId(account.getId());
        adhoc.setUserId(getBasicUserId());

        SharedCollection shareFromAdhoc = (SharedCollection) resourceCollectionService.createShareFromAdhoc(adhoc, null, null, account, getAdminUser());
        assertTrue(shareFromAdhoc instanceof SharedCollection);
        assertTrue(shareFromAdhoc.getResources().containsAll(account.getResources()));
        assertEquals(2, shareFromAdhoc.getResources().size());
        assertEquals(getAdminUser(), shareFromAdhoc.getOwner());
    }

    @Test
    @Rollback(true)
    public void testShareWithSharedCollectionAndResource() {
        SharedCollection list = new SharedCollection("test", "test", getBasicUser());
        list.getResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_NOTHING_ID));
        list.markUpdated(getBasicUser());
        genericService.saveOrUpdate(list);
        assertTrue(PersistableUtils.isNotNullOrTransient(list));
        AdhocShare adhoc = new AdhocShare();
        adhoc.setCollectionId(list.getId());
        adhoc.getResourceIds().add(TestConstants.DOCUMENT_INHERITING_CULTURE_ID);
        adhoc.setUserId(getBasicUserId());
        Resource find = genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_CULTURE_ID);

        SharedCollection shareFromAdhoc = (SharedCollection) resourceCollectionService.createShareFromAdhoc(adhoc, Arrays.asList(find), list, null,
                getAdminUser());
        assertTrue(shareFromAdhoc instanceof SharedCollection);
        assertTrue(shareFromAdhoc.getResources().containsAll(list.getResources()));
        assertTrue(shareFromAdhoc.getResources().contains(find));
        assertEquals(2, shareFromAdhoc.getResources().size());
        assertEquals(getAdminUser(), shareFromAdhoc.getOwner());
        assertNotEquals(list, shareFromAdhoc);
    }

    @Test
    @Rollback(true)
    public void testShareWithoutRights() {
        SharedCollection list = new SharedCollection("test", "test",  getEditorUser());
        list.getResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_NOTHING_ID));
        list.getResources().add(genericService.find(Resource.class, TestConstants.DOCUMENT_INHERITING_CULTURE_ID));
        list.markUpdated(getEditorUser());
        genericService.saveOrUpdate(list);
        assertTrue(PersistableUtils.isNotNullOrTransient(list));
        AdhocShare adhoc = new AdhocShare();
        adhoc.setCollectionId(list.getId());
        adhoc.setUserId(getEditorUserId());
        boolean seen = false;
        try {
            SharedCollection shareFromAdhoc = (SharedCollection) resourceCollectionService.createShareFromAdhoc(adhoc, null, list, null, getBasicUser());
        } catch (TdarAuthorizationException tae) {
            seen = true;
        }
        assertTrue("should see authorization exception", seen);
    }

    @Test
    @Rollback(true)
    public void testShareWithExpiry() {
        AdhocShare adhoc = new AdhocShare();
        adhoc.getResourceIds().add(TestConstants.DOCUMENT_INHERITING_NOTHING_ID);
        adhoc.getResourceIds().add(TestConstants.DOCUMENT_INHERITING_CULTURE_ID);
        adhoc.setUserId(getBasicUserId());
        Date expires = DateTime.now().minusDays(2).toDate();
        adhoc.setExpires(expires);
        List<Resource> resources = genericService.findAll(Resource.class, adhoc.getResourceIds());
        SharedCollection shareFromAdhoc = (SharedCollection) resourceCollectionService.createShareFromAdhoc(adhoc, resources, null, null, getAdminUser());
        assertTrue(shareFromAdhoc instanceof SharedCollection);
        assertTrue(shareFromAdhoc.getResources().containsAll(resources));
        assertEquals(2, shareFromAdhoc.getResources().size());
        assertEquals(getAdminUser(), shareFromAdhoc.getOwner());
        logger.debug(shareFromAdhoc.getName());
        AuthorizedUser au = null;
        for (AuthorizedUser auth : shareFromAdhoc.getAuthorizedUsers()) {
            if (Objects.equal(auth.getUser(), getBasicUser())) {
                au = auth;
            }
        }
        assertNotNull(au);
        assertEquals(expires, au.getDateExpires());
        assertEquals(getAdminUser(), au.getCreatedBy());
    }

    @Test
    @Rollback(true)
    public void testShareWithEmail() {
        AdhocShare adhoc = new AdhocShare();
        adhoc.getResourceIds().add(TestConstants.DOCUMENT_INHERITING_NOTHING_ID);
        String email = "abc123@qhe1.com";
        adhoc.setEmail(email);
        adhoc.setFirstName("abc123");
        adhoc.setLastName("1234");
        adhoc.setPermission(GeneralPermissions.MODIFY_RECORD);
        
        List<Resource> resources = genericService.findAll(Resource.class, adhoc.getResourceIds());
        InternalCollection shareFromAdhoc = (InternalCollection) resourceCollectionService.createShareFromAdhoc(adhoc, resources, null, null, getAdminUser());
        assertTrue(shareFromAdhoc instanceof InternalCollection);
        assertTrue(shareFromAdhoc.getResources().containsAll(resources));
        assertEquals(1, shareFromAdhoc.getResources().size());
        assertEquals(getAdminUser(), shareFromAdhoc.getOwner());
        List<UserInvite> findAll = genericService.findAll(UserInvite.class);
        UserInvite inv = null;
        for (UserInvite invite : findAll) {
            if (invite.getUser().getEmail().equals(email)) {
                inv = invite; 
            }
        };
        assertTrue("should have seen invite", inv != null);
        assertEquals(inv.getResourceCollection(), shareFromAdhoc);
        assertEquals(inv.getPermissions(), GeneralPermissions.MODIFY_RECORD);
    }

}

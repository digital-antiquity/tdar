package org.tdar.struts.action.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TestResourceCollectionHelper;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.resource.ResourceRightsController;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Action;

public class ResourceCollectionRightsITCase extends AbstractControllerITCase implements TestResourceCollectionHelper {

    private static final String TEST123 = "test123";

    @Autowired
    private GenericService genericService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Autowired
    AuthorizedUserDao authorizedUserDao;

    ResourceCollectionController controller;

    static int indexCount = 0;

    @Before
    public void setup() {
        controller = generateNewInitializedController(ResourceCollectionController.class);
        if (indexCount < 1) {
            reindex();
        }
        indexCount++;
    }

    @Test
    @Rollback
    public void testResourceShareController() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource generateInformationResourceWithFile = generateDocumentWithUser();
        InformationResource generateInformationResourceWithFile2 = generateDocumentWithUser();
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(
                new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION),
                new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.MODIFY_RECORD),
                new AuthorizedUser(getAdminUser(), testPerson, Permissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        ResourceCollection collection = generateResourceCollection(name, description, true, users, resources, null);
        Long collectionid = collection.getId();
        logger.info("{}", collection.getManagedResources());
        assertFalse(collectionid.equals(-1L));
        collection = null;
        ResourceCollection foundCollection = genericService.find(ResourceCollection.class, collectionid);
        assertNotNull(foundCollection);
        assertEquals(3, foundCollection.getAuthorizedUsers().size());
        assertEquals(2, foundCollection.getManagedResources().size());

        assertEquals(name, foundCollection.getName());
        assertEquals(description, foundCollection.getDescription());
        assertEquals(SortOption.RESOURCE_TYPE, foundCollection.getSortBy());

        assertTrue(foundCollection.getManagedResources().contains(generateInformationResourceWithFile2));
        assertTrue(foundCollection.getManagedResources().contains(generateInformationResourceWithFile));

        int count = 0;
        for (AuthorizedUser user : foundCollection.getAuthorizedUsers()) {
            if (user.getUser().equals(testPerson)) {
                count++;
                assertEquals(Permissions.MODIFY_RECORD, user.getGeneralPermission());
            }
            if (user.getUser().equals(getAdminUser())) {
                count++;
                assertEquals(Permissions.MODIFY_RECORD, user.getGeneralPermission());
            }
            if (user.getUser().equals(getBasicUser())) {
                count++;
                assertEquals(Permissions.ADMINISTER_COLLECTION, user.getGeneralPermission());
            }
        }
        assertEquals(3, count);
    }

    @Test
    @Rollback
    public void testResourceShareControllerAdministerEdit() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        // InformationResource generateInformationResourceWithFile = generateDocumentWithUser();
        InformationResource generateInformationResourceWithFile2 = generateDocumentWithUser();
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(
                new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION),
                new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.MODIFY_RECORD),
                new AuthorizedUser(getAdminUser(), testPerson, Permissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile2));
        ResourceCollection collection = generateResourceCollection(name, description, true, users, getEditorUser(), resources, null);
        Long collectionid = collection.getId();
        logger.info("{}", collection.getManagedResources());
        assertFalse(collectionid.equals(-1L));
        collection = null;
        TdarUser transientSelf = new TdarUser();
        transientSelf.setId(getBasicUserId());
        TdarUser transientAdmin = new TdarUser();
        transientAdmin.setId(getAdminUserId());
        TdarUser transientTest = new TdarUser();
        transientTest.setId(testPerson.getId());
        List<UserRightsProxy> transientUsers = new ArrayList<>(Arrays.asList(
                new UserRightsProxy(new AuthorizedUser(getAdminUser(), transientSelf, Permissions.ADMINISTER_COLLECTION)),
                new UserRightsProxy(new AuthorizedUser(getAdminUser(), transientAdmin, Permissions.MODIFY_RECORD)),
                new UserRightsProxy(new AuthorizedUser(getAdminUser(), transientTest, Permissions.MODIFY_RECORD))));
        ResourceCollectionRightsController cc = generateNewInitializedController(ResourceCollectionRightsController.class, getBasicUser());
        cc.setId(collectionid);
        cc.prepare();
        cc.setProxies(transientUsers);
        cc.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS, cc.save());

    }

    @Test
    @Rollback
    public void testCollectionParentRightsCreateIssue() throws Exception {
        InformationResource generateInformationResourceWithFile = generateDocumentWithUser();

        // create a parent collection where basic user has administer groups, and with one resource
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(
                Arrays.asList(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile));
        ResourceCollection collection = generateResourceCollection("parent", "parent", false, users, resources, null);
        collection.setOwner(getAdminUser());
        genericService.saveOrUpdate(collection);
        authorizedUserDao.clearUserPermissionsCache();

        // create child collection with parent
        ResourceCollectionController cc = generateNewInitializedController(ResourceCollectionController.class, getBasicUser());
        cc.setParentId(collection.getId());
        cc.prepare();
        assertEquals(TdarActionSupport.SUCCESS, cc.add());
        cc.getResourceCollection().setName("test child");

        // save
        cc.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS, cc.save());
        assertTrue(cc.getResourceCollection().getParent().equals(collection));

        // save again
        Long id = cc.getResourceCollection().getId();
        cc = generateNewInitializedController(ResourceCollectionController.class, getBasicUser());
        cc.setId(id);
        cc.prepare();
        cc.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS, cc.save());
    }

    @Test
    @Rollback
    public void testResourceCollectionPermissionsController() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a2@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource generateInformationResourceWithFile = generateDocumentWithUser();
        InformationResource generateInformationResourceWithFile2 = generateDocumentWithUser();

        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(
                new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION),
                new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.MODIFY_RECORD),
                new AuthorizedUser(getAdminUser(), testPerson, Permissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        ResourceCollection collection = generateResourceCollection(name, description, false, users, resources, null);
        Long id = collection.getId();
        collection = null;
        assertFalse(id.equals(-1L));

        ResourceCollection foundCollection = genericService.find(ResourceCollection.class, id);

        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile, Permissions.MODIFY_METADATA));
        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile2, Permissions.MODIFY_METADATA));
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), generateInformationResourceWithFile2,
                Permissions.MODIFY_METADATA));

        assertTrue(authenticationAndAuthorizationService.canEditCollection(getBasicUser(), foundCollection));
        assertFalse(authenticationAndAuthorizationService.canEditCollection(testPerson, foundCollection));
    }

    @Test
    @Rollback
    public void testResourceCollectionPermissionsWithDepthController() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(
                new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION),
                new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.MODIFY_RECORD),
                new AuthorizedUser(getAdminUser(), testPerson, Permissions.MODIFY_RECORD)));

        Long resId = setupResource(testPerson, users);
        InformationResource generateInformationResourceWithFile = genericService.find(InformationResource.class, resId);
        logger.debug("collections: {}", generateInformationResourceWithFile.getManagedResourceCollections());
        authorizedUserDao.clearUserPermissionsCache();
        assertTrue("user can edit based on parent of parent resource collection",
                authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile, Permissions.MODIFY_METADATA));
    }

    /**
     * Create a user with administre permissions at the root, and making sure they can add something to a child collection
     * 
     * @throws Exception
     */
    @Test
    @Rollback(value = true)
    public void testInheritedAdminister() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@asdaasd.com", "1234");
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(
                new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION),
                new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.MODIFY_RECORD),
                new AuthorizedUser(getAdminUser(), testPerson, Permissions.ADMINISTER_COLLECTION)));
        ResourceCollection collection = generateResourceCollection("test parent", "test parent", false, users, null, null);
        Long parentId = collection.getId();
        ResourceCollection child = generateResourceCollection("test child", "test child", false, null, null, parentId);
        Long childId = child.getId();
        child = null;
        collection = null;
        users = null;
        InformationResource res = createAndSaveNewInformationResource(Document.class, testPerson);
        // res.getResourceCollections().add(new ResourceCollection("INTERNAL", "INTERNAL", SortOption.TITLE, CollectionType.INTERNAL, true, testPerson));
        Long resId = res.getId();
        // res.getInternalResourceCollection().getAuthorizedUsers().add(new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(res);
        res = null;
        authorizedUserDao.clearUserPermissionsCache();
        // assertTrue("user can edit based on parent of parent resource collection",
        // authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile, GeneralPermissions.MODIFY_METADATA));
        ResourceCollection find = genericService.find(ResourceCollection.class, childId);
        logger.debug("parent:{}", find.getParent());
        assertTrue(authenticationAndAuthorizationService.canAddToCollection(testPerson, find));
        authorizedUserDao.clearUserPermissionsCache();
        find = null;
        controller = generateNewInitializedController(ResourceCollectionController.class, testPerson);
        controller.setId(childId);
        controller.setParentId(parentId);
        controller.prepare();
        controller.getToAddManaged().add(resId);
        controller.setServletRequest(getServletPostRequest());
        String status = controller.save();
        assertEquals(TdarActionSupport.SUCCESS, status);

    }

    @Test
    @Rollback
    public void testResourceCollectionPermissionsWithDepthInvalidController() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        Long resId = setupResource(testPerson, null);
        InformationResource generateInformationResourceWithFile = genericService.find(InformationResource.class, resId);
        //
        // assertTrue("user can no longer edit",
        // authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile, GeneralPermissions.MODIFY_METADATA));
        authorizedUserDao.clearUserPermissionsCache();
        assertFalse("user can no longer edit",
                authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile, Permissions.MODIFY_METADATA));
    }

    @SuppressWarnings("unused")
    private Long setupResource(TdarUser testPerson, List<AuthorizedUser> users) throws Exception {
        String name = "test collection";
        String description = "test description";

        InformationResource generateInformationResourceWithFile = generateDocumentWithUser();
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile));
        ResourceCollection collection = generateResourceCollection(name, description, false, users, null, null);
        logger.debug("parent: {}", collection);
        ResourceCollection child = generateResourceCollection(name, description, false, null, resources, collection.getId());
        Long childId = child.getId();
        logger.info("{}", generateInformationResourceWithFile);
        Long resId = generateInformationResourceWithFile.getId();
        return resId;
    }

    @Test
    @Rollback
    public void testDocumentControllerAssigningResourceCollectionsWithoutLocalRights() throws Exception {
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", false, null, new ArrayList<Resource>(), null);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = controller.getDocument();
        document.setTitle("test");
        document.setDescription("test");
        document.setDate(1234);
        controller.getShares().add(collection1);
        controller.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, controller.save());
        evictCache();
        ResourceCollection first = document.getManagedResourceCollections().iterator().next();
        assertEquals(1, document.getManagedResourceCollections().size());
        assertEquals(collection1, first);
        assertEquals(getUser(), first.getOwner());
        assertEquals(1, first.getManagedResources().size());
    }

    @Test
    @Rollback
    public void testRightsEscalation() throws Exception {
        // Create document, add user to it with MODIFY_METADATA, have them create a collection, and add it where they're the owner and thus have higher rights
        Document document = generateDocumentWithUser();
        document.setSubmitter(getAdminUser());
        genericService.save(document);
        Long docId = document.getId();
        // document = null;
        ResourceRightsController controller = generateNewInitializedController(ResourceRightsController.class, getAdminUser());
        controller.setId(docId);
        controller.prepare();
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.MODIFY_METADATA)));
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        controller = null;
        // try and assign access to aa document that user should not have rights
        // to add, assert that this document cannot be added

        ResourceCollectionController cc = generateNewInitializedController(ResourceCollectionController.class, getBasicUser());
        cc.prepare();
        cc.getResourceCollection().setName("test");
        cc.getResourceCollection().setDescription("test");
        cc.getToAddManaged().add(document.getId());
        assertWeFailedToSave(cc);
    }

    @Test
    @Rollback
    public void testOwnRightsEscalation() throws Exception {
        // Create document, add user to it with MODIFY_METADATA, have them create a collection, and add it where they're the owner and thus have higher rights
        Document document = generateDocumentWithUser();
        document.setSubmitter(getAdminUser());
        genericService.save(document);
        Long docId = document.getId();
        ResourceRightsController controller = generateNewInitializedController(ResourceRightsController.class, getAdminUser());
        controller.setId(docId);
        controller.prepare();
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.MODIFY_METADATA)));
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        controller = null;
        // try and assign access to aa document that user should not have rights
        // to add, assert that this document cannot be added
        controller = generateNewInitializedController(ResourceRightsController.class, getBasicUser());
        controller.setId(docId);
        Exception e = null;
        try {
            controller.prepare();
            controller.edit();
            controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.MODIFY_RECORD)));
            resourceCollectionService.saveResourceRights(controller.getProxies(), getBasicUser(), controller.getResource());
        } catch (Exception es) {
            e = es;
        }
        assertNotNull(e);
    }

    @Test
    @Rollback
    public void testInvalidRightsAssignment() throws Exception {
        Document document = generateDocumentWithUser();
        document.getAuthorizedUsers().iterator().next().setUser(getAdminUser());
        genericService.save(document);
        // try and assign access to a document that user should not have rights
        // to add, assert that this document cannot be added

        ResourceCollectionController controller = generateNewController(ResourceCollectionController.class);
        init(controller, getBasicUser());
        controller.add();
        ResourceCollection resourceCollection = controller.getResourceCollection();
        resourceCollection.setName("tst");
        resourceCollection.setDescription("tst");
        resourceCollection.markUpdated(getBasicUser());
        // resourceCollection.setSortBy(SortOption.ID);
        controller.getToAddManaged().add(document.getId());
        controller.setServletRequest(getServletPostRequest());
        String result = controller.save();
        assertFalse(result.equals(Action.SUCCESS));
        controller = generateNewInitializedController(ResourceCollectionController.class);
        controller.setId(resourceCollection.getId());
        assertEquals(0, resourceCollection.getManagedResources().size());
        resourceCollection = null;
        controller.prepare();
        controller.edit();
        assertEquals(0, controller.getResourceCollection().getManagedResources().size());

    }

    private void assertWeFailedToSave(AbstractPersistableController<?> cc) {
        cc.setServletRequest(getServletPostRequest());
        String result = Action.SUCCESS;
        setIgnoreActionErrors(true);
        try {
            cc.prepare();
            result = cc.save();
        } catch (Exception e) {
            logger.error("{}", e);
            result = null;
        }
        assertFalse(Action.SUCCESS.equals(result));
    }

    private void assertWeFailedToSave(ResourceCollectionRightsController cc) {
        cc.setServletRequest(getServletPostRequest());
        String result = Action.SUCCESS;
        setIgnoreActionErrors(true);
        try {
            cc.prepare();
            result = cc.save();
        } catch (Exception e) {
            logger.error("{}", e);
            result = null;
        }
        assertFalse(Action.SUCCESS.equals(result));
    }

    @Test
    @Rollback
    public void testRightsEscalationUserUpsSelf() throws Exception {
        // Create document, add user to it with MODIFY_METADATA, have them edit document and add it to an adhoc collection, then try and add higher rights
        Document document = generateDocumentWithUser();
        document.setSubmitter(getAdminUser());
        genericService.save(document);
        Long docId = document.getId();
        document = null;
        ResourceRightsController controller = generateNewInitializedController(ResourceRightsController.class, getAdminUser());
        controller.setId(docId);
        controller.prepare();
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.MODIFY_METADATA)));
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        DocumentController dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.setId(docId);
        dc.prepare();
        dc.getShares().add(new ResourceCollection(TEST123, TEST123, getBasicUser()));
        dc.setServletRequest(getServletPostRequest());
        dc.save();

        controller = null;
        dc = null;
        genericService.synchronize();
        evictCache();
        Long id = getTestCollectionId(docId);
        assertNull(id);
        // try and assign access to aa document that user should not have rights
        // to add, assert that this document cannot be added
        ResourceCollectionRightsController cc = generateNewInitializedController(ResourceCollectionRightsController.class, getBasicUser());
        cc.setId(id);
        logger.debug("id: {}", id);
        boolean seenException = false;
        try {
            cc.prepare();
            // controller.getResources().add(document);
            cc.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.MODIFY_RECORD)));
            assertWeFailedToSave(cc);

        } catch (Exception e) {
            seenException = true;
            logger.error("{}", e, e);
        }
        assertTrue("should have gotten exception when trying to edit rights", seenException);
    }

    private Long getTestCollectionId(Long docId) {
        Document document;
        document = genericService.find(Document.class, docId);
        Long id = null;
        for (ResourceCollection c : document.getManagedResourceCollections()) {
            if (c instanceof ResourceCollection && ((ResourceCollection) c).getTitle().equals(TEST123)) {
                id = c.getId();
            }
        }
        return id;
    }

    @SuppressWarnings("unchecked")
    @Test
    @Rollback
    public void testRightsEscalationUserUpsParent() throws Exception {
        List<AuthorizedUser> users = Arrays.asList(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION));
        ResourceCollection parent = generateResourceCollection("parent", "parent", true, users, getBasicUser(), Collections.EMPTY_LIST,
                null);
        Long parentId = parent.getId();
        // Create document, add user to it with MODIFY_METADATA, have them edit document and add it to an adhoc collection, then try and add higher rights
        Document document = generateDocumentWithUser();
        document.setSubmitter(getAdminUser());
        genericService.save(document);
        Long docId = document.getId();
        document = null;
        ResourceRightsController controller = generateNewInitializedController(ResourceRightsController.class, getAdminUser());
        controller.setId(docId);
        controller.prepare();
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.MODIFY_METADATA)));
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        DocumentController dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.setId(docId);
        dc.prepare();
        dc.getShares().add(new ResourceCollection(TEST123, TEST123, getBasicUser()));
        dc.setServletRequest(getServletPostRequest());
        dc.save();
        Long id = -1L;
        for (ResourceCollection c : dc.getShares()) {
            if (c instanceof ResourceCollection && ((ResourceCollection) c).getTitle().equals(TEST123)) {
                id = c.getId();
            }
        }
        dc = null;
        // try and assign access to aa document that user should not have rights
        // to add, assert that this document cannot be added

        ResourceCollectionController cc = generateNewInitializedController(ResourceCollectionController.class, getBasicUser());
        cc.setId(id);
        // cc.prepare();
        cc.setParentId(parentId);
        assertWeFailedToSave(cc);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    @Ignore("duplicated by web test; fails because of transactional issue that I belive is related to the test setup, web test passes")
    public void testResourceCollectionRightsRevoking() throws Exception {
        TdarUser registeredUser = createAndSaveNewPerson("testDraftResourceVisibleByAuthuser", "foo");
        final Long userId = registeredUser.getId();
        controller = generateNewInitializedController(ResourceCollectionController.class, getUser());
        controller.prepare();
        // project = null;
        // Long pid = project.getId();
        controller.getPersistable().setName("test");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        // controller.setAsync(false);
        String result = controller.save();
        assertEquals(Action.SUCCESS, result);
        controller = null;
        genericService.synchronize();
        final Long rcid = controller.getPersistable().getId();
        ResourceCollectionRightsController cc = generateNewInitializedController(ResourceCollectionRightsController.class, getBasicUser());
        cc.setId(rcid);
        cc.prepare();
        cc.edit();
        cc.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), registeredUser, Permissions.ADMINISTER_COLLECTION)));
        cc.setServletRequest(getServletPostRequest());
        // cc.setAsync(false);
        assertEquals(Action.SUCCESS, cc.save());

        // confirm resource is viewable by author of collection
        cc = generateNewInitializedController(ResourceCollectionRightsController.class, getUser());
        cc.setId(rcid);
        cc.prepare();
        cc.edit();
        cc.getProxies().clear();
        cc.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getEditorUser(), Permissions.ADMINISTER_COLLECTION)));
        cc.setServletRequest(getServletPostRequest());
        // cc.setAsync(false);
        result = cc.save();
        genericService.evictFromCache(cc.getResourceCollection());
        cc = null;
        genericService.synchronize();
        ResourceCollection collection = genericService.find(ResourceCollection.class, rcid);
        logger.debug("AU:{}", collection.getAuthorizedUsers());
        registeredUser = null;
        collection = null;

        // make sure it draft resource can't be seen by registered user (but not an authuser)
        TdarUser tdarUser = genericService.find(TdarUser.class, userId);
        controller = generateNewInitializedController(ResourceCollectionController.class, tdarUser);
        controller.setId(rcid);
        boolean seenException = false;
        ignoreActionErrors(true);
        try {
            controller.prepare();
            logger.debug("{}", controller.getResourceCollection());
            logger.debug("{}", controller.getResourceCollection().getSubmitter());
            logger.debug("{}", controller.getResourceCollection().getAuthorizedUsers());
            controller.edit();
        } catch (Exception e) {
            seenException = true;
            logger.warn("error", e);
        }
        assertTrue(seenException);
        genericService.forceDelete(tdarUser);
        genericService.delete(genericService.find(ResourceCollection.class, rcid));
    }

    @SuppressWarnings("unused")
    @Test
    public void testResourceCollectionRightsRevokingHier() throws Exception {
        TdarUser registeredUser = createAndSaveNewPerson("testDraftResourceVisibleByAuthuser", "foo");
        controller = generateNewInitializedController(ResourceCollectionController.class, getUser());
        controller.prepare();
        ResourceCollection rc = controller.getPersistable();
        // project = null;
        // Long pid = project.getId();
        controller.getPersistable().setName("test");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        // controller.setAsync(false);
        String result = controller.save();
        assertEquals(Action.SUCCESS, result);
        Long rcid = controller.getPersistable().getId();

        ResourceCollectionRightsController cc = generateNewInitializedController(ResourceCollectionRightsController.class, getBasicUser());
        cc.setId(rcid);
        cc.prepare();
        cc.edit();
        cc.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), registeredUser, Permissions.ADMINISTER_COLLECTION)));
        cc.setServletRequest(getServletPostRequest());
        // cc.setAsync(false);
        assertEquals(Action.SUCCESS, cc.save());

        logger.debug("--------- creating child ----------");
        controller = generateNewInitializedController(ResourceCollectionController.class, getUser());
        controller.setParentId(rcid);

        controller.prepare();
        ResourceCollection rcChild = controller.getPersistable();
        // project = null;
        // Long pid = project.getId();
        controller.setParentId(rcid);
        controller.getPersistable().setName("test child");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        // controller.setAsync(false);
        result = controller.save();
        assertEquals(Action.SUCCESS, result);
        Long rcid2 = controller.getPersistable().getId();

        // confirm resource is viewable by author of collection
        logger.debug("--------- clearing parent ----------");
        controller = generateNewInitializedController(ResourceCollectionController.class, registeredUser);
        controller.setId(rcid2);
        controller.prepare();
        controller.edit();
        controller.setParentId(null);
        controller.setParentCollectionName("");
        controller.setServletRequest(getServletPostRequest());
        // controller.setAsync(false);
        result = controller.save();

        genericService.synchronize();

        // make sure it draft resource can't be seen by registered user (but not an authuser)
        controller = generateNewInitializedController(ResourceCollectionController.class, registeredUser);
        controller.setId(rcid2);
        boolean seen = false;
        ignoreActionErrors(true);
        try {
            controller.prepare();
            controller.edit();
        } catch (Exception e) {
            seen = true;
            logger.warn("error", e);
        }
        assertTrue(seen);
    }

    @SuppressWarnings("unused")
    @Test
    public void testResourceCollectionRightsRevokingHierOwnerFails() throws Exception {
        TdarUser registeredUser = createAndSaveNewPerson("testDraftResourceVisibleByAuthuser", "foo");
        controller = generateNewInitializedController(ResourceCollectionController.class, registeredUser);
        controller.prepare();
        ResourceCollection rc = controller.getPersistable();
        // project = null;
        // Long pid = project.getId();
        controller.getPersistable().setName("test");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        // controller.setAsync(false);
        String result = controller.save();
        assertEquals(Action.SUCCESS, result);
        Long rcid = controller.getPersistable().getId();

        ResourceCollectionRightsController cc = generateNewInitializedController(ResourceCollectionRightsController.class, registeredUser);
        cc.setId(rcid);
        cc.prepare();
        cc.edit();
        cc.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getUser(), Permissions.ADMINISTER_COLLECTION)));
        cc.setServletRequest(getServletPostRequest());
        // cc.setAsync(false);
        assertEquals(Action.SUCCESS, cc.save());

        controller = generateNewInitializedController(ResourceCollectionController.class, getUser());
        controller.setParentId(rcid);
        controller.prepare();
        // project = null;
        // Long pid = project.getId();
        ResourceCollection rcChild = controller.getPersistable();
        controller.getPersistable().setName("test child");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        // controller.setAsync(false);
        result = controller.save();
        assertEquals(Action.SUCCESS, result);
        Long rcid2 = controller.getPersistable().getId();

        /*
         * This test is not expected to fail in-that hierarchical collection "owners" have rights explicitly.
         */
        controller = generateNewInitializedController(ResourceCollectionController.class, registeredUser);
        controller.setId(rcid2);
        boolean seen = false;
        ignoreActionErrors(true);
        try {
            controller.prepare();
            controller.edit();
        } catch (Exception e) {
            seen = true;
            logger.warn("error", e);
        }
        assertFalse(seen);
    }

}

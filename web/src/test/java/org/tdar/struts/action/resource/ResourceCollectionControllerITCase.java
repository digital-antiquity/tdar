package org.tdar.struts.action.resource;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.browse.BrowseCollectionController;
import org.tdar.struts.action.collection.CollectionController;
import org.tdar.struts.action.collection.CollectionDeleteAction;
import org.tdar.struts.action.collection.CollectionViewAction;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.project.ProjectController;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Action;


public class ResourceCollectionControllerITCase extends AbstractResourceControllerITCase {

    @Autowired
    private GenericService genericService;

    @Autowired
    private EntityService entityService;

    @Autowired
    AuthorizedUserDao authorizedUserDao;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    CollectionController controller;

    static int indexCount = 0;

    @Before
    public void setup() {
        controller = generateNewInitializedController(CollectionController.class);
        if (indexCount < 1) {
            reindex();
        }
        indexCount++;
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

        InformationResource normal = generateDocumentWithFileAndUseDefaultUser();
        InformationResource draft = generateDocumentWithFileAndUseDefaultUser();
        final Long normalId = normal.getId();
        final Long draftId = draft.getId();
        draft.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(draft);
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        ResourceCollection collection = generateResourceCollection(name, description, CollectionType.SHARED, false, users, resources, null);

        final Long id = collection.getId();
        String slug = collection.getSlug();
        collection = null;

        controller = generateNewInitializedController(CollectionController.class, getAdminUser());
        controller.setId(id);
        controller.prepare();
        controller.edit();
        controller.setServletRequest(getServletPostRequest());
        controller.getAuthorizedUsers().add(new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD));
        controller.setAsync(false);
        controller.save();

        // setVerifyTransactionCallback(new TransactionCallback<Resource>() {
        //
        // @Override
        // public Resource doInTransaction(TransactionStatus arg0) {
        // InformationResource normal = informationResourceService.find(normalId);
        // InformationResource draft = informationResourceService.find(draftId);

        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, normal, GeneralPermissions.MODIFY_METADATA));
        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, draft, GeneralPermissions.MODIFY_METADATA));
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), draft, GeneralPermissions.MODIFY_METADATA));
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), normal, GeneralPermissions.MODIFY_METADATA));

        assertTrue(authenticationAndAuthorizationService.canViewResource(getBasicUser(), draft));
        assertTrue(authenticationAndAuthorizationService.canViewResource(getBasicUser(), normal));

        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class, testPerson);
        vc.setId(id);
        vc.setSlug(slug);
        vc.prepare();
        try {
            vc.view();
        } catch (TdarActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.info("results: {} ", vc.getResults());
        assertTrue(vc.getResults().contains(normal));
        assertTrue(vc.getResults().contains(draft));
        genericService.delete(vc.getResourceCollection().getAuthorizedUsers());
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testPublicCollection() throws Exception {
        String email = "a243@basda.com";
        entityService.delete(entityService.findByEmail(email));

        final TdarUser testPerson = createAndSaveNewPerson(email, "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource normal = generateDocumentWithFileAndUseDefaultUser();
        InformationResource draft = generateDocumentWithFileAndUseDefaultUser();
        final Long normalId = normal.getId();
        final Long draftId = draft.getId();
        draft.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(draft);
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        ResourceCollection collection = generateResourceCollection(name, description, CollectionType.LIST, false, users, testPerson, resources, null);

        final Long id = collection.getId();
        String slug = collection.getSlug();
        collection = null;

        controller = generateNewInitializedController(CollectionController.class, getAdminUser());
        controller.setId(id);
        controller.prepare();
        controller.edit();
        controller.setServletRequest(getServletPostRequest());
        controller.getAuthorizedUsers().add(new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD));
        controller.setAsync(false);
        controller.save();

        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, normal, GeneralPermissions.MODIFY_METADATA));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, draft, GeneralPermissions.MODIFY_RECORD));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, draft));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, normal));
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testLimitedCollectionPermissions() throws Exception {
        String email = "a243@basda.com";
        entityService.delete(entityService.findByEmail(email));

        final TdarUser testPerson = createAndSaveNewPerson(email, "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource normal = generateDocumentWithFileAndUseDefaultUser();
        final Long normalId = normal.getId();
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getAdminUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_METADATA)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal));
        ResourceCollection collection = generateResourceCollection(name, description, CollectionType.SHARED, false, users, resources, null);

        final Long id = collection.getId();
        collection = null;

        DocumentController dcontroller = generateNewInitializedController(DocumentController.class, testPerson);
        dcontroller.setId(normalId);
        dcontroller.prepare();
        dcontroller.getDocument().setTitle("TEST 123");
        dcontroller.edit();
        dcontroller.setServletRequest(getServletPostRequest());
        dcontroller.setAsync(false);
        String save = dcontroller.save();
        assertEquals(Action.SUCCESS, save);
    }

    @Test
    @Rollback
    public void testRemoveResources() throws Exception {
        ResourceCollection resourceCollection = new ResourceCollection(CollectionType.SHARED);
        resourceCollection.setName("a resource collection");
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        TdarUser owner = new TdarUser("bob", "loblaw", "bobloblaw@tdar.net");
        owner.setContributor(true);

        genericService.saveOrUpdate(owner);
        resourceCollection.setOwner(owner);
        resourceCollection.markUpdated(owner);
        genericService.saveOrUpdate(resourceCollection);

        genericService.saveOrUpdate(resourceCollection);
        for (Document doc : docList) {
            doc.getResourceCollections().add(resourceCollection);
            doc.setSubmitter(owner);
            genericService.saveOrUpdate(doc);
        }
        genericService.saveOrUpdate(resourceCollection);

        evictCache();

        // okay, now let's try to remove the resources from the collection via the
        // controller.
        Long rcid = resourceCollection.getId();
        controller.setId(rcid);
        resourceCollection = null;
        init(controller, owner);
        controller.prepare();
        for (Document doc : docList) {
            controller.getToRemove().add(doc.getId());
        }
        controller.setServletRequest(getServletPostRequest());
        assertNotNull(controller.getPersistable());
        assertTrue("resource list should not be empty", !controller.getPersistable().getResources().isEmpty());

        // clear the list of incoming resources, then save
        // controller.getResources().clear(); // strictly speaking this line is not
        // // necessary.
        controller.save();

        evictCache();

        // now load our resource collection again. the resources should be gone.
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        assertEquals("resource list should be empty", 0, resourceCollection.getResources().size());
    }

    @Test
    @Rollback(true)
    public void testDeleteResourceCollection() throws Exception {
        ResourceCollection resourceCollection = new ResourceCollection(CollectionType.SHARED);
        ResourceCollection resourceCollectionParent = new ResourceCollection(CollectionType.SHARED);
        ResourceCollection resourceCollectionChild = new ResourceCollection(CollectionType.SHARED);
        resourceCollectionChild.setName("child collection");
        resourceCollectionParent.setName("parent collection");
        resourceCollection.setName("a resource collection");
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        TdarUser owner = new TdarUser("bob", "loblaw", "bobloblaw@tdar.net");
        owner.setContributor(true);
        genericService.saveOrUpdate(owner);
        resourceCollection.markUpdated(owner);
        resourceCollectionParent.markUpdated(owner);
        resourceCollectionChild.markUpdated(owner);
        genericService.saveOrUpdate(resourceCollection);
        genericService.saveOrUpdate(resourceCollectionParent);
        genericService.saveOrUpdate(resourceCollectionChild);
        resourceCollectionChild.setParent(resourceCollection);
        resourceCollection.setParent(resourceCollectionParent);
        genericService.saveOrUpdate(resourceCollection);
        genericService.saveOrUpdate(resourceCollectionParent);
        genericService.saveOrUpdate(resourceCollectionChild);
        evictCache();

        Long parentId = resourceCollectionParent.getId();
        Long childId = resourceCollectionChild.getId();

        // okay, now let's try to remove the resources from the collection via the
        // controller.
        Long rcid = resourceCollection.getId();
        CollectionDeleteAction deleteAction = generateNewController(CollectionDeleteAction.class);
        deleteAction.setId(rcid);
        resourceCollection = null;
        init(deleteAction, owner);
        deleteAction.prepare();
        assertNotNull(deleteAction.getPersistable());
        assertTrue("resource list should not be empty", !deleteAction.getPersistable().getResources().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        deleteAction.delete();

        // now load our resource collection again. the resources should be gone.
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        assertFalse("user should not be able to delete collection", resourceCollection == null);

        for (ResourceCollection child : resourceCollectionService.findDirectChildCollections(rcid, null, CollectionType.SHARED)) {
            child.setParent(null);
            genericService.saveOrUpdate(child);
        }
//        evictCache();

        deleteAction = generateNewInitializedController(CollectionDeleteAction.class);
        deleteAction.setId(rcid);
        resourceCollection = null;
        init(deleteAction, owner);
        deleteAction.prepare();
        assertNotNull(deleteAction.getPersistable());
        assertTrue("resource list should not be empty", !deleteAction.getPersistable().getResources().isEmpty());
        // resourceCollection.setParent(parent)
        setHttpServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        deleteAction.delete();
//        evictCache();
        assertEquals(null, deleteAction.getDeleteIssue());
        resourceCollection = null;
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        logger.info("{}", genericService.find(ResourceCollection.class, rcid));
        assertTrue("user should be able to delete collection", resourceCollection == null);
        resourceCollectionChild = null;
        resourceCollectionParent = null;
        ResourceCollection child = genericService.find(ResourceCollection.class, childId);
        List<ResourceCollection> children = resourceCollectionService.findDirectChildCollections(parentId, null, CollectionType.SHARED);
        logger.info("child: {}", child.getParent());
        logger.info("children: {}", children);
        assertTrue(child.getParent() == null);
        assertTrue((children == null) || (children.size() == 0));
//        evictCache();

    }

    @Test
    @Rollback(true)
    public void testDeleteResourceCollectionWithUser() throws Exception {
        ResourceCollection resourceCollection = new ResourceCollection(CollectionType.SHARED);
        resourceCollection.setName("a resource collection");
        resourceCollection.setSortBy(SortOption.DATE);
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        TdarUser owner = new TdarUser("bob", "loblaw", "bobloblaw@tdar.net");
        owner.setContributor(true);
        genericService.saveOrUpdate(owner);
        resourceCollection.markUpdated(owner);
        AuthorizedUser authorizedUser = new AuthorizedUser(owner, GeneralPermissions.MODIFY_RECORD);
        resourceCollection.getAuthorizedUsers().addAll(Arrays.asList(authorizedUser));
        genericService.saveOrUpdate(resourceCollection);
//        evictCache();

        // okay, now let's try to remove the resources from the collection via the
        // controller.
        Long rcid = resourceCollection.getId();
        CollectionDeleteAction deleteAction = generateNewController(CollectionDeleteAction.class);
        deleteAction.setId(rcid);
        resourceCollection = null;
        init(deleteAction, owner);
        deleteAction.prepare();
        assertNotNull(deleteAction.getPersistable());
        assertTrue("resource list should not be empty", !deleteAction.getPersistable().getResources().isEmpty());
        assertTrue("user list should not be empty", !deleteAction.getPersistable().getAuthorizedUsers().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        deleteAction.delete();

        // now load our resource collection again. the resources should be gone.
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        assertFalse("user should not be able to delete collection", resourceCollection == null);

        deleteAction = generateNewController(CollectionDeleteAction.class);
        deleteAction.setId(rcid);
        resourceCollection = null;
        init(deleteAction, owner);
        deleteAction.prepare();
        assertNotNull(deleteAction.getPersistable());
        assertTrue("resource list should not be empty", !deleteAction.getPersistable().getResources().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        deleteAction.delete();
//        evictCache();
        assertEquals(null, deleteAction.getDeleteIssue());
        resourceCollection = null;
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        logger.info("{}", genericService.find(ResourceCollection.class, rcid));
        assertTrue("user should be able to delete collection", resourceCollection == null);
//        evictCache();
    }

    @Test
    @Rollback
    public void testSaveAndDeleteWithRedundantAccessRights() throws TdarActionException {
        controller.prepare();
        controller.add();

        ResourceCollection rc = controller.getResourceCollection();
        rc.setName("test delete w/ redundant rights");
        rc.setDescription("a tragedy in three acts");
        rc.setHidden(false);
        rc.setSortBy(SortOption.ID);
        rc.setOrientation(DisplayOrientation.LIST);

        // Add three authusers. two of the authusers are redundant and should be normalized to the user with
        // the best permissions.
        AuthorizedUser user1Viewer = createAuthUser(GeneralPermissions.VIEW_ALL);
        AuthorizedUser user1Modifier = new AuthorizedUser(user1Viewer.getUser(), GeneralPermissions.MODIFY_METADATA);
        AuthorizedUser user2 = createAuthUser(GeneralPermissions.ADMINISTER_GROUP);
        user2.setTest("1234");
        controller.getAuthorizedUsers().addAll(Arrays.asList(user1Viewer, user1Modifier, user2));

        controller.setServletRequest(getServletPostRequest());
        controller.save();

        Long id = rc.getId();
        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setSessionData(getSessionData());
        vc.setId(id);
        vc.prepare();
        vc.view();

        ResourceCollection rc2 = vc.getResourceCollection();
        assertEquals(rc.getName(), rc2.getName());
        assertEquals("2 redundant authusers should have been normalized", 2, rc2.getAuthorizedUsers().size());

        assertEquals("size should be 2", 2, rc2.getAuthorizedUsers().size());

        // if this list is truly normalized, each queue should be length 1
        HashMap<Long, GeneralPermissions> map = new HashMap<>();
        for (AuthorizedUser authuser : rc2.getAuthorizedUsers()) {
            map.put(authuser.getUser().getId(), authuser.getGeneralPermission());
        }
        assertEquals("user 1 should have best permission", GeneralPermissions.MODIFY_METADATA, map.get(user1Modifier.getUser().getId()));
        assertNotNull("only the modifier & admin authusers should remain", map.get(user2.getUser().getId()));
    }

    @Test
    @Rollback
    public void testNormalizeAuthorizedUsers() {
        // Add three authusers. two of the authusers are redundant and should be normalized to the user with
        // the best permissions.
        AuthorizedUser user1Viewer = createAuthUser(GeneralPermissions.VIEW_ALL);
        AuthorizedUser user1Modifier = new AuthorizedUser(user1Viewer.getUser(), GeneralPermissions.MODIFY_METADATA);
        AuthorizedUser user2 = createAuthUser(GeneralPermissions.ADMINISTER_GROUP);
        List<AuthorizedUser> authusers = new ArrayList<>(Arrays.asList(user1Viewer, user1Modifier, user2));
        int origCount = authusers.size();
        resourceCollectionService.normalizeAuthorizedUsers(authusers);
        int newCount = authusers.size();
        assertThat(newCount, lessThan(origCount));
    }

    private AuthorizedUser createAuthUser(GeneralPermissions permissions) {
        String string = UUID.randomUUID().toString();
        TdarUser person = new TdarUser(string, string, string + "@tdar.net");
        person.setContributor(true);
        genericService.saveOrUpdate(person);
        AuthorizedUser authuser = new AuthorizedUser(person, permissions);
        return authuser;
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testBrowseControllerVisibleCollections() throws Exception {
        genericService.synchronize();
        logger.debug("------------------------------------------------------------------------------------------------------------------");
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(testPerson, GeneralPermissions.ADMINISTER_GROUP)));
        ResourceCollection collection1 = generateResourceCollection("INTERNAL", "", CollectionType.INTERNAL, false, new ArrayList<>(users),
                new ArrayList<Resource>(), null);
        ResourceCollection collection2 = generateResourceCollection("SHARED", "", CollectionType.SHARED, false, new ArrayList<>(users),
                new ArrayList<Resource>(), null);
        InformationResource testFile = generateDocumentWithUser();
        ResourceCollection parentCollection = generateResourceCollection("PARENT", "", CollectionType.SHARED, true, new ArrayList<>(users),
                Arrays.asList(testFile), null);
        Long id = parentCollection.getId();
        ResourceCollection childCollection = generateResourceCollection("CHILD", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), id);
        ResourceCollection childCollectionHidden = generateResourceCollection("HIDDEN CHILD", "", CollectionType.SHARED, false, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), id);
        // genericService.saveOrUpdate(parentCollection);
        Long parentCollectionId = parentCollection.getId();
        parentCollection = null;
        BrowseCollectionController controller_ = generateNewInitializedController(BrowseCollectionController.class);
        initAnonymousUser(controller_);
        Long fileId = testFile.getId();

        genericService.synchronize();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        testFile = null;
        // WHY DOES THE SYNCHRONIZE ON THE INDEX CALL DO ANYTHING HERE VS THE
        // SYNCHRONIZE ABOVE
        testFile = genericService.find(Document.class, fileId);
        logger.info("{} : {}", testFile, testFile.getResourceCollections());

        controller_.setRecordsPerPage(1000);
        assertEquals(Action.SUCCESS, controller_.browseCollections());
        List<ResourceCollection> collections_ = controller_.getResults();
        for (ResourceCollection result : collections_) {
            if (result != null) {
                logger.debug("{} {} {} {} ", result.getTitle(), result.getId(), result.isHidden(), result.isTopLevel());
            }
            logger.debug("NULL");
        }
        List<Long> collections = PersistableUtils.extractIds(collections_);
        collections_ = null;
        Long childCollectionId = childCollection.getId();
        Long childCollectionHiddenId = childCollectionHidden.getId();
        Long collection1Id = collection1.getId();
        Long collection2Id = collection2.getId();
        
        childCollection = null;
        childCollectionHidden = null;
        collection1 = null;
        collection2 = null;
        assertFalse(collections.contains(collection1Id));
        assertFalse(collections.contains(collection2Id));

        assertEquals(1, testFile.getResourceCollections().size());
        parentCollection = genericService.find(ResourceCollection.class, id);
        assertTrue(parentCollection.isShared());
        assertTrue(!parentCollection.isHidden());
        assertTrue(parentCollection.isTopLevel());
        String slug = parentCollection.getSlug();
        parentCollection = null;
        assertTrue(String.format("collections %s should contain %s", collections, parentCollectionId), collections.contains(parentCollectionId));
        // assertFalse(childCollection.isHidden());
        assertFalse(collections.contains(childCollectionId));
        assertFalse(collections.contains(childCollectionHiddenId));
        // genericService.synchronize();
        collections = null;

        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        // TESTING ANONYMOUS USER
        initAnonymousUser(vc);
        vc.setId(id);
        vc.setSlug(slug);
        vc.prepare();
        assertEquals(Action.SUCCESS, vc.view());
        collections = PersistableUtils.extractIds(vc.getCollections());
        assertTrue(collections.contains(childCollectionId));
        assertFalse(collections.contains(childCollectionHiddenId));
        assertEquals(1, vc.getResults().size());

        // TESTING MORE ADVANCED VIEW RIGHTS
        logger.info("{}", vc.getActionErrors());
        vc = generateNewController(CollectionViewAction.class);
        init(vc, testPerson);
        vc.setId(id);
        vc.setSlug(slug);
        vc.prepare();
        assertEquals(Action.SUCCESS, vc.view());
        collections = PersistableUtils.extractIds(vc.getCollections());
        assertEquals(2, collections.size());
        assertTrue(collections.contains(childCollectionId));
        assertTrue(collections.contains(childCollectionHiddenId));

        logger.info("{}", vc.getActionErrors());

        //evictCache();
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testHiddenParentVisibleChild() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");

        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, false, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), collection1.getId());
        evictCache();
        searchIndexService.index(collection1, collection2);

//        logger.debug("1:{} v: {} h:{}", collection1.getId(), collection1.getUsersWhoCanView(), collection1.isHidden());
//        logger.debug("2:{} v: {} h:{}", collection2.getId(), collection2.getUsersWhoCanView(), collection2.isHidden());
        genericService.synchronize();
        BrowseCollectionController browseController = generateNewInitializedController(BrowseCollectionController.class, testPerson);
        browseController.setRecordsPerPage(Integer.MAX_VALUE);
        browseController.browseCollections();
        logger.debug("results:{}", PersistableUtils.extractIds(browseController.getResults()));
        assertTrue("should see child collection of hidden parent", browseController.getResults().contains(collection2));
        assertFalse("should not see hidden collection", browseController.getResults().contains(collection1));
    }

    @Test
    @Rollback
    public void testNestedCollectionEdit() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(testPerson, GeneralPermissions.ADMINISTER_GROUP)));
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, false, users, new ArrayList<Resource>(), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), collection1.getId());

        evictCache();
        assertTrue(authenticationAndAuthorizationService.canEditCollection(testPerson, collection1));
        assertTrue(authenticationAndAuthorizationService.canEditCollection(testPerson, collection2));
    }

    @Test
    @Rollback
    public void testResourceCollectionParentCollectionsFoundProperly() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a2@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        Document generateInformationResourceWithFile = generateDocumentWithUser();
        Document generateInformationResourceWithFile2 = generateDocumentWithUser();

        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD), new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        // use case 1 -- use owner
        ResourceCollection collectionWithUserAsOwner = generateResourceCollection(name, description, CollectionType.SHARED, true, null, getBasicUser(),
                resources, null);
        // use case 2 -- use administrator
        ResourceCollection collectionWithUserAsAdministrator = generateResourceCollection(name, description, CollectionType.SHARED, true, users,
                getAdminUser(), resources, null);

        evictCache();

        controller = generateNewInitializedController(CollectionController.class, getBasicUser());
        controller.prepare();
        controller.add();
        logger.info("{}", controller.getCandidateParentResourceCollections());
        assertTrue(controller.getCandidateParentResourceCollections().contains(collectionWithUserAsOwner));
        assertTrue(controller.getCandidateParentResourceCollections().contains(collectionWithUserAsAdministrator));
    }

    @Test
    @Rollback
    public void testDocumentControllerAssigningResourceCollections() throws Exception {
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, false, null, new ArrayList<Resource>(), null);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = controller.getDocument();
        document.setTitle("test");
        document.setDescription("test");
        document.setDate(1234);
        ResourceCollection fakeIncoming = new ResourceCollection(CollectionType.SHARED);
        fakeIncoming.setName(collection1.getName());
        fakeIncoming.setId(collection1.getId());
        controller.setServletRequest(getServletPostRequest());
        controller.getResourceCollections().add(fakeIncoming);
        assertEquals(Action.SUCCESS, controller.save());
        ResourceCollection first = document.getResourceCollections().iterator().next();
        assertEquals(1, document.getResourceCollections().size());
        assertEquals(collection1, first);
        assertEquals(getUser(), first.getOwner());
        assertEquals(1, first.getResources().size());
    }

    @Test
    @Rollback
    public void testDocumentControllerAssigningResourceCollectionsWithLocalRights() throws Exception {
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, true, null, new ArrayList<Resource>(), null);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = controller.getDocument();
        document.setTitle("test");
        document.setDescription("test");
        document.setDate(1234);
        controller.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), GeneralPermissions.VIEW_ALL));
        controller.getResourceCollections().add(collection1);
        controller.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, controller.save());

        assertEquals(2, document.getResourceCollections().size());
        assertTrue(document.getResourceCollections().contains(collection1));
        assertEquals(1, collection1.getResources().size());
        searchIndexService.index(document);
        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setId(collection1.getId());
        vc.setSlug(collection1.getSlug());
        vc.prepare();
        assertEquals(Action.SUCCESS, vc.view());
        logger.info("results: {}", vc.getResults());
        assertTrue(vc.getResults().contains(document));
    }

    @Test
    @Rollback
    public void testResourceCollectionDraftDisplay() throws Exception {
        Document draftDocument = generateDocumentWithUser();
        Document activeDocument = generateDocumentWithUser();
        draftDocument.setStatus(Status.DRAFT);
        genericService.save(draftDocument);
        evictCache();
        ResourceCollection collection = generateResourceCollection("test collection w/Draft", "testing draft...", CollectionType.SHARED, true, null,
                Arrays.asList(draftDocument, activeDocument), null);
        collection.setOwner(getAdminUser());
        logger.info("DOCUMENT: {} ", draftDocument.getSubmitter());

        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setId(collection.getId());
        vc.setSlug(collection.getSlug());
        vc.prepare();
        logger.info(vc.view());
        assertTrue(vc.getResults().contains(draftDocument));
        assertTrue(vc.getResults().contains(activeDocument));

        vc = generateNewController(CollectionViewAction.class);
        initAnonymousUser(vc);
        vc.setId(collection.getId());
        vc.setSlug(collection.getSlug());
        vc.prepare();
        vc.view();
        assertFalse(vc.getResults().contains(draftDocument));
        assertTrue(vc.getResults().contains(activeDocument));
    }

    @Test
    @Rollback
    public void testSharedResourceCollectionQuery() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        List<AuthorizedUser> authList = new ArrayList<>(Arrays.asList(new AuthorizedUser(testPerson, GeneralPermissions.VIEW_ALL)));

        ResourceCollection collection = generateResourceCollection("test collection w/Draft", "testing draft...", CollectionType.SHARED, true,
                authList, null, null);
        collection.setOwner(getAdminUser());
        List<ResourceCollection> findAccessibleResourceCollections = entityService.findAccessibleResourceCollections(testPerson);
        assertTrue(findAccessibleResourceCollections.contains(collection));
    }

    @Test
    @Rollback
    public void testRemoveResourceCollectionButMaintainSome() throws Exception {
        Document doc = generateDocumentWithUser();
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(doc.getSubmitter(), GeneralPermissions.ADMINISTER_GROUP)));
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, false, users, Arrays.asList(doc), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(),
                Arrays.asList(doc), collection1.getId());

        ResourceCollection fake = new ResourceCollection(CollectionType.SHARED);
        fake.setId(collection2.getId());
        fake.setName(collection2.getName());

        DocumentController docController = generateNewInitializedController(DocumentController.class);
        init(docController, (TdarUser) doc.getSubmitter());
        docController.setId(doc.getId());
        docController.prepare();
        docController.edit();
        docController.getResourceCollections().clear();
        docController.getResourceCollections().add(fake);
        docController.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, docController.save());
        evictCache();

    }

    @Test
    @Rollback
    public void testFullUser() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.MODIFY_RECORD);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(datasetId);
        controller.prepare();
        controller.edit();

        addAuthorizedUser(controller.getDataset(), getUser(), GeneralPermissions.MODIFY_RECORD);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(1, dataset.getInternalResourceCollection().getAuthorizedUsers().size());
        assertEquals(getAdminUserId(), dataset.getInternalResourceCollection().getAuthorizedUsers().iterator().next().getUser().getId());
    }

    @Test
    @Rollback
    public void testReadUser() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(datasetId);
        controller.prepare();
        controller.edit();
        assertEquals(1, controller.getAuthorizedUsers().size());
        ArrayList<AuthorizedUser> authorizedUsers = new ArrayList<>();
        authorizedUsers.add(new AuthorizedUser(getBasicUser(), GeneralPermissions.VIEW_ALL));
        authorizedUsers.add(new AuthorizedUser(getAdminUser(), GeneralPermissions.VIEW_ALL));
        controller.setAuthorizedUsers(authorizedUsers);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        dataset = datasetService.find(datasetId);
        ResourceCollection internalResourceCollection = dataset.getInternalResourceCollection();
        assertEquals(2, internalResourceCollection.getAuthorizedUsers().size());
        Set<Long> seen = new HashSet<>();
        for (AuthorizedUser r : internalResourceCollection.getAuthorizedUsers()) {
            seen.add(r.getUser().getId());
        }
        // FIXME: this fails but clearly, above it works
        // assertTrue(internalResourceCollection.getResources().contains(dataset));
        seen.remove(getUserId());
        seen.remove(getAdminUserId());
        assertTrue("should have seen all user ids already", seen.isEmpty());
    }

    @Test
    @Rollback
    public void testReadUserEmpty() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(datasetId);
        controller.prepare();
        controller.edit();
        controller.setAuthorizedUsers(Collections.<AuthorizedUser> emptyList());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(0, dataset.getInternalResourceCollection().getAuthorizedUsers().size());
    }

    @Test
    @Rollback
    public void testReadUserChangeRights() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(datasetId);
        controller.prepare();
        controller.edit();
        controller.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_METADATA));
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(1, dataset.getInternalResourceCollection().getAuthorizedUsers().size());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testControllerWithActiveResourceThatBecomesDeleted() throws Exception {
        controller = generateNewInitializedController(CollectionController.class, getUser());
        controller.prepare();
        ResourceCollection rc = controller.getPersistable();
        Project project = createAndSaveNewResource(Project.class, getUser(), "test project");
        // not 100% sure why we're using a proxy here, but technically, I think this i closer to what we do in real life
        // Project proxy = new Project(project.getId(), project.getTitle());
        Long pid = project.getId();
        project = null;
        controller.setAuthorizedUsers(Collections.<AuthorizedUser> emptyList());
        controller.getToAdd().add(pid);
        controller.getPersistable().setName("testControllerWithActiveResourceThatBecomesDeleted");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        String result = controller.save();
        Assert.assertEquals(Action.SUCCESS, result);
        Long rcid = rc.getId();
        String slug = rc.getSlug();

        // so, wait, is this resource actually in the collection?
        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        vc.view();
        assertEquals("okay, we should have one resource in this collection now", 1, vc.getResults().size());
        project = genericService.find(Project.class, pid);
        project.getResourceCollections().add(rc);
        genericService.saveOrUpdate(project);
        evictCache();
        // okay now lets delete the resource
        ResourceDeleteAction resourceDeleteAction = generateNewInitializedController(ResourceDeleteAction.class);
        resourceDeleteAction.setServletRequest(getServletPostRequest());
        resourceDeleteAction.setId(pid);
        resourceDeleteAction.prepare();
        resourceDeleteAction.setDelete(TdarActionSupport.DELETE);
        resourceDeleteAction.setAsync(false);
        resourceDeleteAction.delete();
        genericService.synchronize();

        // go back to the collection's 'edit' page and make sure that we are not displaying the deleted resource
        vc = generateNewInitializedController(CollectionViewAction.class, getUser());
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        vc.view();
        List<Long> results = PersistableUtils.extractIds(vc.getResults());
        logger.debug("pid: {}  | {}", pid, results);
        Assert.assertFalse("deleted resource should not appear on edit page", results.contains(pid));

        // so far so good. but lets make sure that the resource *is* actually in the collection
        rc = genericService.find(ResourceCollection.class, rcid);
        assertTrue(rc.getResources().contains(project));
        logger.info("{}", resourceDeleteAction.getPersistable().getResourceCollections());
    }

    @Test
    @Rollback(true)
    public void testControllerWithDeletedResourceThatBecomesActive() throws Exception {
        Project project = createAndSaveNewResource(Project.class, getUser(), "test project");
        Long pid = project.getId();
        ResourceCollection collection = generateResourceCollection("test collection with deleted", "test", CollectionType.SHARED, true, null, getUser(),
                Arrays.asList(project), null);
        project = null;
        project = genericService.find(Project.class, pid);
        project.setStatus(Status.DELETED);
        project.getResourceCollections().add(collection);
        genericService.saveOrUpdate(project);
        Long rcid = collection.getId();
        String slug = collection.getSlug();

        searchIndexService.index(collection);
        searchIndexService.index(project);
        collection = null;
        project = null;
        // so, wait, is this resource actually in the collection?

        // undelete the project, then make sure that the collection shows up on the collection view page
        ProjectController projectController = generateNewInitializedController(ProjectController.class, getAdminUser());
        projectController.setId(pid);
        projectController.prepare();
        projectController.edit();
        Project project2 = projectController.getPersistable();
        project2.setStatus(Status.ACTIVE);
        projectController.setServletRequest(getServletPostRequest());
        projectController.setAsync(false);
        projectController.save();
        evictCache();

        searchIndexService.index(project2);

        logger.info("{}", project2.getResourceCollections());
        assertTrue(PersistableUtils.extractIds(project2.getResourceCollections()).contains(rcid));
        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        vc.view();
        logger.info("{}", vc.getResourceCollection().getResources().iterator().next().getStatus());
        assertTrue("collection should show the newly undeleted project", CollectionUtils.isNotEmpty(vc.getResults()));

        // we should also see the newly-undeleted resource on the edit page
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(rcid);
        controller.prepare();
        controller.edit();
        // logger.info("resources:{}", controller.getResources());
        logger.info("?:{}", controller.getResourceCollection().getResources());
        assertTrue("collection should show the newly undeleted project", CollectionUtils.isNotEmpty(controller.getResourceCollection().getResources()));
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testDraftResourceVisibleByAuthuser() throws Exception {
        controller = generateNewInitializedController(CollectionController.class, getUser());
        controller.prepare();
        ResourceCollection rc = controller.getPersistable();
        Project project = createAndSaveNewResource(Project.class, getUser(), "test project");
        project.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(project);
        // project = null;
        // Long pid = project.getId();
        Project proxy = new Project(project.getId(), project.getTitle());
        controller.setAuthorizedUsers(Collections.<AuthorizedUser> emptyList());
        controller.getToAdd().add(proxy.getId());
        controller.getPersistable().setName("testControllerWithActiveResourceThatBecomesDeleted");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        String result = controller.save();
        // searchIndexService.index(proxy);

        Assert.assertEquals(Action.SUCCESS, result);
        Long rcid = rc.getId();
        String slug = rc.getSlug();
        // confirm resource is viewable by author of collection
        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        vc.view();
        assertEquals("collection should have one resource inside", 1, vc.getResults().size());
        vc = null;
        // make sure it draft resource can't be seen by registered user (but not an authuser)
        TdarUser registeredUser = createAndSaveNewPerson("testDraftResourceVisibleByAuthuser", "foo");
        vc = generateNewInitializedController(CollectionViewAction.class, registeredUser);
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        vc.view();
        assertEquals(controller.getAuthenticatedUser(), registeredUser);
        assertTrue("resource should not be viewable", vc.getResults().isEmpty());
        // assertFalse("resource should not be viewable", controller.getResults().get(0).isViewable());

        Long ruid = registeredUser.getId();
        // now make the user an authorizedUser
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(rcid);
        controller.prepare();
        AuthorizedUser authUser = new AuthorizedUser(registeredUser, GeneralPermissions.MODIFY_RECORD);
        List<AuthorizedUser> authList = new ArrayList<>(Arrays.asList(authUser));
        controller.setAuthorizedUsers(authList);
        controller.getToAdd().add(proxy.getId());
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        controller.save();
        evictCache();
        genericService.synchronize();

        // searchIndexService.indexAll();
        // registered user is now authuser of the collection, and should be able to see the resource
        vc = generateNewInitializedController(CollectionViewAction.class, registeredUser);
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        vc.view();
        assertTrue("resource should be viewable", ((Viewable) (vc.getResults().get(0))).isViewable());
        registeredUser = null;
        registeredUser = genericService.find(TdarUser.class, ruid);
        // now make the registeredUser a non-contributor. make sure they can see the resource (TDAR-2028)
        registeredUser.setContributor(false);
        genericService.saveOrUpdate(registeredUser);
        searchIndexService.index(registeredUser);
        vc = generateNewInitializedController(CollectionViewAction.class, registeredUser);
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        vc.view();
        assertTrue("resource should be viewable", ((Viewable) vc.getResults().get(0)).isViewable());
    }

    /**
     * Assert that the sparse resource list returned by findCollectionSparseResources matches the persisted list (for
     * the subset of properties in Resource that we care about)
     */
    @Test
    public void testfindCollectionSparseResources() {

        // For now we rely on the the init-test and any data created by intervening web tests. In this way this test
        // is brittle. A better idea would be to create our own sample data.
        List<ResourceCollection> allCollections = resourceCollectionService.findAll();
        assertThat("sample data set size", allCollections.size(), greaterThan(5));

        for (ResourceCollection collection : allCollections) {
            // get map of persisted resources
            Map<Long, Resource> persistedResourceMap = PersistableUtils.createIdMap(collection.getResources());

            // get list of sparse resources, make sure it has same size & contents as the persisted resource list.
            List<Resource> sparseResources = resourceCollectionService.findCollectionSparseResources(collection.getId());
            assertThat(collection.getResources(), hasSize(sparseResources.size()));

            for (Resource sparseResource : sparseResources) {
                logger.trace("evaluating resource:{}", sparseResource);
                assertThat(persistedResourceMap, hasKey(sparseResource.getId()));

                Resource resource = persistedResourceMap.get(sparseResource.getId());
                assertThat(sparseResource.getTitle(), is(resource.getTitle()));
                assertThat(sparseResource.getResourceType(), is(resource.getResourceType()));
                assertThat(sparseResource.getStatus(), is(resource.getStatus()));
                assertThat(sparseResource.getSubmitter(), is(resource.getSubmitter())); // here we assume ID equality
            }
        }
    }
}

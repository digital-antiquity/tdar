package org.tdar.struts.action.collection;

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
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.Viewable;
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
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.collection.CollectionRightsComparator;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TestResourceCollectionHelper;
import org.tdar.struts.action.browse.BrowseCollectionController;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.project.ProjectController;
import org.tdar.struts.action.resource.ResourceDeleteAction;
import org.tdar.struts.action.resource.ResourceRightsController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Action;

public class ResourceCollectionControllerITCase extends AbstractControllerITCase implements TestResourceCollectionHelper {

    @Autowired
    private GenericService genericService;

    @Autowired
    private EntityService entityService;

    @Autowired
    AuthorizedUserDao authorizedUserDao;

    @Autowired
    private ResourceCollectionService resourceCollectionService;


    static int indexCount = 0;

    @Before
    public void setup() {
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

        InformationResource normal = generateDocumentAndUseDefaultUser();
        InformationResource draft = generateDocumentAndUseDefaultUser();
        final Long normalId = normal.getId();
        final Long draftId = draft.getId();
        draft.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(draft);
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(
                new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE),
                new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        ResourceCollection collection = generateResourceCollection(name, description, false, users, resources, null);

        final Long id = collection.getId();
        String slug = collection.getSlug();
        collection = null;

        ShareCollectionRightsController sc = generateNewInitializedController(ShareCollectionRightsController.class, getAdminUser());
        sc.setId(id);
        sc.prepare();
        sc.edit();
        sc.setServletRequest(getServletPostRequest());
        sc.getProxies().add(new UserRightsProxy( new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.MODIFY_RECORD)));
        sc.setAsync(false);
        sc.save();

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
    public void testLimitedCollectionPermissions() throws Exception {
        String email = "a243@basda.com";
        entityService.delete(entityService.findByEmail(email));

        final TdarUser testPerson = createAndSaveNewPerson(email, "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource normal = generateDocumentAndUseDefaultUser();
        final Long normalId = normal.getId();
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(
                new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.ADMINISTER_SHARE),
                new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.MODIFY_METADATA)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal));
        ResourceCollection collection = generateResourceCollection(name, description, false, users, resources, null);

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
        ResourceCollection resourceCollection = new ResourceCollection();
        resourceCollection.setName("a resource collection");
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        TdarUser owner = new TdarUser("bob", "loblaw", "bobloblaw@tdar.net","bobloblaw123" );
        owner.setContributor(true);

        genericService.saveOrUpdate(owner);
        resourceCollection.setOwner(owner);
        resourceCollection.getAuthorizedUsers().add(new AuthorizedUser(owner, owner, GeneralPermissions.ADMINISTER_SHARE));
        resourceCollection.markUpdated(owner);
        genericService.saveOrUpdate(resourceCollection);

        genericService.saveOrUpdate(resourceCollection);
        for (Document doc : docList) {
            doc.getSharedCollections().add(resourceCollection);
            doc.setSubmitter(owner);
            genericService.saveOrUpdate(doc);
        }
        genericService.saveOrUpdate(resourceCollection);
        Long rcid = resourceCollection.getId();
        resourceCollection = null;
        evictCache();

        // okay, now let's try to remove the resources from the collection via the
        // controller.
        logger.debug("{}", owner);
        ShareCollectionController controller = generateNewController(ShareCollectionController.class);
        init(controller, owner);
        controller.setId(rcid);
        controller.prepare();
        controller.edit();
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
        ResourceCollection resourceCollection = new ResourceCollection();
        ResourceCollection resourceCollectionParent = new ResourceCollection();
        ResourceCollection resourceCollectionChild = new ResourceCollection();
        resourceCollectionChild.setName("child collection");
        resourceCollectionParent.setName("parent collection");
        resourceCollection.setName("a resource collection");
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        TdarUser owner = new TdarUser("bob", "loblaw", "bobloblaw@tdar.net","bobloblaw1234");
        owner.setContributor(true);
        genericService.saveOrUpdate(owner);
        resourceCollection.markUpdated(owner);
        resourceCollectionParent.markUpdated(owner);
        resourceCollectionChild.markUpdated(owner);
        resourceCollection.getAuthorizedUsers().add(new AuthorizedUser(owner, resourceCollection.getOwner(), GeneralPermissions.ADMINISTER_SHARE));
        resourceCollectionParent.getAuthorizedUsers().add(new AuthorizedUser(owner, resourceCollectionParent.getOwner(), GeneralPermissions.ADMINISTER_SHARE));
        resourceCollectionChild.getAuthorizedUsers().add(new AuthorizedUser(owner, resourceCollectionChild.getOwner(), GeneralPermissions.ADMINISTER_SHARE));
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
        assertTrue("resource list should not be empty", !((ResourceCollection)deleteAction.getPersistable()).getResources().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        deleteAction.delete();

        // now load our resource collection again. the resources should be gone.
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        assertFalse("user should not be able to delete collection", resourceCollection == null);

        for (ResourceCollection child : resourceCollectionService.findDirectChildCollections(rcid, null)) {
            ((ResourceCollection) child).setParent(null);
            genericService.saveOrUpdate(child);
        }
        // evictCache();

        deleteAction = generateNewInitializedController(CollectionDeleteAction.class);
        deleteAction.setId(rcid);
        resourceCollection = null;
        init(deleteAction, owner);
        deleteAction.prepare();
        assertNotNull(deleteAction.getPersistable());
        assertTrue("resource list should not be empty", !((ResourceCollection)deleteAction.getPersistable()).getResources().isEmpty());
        // resourceCollection.setParent(parent)
        setHttpServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        deleteAction.delete();
        // evictCache();
        assertEquals(null, deleteAction.getDeleteIssue());
        resourceCollection = null;
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        logger.info("{}", genericService.find(ResourceCollection.class, rcid));
        assertFalse("user should be able to delete collection", resourceCollection == null);
        assertTrue("user should be able to delete collection", resourceCollection.isDeleted());
        resourceCollectionChild = null;
        resourceCollectionParent = null;
        ResourceCollection child = genericService.find(ResourceCollection.class, childId);
        List<ResourceCollection> children = resourceCollectionService.findDirectChildCollections(parentId, null);
        logger.info("child: {}", child.getParent());
        logger.info("children: {}", children);
        assertTrue(child.getParent() == null);
        assertTrue((children == null) || (children.size() == 0));
        // evictCache();

    }

    @Test
    @Rollback(true)
    public void testDeleteResourceCollectionWithUser() throws Exception {
        ResourceCollection resourceCollection = new ResourceCollection();
        resourceCollection.setName("a resource collection");
        // resourceCollection.setSortBy(SortOption.DATE);
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        TdarUser owner = new TdarUser("bob", "loblaw", "bobloblaw@tdar.net");
        owner.setContributor(true);
        genericService.saveOrUpdate(owner);
        resourceCollection.markUpdated(owner);
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(),owner, GeneralPermissions.MODIFY_RECORD);
        resourceCollection.getAuthorizedUsers().add(new AuthorizedUser(owner, resourceCollection.getOwner(), GeneralPermissions.ADMINISTER_SHARE));

        resourceCollection.getAuthorizedUsers().addAll(Arrays.asList(authorizedUser));
        genericService.saveOrUpdate(resourceCollection);
        // evictCache();

        // okay, now let's try to remove the resources from the collection via the
        // controller.
        Long rcid = resourceCollection.getId();
        CollectionDeleteAction deleteAction = generateNewController(CollectionDeleteAction.class);
        deleteAction.setId(rcid);
        resourceCollection = null;
        init(deleteAction, owner);
        deleteAction.prepare();
        assertNotNull(deleteAction.getPersistable());
        assertTrue("resource list should not be empty", !((ResourceCollection)deleteAction.getPersistable()).getResources().isEmpty());
        assertTrue("user list should not be empty", !deleteAction.getPersistable().getAuthorizedUsers().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        deleteAction.delete();

        // now load our resource collection again. the resources should be gone.
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        assertFalse("user should not be able to delete collection", resourceCollection.isDeleted());

        deleteAction = generateNewController(CollectionDeleteAction.class);
        deleteAction.setId(rcid);
        resourceCollection = null;
        init(deleteAction, owner);
        deleteAction.prepare();
        assertNotNull(deleteAction.getPersistable());
        assertTrue("resource list should not be empty", !((ResourceCollection)deleteAction.getPersistable()).getResources().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        deleteAction.delete();
        // evictCache();
        assertEquals(null, deleteAction.getDeleteIssue());
        resourceCollection = null;
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        logger.info("{}", genericService.find(ResourceCollection.class, rcid));
        assertTrue("user should be able to delete collection", resourceCollection.isDeleted());
        // evictCache();
    }

    @Test
    @Rollback
    public void testSaveAndDeleteWithRedundantAccessRights() throws TdarActionException {
        ShareCollectionController controller = generateNewInitializedController(ShareCollectionController.class,getUser());

        controller.prepare();
        controller.add();

        ResourceCollection rc = controller.getResourceCollection();
        rc.setName("test delete w/ redundant rights");
        rc.setDescription("a tragedy in three acts");
        rc.setHidden(false);
        // rc.setSortBy(SortOption.ID);
        // rc.setOrientation(DisplayOrientation.LIST);

        controller.setServletRequest(getServletPostRequest());
        controller.save();
        
        Long id = rc.getId();

        ShareCollectionRightsController sc = generateNewInitializedController(ShareCollectionRightsController.class);
        sc.setId(id);
        sc.prepare();
        sc.edit();
        // Add three authusers. two of the authusers are redundant and should be normalized to the user with
        // the best permissions.
        AuthorizedUser user1Viewer = createAuthUser(GeneralPermissions.VIEW_ALL);
        AuthorizedUser user1Modifier = new AuthorizedUser(getAdminUser(),user1Viewer.getUser(), GeneralPermissions.MODIFY_METADATA);
        AuthorizedUser user2 = createAuthUser(GeneralPermissions.ADMINISTER_SHARE);
        sc.getProxies().addAll(Arrays.asList(new UserRightsProxy( user1Viewer), new UserRightsProxy( user1Modifier), new UserRightsProxy( user2)));
        sc.setServletRequest(getServletPostRequest());
        sc.save();


        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setSessionData(getSessionData());
        vc.setId(id);
        vc.prepare();
        vc.view();

        ResourceCollection rc2 = vc.getResourceCollection();
        assertEquals(rc.getName(), rc2.getName());
        assertEquals("3 redundant authusers should have been normalized", 3, rc2.getAuthorizedUsers().size());


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
        AuthorizedUser user1Modifier = new AuthorizedUser(getAdminUser(),user1Viewer.getUser(), GeneralPermissions.MODIFY_METADATA);
        AuthorizedUser user2 = createAuthUser(GeneralPermissions.ADMINISTER_SHARE);
        List<AuthorizedUser> authusers = new ArrayList<>(Arrays.asList(user1Viewer, user1Modifier, user2));
        int origCount = authusers.size();
        CollectionRightsComparator crc = new CollectionRightsComparator(new HashSet<>(), new HashSet<>());
        Set<AuthorizedUser> au2 = crc.normalizeAuthorizedUsers(authusers);
        int newCount = au2.size();
        assertThat(newCount, lessThan(origCount));
    }

    private AuthorizedUser createAuthUser(GeneralPermissions permissions) {
        String string = UUID.randomUUID().toString();
        TdarUser person = new TdarUser(string, string, string + "@tdar.net");
        person.setContributor(true);
        genericService.saveOrUpdate(person);
        AuthorizedUser authuser = new AuthorizedUser(getAdminUser(),person, permissions);
        return authuser;
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testBrowseControllerVisibleCollections() throws Exception {
        genericService.synchronize();
        logger.debug("------------------------------------------------------------------------------------------------------------------");
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.ADMINISTER_SHARE)));
//        InternalCollection collection1 = new InternalCollection();
//        collection1.markUpdated(getUser());
//        collection1.getAuthorizedUsers().addAll(users);
//        genericService.saveOrUpdate(collection1);
        ResourceCollection collection2 = generateResourceCollection("SHARED", "", false, new ArrayList<>(users), getUser(),
                new ArrayList<Resource>(), null);
        InformationResource testFile = generateDocumentWithUser();
        ResourceCollection parentCollection = generateResourceCollection("PARENT", "", true, new ArrayList<>(users), getUser(),
                Arrays.asList(), null);
        parentCollection.getUnmanagedResources().add(testFile);
        genericService.saveOrUpdate(parentCollection);
        testFile.getUnmanagedResourceCollections().add(parentCollection);
        genericService.saveOrUpdate(testFile);
        Long id = parentCollection.getId();
        ResourceCollection childCollection = generateResourceCollection("CHILD", "", true, new ArrayList<AuthorizedUser>(), getUser(),
                new ArrayList<Resource>(), id);
        ResourceCollection childCollectionHidden = generateResourceCollection("HIDDEN CHILD", "", false, new ArrayList<AuthorizedUser>(), getUser(),
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
        logger.info("{} : {}", testFile, testFile.getRightsBasedResourceCollections());

        controller_.setRecordsPerPage(1000);
        assertEquals(Action.SUCCESS, controller_.browseCollections());
        List<ResourceCollection> collections_ = controller_.getResults();
        for (ResourceCollection result : collections_) {
            if (result != null) {
                logger.debug("{} {} {} {} ", result.getId(), result.isHidden());
            }
            logger.debug("NULL");
        }
        List<Long> collections = PersistableUtils.extractIds(collections_);
        collections_ = null;
        Long childCollectionId = childCollection.getId();
        Long childCollectionHiddenId = childCollectionHidden.getId();
//        Long collection1Id = collection1.getId();
        Long collection2Id = collection2.getId();

        childCollection = null;
        childCollectionHidden = null;
//        collection1 = null;
        collection2 = null;
//        assertFalse(collections.contains(collection1Id));
        assertFalse(collections.contains(collection2Id));
        assertEquals(0, testFile.getRightsBasedResourceCollections().size());
        assertEquals(1, testFile.getUnmanagedResourceCollections().size());
        parentCollection = genericService.find(ResourceCollection.class, id);
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
        assertEquals(CollectionViewAction.SUCCESS, vc.view());
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
        assertEquals(CollectionViewAction.SUCCESS, vc.view());
        collections = PersistableUtils.extractIds(vc.getCollections());
        assertEquals(2, collections.size());
        assertTrue(collections.contains(childCollectionId));
        assertTrue(collections.contains(childCollectionHiddenId));

        logger.info("{}", vc.getActionErrors());

        // evictCache();
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testHiddenParentVisibleChild() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", false, new ArrayList<AuthorizedUser>(), getUser(),
                new ArrayList<Resource>(), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", true, new ArrayList<AuthorizedUser>(), getUser(),
                new ArrayList<Resource>(), collection1.getId());
        evictCache();
        searchIndexService.index(collection1, collection2);

        // logger.debug("1:{} v: {} h:{}", collection1.getId(), collection1.getUsersWhoCanView(), collection1.isHidden());
        // logger.debug("2:{} v: {} h:{}", collection2.getId(), collection2.getUsersWhoCanView(), collection2.isHidden());
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
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.ADMINISTER_SHARE)));
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", false, users, new ArrayList<Resource>(), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", true, new ArrayList<AuthorizedUser>(),
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

        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(
                new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE),
                new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.MODIFY_RECORD), 
                new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        // use case 1 -- use owner
        ResourceCollection collectionWithUserAsOwner = generateResourceCollection(name, description, true, null, getBasicUser(),
                resources, null);
        // use case 2 -- use administrator
        ResourceCollection collectionWithUserAsAdministrator = generateResourceCollection(name, description, true, users,
                getAdminUser(), resources, null);

        evictCache();

        ShareCollectionController controller = generateNewInitializedController(ShareCollectionController.class, getBasicUser());
        controller.prepare();
        controller.add();
        logger.info("{}", controller.getCandidateParentResourceCollections());
        assertTrue(controller.getCandidateParentResourceCollections().contains(collectionWithUserAsOwner));
        assertTrue(controller.getCandidateParentResourceCollections().contains(collectionWithUserAsAdministrator));
    }

    @Test
    @Rollback
    public void testDocumentControllerAssigningResourceCollections() throws Exception {
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", false, null, new ArrayList<Resource>(), null);
        genericService.refresh(collection1);
        assertNotNull(collection1.getOwner());
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = controller.getDocument();
        document.setTitle("test");
        document.setDescription("test");
        document.setDate(1234);
        ResourceCollection fakeIncoming = new ResourceCollection();
        fakeIncoming.setName(collection1.getName());
        fakeIncoming.setId(collection1.getId());
        controller.setServletRequest(getServletPostRequest());
        controller.getShares().add(fakeIncoming);
        assertEquals(Action.SUCCESS, controller.save());
        ResourceCollection first = document.getRightsBasedResourceCollections().iterator().next();
        assertEquals(1, document.getRightsBasedResourceCollections().size());
        assertEquals(collection1, first);
        assertEquals(getUser(), first.getOwner());
        assertEquals(1, first.getResources().size());
    }

    @Test
    @Rollback
    public void testDocumentControllerAssigningResourceCollectionsWithLocalRights() throws Exception {
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", true, null, new ArrayList<Resource>(), null);
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

        Long id = document.getId();
        document = null;
        ResourceRightsController rrc = generateNewInitializedController(ResourceRightsController.class);
        rrc.setId(id);
        rrc.prepare();
        rrc.edit();
        rrc.getProxies().add(new UserRightsProxy( new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.VIEW_ALL)));
        rrc.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, rrc.save());

        evictCache();
        document = genericService.find(Document.class, id);
        logger.debug("RRC: {}", document.getRightsBasedResourceCollections());
//        logger.debug("IC: {}", document.getInternalCollections());
        assertEquals(1, document.getRightsBasedResourceCollections().size());
        assertTrue(document.getRightsBasedResourceCollections().contains(collection1));
        assertEquals(1, collection1.getResources().size());
        searchIndexService.index(document);
        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setId(collection1.getId());
        vc.setSlug(collection1.getSlug());
        vc.prepare();
        assertEquals(CollectionViewAction.SUCCESS, vc.view());
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
        ResourceCollection collection = generateResourceCollection("test collection w/Draft", "testing draft...", true, null, getAdminUser(),
                Arrays.asList(draftDocument, activeDocument), null);

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
        List<AuthorizedUser> authList = new ArrayList<>(Arrays.asList(new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.VIEW_ALL)));

        ResourceCollection collection = generateResourceCollection("test collection w/Draft", "testing draft...", true,
                authList, null, null);
        collection.setOwner(getAdminUser());
        List<ResourceCollection> findAccessibleResourceCollections = entityService.findAccessibleResourceCollections(testPerson);
        assertTrue(findAccessibleResourceCollections.contains(collection));
    }

    @Test
    @Rollback
    public void testRemoveResourceCollectionButMaintainSome() throws Exception {
        Document doc = generateDocumentWithUser();
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getAdminUser(),doc.getSubmitter(), GeneralPermissions.ADMINISTER_SHARE)));
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", false, users, Arrays.asList(doc), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", true, new ArrayList<AuthorizedUser>(),
                Arrays.asList(doc), collection1.getId());

        ResourceCollection fake = new ResourceCollection();
        fake.setId(collection2.getId());
        fake.setName(collection2.getName());

        DocumentController docController = generateNewInitializedController(DocumentController.class);
        init(docController, (TdarUser) doc.getSubmitter());
        docController.setId(doc.getId());
        docController.prepare();
        docController.edit();
        docController.getShares().clear();
        docController.getShares().add(fake);
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

        ResourceRightsController controller = generateNewInitializedController(ResourceRightsController.class);
        controller.setId(datasetId);
        controller.prepare();
        controller.edit();
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getUser(), getUser(), GeneralPermissions.MODIFY_RECORD)));
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(2, dataset.getAuthorizedUsers().size());
        AuthorizedUser admin  = null;
        for ( AuthorizedUser au : dataset.getAuthorizedUsers()) {
            if (au.getUser().getId().equals(getAdminUserId())) {
                admin = au;
            }
        }
        assertNotNull(admin);
    }

    @Test
    @Rollback
    public void testReadUser() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        ResourceRightsController controller = generateNewInitializedController(ResourceRightsController.class);
        controller.setId(datasetId);
        controller.prepare();
        controller.edit();
        assertEquals(2, controller.getProxies().size());
        ArrayList<UserRightsProxy> authorizedUsers = new ArrayList<>();
        authorizedUsers.add(new UserRightsProxy(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.VIEW_ALL)));
        authorizedUsers.add(new UserRightsProxy(new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.VIEW_ALL)));
        controller.setProxies(authorizedUsers);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        genericService.synchronize();
        dataset = datasetService.find(datasetId);
//        ResourceCollection internalResourceCollection = dataset.getInternalResourceCollection();
//        logger.debug("{}", dataset.getInternalResourceCollection().getAuthorizedUsers());
        assertEquals(2, dataset.getAuthorizedUsers().size());
        Set<Long> seen = new HashSet<>();
        for (AuthorizedUser r : dataset.getAuthorizedUsers()) {
            seen.add(r.getUser().getId());
        }
        // FIXME: this fails but clearly, above it works
        // assertTrue(internalResourceCollection.getResources().contains(dataset));
        seen.remove(getUserId());
        seen.remove(getAdminUserId());
        assertTrue("should have seen all user ids already", seen.isEmpty());
    }

    @Test
    @Rollback(false)
    public void testReadUserEmpty() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        evictCache();
         setVerifyTransactionCallback(new TransactionCallback<Resource>() {

            @Override
            public Resource doInTransaction(TransactionStatus arg0) {
                ResourceRightsController controller = generateNewInitializedController(ResourceRightsController.class);
                controller.setId(datasetId);
                try {
                    controller.prepare();
                    controller.edit();
                    controller.setProxies(Collections.<UserRightsProxy> emptyList());
                    controller.setServletRequest(getServletPostRequest());
                    controller.save();
                    evictCache();
                    Dataset ds = datasetService.find(datasetId);
                    assertEquals(0, ds.getAuthorizedUsers().size());
                    genericService.forceDelete(ds);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }
         });
    }

    @Test
    @Rollback
    public void testReadUserChangeRights() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        ResourceRightsController controller = generateNewInitializedController(ResourceRightsController.class);
        controller.setId(datasetId);
        controller.prepare();
        controller.edit();
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.MODIFY_METADATA)));
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        genericService.synchronize();
        dataset = datasetService.find(datasetId);
        logger.debug("au: {}", dataset.getAuthorizedUsers());
        assertEquals(2, dataset.getAuthorizedUsers().size());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testControllerWithActiveResourceThatBecomesDeleted() throws Exception {
        ShareCollectionController controller = generateNewInitializedController(ShareCollectionController.class, getUser());
        controller.prepare();
        ResourceCollection rc = controller.getPersistable();
        Project project = createAndSaveNewResource(Project.class, getUser(), "test project");
        // not 100% sure why we're using a proxy here, but technically, I think this i closer to what we do in real life
        // Project proxy = new Project(project.getId(), project.getTitle());
        Long pid = project.getId();
        project = null;
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
        project.getSharedCollections().add(rc);
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
        logger.info("{}", resourceDeleteAction.getPersistable().getRightsBasedResourceCollections());
    }

    @Test
    @Rollback(true)
    public void testControllerWithDeletedResourceThatBecomesActive() throws Exception {
        Project project = createAndSaveNewResource(Project.class, getUser(), "test project");
        Long pid = project.getId();
        ResourceCollection collection = generateResourceCollection("test collection with deleted", "test", true, null, getUser(),
                Arrays.asList(project), null);
        project = null;
        project = genericService.find(Project.class, pid);
        project.setStatus(Status.DELETED);
        project.getSharedCollections().add(collection);
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
        logger.debug("shares: {}", projectController.getShares());
        Project project2 = projectController.getPersistable();
        project2.setStatus(Status.ACTIVE);
        projectController.setServletRequest(getServletPostRequest());
        projectController.setAsync(false);
        projectController.save();
        evictCache();

        searchIndexService.index(project2);

        logger.info("{}", project2.getRightsBasedResourceCollections());
        assertTrue(PersistableUtils.extractIds(project2.getRightsBasedResourceCollections()).contains(rcid));
        CollectionViewAction<ResourceCollection> vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        vc.view();
        logger.info("{}", ((ResourceCollection) vc.getResourceCollection()).getResources().iterator().next().getStatus());
        assertTrue("collection should show the newly undeleted project", CollectionUtils.isNotEmpty(vc.getResults()));

        // we should also see the newly-undeleted resource on the edit page
        ShareCollectionController controller = generateNewInitializedController(ShareCollectionController.class);
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
        ShareCollectionController shareController = generateNewInitializedController(ShareCollectionController.class, getUser());
        shareController.prepare();
        ResourceCollection rc = shareController.getPersistable();
        Project project = createAndSaveNewResource(Project.class, getUser(), "test project");
        project.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(project);
        // project = null;
        // Long pid = project.getId();
        Project proxy = new Project(project.getId(), project.getTitle());
        shareController.getToAdd().add(proxy.getId());
        shareController.getPersistable().setName("testControllerWithActiveResourceThatBecomesDeleted");
        shareController.getPersistable().setDescription("description");
        shareController.getPersistable().setHidden(true);
        shareController.setServletRequest(getServletPostRequest());
        shareController.setAsync(false);
        String result = shareController.save();
        // searchIndexService.index(proxy);

        Assert.assertEquals(Action.SUCCESS, result);
        Long rcid = rc.getId();
        String slug = rc.getSlug();
        // confirm resource is viewable by author of collection
        CollectionViewAction vc = generateNewInitializedController(CollectionViewAction.class);
        vc.setId(rcid);
        vc.setSlug(slug);
        vc.prepare();
        logger.debug("hidden:? {} ", vc.getResourceCollection().isHidden());
        vc.view();
        assertEquals("collection should have one resource inside", 1, vc.getResults().size());
        vc = null;

        setIgnoreActionErrors(true);
        // make sure it draft resource can't be seen by registered user (but not an authuser)
        TdarUser registeredUser = createAndSaveNewPerson("testDraftResourceVisibleByAuthuser", "foo");
        boolean expectingError = false;
        try {
            vc = generateNewInitializedController(CollectionViewAction.class, registeredUser);
            vc.setId(rcid);
            vc.setSlug(slug);
            vc.prepare();
            vc.view();
            assertEquals(vc.getAuthenticatedUser(), registeredUser);
            assertTrue("resource should not be viewable", vc.getResults().isEmpty());
            // assertFalse("resource should not be viewable", controller.getResults().get(0).isViewable());
        } catch (TdarActionException tae) {
            expectingError = true;
        }
        assertTrue("should see tDAR Action Exception", expectingError);
        setIgnoreActionErrors(false);

        Long ruid = registeredUser.getId();
        
        
        
        ShareCollectionController controller = generateNewInitializedController(ShareCollectionController.class);
        controller.setId(rcid);
        controller.prepare();
        controller.getToAdd().add(proxy.getId());
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        controller.save();
        
        // now make the user an authorizedUser
        ShareCollectionRightsController scc = generateNewInitializedController(ShareCollectionRightsController.class);
        scc.setId(rcid);
        scc.prepare();
        AuthorizedUser authUser = new AuthorizedUser(getAdminUser(),registeredUser, GeneralPermissions.MODIFY_RECORD);
        List<UserRightsProxy> authList = new ArrayList<>(Arrays.asList(new UserRightsProxy( authUser)));
        scc.setProxies(authList);
        scc.setServletRequest(getServletPostRequest());
        scc.setAsync(false);
        scc.save();
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
        List<ResourceCollection> allCollections = genericService.findAll(ResourceCollection.class);
        assertThat("sample data set size", allCollections.size(), greaterThan(1));

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
                assertThat(StringUtils.trim(sparseResource.getTitle()), is(StringUtils.trim(resource.getTitle())));
                assertThat(sparseResource.getResourceType(), is(resource.getResourceType()));
                assertThat(sparseResource.getStatus(), is(resource.getStatus()));
                assertThat(sparseResource.getSubmitter(), is(resource.getSubmitter())); // here we assume ID equality
            }
        }
    }
}

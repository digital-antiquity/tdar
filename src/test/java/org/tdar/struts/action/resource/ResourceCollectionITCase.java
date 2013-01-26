package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.CollectionController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.search.BrowseController;

public class ResourceCollectionITCase extends AbstractResourceControllerITCase
{

    @Autowired
    private GenericService genericService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    CollectionController controller;

    @Before
    public void setup()
    {
        controller = generateNewInitializedController(CollectionController.class);
    }

    @Test
    @Rollback
    public void testResourceCollectionController() throws Exception
    {
        Person testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource generateInformationResourceWithFile = generateDocumentWithUser();
        InformationResource generateInformationResourceWithFile2 = generateDocumentWithUser();
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD), new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        ResourceCollection collection = generateResourceCollection(name, description, CollectionType.SHARED, true, users, resources, null);
        Long collectionid = collection.getId();
        logger.info("{}", collection.getResources());
        assertFalse(collectionid.equals(-1L));
        collection = null;
        ResourceCollection foundCollection = genericService.find(ResourceCollection.class, collectionid);
        assertNotNull(foundCollection);
        assertEquals(3, foundCollection.getAuthorizedUsers().size());
        assertEquals(2, foundCollection.getResources().size());

        assertEquals(name, foundCollection.getName());
        assertEquals(description, foundCollection.getDescription());
        assertEquals(CollectionType.SHARED, foundCollection.getType());
        assertEquals(SortOption.RESOURCE_TYPE, foundCollection.getSortBy());

        assertTrue(foundCollection.getResources().contains(generateInformationResourceWithFile2));
        assertTrue(foundCollection.getResources().contains(generateInformationResourceWithFile));

        int count = 0;
        for (AuthorizedUser user : foundCollection.getAuthorizedUsers())
        {
            if (user.getUser().equals(testPerson))
            {
                count++;
                assertEquals(GeneralPermissions.MODIFY_RECORD, user.getGeneralPermission());
            }
            if (user.getUser().equals(getAdminUser()))
            {
                count++;
                assertEquals(GeneralPermissions.MODIFY_RECORD, user.getGeneralPermission());
            }
            if (user.getUser().equals(getBasicUser()))
            {
                count++;
                assertEquals(GeneralPermissions.ADMINISTER_GROUP, user.getGeneralPermission());
            }
        }
        assertEquals(3, count);
    }

    @Test
    @Rollback
    public void testResourceCollectionPermissionsController() throws Exception
    {
        Person testPerson = createAndSaveNewPerson("a2@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource generateInformationResourceWithFile = generateDocumentWithUser();
        InformationResource generateInformationResourceWithFile2 = generateDocumentWithUser();

        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD), new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        ResourceCollection collection = generateResourceCollection(name, description, CollectionType.SHARED, false, users, resources, null);
        Long id = collection.getId();
        collection = null;
        assertFalse(id.equals(-1L));

        ResourceCollection foundCollection = genericService.find(ResourceCollection.class, id);

        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile));
        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile2));
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), generateInformationResourceWithFile2));

        assertTrue(authenticationAndAuthorizationService.canEditCollection(getBasicUser(), foundCollection));
        assertFalse(authenticationAndAuthorizationService.canEditCollection(testPerson, foundCollection));
    }

    @Test
    @Rollback(true)
    public void testDraftResourceIssue() throws Exception
    {
        String email = "a243@basda.com";
        entityService.delete(entityService.findByEmail(email));

        final Person testPerson = createAndSaveNewPerson(email, "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource normal = generateInformationResourceWithFileAndUser();
        InformationResource draft = generateInformationResourceWithFileAndUser();
        final Long normalId = normal.getId();
        final Long draftId = draft.getId();
        draft.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(draft);
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        ResourceCollection collection = generateResourceCollection(name, description, CollectionType.SHARED, false, users, resources, null);

        final Long id = collection.getId();
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

        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, normal));
        assertTrue(authenticationAndAuthorizationService.canEditResource(testPerson, draft));
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), draft));
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), normal));

        assertTrue(authenticationAndAuthorizationService.canViewResource(getBasicUser(), draft));
        assertTrue(authenticationAndAuthorizationService.canViewResource(getBasicUser(), normal));

        controller = generateNewInitializedController(CollectionController.class, testPerson);
        controller.setId(id);
        controller.prepare();
        try {
            controller.view();
        } catch (TdarActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.info("results: {} ", controller.getResults());
        logger.info("results: {} ", controller.getResources());
        assertTrue(controller.getResults().contains(normal));
        assertTrue(controller.getResults().contains(draft));
        entityService.delete(controller.getResourceCollection().getAuthorizedUsers());
        // return null;
        // }
        //
        // });
    }

    @Test
    @Rollback
    public void testResourceCollectionPermissionsWithDepthController() throws Exception
    {
        Person testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource generateInformationResourceWithFile = generateDocumentWithUser();

        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD), new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile));
        ResourceCollection collection = generateResourceCollection(name, description, CollectionType.SHARED, false, users, null, null);
        ResourceCollection collection2 = generateResourceCollection(name, description, CollectionType.SHARED, false, null, resources, collection.getId());

        logger.info("{}", generateInformationResourceWithFile);
        assertTrue("user can edit based on parent of parent resource collection",
                authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile));
        collection.getAuthorizedUsers().clear();
        genericService.save(collection);
        assertFalse("user can no longer edit", authenticationAndAuthorizationService.canEditResource(testPerson, generateInformationResourceWithFile));
    }

    @Override
    protected TdarActionSupport getController()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Test
    @Rollback
    public void testRemoveResources() throws Exception
    {
        ResourceCollection resourceCollection = new ResourceCollection();
        resourceCollection.setName("a resource collection");
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<Document>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        Person owner = new Person("bob", "loblaw", "bobloblaw@mailinator.com");
        owner.setRegistered(true);

        genericService.saveOrUpdate(owner);
        resourceCollection.setOwner(owner);
        genericService.saveOrUpdate(resourceCollection);
        genericService.synchronize();

        // okay, now let's try to remove the resources from the collection via the
        // controller.
        Long rcid = resourceCollection.getId();
        controller.setId(rcid);
        resourceCollection = null;
        init(controller, owner);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        assertNotNull(controller.getPersistable());
        assertTrue("resource list should not be empty", !controller.getPersistable().getResources().isEmpty());

        // clear the list of incoming resources, then save
        controller.getResources().clear(); // strictly speaking this line is not
                                           // necessary.
        controller.save();

        genericService.synchronize();

        // now load our resource collection again. the resources should be gone.
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        assertEquals("resource list should be empty", 0, resourceCollection.getResources().size());
    }

    @Test
    @Rollback
    public void testDeleteResourceCollection() throws Exception
    {
        ResourceCollection resourceCollection = new ResourceCollection(CollectionType.SHARED);
        ResourceCollection resourceCollectionParent = new ResourceCollection(CollectionType.SHARED);
        ResourceCollection resourceCollectionChild = new ResourceCollection(CollectionType.SHARED);
        resourceCollectionChild.setName("child collection");
        resourceCollectionParent.setName("parent collection");
        resourceCollection.setName("a resource collection");
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<Document>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        Person owner = new Person("bob", "loblaw", "bobloblaw@mailinator.com");
        owner.setRegistered(true);
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
        genericService.synchronize();

        Long parentId = resourceCollectionParent.getId();
        Long childId = resourceCollectionChild.getId();

        // okay, now let's try to remove the resources from the collection via the
        // controller.
        Long rcid = resourceCollection.getId();
        controller = generateNewController(CollectionController.class);
        controller.setId(rcid);
        resourceCollection = null;
        init(controller, owner);
        controller.prepare();
        assertNotNull(controller.getPersistable());
        assertTrue("resource list should not be empty", !controller.getPersistable().getResources().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        controller.setDelete(AbstractPersistableController.DELETE_CONSTANT);
        controller.delete();

        // now load our resource collection again. the resources should be gone.
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        assertFalse("user should not be able to delete collection", resourceCollection == null);

        for (ResourceCollection child : resourceCollectionService.findAllDirectChildCollections(rcid, null, CollectionType.SHARED))
        {
            child.setParent(null);
            genericService.saveOrUpdate(child);
        }
        genericService.synchronize();

        controller = generateNewController(CollectionController.class);
        controller.setId(rcid);
        resourceCollection = null;
        controller.prepare();
        init(controller, owner);
        assertNotNull(controller.getPersistable());
        assertTrue("resource list should not be empty", !controller.getPersistable().getResources().isEmpty());
        // resourceCollection.setParent(parent)
        setHttpServletRequest(getServletPostRequest());
        controller.setDelete(AbstractPersistableController.DELETE_CONSTANT);
        controller.delete();
        genericService.synchronize();
        assertEquals(0, controller.getDeleteIssues().size());
        resourceCollection = null;
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        logger.info("{}", genericService.find(ResourceCollection.class, rcid));
        assertTrue("user should be able to delete collection", resourceCollection == null);
        resourceCollectionChild = null;
        resourceCollectionParent = null;
        ResourceCollection child = genericService.find(ResourceCollection.class, childId);
        List<ResourceCollection> children = resourceCollectionService.findAllDirectChildCollections(parentId, null, CollectionType.SHARED);
        logger.info("child: {}", child.getParent());
        logger.info("children: {}", children);
        assertTrue(child.getParent() == null);
        assertTrue(children == null || children.size() == 0);
        genericService.synchronize();

    }

    @Test
    @Rollback
    public void testDeleteResourceCollectionWithUser() throws Exception
    {
        ResourceCollection resourceCollection = new ResourceCollection(CollectionType.SHARED);
        resourceCollection.setName("a resource collection");
        resourceCollection.setSortBy(SortOption.DATE);
        resourceCollection.setDescription("testing add then remove resources");
        List<Document> docList = new ArrayList<Document>();
        docList.add(createAndSaveNewInformationResource(Document.class));
        resourceCollection.getResources().addAll(docList);
        resourceCollection.setDateCreated(new Date());
        Person owner = new Person("bob", "loblaw", "bobloblaw@mailinator.com");
        owner.setRegistered(true);
        genericService.saveOrUpdate(owner);
        resourceCollection.markUpdated(owner);
        AuthorizedUser authorizedUser = new AuthorizedUser(owner, GeneralPermissions.VIEW_ALL);
        resourceCollection.getAuthorizedUsers().addAll(Arrays.asList(authorizedUser));
        
        genericService.saveOrUpdate(resourceCollection);
        genericService.synchronize();

        // okay, now let's try to remove the resources from the collection via the
        // controller.
        Long rcid = resourceCollection.getId();
        controller = generateNewController(CollectionController.class);
        controller.setId(rcid);
        resourceCollection = null;
        init(controller, owner);
        controller.prepare();
        assertNotNull(controller.getPersistable());
        assertTrue("resource list should not be empty", !controller.getPersistable().getResources().isEmpty());
        assertTrue("user list should not be empty", !controller.getPersistable().getAuthorizedUsers().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        controller.setDelete(AbstractPersistableController.DELETE_CONSTANT);
        controller.delete();

        // now load our resource collection again. the resources should be gone.
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        assertFalse("user should not be able to delete collection", resourceCollection == null);

        controller = generateNewController(CollectionController.class);
        controller.setId(rcid);
        resourceCollection = null;
        init(controller, owner);
        controller.prepare();
        assertNotNull(controller.getPersistable());
        assertTrue("resource list should not be empty", !controller.getPersistable().getResources().isEmpty());
        setHttpServletRequest(getServletPostRequest());
        controller.setDelete(AbstractPersistableController.DELETE_CONSTANT);
        controller.delete();
        genericService.synchronize();
        assertEquals(0, controller.getDeleteIssues().size());
        resourceCollection = null;
        resourceCollection = genericService.find(ResourceCollection.class, rcid);
        logger.info("{}", genericService.find(ResourceCollection.class, rcid));
        assertTrue("user should be able to delete collection", resourceCollection == null);
        genericService.synchronize();
    }
    
    @Test
    @Rollback
    public void testSaveAndDeleteWithRedundantAccessRights() throws TdarActionException {
        controller.prepare();
        controller.add();

        ResourceCollection rc = controller.getResourceCollection();
        rc.setName("test delete w/ redundant rights");
        rc.setDescription("a tragedy in three acts");
        rc.setVisible(true);
        rc.setSortBy(SortOption.ID);
        rc.setOrientation(DisplayOrientation.LIST);
        
        //add two redundant access rights records (if you have administer_group,  you already have view_all implicitly)
        Person persistedUser = new Person("bob", "loblaw", "bobloblaw@mailinator.com");
        persistedUser.setRegistered(true);
        genericService.saveOrUpdate(persistedUser);
        Person strutsUser1 = new Person();
        Person strutsUser2 = new Person();
        strutsUser1.setId(persistedUser.getId());
        strutsUser2.setId(persistedUser.getId());
        AuthorizedUser bobCopy1 = new AuthorizedUser(strutsUser1, GeneralPermissions.ADMINISTER_GROUP);
        AuthorizedUser bobCopy2 = new AuthorizedUser(strutsUser2, GeneralPermissions.VIEW_ALL);
        controller.getAuthorizedUsers().add(bobCopy1);
        controller.getAuthorizedUsers().add(bobCopy2);

        controller.setServletRequest(getServletPostRequest());
        controller.save();
        
        Long id = rc.getId();
        
        controller = generateNewController(CollectionController.class);
        controller.setSessionData(getSessionData());
        controller.setId(id);
        controller.prepare();
        controller.view();
        
        ResourceCollection rc2 = controller.getResourceCollection();
        assertEquals(rc.getName(), rc2.getName());
        assertEquals("2 redundant authusers should have been normalized to one", 1, rc2.getAuthorizedUsers().size());
        assertEquals("the one remaining authuser should have highest permissions", GeneralPermissions.ADMINISTER_GROUP, rc2.getAuthorizedUsers().iterator().next().getGeneralPermission());
    }
        

    @Test
    @Rollback
    public void testBrowseControllerVisibleCollections() throws Exception
    {
        Person testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(new AuthorizedUser(testPerson, GeneralPermissions.ADMINISTER_GROUP)));
        ResourceCollection collection1 = generateResourceCollection("test 1", "", CollectionType.INTERNAL, false, new ArrayList<AuthorizedUser>(users),
                new ArrayList<Resource>(), null);
        ResourceCollection collection2 = generateResourceCollection("test 2", "", CollectionType.SHARED, false, new ArrayList<AuthorizedUser>(users),
                new ArrayList<Resource>(), null);
        InformationResource testFile = generateDocumentWithUser();
        ResourceCollection parentCollection = generateResourceCollection("test 3", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(users),
                Arrays.asList(testFile), null);
        ResourceCollection childCollection = generateResourceCollection("test 4", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), parentCollection.getId());
        ResourceCollection childCollectionHidden = generateResourceCollection("test 5", "", CollectionType.SHARED, false, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), parentCollection.getId());
        genericService.saveOrUpdate(parentCollection);

        BrowseController controller_ = generateNewInitializedController(BrowseController.class);
        Long fileId = testFile.getId();
        searchIndexService.flushToIndexes();
        searchIndexService.indexAll(Resource.class);
        testFile = null;
        genericService.synchronize();
        // WHY DOES THE SYNCHRONIZE ON THE INDEX CALL DO ANYTHING HERE VS THE
        // SYNCHRONIZE ABOVE
        testFile = genericService.find(Document.class, fileId);
        logger.info("{} : {}", testFile, testFile.getResourceCollections());

        controller_.setRecordsPerPage(1000);
        assertEquals(TdarActionSupport.SUCCESS, controller_.browseCollections());
        List<ResourceCollection> collections = controller_.getResults();
        assertFalse(collections.contains(collection1));
        assertFalse(collections.contains(collection2));
        // FIXME: @ManyToMany directional issue
        // assertEquals(1,parentCollection.getResources().size());
        assertEquals(1, testFile.getResourceCollections().size());
        
        assertTrue(parentCollection.isShared());
        assertTrue(parentCollection.isVisible());
        assertTrue(parentCollection.isTopLevel());

        
        assertTrue(String.format("collections %s should contain %s", collections, parentCollection), collections.contains(parentCollection));
        assertFalse(collections.contains(childCollection));
        assertFalse(collections.contains(childCollectionHidden));
        controller = generateNewController(CollectionController.class);
        // TESTING ANONYMOUS USER
        initAnonymousUserinit(controller);
        controller.setId(parentCollection.getId());
        controller.prepare();
        assertEquals(TdarActionSupport.SUCCESS, controller.view());
        collections = controller.getCollections();
        assertTrue(collections.contains(childCollection));
        assertFalse(collections.contains(childCollectionHidden));
        assertEquals(1, controller.getResults().size());

        // TESTING MORE ADVANCED VIEW RIGHTS
        logger.info("{}", controller.getActionErrors());
        controller = generateNewController(CollectionController.class);
        init(controller, testPerson);
        controller.setId(parentCollection.getId());
        controller.prepare();
        assertEquals(TdarActionSupport.SUCCESS, controller.view());
        collections = controller.getCollections();
        assertEquals(2, collections.size());
        assertTrue(collections.contains(childCollection));
        assertTrue(collections.contains(childCollectionHidden));

        logger.info("{}", controller.getActionErrors());

        // controller =
        // generateNewInitializedController(CollectionController.class);
        // controller.setId(parentCollection.getId());
        // controller.prepare();
        // assertEquals(TdarActionSupport.SUCCESS, controller.view());
        // assertTrue(collections.contains(childCollection));
        // assertTrue(collections.contains(childCollectionHidden));

        genericService.synchronize();
    }

    @Test
    @Rollback
    public void testHiddenParentVisibleChild() throws Exception
    {
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, false, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), collection1.getId());

        searchIndexService.index(collection1, collection2);
        BrowseController browseController = generateNewInitializedController(BrowseController.class);
        browseController.setRecordsPerPage(Integer.MAX_VALUE);
        browseController.browseCollections();
        assertTrue("should see child collection of hidden parent", browseController.getResults().contains(collection2));
        assertFalse("should not see hidden collection", browseController.getResults().contains(collection1));
    }

    @Test
    @Rollback
    public void testNestedCollectionEdit() throws Exception
    {
        Person testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(new AuthorizedUser(testPerson, GeneralPermissions.ADMINISTER_GROUP)));
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, false, users, new ArrayList<Resource>(), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(),
                new ArrayList<Resource>(), collection1.getId());

        genericService.synchronize();
        assertTrue(authenticationAndAuthorizationService.canEditCollection(testPerson, collection1));
        assertTrue(authenticationAndAuthorizationService.canEditCollection(testPerson, collection2));
    }

    @Test
    @Rollback
    public void testResourceCollectionParentCollectionsFoundProperly() throws Exception
    {
        Person testPerson = createAndSaveNewPerson("a2@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        Document generateInformationResourceWithFile = generateDocumentWithUser();
        Document generateInformationResourceWithFile2 = generateDocumentWithUser();

        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD), new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        // use case 1 -- use owner
        ResourceCollection collectionWithUserAsOwner = generateResourceCollection(name, description, CollectionType.SHARED, true, null, getBasicUser(),
                resources, null);
        // use case 2 -- use administrator
        ResourceCollection collectionWithUserAsAdministrator = generateResourceCollection(name, description, CollectionType.SHARED, true, users,
                getAdminUser(), resources, null);

        genericService.synchronize();
        searchIndexService.flushToIndexes();

        controller = generateNewInitializedController(CollectionController.class, getBasicUser());
        controller.prepare();
        controller.add();
        logger.info("{}", controller.getCandidateParentResourceCollections());
        assertTrue(controller.getCandidateParentResourceCollections().contains(collectionWithUserAsOwner));
        assertTrue(controller.getCandidateParentResourceCollections().contains(collectionWithUserAsAdministrator));
    }

    @Test
    @Rollback
    public void testDocumentControllerAssigningResourceCollectionsWithoutLocalRights() throws Exception
    {
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, false, null, new ArrayList<Resource>(), null);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = controller.getDocument();
        document.setTitle("test");
        document.setDescription("test");
        document.setDate(1234);
        controller.getResourceCollections().add(collection1);
        controller.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
        genericService.synchronize();
        ResourceCollection first = document.getResourceCollections().iterator().next();
        assertEquals(1, document.getResourceCollections().size());
        assertEquals(collection1, first);
        assertEquals(getUser(), first.getOwner());
        assertEquals(1, first.getResources().size());
    }

    @Test
    @Rollback
    public void testDocumentControllerAssigningResourceCollections() throws Exception
    {
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
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
        ResourceCollection first = document.getResourceCollections().iterator().next();
        assertEquals(1, document.getResourceCollections().size());
        assertEquals(collection1, first);
        assertEquals(getUser(), first.getOwner());
        assertEquals(1, first.getResources().size());
    }

    @Test
    @Rollback
    public void testDocumentControllerAssigningResourceCollectionsWithLocalRights() throws Exception
    {
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
        assertEquals(TdarActionSupport.SUCCESS, controller.save());

        assertEquals(2, document.getResourceCollections().size());
        assertTrue(document.getResourceCollections().contains(collection1));
        assertEquals(1, collection1.getResources().size());
        searchIndexService.index(document);
        CollectionController controller2 = generateNewInitializedController(CollectionController.class);
        controller2.setId(collection1.getId());
        controller2.prepare();
        assertEquals(TdarActionSupport.SUCCESS, controller2.view());
        logger.info("results: {}", controller2.getResults());
        assertTrue(controller2.getResults().contains(document));
    }

    @Test
    @Rollback
    public void testInvalidRightsAssignment() throws Exception
    {
        Document document = (Document) generateDocumentWithUser();
        document.setSubmitter(getAdminUser());
        genericService.save(document);
        // try and assign access to aa document that user should not have rights
        // to add, assert that this document cannot be added

        CollectionController controller = generateNewController(CollectionController.class);
        init(controller, getBasicUser());
        controller.add();
        ResourceCollection resourceCollection = controller.getResourceCollection();
        resourceCollection.setType(CollectionType.SHARED);
        resourceCollection.setName("tst");
        resourceCollection.setDescription("tst");
        resourceCollection.markUpdated(getBasicUser());
        resourceCollection.setSortBy(SortOption.ID);
        controller.getResources().add(document);
        controller.setServletRequest(getServletPostRequest());
        String result = controller.save();
        assertFalse(result.equals(TdarActionSupport.SUCCESS));
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(resourceCollection.getId());
        assertEquals(0, resourceCollection.getResources().size());
        resourceCollection = null;
        controller.prepare();
        controller.edit();
        assertEquals(0, controller.getResources().size());
        assertEquals(0, controller.getResourceCollection().getResources().size());

    }

    @Test
    @Rollback
    public void testResourceCollectionDraftDisplay() throws Exception
    {
        Document draftDocument = generateDocumentWithUser();
        Document activeDocument = generateDocumentWithUser();
        draftDocument.setStatus(Status.DRAFT);
        genericService.save(draftDocument);
        genericService.synchronize();
        ResourceCollection collection = generateResourceCollection("test collection w/Draft", "testing draft...", CollectionType.SHARED, true, null,
                Arrays.asList(draftDocument, activeDocument), null);
        collection.setOwner(getAdminUser());
        logger.info("DOCUMENT: {} ", draftDocument.getSubmitter());
        searchIndexService.flushToIndexes();
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(collection.getId());
        controller.prepare();
        logger.info(controller.view());
        assertTrue(controller.getResults().contains(draftDocument));
        assertTrue(controller.getResults().contains(activeDocument));

        controller = generateNewController(CollectionController.class);
        initAnonymousUserinit(controller);
        controller.setId(collection.getId());
        controller.prepare();
        controller.view();
        assertFalse(controller.getResults().contains(draftDocument));
        assertTrue(controller.getResults().contains(activeDocument));
    }

    @Test
    @Rollback
    public void testSharedResourceCollectionQuery() throws Exception
    {
        Person testPerson = createAndSaveNewPerson("a@basda.com", "1234");

        ResourceCollection collection = generateResourceCollection("test collection w/Draft", "testing draft...", CollectionType.SHARED, true,
                Arrays.asList(new AuthorizedUser(testPerson, GeneralPermissions.VIEW_ALL)), null, null);
        collection.setOwner(getAdminUser());
        List<ResourceCollection> findAccessibleResourceCollections = entityService.findAccessibleResourceCollections(testPerson);
        assertTrue(findAccessibleResourceCollections.contains(collection));
    }

    @Test
    @Rollback
    public void testRemoveResourceCollectionButMaintainSome() throws Exception
    {
        Document doc = (Document) generateDocumentWithUser();
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(new AuthorizedUser(doc.getSubmitter(), GeneralPermissions.ADMINISTER_GROUP)));
        ResourceCollection collection1 = generateResourceCollection("test 1 private", "", CollectionType.SHARED, false, users, Arrays.asList(doc), null);
        ResourceCollection collection2 = generateResourceCollection("test 2 public", "", CollectionType.SHARED, true, new ArrayList<AuthorizedUser>(),
                Arrays.asList(doc), collection1.getId());

        ResourceCollection fake = new ResourceCollection(CollectionType.SHARED);
        fake.setId(collection2.getId());
        fake.setName(collection2.getName());

        DocumentController docController = generateNewInitializedController(DocumentController.class);
        init(docController, doc.getSubmitter());
        docController.setId(doc.getId());
        docController.prepare();
        docController.edit();
        docController.getResourceCollections().clear();
        docController.getResourceCollections().add(fake);
        docController.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS, docController.save());
        genericService.synchronize();

    }

    @Test
    @Rollback
    public void testFullUser() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class, false);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.MODIFY_RECORD);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, datasetId);
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
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class, false);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, datasetId);
        assertEquals(1, controller.getAuthorizedUsers().size());
        ArrayList<AuthorizedUser> authorizedUsers = new ArrayList<AuthorizedUser>();
        authorizedUsers.add(new AuthorizedUser(getBasicUser(), GeneralPermissions.VIEW_ALL));
        authorizedUsers.add(new AuthorizedUser(getAdminUser(), GeneralPermissions.VIEW_ALL));
        controller.setAuthorizedUsers(authorizedUsers);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        dataset = datasetService.find(datasetId);
        ResourceCollection internalResourceCollection = dataset.getInternalResourceCollection();
        assertEquals(2, internalResourceCollection.getAuthorizedUsers().size());
        Set<Long> seen = new HashSet<Long>();
        for (AuthorizedUser r : internalResourceCollection.getAuthorizedUsers()) {
            seen.add(r.getUser().getId());
        }
        // FIXME: this fails but clearly, above it works
        // assertTrue(internalResourceCollection.getResources().contains(dataset));
        seen.remove(TestConstants.USER_ID);
        seen.remove(getAdminUserId());
        assertTrue("should have seen all user ids already", seen.isEmpty());
    }

    @Test
    @Rollback
    public void testReadUserEmpty() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class, false);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, datasetId);
        controller.setAuthorizedUsers(Collections.<AuthorizedUser> emptyList());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(0, dataset.getInternalResourceCollection().getAuthorizedUsers().size());
    }

    @Test
    @Rollback(true)
    public void testControllerWithActiveResourceThatBecomesDeleted() throws Exception {
        controller = generateNewInitializedController(CollectionController.class, getUser());
        controller.prepare();
        ResourceCollection rc = controller.getPersistable();
        Project project = createAndSaveNewResource(Project.class, getUser(), "test project");
        //not 100% sure why we're using a proxy here, but technically, I think this i closer to what we do in real life
//        Project proxy = new Project(project.getId(), project.getTitle());
        Long pid = project.getId();

        controller.setAuthorizedUsers(Collections.<AuthorizedUser> emptyList());
        controller.getResources().add(project);
        controller.getPersistable().setName("testControllerWithActiveResourceThatBecomesDeleted");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        String result = controller.save();
        Assert.assertEquals(CollectionController.SUCCESS, result);
        Long rcid = rc.getId();
        searchIndexService.flushToIndexes();
        // so, wait, is this resource actually in the collection?
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(rcid);
        controller.prepare();
        controller.view();
        assertEquals("okay, we should have one resource in this collection now", 1, controller.getResults().size());

        project.getResourceCollections().add(rc);
        genericService.saveOrUpdate(project);
        genericService.synchronize();
        // okay now lets delete the resource
        ProjectController projectController = generateNewInitializedController(ProjectController.class);
        projectController.setServletRequest(getServletPostRequest());
        projectController.setId(pid);
        projectController.prepare();
        projectController.setDelete(AbstractPersistableController.DELETE_CONSTANT);
        projectController.delete();
        searchIndexService.flushToIndexes();

        // go back to the collection's 'edit' page and make sure that we are not displaying the deleted resource
        controller = generateNewInitializedController(CollectionController.class, getUser());
        controller.setId(rcid);
        controller.prepare();
        controller.edit();
        assertEquals("deleted resource should not appear on edit page", 0, controller.getResources().size());

        // so far so good. but lets make sure that the resource *is* actually in the collection
        rc = genericService.find(ResourceCollection.class, rcid);
        assertTrue(rc.getResources().contains(project));
        logger.info("{}", projectController.getProject().getResourceCollections());
    }

    
    
    @Test
    @Rollback(true)
    public void testControllerWithDeletedResourceThatBecomesActive() throws Exception {
        Project project = createAndSaveNewResource(Project.class, getUser(), "test project");
        ResourceCollection collection = generateResourceCollection("test collection with deleted", "test", CollectionType.SHARED, true, null, getUser(), Arrays.asList(project), null);
        project.setStatus(Status.DELETED);
        project.getResourceCollections().add(collection);
        genericService.saveOrUpdate(project);
        Long rcid = collection.getId();
        Long pid = project.getId();
        
        searchIndexService.flushToIndexes();
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
        searchIndexService.flushToIndexes();
        searchIndexService.index(project2);
        
        
        logger.info("{}", project2.getResourceCollections());
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(rcid);
        controller.prepare();
        controller.view();
        logger.info("{}" , controller.getResourceCollection().getResources().iterator().next().getStatus());
        assertTrue("collection should show the newly undeleted project", CollectionUtils.isNotEmpty(controller.getResults()));

        // we should also see the newly-undeleted resource on the edit page
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(rcid);
        controller.prepare();
        controller.edit();
        assertTrue("collection should show the newly undeleted project", CollectionUtils.isNotEmpty(controller.getResources()));
    }
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
        controller.getResources().add(proxy);
        controller.getPersistable().setName("testControllerWithActiveResourceThatBecomesDeleted");
        controller.getPersistable().setDescription("description");
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        String result = controller.save();
        // searchIndexService.index(proxy);

        Assert.assertEquals(CollectionController.SUCCESS, result);
        Long rcid = rc.getId();

        // confirm resource is viewable by author of collection
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(rcid);
        controller.prepare();
        controller.view();
        assertEquals("collection should have one resource inside", 1, controller.getResults().size());
        controller = null;
        // make sure it draft resource can't be seen by registered user (but not an authuser)
        Person registeredUser = createAndSaveNewPerson("testDraftResourceVisibleByAuthuser", "foo");
        controller = generateNewInitializedController(CollectionController.class, registeredUser);
        controller.setId(rcid);
        controller.prepare();
        controller.view();
        assertEquals(controller.getAuthenticatedUser(), registeredUser);
        assertTrue("resource should not be viewable", controller.getResults().isEmpty());
//        assertFalse("resource should not be viewable", controller.getResults().get(0).isViewable());

        // now make the user an authorizedUser
        controller = generateNewInitializedController(CollectionController.class);
        controller.setId(rcid);
        controller.prepare();
        AuthorizedUser authUser = new AuthorizedUser(registeredUser, GeneralPermissions.MODIFY_RECORD);
        controller.setAuthorizedUsers(Arrays.asList(authUser));
        controller.getResources().add(proxy);
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        controller.save();
        genericService.synchronize();

        // searchIndexService.indexAll();
        // registered user is now authuser of the collection, and should be able to see the resource
        controller = generateNewInitializedController(CollectionController.class, registeredUser);
        controller.setId(rcid);
        controller.prepare();
        controller.view();
        assertTrue("resource should be viewable", ((Viewable) (controller.getResults().get(0))).isViewable());

        // now make the registeredUser a non-contributor. make sure they can see the resource (TDAR-2028)
        registeredUser.setContributor(false);
        genericService.saveOrUpdate(registeredUser);
        searchIndexService.index(registeredUser);
        controller = generateNewInitializedController(CollectionController.class, registeredUser);
        controller.setId(rcid);
        controller.prepare();
        controller.view();
        assertTrue("resource should be viewable", ((Viewable)controller.getResults().get(0)).isViewable());
    }

}

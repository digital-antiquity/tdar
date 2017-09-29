package org.tdar.struts.action.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TestResourceCollectionHelper;

public class CollectionControllerITCase extends AbstractControllerITCase implements TestResourceCollectionHelper  {

    @Autowired
    private GenericService genericService;

    @Autowired
    AuthorizedUserDao authorizedUserDao;

    ShareCollectionController controller;

    static int indexCount = 0;

    @Before
    public void setup() {
        controller = generateNewInitializedController(ShareCollectionController.class);
        if (indexCount < 1) {
            reindex();
        }
        indexCount++;
    }


    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testPublicCollection() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        String email = "abc"+currentTimeMillis+"@ab.com";
        final TdarUser testPerson = createAndSaveNewPerson(email, ""+currentTimeMillis);
        String name = "test collection";
        String description = "test description";

        InformationResource normal = createAndSaveDocumentWithFileAndUseDefaultUser();
        InformationResource draft = createAndSaveDocumentWithFileAndUseDefaultUser();
        final Long normalId = normal.getId();
        final Long draftId = draft.getId();
        draft.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(draft);
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(
                new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE),
                new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.ADD_TO_SHARE)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        ResourceCollection collection = generateResourceCollection(name, description, false, users, testPerson, new ArrayList<>(), null);
        
        
        final Long id = collection.getId();
        String slug = collection.getSlug();
        collection = null;
        collection = null;
        resources = null;
        normal = null;
        draft = null;
        genericService.synchronize();
        collection = genericService.find(ResourceCollection.class, id);
        normal = genericService.find(InformationResource.class, normalId);
        normal.getUnmanagedResourceCollections().add(collection);
        draft = genericService.find(InformationResource.class, draftId);
        draft.getUnmanagedResourceCollections().add(collection);
//        collection.getUnmanagedResources().addAll(resources);
//        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(normal);
        genericService.saveOrUpdate(draft);

        
//-----------------------
        ShareCollectionRightsController cc = generateNewInitializedController(ShareCollectionRightsController.class, getAdminUser());
        cc.setId(id);
        cc.prepare();
        cc.edit();
        cc.setServletRequest(getServletPostRequest());
        cc.getProxies().add(new UserRightsProxy( new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.MODIFY_RECORD)));
        cc.setAsync(false);
        cc.save();
        cc = null;
        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, normal, GeneralPermissions.MODIFY_METADATA));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, draft, GeneralPermissions.MODIFY_RECORD));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, draft));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, normal));
        /*
*/
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
                new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE),
                new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.MODIFY_RECORD), 
                new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        ResourceCollection collection = 
                generateResourceCollection(name, description, true, users, getUser(), resources, null, ShareCollectionController.class, ResourceCollection.class);
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
        for (AuthorizedUser user : foundCollection.getAuthorizedUsers()) {
            if (user.getUser().equals(testPerson)) {
                count++;
                assertEquals(GeneralPermissions.MODIFY_RECORD, user.getGeneralPermission());
            }
            if (user.getUser().equals(getAdminUser())) {
                count++;
                assertEquals(GeneralPermissions.MODIFY_RECORD, user.getGeneralPermission());
            }
            if (user.getUser().equals(getBasicUser())) {
                count++;
                assertEquals(GeneralPermissions.ADMINISTER_SHARE, user.getGeneralPermission());
            }
        }
        assertEquals(3, count);
    }


}

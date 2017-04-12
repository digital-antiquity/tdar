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
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;

public class CollectionControllerITCase extends AbstractResourceControllerITCase {

    @Autowired
    private GenericService genericService;

    @Autowired
    private EntityService entityService;

    @Autowired
    AuthorizedUserDao authorizedUserDao;

    ListCollectionController controller;

    static int indexCount = 0;

    @Before
    public void setup() {
        controller = generateNewInitializedController(ListCollectionController.class);
        if (indexCount < 1) {
            reindex();
        }
        indexCount++;
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
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(
                new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        ListCollection collection = generateResourceCollection(name, description, false, users, testPerson, resources, null, 
                ListCollectionController.class, ListCollection.class);
        final Long id = collection.getId();
        String slug = collection.getSlug();
        collection = null;

        controller = generateNewInitializedController(ListCollectionController.class, getAdminUser());
        controller.setId(id);
        controller.prepare();
        controller.edit();
        controller.setServletRequest(getServletPostRequest());
        controller.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),testPerson, GeneralPermissions.MODIFY_RECORD));
        controller.setAsync(false);
        controller.save();

        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, normal, GeneralPermissions.MODIFY_METADATA));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, draft, GeneralPermissions.MODIFY_RECORD));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, draft));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, normal));
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
        ListCollection collection = 
                generateResourceCollection(name, description, true, users, getUser(), resources, null, ListCollectionController.class, ListCollection.class);
        Long collectionid = collection.getId();
        logger.info("{}", collection.getUnmanagedResources());
        assertFalse(collectionid.equals(-1L));
        collection = null;
        ListCollection foundCollection = genericService.find(ListCollection.class, collectionid);
        assertNotNull(foundCollection);
        assertEquals(3, foundCollection.getAuthorizedUsers().size());
        assertEquals(2, foundCollection.getUnmanagedResources().size());

        assertEquals(name, foundCollection.getName());
        assertEquals(description, foundCollection.getDescription());
        assertEquals(CollectionType.LIST, foundCollection.getType());
        assertEquals(SortOption.RESOURCE_TYPE, foundCollection.getSortBy());

        assertTrue(foundCollection.getUnmanagedResources().contains(generateInformationResourceWithFile2));
        assertTrue(foundCollection.getUnmanagedResources().contains(generateInformationResourceWithFile));

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

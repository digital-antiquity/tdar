package org.tdar.struts.action.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
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
import org.tdar.struts.action.collection.ListCollectionController;

public class ListCollectionControllerITCase extends AbstractResourceControllerITCase {

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
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP),
                new AuthorizedUser(getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
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
        controller.getAuthorizedUsers().add(new AuthorizedUser(testPerson, GeneralPermissions.MODIFY_RECORD));
        controller.setAsync(false);
        controller.save();

        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, normal, GeneralPermissions.MODIFY_METADATA));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, draft, GeneralPermissions.MODIFY_RECORD));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, draft));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, normal));
    }

}

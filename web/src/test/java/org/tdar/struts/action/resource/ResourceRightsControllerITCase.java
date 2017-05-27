package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Action;

public class ResourceRightsControllerITCase extends AbstractResourceControllerITCase {


    @Test
    @Rollback
    public void testUserPermIssuesUsers() throws Exception {
        // setup document
        TdarUser newUser = createAndSaveNewPerson();
        DocumentController dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.prepare();
        Document doc = dc.getDocument();
        doc.setTitle("test");
        doc.setDate(1234);
        doc.setDescription("my description");
        dc.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, dc.save());
        
        Long id = doc.getId();
        doc = null;

        evictCache();
        AuthorizedUser au = new AuthorizedUser(getAdminUser(),newUser, GeneralPermissions.MODIFY_METADATA);
        ResourceRightsController rrc;
        saveUser(id, au);
        doc = genericService.find(Document.class, id);
        logger.debug("RC: {}", doc.getAuthorizedUsers());
        // change the submitter to the admin
        AuthorizedUser au2 = new AuthorizedUser(getAdminUser(),newUser, GeneralPermissions.ADMINISTER_SHARE);
        saveUser(id, au2);
        logger.debug("RC: {}", doc.getAuthorizedUsers());

        evictCache();
    }

    

    @Test
    @Rollback
    public void testUserRemoveThemself() throws Exception {

        ImageController imageController = generateNewInitializedController(ImageController.class);
        imageController.prepare();
        Image image = imageController.getImage();
        image.setTitle("test image");
        image.setDescription("test description");
        imageController.setServletRequest(getServletPostRequest());
        TdarUser p = createAndSaveNewPerson();

        // create the dataset
        imageController.save();
        evictCache();
        Long imgId = image.getId();
        assertNotNull(imgId);
        saveUser(imgId, new AuthorizedUser(getAdminUser(),p, GeneralPermissions.MODIFY_RECORD));

        ResourceRightsController rrc = generateNewInitializedController(ResourceRightsController.class, getBasicUser());
        rrc.setId(imgId);
        rrc.prepare();
        rrc.edit();
        rrc.getProxies().clear();
        rrc.setServletRequest(getServletPostRequest());
        rrc.save();
        assertEquals(Action.SUCCESS, rrc.save());

        imageController = generateNewController(ImageController.class);
        init(imageController, p);
        imageController.setId(imgId);
        boolean seen = false;
        try {
            imageController.prepare();
            imageController.edit();
        } catch (TdarActionException e) {
            seen = true;
        }
        assertTrue(seen);
    }

    
    private void saveUser(Long id, AuthorizedUser au) throws Exception, TdarActionException {
        ResourceRightsController rrc = generateNewInitializedController(ResourceRightsController.class, getBasicUser());
        rrc.setId(id);
        rrc.prepare();
        rrc.edit();
        rrc.getProxies().add(new UserRightsProxy(au));
        rrc.setServletRequest(getServletPostRequest());
        rrc.save();
        assertEquals(Action.SUCCESS, rrc.save());
        evictCache();

    }
}

/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Action;

/**
 * @author Adam Brin
 * 
 */
public class UserPermissionsITCase extends AbstractResourceControllerITCase {

    @Autowired
    EntityService entityService;
    @Autowired
    SerializationService serializationService;

    private List<AuthorizedUser> authUsers;

    @Test
    @Rollback
    public void testUserRemoveThemself() throws TdarActionException {

        ImageController imageController = generateNewInitializedController(ImageController.class);
        imageController.prepare();
        Image image = imageController.getImage();
        image.setTitle("test image");
        image.setDescription("test description");
        imageController.setServletRequest(getServletPostRequest());
        TdarUser p = createAndSaveNewPerson();
        imageController.getAuthorizedUsers().add(new AuthorizedUser(p, GeneralPermissions.MODIFY_RECORD));

        // create the dataset
        imageController.save();
        evictCache();
        Long imgId = image.getId();
        assertNotNull(imgId);

        imageController = generateNewController(ImageController.class);
        init(imageController, p);
        imageController.setId(imgId);
        imageController.prepare();
        imageController.edit();
        imageController.setAuthorizedUsers(new ArrayList<AuthorizedUser>());
        imageController.setServletRequest(getServletPostRequest());
        // create the dataset
        assertEquals(Action.SUCCESS, imageController.save());
        evictCache();
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

    /**
     * tests that a user with MODIFY_METADATA Permissions has limited rights -- specifically cannot modify collection assignments or authorized users
     * @throws Exception
     */
    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testUserRemovingCollectionWithTheirRights() throws Exception {
        final TdarUser p = createAndSaveNewPerson();

        // adminUser creates a a new image and assigns p as an authorized user
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>();
        users.add(new AuthorizedUser(p, GeneralPermissions.MODIFY_METADATA));
        SharedCollection coll = generateResourceCollection("test", "test", true, users, getUser(), null, null);
        evictCache();
        ImageController imageController = generateNewInitializedController(ImageController.class);
        imageController.prepare();
        Image image = imageController.getImage();
        image.setTitle("test image");
        image.setDescription("test description");
        imageController.setServletRequest(getServletPostRequest());
        assertTrue(PersistableUtils.isNotNullOrTransient(coll));
        imageController.getShares().add(coll);
        imageController.save();
        final Long imgId = image.getId();
        assertNotNull(imgId);
        coll = null;

        // p logs in and wants to edit the image
        imageController = generateNewController(ImageController.class);
        init(imageController, p);
        imageController.setId(imgId);
        imageController.prepare();
        imageController.edit();

        // Whaaat? p just removed the authuser entry that gives p the ability to edit this item in the first place. p, you crazy.
        imageController.getAuthorizedUsers().clear();
        imageController.getShares().clear();
        imageController.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, imageController.save());
        evictCache();

        genericService.refresh(image);
        logger.debug("resource collections: {}", image.getSharedCollections());

        authUsers = resourceCollectionService.getAuthorizedUsersForResource(image, p);
        assertEquals("expecting authuser list should be empty now", 0, authUsers.size());
        assertEquals("we should have cleared the collections list should not be empty now", 1, image.getSharedResourceCollections().size());

        assertNotEquals("submitter and p should not be the same", image.getSubmitter().getId(), p.getId());
        image.markUpdated(getAdminUser());
        genericService.saveOrUpdate(image);
        image.getSharedCollections().clear();
        genericService.saveOrUpdate(image);
        image = null;
        evictCache();

        // Now p comes back, expecting to be able to edit this image. The system should not allow this request.
        imageController = generateNewController(ImageController.class);
        init(imageController, p);
        imageController.setId(imgId);
        boolean exceptionOccured = false;
        String result = null;
        try {
            imageController.prepare();
            result = imageController.edit();
            logger.debug("action error count:{}, they are:{}", imageController.getActionErrors().size(), imageController.getActionErrors());
            // logger.debug("brace yourself: \n\n\n\n{} \n\n\n", serializationService.convertToXML(image));

        } catch (TdarActionException e) {
            exceptionOccured = true;
        }

        Long pid = p.getId();

        logger.debug("authusers on view: {}  result: {}", imageController.getAuthorizedUsers(), result);
        // we should have received an exception.
        if (!exceptionOccured) {
            fail("controller action was expected to throw an exception, but didn't");
        }
    }
}

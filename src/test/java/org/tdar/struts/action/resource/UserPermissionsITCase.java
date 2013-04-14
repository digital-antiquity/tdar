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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.service.EntityService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

/**
 * @author Adam Brin
 * 
 */
public class UserPermissionsITCase extends AbstractResourceControllerITCase {

    @Autowired
    EntityService entityService;

    @Test
    @Rollback
    public void testUserRemoveThemself() throws TdarActionException {

        ImageController imageController = generateNewInitializedController(ImageController.class);
        imageController.prepare();
        Image image = imageController.getImage();
        image.setTitle("test image");
        image.setDescription("test description");
        imageController.setServletRequest(getServletPostRequest());
        Person p = createAndSaveNewPerson();
        imageController.getAuthorizedUsers().add(new AuthorizedUser(p, GeneralPermissions.MODIFY_METADATA));

        // create the dataset
        imageController.save();
        genericService.synchronize();
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
        assertEquals(TdarActionSupport.SUCCESS, imageController.save());
        genericService.synchronize();
        imageController = generateNewController(ImageController.class);
        init(imageController, p);
        imageController.setId(imgId);
        imageController.prepare();
        boolean seen = false;
        try {
            imageController.edit();
        } catch (TdarActionException e) {
            seen = true;
        }
        assertTrue(seen);

    }

    @Test
    @Rollback
    public void testUserRemovingCollectionWithTheirRights() throws Exception {
        Person p = createAndSaveNewPerson();
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>();
        users.add(new AuthorizedUser(p, GeneralPermissions.MODIFY_METADATA));
        ResourceCollection coll = generateResourceCollection("test", "test", CollectionType.SHARED, true, users, getAdminUser(), null, null);
        ImageController imageController = generateNewInitializedController(ImageController.class);
        imageController.prepare();
        Image image = imageController.getImage();
        image.setTitle("test image");
        image.setDescription("test description");
        imageController.setServletRequest(getServletPostRequest());
        imageController.getResourceCollections().add(coll);

        // create the dataset
        imageController.save();
        genericService.synchronize();
        Long imgId = image.getId();
        assertNotNull(imgId);

        imageController = generateNewController(ImageController.class);
        init(imageController, p);
        imageController.setId(imgId);
        imageController.prepare();
        imageController.edit();
        imageController.setAuthorizedUsers(new ArrayList<AuthorizedUser>());
        imageController.setResourceCollections(new ArrayList<ResourceCollection>());
        imageController.setServletRequest(getServletPostRequest());
        // create the dataset
        assertEquals(TdarActionSupport.SUCCESS, imageController.save());
        genericService.synchronize();
        imageController = generateNewController(ImageController.class);
        init(imageController, p);
        imageController.setId(imgId);
        imageController.prepare();
        boolean seen = false;
        try {
            imageController.edit();
        } catch (TdarActionException e) {
            seen = true;
        }
        assertTrue(seen);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return new ImageController();
    }

}

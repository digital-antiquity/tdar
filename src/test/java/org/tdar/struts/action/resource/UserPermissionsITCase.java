/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.XmlService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

/**
 * @author Adam Brin
 * 
 */
public class UserPermissionsITCase extends AbstractResourceControllerITCase {

    @Autowired
    EntityService entityService;
    @Autowired
    XmlService xmlService;

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
    @Rollback(value = true)
    public void testUserRemovingCollectionWithTheirRights() throws Exception {
        final Person p = createAndSaveNewPerson();

        // adminUser creates a a new image and assigns p as an authorized user
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>();
        users.add(new AuthorizedUser(p, GeneralPermissions.MODIFY_METADATA));
        ResourceCollection coll = generateResourceCollection("test", "test", CollectionType.SHARED, true, users, getUser(), null, null);
        genericService.synchronize();
        ImageController imageController = generateNewInitializedController(ImageController.class);
        imageController.prepare();
        Image image = imageController.getImage();
        image.setTitle("test image");
        image.setDescription("test description");
        imageController.setServletRequest(getServletPostRequest());
        assertTrue(Persistable.Base.isNotNullOrTransient(coll));
        imageController.getResourceCollections().add(coll);
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
        imageController.getResourceCollections().clear();
        imageController.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS, imageController.save());
        genericService.synchronize();

        genericService.refresh(image);
        logger.debug("resource collections: {}", image.getResourceCollections());

        authUsers = resourceCollectionService.getAuthorizedUsersForResource(image);
        assertEquals("expecting authuser list should be empty now", 0, authUsers.size());
        assertEquals("we should have cleared the collections list should be empty now", 0, image.getSharedResourceCollections().size());

        assertNotEquals("submitter and p should not be the same", image.getSubmitter().getId(), p.getId());
        image.markUpdated(getAdminUser());
        genericService.saveOrUpdate(image);
        image = null;
        genericService.synchronize();

        // Now p comes back, expecting to be able to edit this image. The system should not allow this request.
         imageController = generateNewController(ImageController.class);
         init(imageController, p);
         imageController.setId(imgId);
         imageController.prepare();
         boolean exceptionOccured = false;
         String result = null;
         try {
         result = imageController.edit();
         logger.debug("action error count:{}, they are:{}", imageController.getActionErrors().size(), imageController.getActionErrors());
         // logger.debug("brace yourself: \n\n\n\n{} \n\n\n", xmlService.convertToXML(image));
        
         } catch (TdarActionException e) {
         exceptionOccured = true;
         }

        final Long pid = p.getId();
        // setVerifyTransactionCallback(new TransactionCallback<String>() {
        // public String doInTransaction(TransactionStatus status) {
        // ImageController imcon = generateNewController(ImageController.class);
        // Person otherPerson = genericService.find(Person.class, pid);
        // init(imcon, otherPerson);
        // imcon.setId(imgId);
        // imcon.prepare();
        // boolean excep = false;
        // try {
        // String result = imcon.edit();
        // logger.debug("result of image edit was: {}", result);
        // } catch (TdarActionException e) {
        // excep = true;
        // }
        //
        // if(!excep) fail("was expecting an exception but none happened");
        //
        // if(otherPerson != null) genericService.delete(otherPerson);
        // if(imcon.getImage() != null) genericService.delete(imcon.getImage());
        // logger.debug("done after callback");
        // return "okay";
        // }
        // });

        // setVerifyTransactionCallback(new TransactionCallback<Resource>() {
        // public Resource doInTransaction(TransactionStatus arg0) {
        // InformationResource normal = informationResourceService.find(normalId);
        // InformationResource draft = informationResourceService.find(draftId);
        // }
        // });

         logger.debug("authusers on view: {}  result: {}", imageController.getAuthorizedUsers(), result);
         //we should have received an exception.
         if(!exceptionOccured) {
         fail("controller action was expected to throw an exception, but didn't");
         }

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

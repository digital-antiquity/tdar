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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TestResourceCollectionHelper;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Action;

/**
 * @author Adam Brin
 * 
 */
public class UserPermissionsITCase extends AbstractControllerITCase  implements TestResourceCollectionHelper {

    @Autowired
    EntityService entityService;
    @Autowired
    SerializationService serializationService;

    // private List<AuthorizedUser> authUsers;

    /**
     * tests that a user with MODIFY_METADATA Permissions has limited rights -- specifically cannot modify collection assignments or authorized users
     * 
     * @throws Exception
     */
    @SuppressWarnings("unused")
    @Test
    @Rollback(false)
    public void testUserRemovingCollectionWithTheirRights() throws Exception {
        final TdarUser p = createAndSaveNewPerson(System.currentTimeMillis() + "a", "aaa");
        final Long pid = p.getId();
        // adminUser creates a a new image and assigns p as an authorized user
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>();
        users.add(new AuthorizedUser(getAdminUser(), p, GeneralPermissions.MODIFY_RECORD));
        ResourceCollection coll = generateResourceCollection("test", "test", true, users, getUser(), null, null);
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
        image = null;
        evictCache();
        setVerifyTransactionCallback(new TransactionCallback<Resource>() {

            @Override
            public Resource doInTransaction(TransactionStatus arg0) {
                TdarUser p_ = genericService.find(TdarUser.class, pid);
                // p logs in and wants to edit the image
                ResourceRightsController resourceRightsController = generateNewController(ResourceRightsController.class);
                init(resourceRightsController, p_);
                resourceRightsController.setId(imgId);
                try {
                    resourceRightsController.prepare();
                    resourceRightsController.edit();

                    // Whaaat? p just removed the authuser entry that gives p the ability to edit this item in the first place. p, you crazy.
                    resourceRightsController.getProxies().clear();
                    resourceRightsController.setServletRequest(getServletPostRequest());
                    assertEquals(Action.SUCCESS, resourceRightsController.save());
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                evictCache();
                Image img = genericService.find(Image.class, imgId);
                assertEquals("we should have cleared the collections list should not be empty now", 1, img.getManagedResourceCollections().size());
                assertNotEquals("submitter and p should not be the same", img.getSubmitter().getId(), pid);

                logger.debug("resource collections: {}", img.getManagedResourceCollections());

                List<AuthorizedUser> authUsers = resourceCollectionService.getAuthorizedUsersForResource(img, p_);
                assertEquals("expecting authuser list should be empty now", 0, authUsers.size());
                img.markUpdated(getAdminUser());
                genericService.saveOrUpdate(img);
                img.getManagedResourceCollections().clear();
                genericService.saveOrUpdate(img);
                img = null;
                evictCache();

                // Now p comes back, expecting to be able to edit this image. The system should not allow this request.
                ImageController iontroller = generateNewController(ImageController.class);
                init(iontroller, p_);
                iontroller.setId(imgId);
                boolean exceptionOccured = false;
                String result = null;
                try {
                    iontroller.prepare();
                    result = iontroller.edit();
                    logger.debug("action error count:{}, they are:{}", iontroller.getActionErrors().size(), iontroller.getActionErrors());
                    // logger.debug("brace yourself: \n\n\n\n{} \n\n\n", serializationService.convertToXML(image));

                } catch (TdarActionException e) {
                    exceptionOccured = true;
                }

                Long pid = p.getId();

                // we should have received an exception.
                if (!exceptionOccured) {
                    fail("controller action was expected to throw an exception, but didn't");
                }
                img = genericService.find(Image.class, imgId);
                genericService.forceDelete(img);
                return null;
            }
        });

    }

    @SuppressWarnings("unused")
    @Test
    @Rollback(false)
    public void testSelfEscalation() throws Exception {
        final TdarUser p = createAndSaveNewPerson(System.currentTimeMillis() + "a", "aaa");
        final Long pid = p.getId();
        // adminUser creates a a new image and assigns p as an authorized user
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>();
        users.add(new AuthorizedUser(getAdminUser(), p, GeneralPermissions.MODIFY_RECORD));
        ResourceCollection coll = generateResourceCollection("test", "test", true, users, getUser(), null, null);
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
        image = null;
        evictCache();
        setVerifyTransactionCallback(new TransactionCallback<Resource>() {

            @Override
            public Resource doInTransaction(TransactionStatus arg0) {
                TdarUser p_ = genericService.find(TdarUser.class, pid);
                // p logs in and wants to edit the image
                ResourceRightsController resourceRightsController = generateNewController(ResourceRightsController.class);
                init(resourceRightsController, p_);
                resourceRightsController.setId(imgId);
                try {
                    resourceRightsController.prepare();
                    resourceRightsController.edit();

                    // Whaaat? p just removed the authuser entry that gives p the ability to edit this item in the first place. p, you crazy.
                    resourceRightsController.getProxies().clear();
                    resourceRightsController.setServletRequest(getServletPostRequest());
                    assertEquals(Action.SUCCESS, resourceRightsController.save());
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                evictCache();
                Image img = genericService.find(Image.class, imgId);
                assertEquals("we should have cleared the collections list should not be empty now", 1, img.getManagedResourceCollections().size());
                assertNotEquals("submitter and p should not be the same", img.getSubmitter().getId(), pid);

                logger.debug("resource collections: {}", img.getManagedResourceCollections());

                List<AuthorizedUser> authUsers = resourceCollectionService.getAuthorizedUsersForResource(img, p_);
                assertEquals("expecting authuser list should be empty now", 0, authUsers.size());
                img.markUpdated(getAdminUser());
                genericService.saveOrUpdate(img);
                img.getManagedResourceCollections().clear();
                genericService.saveOrUpdate(img);
                img = null;
                evictCache();

                // Now p comes back, expecting to be able to edit this image. The system should not allow this request.
                ImageController iontroller = generateNewController(ImageController.class);
                init(iontroller, p_);
                iontroller.setId(imgId);
                boolean exceptionOccured = false;
                String result = null;
                try {
                    iontroller.prepare();
                    result = iontroller.edit();
                    logger.debug("action error count:{}, they are:{}", iontroller.getActionErrors().size(), iontroller.getActionErrors());
                    // logger.debug("brace yourself: \n\n\n\n{} \n\n\n", serializationService.convertToXML(image));

                } catch (TdarActionException e) {
                    exceptionOccured = true;
                }

                Long pid = p.getId();

                // we should have received an exception.
                if (!exceptionOccured) {
                    fail("controller action was expected to throw an exception, but didn't");
                }
                img = genericService.find(Image.class, imgId);
                genericService.forceDelete(img);
                return null;
            }
        });

    }
}

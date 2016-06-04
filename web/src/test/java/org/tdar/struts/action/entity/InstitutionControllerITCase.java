package org.tdar.struts.action.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.InstitutionManagementAuthorization;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.TdarActionException;

public class InstitutionControllerITCase extends AbstractAdminControllerITCase {

    InstitutionController controller;

    @Before
    public void setup() {
        controller = generateNewInitializedController(InstitutionController.class);
    }

    @Test
    @Rollback
    public void testInstitutionAuthorization() throws TdarActionException {
        setIgnoreActionErrors(true);
        Institution test = new Institution(TESTING_AUTH_INSTIUTION);
        // test no authorization
        genericService.saveOrUpdate(test);
        controller = generateNewInitializedController(InstitutionController.class, getBasicUser());
        controller.setId(test.getId());
        boolean seenException = false;
        try {
            controller.prepare();
            assertFalse(controller.isEditable());
        } catch (Exception e) {
            seenException = true;
        }
        assertTrue(seenException);
        seenException = false;
        controller = generateNewInitializedController(InstitutionController.class, getBasicUser());

        // unauthorized authorization
        InstitutionManagementAuthorization ima = new InstitutionManagementAuthorization(test, getBasicUser());
        ima.setAuthorized(false);
        ima.setReason("because");
        genericService.saveOrUpdate(ima);
        controller.setId(test.getId());
        try {
            controller.prepare();
            assertFalse(controller.isEditable());
        } catch (Exception e) {
            seenException = true;
        }
        assertTrue(seenException);
        seenException = false;

        // authorized
        controller = generateNewInitializedController(InstitutionController.class, getBasicUser());
        ima.setAuthorized(true);
        genericService.saveOrUpdate(ima);
        controller.setId(test.getId());
        controller.prepare();
        assertTrue(controller.isEditable());

        // authorized
        controller = generateNewInitializedController(InstitutionController.class, getAdminUser());
        controller.setId(test.getId());
        controller.prepare();
        assertTrue(controller.isEditable());

    }

    @Test
    @Rollback
    public void testSavingInstitution() throws Exception {
        Institution inst = null;
        for (Institution inst_ : entityService.findAllInstitutions()) {
            // the Test fixtures will "refresh" and this is a guard against it (unfortunately)
            if (ObjectUtils.equals(inst_, getAdminUser().getInstitution())) {
                continue;
            }
            inst = inst_;
            break;
        }
        String oldName = inst.getName();
        String newName = oldName.concat(" updated");
        controller.setId(inst.getId());
        controller.prepare();
        controller.edit();
        Assert.assertEquals(inst, controller.getInstitution());

        // simulate the save
        setup();
        controller.setId(inst.getId());
        controller.prepare();
        controller.setName(newName);
        controller.setServletRequest(getServletPostRequest());
        controller.validate();
        controller.save();

        // ensure stuff was changed
        setup();
        controller.setId(inst.getId());
        controller.prepare();
        Assert.assertEquals(newName, controller.getInstitution().getName());
    }

    @Test
    @Rollback
    public void testSavingInstitutionWithImage() throws Exception {
        controller.prepare();
        controller.setName("test institution 123");
        controller.getInstitution().setDescription("my test description");
        controller.setFile(new File(TestConstants.TEST_IMAGE));
        controller.setFileFileName(TestConstants.TEST_IMAGE_NAME);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
    }

    @Test
    @Rollback
    // non-curators should not be able to edit an institution
    public void testEditByNonAdmin() throws TdarActionException {
        setIgnoreActionErrors(true);
        Institution inst = genericService.findAll(Institution.class).iterator().next();
        final Long id = inst.getId();
        String oldName = inst.getName();
        final String newName = oldName.concat(" updated");
        controller = generateNewInitializedController(InstitutionController.class, getBasicUser());
        logger.info("{} -- {} ", controller.getAuthenticatedUser(), inst);
        controller.setId(id);
        try {
            controller.prepare();
            controller.getInstitution().setName(newName);
            controller.edit();
            Assert.fail("edit request from non-admin should have thrown an exception");
        } catch (TdarActionException expected) {
            logger.debug("expected {}", expected);
        }
        setVerifyTransactionCallback(new TransactionCallback<Institution>() {
            @Override
            public Institution doInTransaction(TransactionStatus arg0) {
                Institution institution = genericService.find(Institution.class, id);
                Assert.assertFalse("institution shoudn't have been updated", newName.equals(institution.getName()));
                return institution;
            }
        });
    }

}

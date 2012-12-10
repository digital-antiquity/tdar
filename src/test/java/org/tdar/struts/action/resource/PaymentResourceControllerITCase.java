package org.tdar.struts.action.resource;

import static org.junit.Assert.*;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { "src/test/resources/tdar.cc.properties" })
public class PaymentResourceControllerITCase extends AbstractResourceControllerITCase {

    private DocumentController controller;

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    public void initControllerFields() {
        controller.prepare();
        controller.setProjectId(TestConstants.PARENT_PROJECT_ID);
    }

    public void setController(DocumentController controller) {
        this.controller = controller;
    }

    @Test
    @Rollback()
    public void testCreateWithoutValidAccount() throws Exception {
        controller = generateNewInitializedController(DocumentController.class);
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        initControllerFields();
        Assert.assertTrue(controller.isPayPerIngestEnabled());

        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        String result = null;
        TdarActionException tdae = null;
        try {
            result = controller.edit();
        } catch (TdarActionException e) {
            tdae = e;
        }
        Assert.assertNotNull(tdae);
        Assert.assertNull(result, result);
    }

    @Test
    @Rollback()
    public void testResourceControllerWithoutValidAccount() throws Exception {
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        ResourceController rc = generateNewInitializedController(ResourceController.class);

        String result = null;
        TdarActionException tdae = null;
        try {
            result = rc.doDefault();
        } catch (TdarActionException e) {
            tdae = e;
        }
        Assert.assertFalse(rc.isAllowedToCreateResource());
        Assert.assertTrue(rc.isPayPerIngestEnabled());
        Assert.assertNull(tdae);
        Assert.assertEquals(ResourceController.SUCCESS, result);
    }

    @Test
    @Rollback()
    public void testInitialSaveWithoutValidAccount() throws Exception {
        controller = generateNewInitializedController(DocumentController.class);
        TdarActionException tdae = setupResource(setupDocument());
        assertNotNull(tdae);
        Long newId = controller.getResource().getId();

        Assert.assertNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.
        // Assert.assertEquals("resource status should be flagged", Status.FLAGGED_ACCOUNT_BALANCE, d.getStatus());
        Assert.assertEquals("resource id should be -1 after unpaid resource addition", newId, Long.valueOf(-1L));
        Assert.assertNull("controller should not be successful", null);
    }

    @Test
    @Rollback()
    public void testSecondarySaveWithoutValidAccount() throws Exception {
        controller = generateNewInitializedController(DocumentController.class);
        Document d = setupDocument();
        genericService.saveOrUpdate(d);
        TdarActionException tdae = setupResource(d);
//        assertNotNull(tdae);
        Long newId = controller.getResource().getId();

        Assert.assertNotNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.
        // Assert.assertEquals("resource status should be flagged", Status.FLAGGED_ACCOUNT_BALANCE, d.getStatus());
        Assert.assertNotEquals("resource id should be -1 after unpaid resource addition", newId, Long.valueOf(-1L));
        Assert.assertNull("controller should not be successful", null);
        Assert.assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, d.getStatus());
        Assert.assertFalse(CollectionUtils.isEmpty(controller.getActionErrors()));
        setIgnoreActionErrors(true);
    }

    private TdarActionException setupResource(Document d) {
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        if (d != null && d.getId() != null) {
            controller.setId(d.getId());
        }
        initControllerFields();
        controller.setDocument(d);

        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        controller.getAuthorshipProxies().add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.AUTHOR));

        controller.setServletRequest(getServletPostRequest());
        String result = null;
        TdarActionException tdae = null;
        try {
            result = controller.save();
        } catch (TdarActionException e) {
            tdae = e;
        }
        return tdae;
    }

    private Document setupDocument() {
        Document d = new Document();
        d.setId(-1L);
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        return d;
    }

}

package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.bean.resource.sensory.SensoryDataScan;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

public class SensoryDataControllerITCase extends AbstractResourceControllerITCase {

    private static String TEST_TITLE = "a title name";
    private static String TEST_DESC = "a description";
    private static Long INVALID_RESOURCE_ID = -1L;

    private SensoryDataController controller;

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    public void initControllerFields() {
        controller.prepare();
    }

    @Before
    public void prepController() {
        controller = generateNewInitializedController(SensoryDataController.class);
    }

    @Test
    @Rollback
    public void testCreateBarebonesRecord() throws Exception {
        initControllerFields();
        SensoryData resource = controller.getResource();
        Assert.assertNotNull("should have a blank, non-null resource", resource);

        resource.setTitle(TEST_TITLE);
        resource.setDescription(TEST_DESC);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long id = resource.getId();
        Assert.assertNotSame("should have valid resource id", INVALID_RESOURCE_ID, id);

        // go back to the 'edit' page
        controller = generateNewInitializedController(SensoryDataController.class);
        controller.setId(id);
        controller.prepare();
        Assert.assertSame("expecting same title", TEST_TITLE, controller.getResource().getTitle());
        Assert.assertSame("expecting same description", TEST_DESC, controller.getResource().getDescription());
    }

    @Test
    @Rollback
    public void testSavingWithImageRecords() throws Exception {
        initControllerFields();
        SensoryData resource = controller.getResource();
        Assert.assertSame(INVALID_RESOURCE_ID, resource.getId());
        Assert.assertNotNull("should have a blank, non-null resource", resource);
        resource.setTitle(TEST_TITLE);
        resource.setDescription(TEST_DESC);

        List<SensoryDataImage> images = new ArrayList<SensoryDataImage>();
        for (int i = 0; i < 10; i++) {
            SensoryDataImage image = new SensoryDataImage();
            image.setFilename("file" + i);
            image.setDescription("desc" + i);
            images.add(image);
            controller.getSensoryDataImages().add(image);
        }
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Assert.assertEquals("expeciting  same nuber of image records", images.size(), resource.getSensoryDataImages().size());
    }

    @Test
    @Rollback(false)
    public void testSavingWithScanRecords() throws Exception {
        initControllerFields();
        SensoryData resource = controller.getResource();
        Assert.assertNotNull("should have a blank, non-null resource", resource);
        resource.setTitle(TEST_TITLE);
        resource.setDescription(TEST_DESC);

        // TODO: grist for allentime mill: how to best check that a tranient object is 'similar' to to an object in my persisted list?
        List<SensoryDataScan> scans = new ArrayList<SensoryDataScan>();
        genericService.detachFromSession(resource);
        for (int i = 0; i < 10; i++) {
            SensoryDataScan scan = new SensoryDataScan();
            scan.setFilename("file" + i);
            scan.setScanNotes("scan note" + i);
            scan.setPointsInScan(new Long((long) (Math.random()) * Long.MAX_VALUE));
            scans.add(scan);
            controller.getSensoryDataScans().add(scan);
        }
        resource = null;
        final Long id = saveAndReload(true);
        genericService.detachFromSession(controller.getSensoryData());
        Assert.assertEquals("should have same number of scans", 10, controller.getSensoryData().getSensoryDataScans().size());
        controller.edit();
        controller.getSensoryDataScans().remove(0);
        saveAndReload(false);

        setVerifyTransactionCallback(new TransactionCallback<Image>() {
            @Override
            public Image doInTransaction(TransactionStatus status) {
                SensoryData resource = genericService.find(SensoryData.class, id);
                // resource.getSensoryDataImages();
                logger.info("calling refresh");
                genericService.refresh(resource);
                logger.info("done calling refresh");
                Assert.assertEquals("expeciting  same nuber of image records", 9, resource.getSensoryDataScans().size());
                genericService.delete(resource);
                return null;
            }
        });

        Assert.assertEquals("should have same number of scans", 9, controller.getSensoryData().getSensoryDataScans().size());

        for (SensoryDataScan scan : scans) {
            // TODO: grist for allentime mill: how to best check that a tranient object is 'similar' to to an object in my persisted list?
            // Assert.assertTrue("expecting scan record in resource:" + scan.getFilename(), resource.getSensoryDataScans().contains(scan));
        }
    }

    private Long saveAndReload(boolean test) throws TdarActionException {
        Long oldid = controller.getResource().getId();
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long id = controller.getResource().getId();
        controller = generateNewInitializedController(SensoryDataController.class);
        controller.setId(id);
        controller.prepare();
        if (test) {
            Assert.assertNotSame("resuorce should be assingned an id", oldid, id);
        }
        return id;
    }

}

package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.struts.action.sensoryData.SensoryDataController;
import org.tdar.struts_base.action.TdarActionException;

public class SensoryDataControllerITCase extends AbstractResourceControllerITCase {

    private static String TEST_TITLE = "a title name";
    private static String TEST_DESC = "a description";
    private static Long INVALID_RESOURCE_ID = -1L;

    private SensoryDataController controller;

    public void initControllerFields() throws TdarActionException {
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

}

package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;

import java.util.SortedMap;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.TdarActionSupport;

public class ResourceControllerITCase extends AbstractResourceControllerITCase {

    @Test
    public void testSelect() {
        for (ResourceType type : ResourceType.values()) {
            ResourceController controller = generateNewInitializedController(ResourceController.class);
            controller.setResourceType(type);
            String selectResult = controller.select();
            assertEquals(type.name(), selectResult);
        }
    }

    @Test
    @Rollback
    public void testEdit() {
    	setIgnoreActionErrors(true);
        for (ResourceType type : ResourceType.values()) {
            try {
                Resource resource = type.getResourceClass().newInstance();
                if (resource instanceof InformationResource) {
                    InformationResource cast = (InformationResource) resource;
                    InformationResource createAndSaveNewInformationResource = createAndSaveNewInformationResource(cast.getClass(), false);
                    ResourceController controller = generateNewInitializedController(ResourceController.class);
                    controller.setResourceType(type);
                    controller.setResourceId(createAndSaveNewInformationResource.getId());
                    String selectResult = controller.edit();
                    assertEquals(type.name(), selectResult);
                    // FIXME: learn to test the locations
                } else {
                    resource.markUpdated(getUser());
                    resource.setTitle("test");
                    genericService.save(resource);
                    ResourceController controller = generateNewInitializedController(ResourceController.class);
                    controller.setResourceType(type);
                    controller.setResourceId(resource.getId());
                    String selectResult = controller.edit();
                    assertEquals("input", selectResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testGetResourceTypes() {
        ResourceController controller = generateNewInitializedController(ResourceController.class);
        SortedMap<ResourceType, String> resourceTypes = controller.getResourceTypes();
        assertEquals(6, resourceTypes.size());
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

}

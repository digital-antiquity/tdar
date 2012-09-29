package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;

import java.util.SortedMap;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;

public class ResourceControllerITCase extends AbstractAdminControllerITCase {

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
        int RESOURCECOUNT_FOR_ADMIN = 6;
        int RESOURCECOUNT_FOR_REGISTERED_USER = 6; //was five, but just we unveiled sensorydata
        
        ResourceController controller = generateNewInitializedController(ResourceController.class);
        SortedMap<ResourceType, String> resourceTypes = controller.getResourceTypes();
        assertEquals(RESOURCECOUNT_FOR_ADMIN, resourceTypes.size());
        controller = generateNewController(ResourceController.class);
        init(controller, getTestPerson());
        resourceTypes = controller.getResourceTypes();
        assertEquals(RESOURCECOUNT_FOR_REGISTERED_USER, resourceTypes.size());
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

}

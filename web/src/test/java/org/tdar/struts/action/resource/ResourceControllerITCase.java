package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.TdarActionSupport;

public class ResourceControllerITCase extends AbstractResourceControllerITCase {

    @Test
    @Rollback
    public <R extends Resource> void testFindProject() {
        @SuppressWarnings("unchecked")
        R r = (R) projectService.find(1L);
        logger.info("Resource: {} ", r);
        assertNotNull(r);
        r = resourceService.find(1L);
        logger.info("Resource: {} ", r);
        assertNotNull(r);
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
                    InformationResource createAndSaveNewInformationResource = createAndSaveNewInformationResource(cast.getClass());
                    ResourceController controller = generateNewInitializedController(ResourceController.class);
                    controller.setResourceType(type);
                    controller.setResourceId(createAndSaveNewInformationResource.getId());
                    String selectResult = controller.edit();
                    assertEquals(TdarActionSupport.SUCCESS, selectResult);
                } else {
                    resource.markUpdated(getUser());
                    resource.setTitle("test");
                    resource.setDescription("test");
                    genericService.save(resource);
                    ResourceController controller = generateNewInitializedController(ResourceController.class);
                    controller.setResourceType(type);
                    controller.setResourceId(resource.getId());
                    String selectResult = controller.edit();
                    assertEquals(TdarActionSupport.INPUT, selectResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

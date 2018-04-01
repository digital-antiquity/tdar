package org.tdar.struts.action.collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.api.collection.ListCollectionsForResource;

public class ListCollectionsForResourceControllerITCase extends AbstractAdminControllerITCase {

    @Autowired
    ResourceService resourceService;

    public final static Long VALID_RESOURCE_ID = 4292L;

    public final static Long INVALID_RESOURCE_ID = 100000L;

    @Test
    public void testValidResource() {
        ListCollectionsForResource controller = generateNewInitializedController(ListCollectionsForResource.class);

        try {
            controller.setResourceId(VALID_RESOURCE_ID);
            controller.prepare();
            controller.validate();
            controller.listCollectionsForResource();

            Map<String, ArrayList<ResourceCollection>> result = (Map<String, ArrayList<ResourceCollection>>) controller.getResultObject();

            assertTrue("There are managed collections", CollectionUtils.isNotEmpty(result.get("managed")));
            assertTrue("There are unmanaged collections", CollectionUtils.isNotEmpty(result.get("unmanaged")));

        } catch (Exception e) {
            fail("An error occured: " + e.getMessage());
        }
    }

    @Test
    public void testInvalidResource() {
        ListCollectionsForResource controller = generateNewInitializedController(ListCollectionsForResource.class);
        ignoreActionErrors(true);

        try {
            controller.setResourceId(INVALID_RESOURCE_ID);
            controller.prepare();
            controller.validate();
            logger.debug("Action errors: {}", controller.getActionErrors());
            assertTrue("An action error was thrown", CollectionUtils.isNotEmpty(controller.getActionErrors()));

        } catch (Exception e) {
            fail("An error occured: " + e.getMessage());
        }
    }

}

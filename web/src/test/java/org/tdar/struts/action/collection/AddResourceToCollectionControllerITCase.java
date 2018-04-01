package org.tdar.struts.action.collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.api.collection.AddResourceToCollectionAction;

public class AddResourceToCollectionControllerITCase extends AbstractControllerITCase {

    @Autowired
    ResourceService resourceService;

    public final static Long PERMISSIBLE_RESOURCE_ID = 3L;
    public final static Long NON_PERMISSIBLE_RESOURCE_ID = 42950L;

    public final static Long VALID_COLLECTION_ID = 1575L;
    public final static Long INVALID_COLLECTION_ID = 999L;
    public final static Long INVALID_RESOURCE_ID = 100000L;

    @Test
    @Rollback
    public void testAddValidResourceToManaged() {
        AddResourceToCollectionAction controller = getController();
        setProperties(controller, PERMISSIBLE_RESOURCE_ID, VALID_COLLECTION_ID, true);
        addResourceToCollection(controller);
        Map<String, Object> result = getResult(controller);
        assertTrue("result type is managed", result.get("type").equals("managed"));
        assertTrue("result contains managed resource", result.get("resourceId").equals(PERMISSIBLE_RESOURCE_ID));
    }

    @Test
    @Rollback
    public void testAddValidResourceToUnmanaged() {
        AddResourceToCollectionAction controller = getController();
        setProperties(controller, PERMISSIBLE_RESOURCE_ID, VALID_COLLECTION_ID, false);
        addResourceToCollection(controller);
        Map<String, Object> result = getResult(controller);
        assertTrue("result type is unmanaged", result.get("type").equals("unmanaged"));
        assertTrue("result contains unmanaged resource", result.get("resourceId").equals(PERMISSIBLE_RESOURCE_ID));
    }

    @Test
    @Rollback
    public void testVerifyUnableToAddResourceWithoutPermissions() {
        AddResourceToCollectionAction controller = getController();
        setProperties(controller, NON_PERMISSIBLE_RESOURCE_ID, VALID_COLLECTION_ID, true);
        addResourceToCollection(controller);
        Map<String, Object> result = getResult(controller);
        assertTrue("result contains error message", result.get("status").equals("failure"));
        assertTrue("result contains error message", result.get("reason").equals("addResourceToCollectionAction.no_edit_permission"));
    }

    @Test
    @Rollback
    public void testVerifyCanAddToUnmanagedWithoutPermissions() {
        AddResourceToCollectionAction controller = getController();
        setProperties(controller, NON_PERMISSIBLE_RESOURCE_ID, VALID_COLLECTION_ID, false);
        addResourceToCollection(controller);
        Map<String, Object> result = getResult(controller);
        assertTrue("result contains unmanaged resource", result.get("resourceId").equals(NON_PERMISSIBLE_RESOURCE_ID));
    }

    @Test
    @Rollback
    public void testUnableToAddInvalidResourceToCollection() {
        AddResourceToCollectionAction controller = getController();
        setProperties(controller, INVALID_RESOURCE_ID, VALID_COLLECTION_ID, false);
        prepareAndValidate(controller);
        logger.debug("Action errors: {}", controller.getActionErrors());
        assertTrue("An action error was thrown", CollectionUtils.isNotEmpty(controller.getActionErrors()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResult(AddResourceToCollectionAction controller) {
        Map<String, Object> result = (Map<String, Object>) controller.getResultObject();
        logger.debug("Result is : {}", result);
        return result;
    }

    private AddResourceToCollectionAction getController() {
        return generateNewInitializedController(AddResourceToCollectionAction.class);
    }

    private void addResourceToCollection(AddResourceToCollectionAction controller) {
        prepareAndValidate(controller);
        try {
            ignoreActionErrors(true);
            controller.addResourceToResourceCollection();
        } catch (Exception e) {
            logger.debug("STack trace: {}", e.getStackTrace());
            ;
            // fail("An exception occured"+e.getMessage());
        }
    }

    private void prepareAndValidate(AddResourceToCollectionAction controller) {
        try {
            ignoreActionErrors(true);
            controller.prepare();
            controller.validate();
        } catch (Exception e) {
            fail("An exception occurred: " + e.getMessage());
        }
    }

    private void setProperties(AddResourceToCollectionAction controller, Long resourceId, Long collectionId, boolean managed) {
        controller.setResourceId(resourceId);
        controller.setCollectionId(collectionId);
        controller.setAddAsManagedResource(managed);
    }
}

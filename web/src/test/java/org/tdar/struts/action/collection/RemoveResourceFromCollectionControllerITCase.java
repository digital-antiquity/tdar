package org.tdar.struts.action.collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.api.collection.RemoveResourceFromCollectionAction;

public class RemoveResourceFromCollectionControllerITCase extends AbstractControllerITCase {
    private static final Long VALID_MANAGED_RESOURCE_ID = 4289L;
    private static final Long VALID_UNMANAGED_RESOURCE_ID = 5000L;
    private static final Long NON_EXISTANT_RESOURCE_ID = 10000L;
    private static final Long INVALID_RESOURCE_ID = 42940L;

    private static final Long VALID_MANAGED_COLLECTION_ID = 1575L;
    private static final Long VALID_UNMANAGED_COLLECTION_ID = 1000L;
    private static final Long NON_EXISTANT_COLLECTION_ID = 10000L;
    private static final Long INVALID_COLLECTION_ID = 2578L;

    @Test
    @Rollback
    public void testRemoveResourceFromManaged() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, VALID_MANAGED_RESOURCE_ID, VALID_MANAGED_COLLECTION_ID, CollectionResourceSection.MANAGED);
        removeResource(controller);
        Map<String, Object> result = getResult(controller);
        assertSuccessfulResult(result, VALID_MANAGED_COLLECTION_ID, VALID_MANAGED_RESOURCE_ID, CollectionResourceSection.MANAGED);
    }

    @Test
    @Rollback
    public void testRemoveResourceFromUnmanaged() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, VALID_UNMANAGED_RESOURCE_ID, VALID_UNMANAGED_COLLECTION_ID, CollectionResourceSection.UNMANAGED);
        removeResource(controller);
        Map<String, Object> result = getResult(controller);
        assertSuccessfulResult(result, VALID_UNMANAGED_COLLECTION_ID, VALID_UNMANAGED_RESOURCE_ID, CollectionResourceSection.UNMANAGED);
    }

    @Test
    @Rollback
    public void testRemoveNullResourceFromCollection() {
        RemoveResourceFromCollectionAction controller = getController();
        controller.setCollectionId(VALID_MANAGED_COLLECTION_ID);
        controller.setResourceId(null);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveResourceFromNullCollection() {
        RemoveResourceFromCollectionAction controller = getController();
        controller.setCollectionId(null);
        controller.setResourceId(VALID_MANAGED_RESOURCE_ID);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveNullResourceFromNullCollection() {
        RemoveResourceFromCollectionAction controller = getController();
        controller.setCollectionId(null);
        controller.setResourceId(null);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveResourceFromNonExistantCollection() {
        RemoveResourceFromCollectionAction controller = getController();
        controller.setCollectionId(null);
        controller.setResourceId(NON_EXISTANT_COLLECTION_ID);
        controller.setType(CollectionResourceSection.MANAGED);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveResourceFromCollectionWithoutRights() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, VALID_UNMANAGED_RESOURCE_ID, INVALID_COLLECTION_ID, CollectionResourceSection.MANAGED);
        removeResource(controller);
        assertHasActionErrors(controller);

    }

    @Test
    @Rollback
    public void testRemoveNonExistantResourceFromManaged() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, NON_EXISTANT_RESOURCE_ID, VALID_MANAGED_COLLECTION_ID, CollectionResourceSection.MANAGED);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveNonExistantResourceFromUnmanaged() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, NON_EXISTANT_RESOURCE_ID, VALID_UNMANAGED_COLLECTION_ID, CollectionResourceSection.UNMANAGED);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveResourceNotInCollectionFromManaged() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, VALID_UNMANAGED_RESOURCE_ID, VALID_MANAGED_COLLECTION_ID, CollectionResourceSection.MANAGED);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveResourceNotInCollectionFromUnmanaged() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, VALID_MANAGED_RESOURCE_ID, VALID_UNMANAGED_COLLECTION_ID, CollectionResourceSection.UNMANAGED);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveResourceWithoutRightsNotInCollectionFromUnmanaged() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, INVALID_RESOURCE_ID, VALID_UNMANAGED_COLLECTION_ID, CollectionResourceSection.UNMANAGED);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @Test
    @Rollback
    public void testRemoveResourceWithoutRightsNotInCollectionFromManaged() {
        RemoveResourceFromCollectionAction controller = getController();
        setProperties(controller, INVALID_RESOURCE_ID, VALID_UNMANAGED_COLLECTION_ID, CollectionResourceSection.MANAGED);
        removeResource(controller);
        assertHasActionErrors(controller);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResult(RemoveResourceFromCollectionAction controller) {
        Map<String, Object> result = (Map<String, Object>) controller.getResultObject();
        logger.debug("Result is : {}", result);
        return result;
    }

    private RemoveResourceFromCollectionAction getController() {
        return generateNewInitializedController(RemoveResourceFromCollectionAction.class);
    }

    private void removeResource(RemoveResourceFromCollectionAction controller) {
        prepareAndValidate(controller);
        logger.debug("removeResource action errors: {}", controller.getActionErrors());

        if (CollectionUtils.isEmpty(controller.getActionErrors())) {
            try {
                ignoreActionErrors(true);
                controller.removeResourceFromCollection();
            } catch (Exception e) {
                // logger.debug("Stack trace: {}" ,e.getStackTrace());;
            }
        }
    }

    private void prepareAndValidate(RemoveResourceFromCollectionAction controller) {
        try {
            ignoreActionErrors(true);
            controller.prepare();
            if (CollectionUtils.isEmpty(controller.getActionErrors())) {
                controller.validate();
            }
        } catch (Exception e) {
            logger.debug("An exception occurred:{}", e);
            fail("An exception occurred: " + e.getMessage());
        }
    }

    private void setProperties(RemoveResourceFromCollectionAction controller, Long resourceId, Long collectionId, CollectionResourceSection type) {
        controller.setCollectionId(collectionId);
        controller.setResourceId(resourceId);
        controller.setType(type);
    }

    private void assertHasActionErrors(RemoveResourceFromCollectionAction controller) {
        logger.debug("{}", controller.getActionErrors());
        assertTrue("An action error was thrown", CollectionUtils.isNotEmpty(controller.getActionErrors()));
    }

    private void assertSuccessfulResult(Map<String, Object> result, Long collectionId, Long resourceId, CollectionResourceSection type) {
        assertTrue("result is successful", result.get("status").equals("success"));
        assertTrue("Collection id is the same", result.get("collectionId").equals(collectionId));
        assertTrue("Resource id is the same", result.get("resourceId").equals(resourceId));
        assertTrue("section is the same", result.get("type") == type);
    }

}

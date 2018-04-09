package org.tdar.struts.action.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.struts.action.workspace.integration.IntegrationSettingsController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;

public class WorkspaceSettingsActionITCase extends AbstractWorkspaceActionITCase {

    @Test
    @Rollback
    public void testIntegrationSettingsInvalid() {
        IntegrationSettingsController controller = generateNewInitializedController(IntegrationSettingsController.class, getBasicUser());
        DataIntegrationWorkflow workflow = setupHiddenWorkflow();
        controller.setId(workflow.getId());
        boolean seen = false;
        try {
            controller.prepare();
        } catch (TdarActionException e) {
            seen = true;
            assertEquals("user does not have permissions to perform the requested action", e.getMessage());
        }
        assertTrue("should have seen authorization exception", seen);
        assertFalse("should not be able to edit", controller.authorize());
    }

    @Test
    @Rollback
    public void testIntegrationSettingsValid() throws TdarActionException {
        IntegrationSettingsController controller = generateNewInitializedController(IntegrationSettingsController.class, getBasicUser());
        DataIntegrationWorkflow workflow = setupHiddenWithAuthorizedUser();
        controller.setId(workflow.getId());
        boolean seen = false;
        try {
            controller.prepare();
        } catch (TdarActionException e) {
            seen = true;
            logger.error("error", e);
            assertEquals("user does not have permissions to perform the requested action", e.getMessage());
        }
        assertFalse("should not have seen authorization exception", seen);
        assertTrue("should not be able to edit", controller.authorize());

        assertEquals(TdarActionSupport.SUCCESS, controller.edit());
        controller.setServletRequest(getServletPostRequest());
        assertEquals(IntegrationSettingsController.SUCCESS_WORKSPACE, controller.save());
    }

}

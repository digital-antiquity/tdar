package org.tdar.struts.action.workspace;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;

public class WorkspaceActionITCase extends AbstractWorkspaceActionITCase {

    @Test
    @Rollback
    public void testBasicUserPublicIntegration() {
        DataIntegrationWorkflow workflow = setupPublicWorkflow();
        WorkspaceAction action = generateNewInitializedController(WorkspaceAction.class, getBasicUser());
        action.execute();
        assertTrue("list includes public workflow", action.getWorkflows().contains(workflow));
    }

    @Test
    @Rollback
    public void testBasicUserHiddenWithAccessIntegration() {
        DataIntegrationWorkflow workflow = setupHiddenWithAuthorizedUser();
        genericService.saveOrUpdate(workflow);
        WorkspaceAction action = generateNewInitializedController(WorkspaceAction.class, getBasicUser());
        action.execute();
        assertTrue("list includes shared workflow", action.getWorkflows().contains(workflow));
    }

    @Test
    @Rollback
    public void testBasicUserHiddenIntegration() {
        DataIntegrationWorkflow workflow = setupHiddenWorkflow();
        workflow.getAuthorizedUsers().clear();
        genericService.saveOrUpdate(workflow);
        genericService.synchronize();
        WorkspaceAction action = generateNewInitializedController(WorkspaceAction.class, getBasicUser());
        action.execute();
        assertFalse("list doesn't include hidden workflow", action.getWorkflows().contains(workflow));
    }

}

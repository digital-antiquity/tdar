package org.tdar.struts.action.workspace;

import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.struts.action.AbstractControllerITCase;

public abstract class AbstractWorkspaceActionITCase extends AbstractControllerITCase {

    public DataIntegrationWorkflow setupHiddenWithAuthorizedUser() {
        DataIntegrationWorkflow workflow = setupHiddenWorkflow();
        workflow.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getUser(), GeneralPermissions.EDIT_INTEGRATION));
        genericService.saveOrUpdate(workflow);
        workflow.setJsonData("{}");
        return workflow;
    }

    public DataIntegrationWorkflow setupHiddenWorkflow() {
        DataIntegrationWorkflow workflow = setup();
        workflow.setHidden(true);
        workflow.setTitle("test workflow (public)");
        genericService.saveOrUpdate(workflow);
        return workflow;
    }

    private DataIntegrationWorkflow setup() {
        DataIntegrationWorkflow workflow = new DataIntegrationWorkflow();
        workflow.setSubmitter(getAdminUser());
        workflow.markUpdated(getAdminUser());
        workflow.setJsonData("{}");
        return workflow;
    }

    public DataIntegrationWorkflow setupPublicWorkflow() {
        DataIntegrationWorkflow workflow = setup();
        workflow.setTitle("test workflow (public)");
        workflow.setHidden(false);
        genericService.saveOrUpdate(workflow);
        return workflow;
    }

}

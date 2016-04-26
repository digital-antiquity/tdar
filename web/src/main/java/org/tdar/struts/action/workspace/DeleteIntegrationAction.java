package org.tdar.struts.action.workspace;

import java.util.Objects;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.integration.IntegrationWorkflowService;
import org.tdar.struts.action.AbstractDeleteAction;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/workspace")
public class DeleteIntegrationAction extends AbstractDeleteAction<DataIntegrationWorkflow> {

    private static final long serialVersionUID = 6921699138699176481L;

    @Autowired
    IntegrationWorkflowService integrationWorkflowService;

    private DataIntegrationWorkflow workflow;

    @Override
    protected void delete(DataIntegrationWorkflow persistable) {
        integrationWorkflowService.deleteForController(this, persistable, getAuthenticatedUser());
    }

    @Override
    protected DeleteIssue getDeletionIssues() {
        return null;
    }

    @Override
    protected boolean canDelete() {
        return (Objects.equals(getAuthenticatedUser(), workflow.getSubmitter()));
    }

    @Override
    protected DataIntegrationWorkflow loadPersistable() {
        workflow = integrationWorkflowService.find(getId());
        return workflow;
    }

}

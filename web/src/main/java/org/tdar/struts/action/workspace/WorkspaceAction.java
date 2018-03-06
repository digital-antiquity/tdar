package org.tdar.struts.action.workspace;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.service.integration.IntegrationWorkflowService;
import org.tdar.struts.action.AbstractAuthenticatableAction;

/**
 * $Id$
 * 
 * Data integration activities in the workspace.
 * 
 * @author Allen Lee, Adam Brin
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/workspace")
@Component
@Scope("prototype")
public class WorkspaceAction extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = 8232843043333817727L;

    @Autowired
    private transient IntegrationWorkflowService integrationWorkflowService;

    private List<DataIntegrationWorkflow> workflows = new ArrayList<>();

    /**
     * Pass through actions that will go to <action-name>.ftl or <action-name>.jsp
     */
    @Override
    @Action(value = "list", results = { @Result(name = SUCCESS, location = "workspace.ftl") })
    public String execute() {
        setWorkflows(integrationWorkflowService.getWorkflowsForUser(getAuthenticatedUser()));
        return SUCCESS;
    }

    public List<DataIntegrationWorkflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<DataIntegrationWorkflow> workflows) {
        this.workflows = workflows;
    }

}

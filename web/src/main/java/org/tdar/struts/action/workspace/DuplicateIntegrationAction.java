package org.tdar.struts.action.workspace;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.IntegrationWorkflowService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/workspace")
public class DuplicateIntegrationAction extends AbstractAuthenticatableAction implements Preparable, PersistableLoadingAction<DataIntegrationWorkflow> {

    private static final long serialVersionUID = 6921699138699176481L;

    private Long id;
    
    @Autowired
    IntegrationWorkflowService integrationWorkflowService;

    @Autowired
    AuthorizationService authorizationService;
    
    private DataIntegrationWorkflow workflow;

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.SAVE);
    }

    @Override
    public String execute() throws Exception {
        DataIntegrationWorkflow dup = integrationWorkflowService.duplicateWorkflow(workflow, getAuthenticatedUser());
        return super.execute();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEditWorkflow(getAuthenticatedUser(), getWorkflow());
    }

    @Override
    public Persistable getPersistable() {
        return getWorkflow();
    }

    @Override
    public Class<DataIntegrationWorkflow> getPersistableClass() {
        return DataIntegrationWorkflow.class;
    }

    @Override
    public void setPersistable(DataIntegrationWorkflow persistable) {
        this.setWorkflow(workflow);
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANYTHING;
    }

    public DataIntegrationWorkflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(DataIntegrationWorkflow workflow) {
        this.workflow = workflow;
    }

}

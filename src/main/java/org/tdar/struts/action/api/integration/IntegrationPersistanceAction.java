package org.tdar.struts.action.api.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.IntegrationWorkflowService;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;
import org.tdar.filestore.WorkflowContext;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@ParentPackage("secured")
@Namespace("/api/integration")
@Component
@Scope("prototype")
public class IntegrationPersistanceAction extends AbstractIntegrationAction implements Preparable, PersistableLoadingAction<DataIntegrationWorkflow>,
        Validateable {

    private static final long serialVersionUID = 9053098961621133695L;
    private Long id;
    private DataIntegrationWorkflow workflow;
    private String integration;

    @Autowired
    IntegrationWorkflowService integrationWorkflowService;
    
    @Autowired
    AuthorizationService authorizationService;
    
    @Autowired
    SerializationService serializationService;
    private IntegrationWorkflowData jsonData;

    @Actions(value={
            @Action("save/{id}"),
            @Action("save")
    })
    @PostOnly
    @WriteableSession
    public String save() throws TdarActionException, IOException {
        integrationWorkflowService.saveForController(getPersistable(), jsonData, getAuthenticatedUser());
        Map<String,Object>result = new HashMap<>();
        result.put("status","success");
        result.put("id", workflow.getId());
        setJsonObject(result);
        return SUCCESS;
    }

    @Override
    public Class<DataIntegrationWorkflow> getPersistableClass() {
        return DataIntegrationWorkflow.class;
    }

    @Override
    public void validate() {
        integrationWorkflowService.validateWorkflow(jsonData);
    }

    @Override
    public boolean authorize() {
        return authorizationService.canEditWorkflow(workflow, getAuthenticatedUser());
    }

    @Override
    public DataIntegrationWorkflow getPersistable() {
        return workflow;
    }

    @Override
    public void setPersistable(DataIntegrationWorkflow persistable) {
        workflow = persistable;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANYTHING;
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.SAVE);
        getLogger().debug(integration);
        jsonData = serializationService.readObjectFromJson(integration, IntegrationWorkflowData.class);
        if (workflow == null) {
            workflow = new DataIntegrationWorkflow();
        }
        workflow.copyValuesFromJson(jsonData, integration);

    }

    
    public String getIntegration() {
        return integration;
    }

    public void setIntegration(String integration) {
        this.integration = integration;
    }

    public DataIntegrationWorkflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(DataIntegrationWorkflow workflow) {
        this.workflow = workflow;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

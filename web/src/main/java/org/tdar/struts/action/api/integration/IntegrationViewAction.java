package org.tdar.struts.action.api.integration;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
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
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class IntegrationViewAction extends AbstractJsonApiAction implements PersistableLoadingAction<DataIntegrationWorkflow> {

    private static final long serialVersionUID = -4028463597298660974L;

    @Autowired
    private transient SerializationService serializationService;

    private Long id;
    private IntegrationWorkflowData data;
    private DataIntegrationWorkflow workflow;
    @SuppressWarnings("unused")
    private String workflowJson;
    private int currentJsonVersion = 1;

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private IntegrationWorkflowService integrationWorkflowService;

    @Override
    public void prepare() throws TdarActionException {
        prepareAndLoad(this, RequestType.VIEW);

        if (workflow != null) {
            try {
                data = serializationService.readObjectFromJson(workflow.getJsonData(), IntegrationWorkflowData.class);
                data.setId(workflow.getId());
                workflowJson = serializationService.convertToJson(data);
            } catch (JsonParseException jpe) {
                // not technically needed, but using for explicitness
                getLogger().error("json parsing exception", jpe);
            } catch (JsonMappingException jme) {
                getLogger().error("json mapping exception", jme);
            } catch (IOException e) {
                getLogger().error("other exception", e);
            }

            if (data.getVersion() != currentJsonVersion) {
                // do something
            }
        }
    }

    @Action(value = "view")
    public String viewIntegration() throws IOException {

        setJsonObject(data);
        return SUCCESS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canViewWorkflow( getAuthenticatedUser(),workflow);
    }

    @Override
    public DataIntegrationWorkflow getPersistable() {
        return workflow;
    }

    @Override
    public Class<DataIntegrationWorkflow> getPersistableClass() {
        return DataIntegrationWorkflow.class;
    }

    @Override
    public void setPersistable(DataIntegrationWorkflow persistable) {
        this.workflow = persistable;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.VIEW_ANYTHING;
    }

    public DataIntegrationWorkflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(DataIntegrationWorkflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public void validate() {
        if (data != null) {
            try {
                integrationWorkflowService.validateWorkflow(data, this);
            } catch (IntegrationDeserializationException e) {
                data.getErrors().add(e.getMessage());
                getLogger().error("cannot validate", e);
            }
        }
    }
}

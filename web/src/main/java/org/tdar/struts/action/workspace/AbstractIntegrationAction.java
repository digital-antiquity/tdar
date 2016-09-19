package org.tdar.struts.action.workspace;

import java.io.IOException;

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
import org.tdar.core.service.integration.dto.IntegrationWorkflowWrapper;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;
import org.tdar.struts.WROProfile;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

/**
 *
 * @author adam
 */
@ParentPackage("secured")
@Namespace("/workspace")
@Component
@Scope("prototype")
@HttpsOnly
public abstract class AbstractIntegrationAction extends AbstractAuthenticatableAction implements Preparable, PersistableLoadingAction<DataIntegrationWorkflow>, Validateable {

    private static final long serialVersionUID = 7924205432529421539L;

    private int currentJsonVersion = 1;

    private Long id;
    private DataIntegrationWorkflow workflow;

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient SerializationService serializationService;
    @Autowired
    private transient IntegrationWorkflowService integrationWorkflowService;


    private IntegrationWorkflowWrapper data;

    private String workflowJson;

    @Override
    public String getWroProfile() {
        return WROProfile.NG_INTEGRATE.getProfileName();
    }

    @Override
    public void prepare() throws TdarActionException {
        prepareAndLoad(this, RequestType.VIEW);
        if (workflow != null) {
            try {
                setData(serializationService.readObjectFromJson(workflow.getJsonData(), IntegrationWorkflowData.class));
                getData().setId(workflow.getId());
                workflowJson = serializationService.convertToJson(getData());
            } catch (JsonParseException jpe) {
                // not technically needed, but using for explicitness
                getLogger().error("json parsing exception", jpe);
            } catch (JsonMappingException jme) {
                getLogger().error("json mapping exception", jme);
            } catch (IOException e) {
                getLogger().error("other exception", e);
            }

            if (getData().getVersion() != currentJsonVersion) {
                // do something
            }
        }
    }
    
    public String getWorkflowJson() {
        return workflowJson;
    }

    public String getJson(Object obj) {
        String json = "[]";
        try {
            json = serializationService.convertToJson(obj);
        } catch (IOException e) {
            addActionError(e.getMessage());
        }
        return json;
    }

    public String getNgApplicationName() {
        return "integrationApp";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEditWorkflow(workflow, getAuthenticatedUser());
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
        if (getData() != null) {
            try {
                integrationWorkflowService.validateWorkflow(getData(), this);
            } catch (IntegrationDeserializationException e) {
                getLogger().debug(e.toString());
                getLogger().error("cannot validate", e);
            }
        }
    }

    public IntegrationWorkflowWrapper getData() {
        return data;
    }

    public void setData(IntegrationWorkflowWrapper data) {
        this.data = data;
    }
}

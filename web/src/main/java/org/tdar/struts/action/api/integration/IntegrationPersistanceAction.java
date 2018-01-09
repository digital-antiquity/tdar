package org.tdar.struts.action.api.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
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
import org.tdar.core.service.integration.IntegrationSaveResult;
import org.tdar.core.service.integration.IntegrationWorkflowService;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@ParentPackage("secured")
@Namespace("/api/integration")
@Component
@Scope("prototype")
public class IntegrationPersistanceAction extends AbstractJsonApiAction implements Preparable, PersistableLoadingAction<DataIntegrationWorkflow>,
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
    private IntegrationSaveResult result;
    private List<String> errors = new ArrayList<>();

    @Actions(value = {
            @Action("save/{id}"),
            @Action("save")
    })
    @PostOnly
    @WriteableSession
    public String save() throws TdarActionException, IOException, IntegrationDeserializationException {
        setResult(integrationWorkflowService.saveForController(getPersistable(), jsonData, integration, getAuthenticatedUser(), this));
        setJsonObject(getResult(), JsonIntegrationFilter.class);
        if (result.getStatus() != IntegrationSaveResult.SUCCESS) {
            result.getErrors().addAll(errors);
            return INPUT;
        }
        return SUCCESS;
    }

    @Override
    public Class<DataIntegrationWorkflow> getPersistableClass() {
        return DataIntegrationWorkflow.class;
    }

    @Override
    public void prepare() throws TdarActionException {
        prepareAndLoad(this, RequestType.SAVE);
        getLogger().debug("incoming json:{}", integration);

        if (workflow == null) {
            workflow = new DataIntegrationWorkflow();
        }
        try {
            jsonData = serializationService.readObjectFromJson(integration, IntegrationWorkflowData.class);
            getLogger().debug("jsonData:{}", jsonData);
            if (getLogger().isTraceEnabled()) {
                diff();
            }
        } catch (IOException e) {
            getLogger().error("cannot prepare json", e);
            errors.add(e.getMessage());
        }
    }

    /**
     * Compare raw json to parsed, validated json. This method is expensive and swallows exceptions so it might screw something up prior to your save.
     * Therefore, you probably only want to use this only in a debugging context.
     */
    private void diff() {
        try {
            String parsedJson = serializationService.convertToJson(jsonData);
            int levenshteinDistance = StringUtils.getLevenshteinDistance(integration, parsedJson);
            getLogger().trace("Comparing original json to parsed json:");
            getLogger().trace("\tincoming json:{}", integration);
            getLogger().trace("\t  parsed json:{}", jsonData);
            getLogger().trace("\t     distance:{}", levenshteinDistance);
        } catch (IOException e) {
            getLogger().error("Integration-save encountered an exception during diff() comparison");
        }
    }

    @Override
    public void validate() {
        if (jsonData == null) {
            return;
        }
        try {
            integrationWorkflowService.validateWorkflow(jsonData, this);
        } catch (IntegrationDeserializationException e) {
            getLogger().error("error validating json", e);
            if (CollectionUtils.isNotEmpty(e.getErrors())) {
                getLogger().debug("errs: {}", e.getErrors());
                errors.addAll(e.getErrors());
            }
            // fixme -- cleanup
            if (MapUtils.isNotEmpty(e.getFieldErrors())) {
                getLogger().debug("fieldErrs: {}", e.getFieldErrors());
                errors.add(e.getFieldErrors().toString());
            }
            setupErrorResult();
        }
    }

    private void setupErrorResult() {
        try {
            setResult(new IntegrationSaveResult(errors));
            setJsonObject(getResult(), JsonIntegrationFilter.class);
            getActionErrors().addAll(errors);
        } catch (IOException e1) {
            getLogger().error("erro setting up error json", e1);
        }
    }

    @Override
    public boolean authorize() {
        return authorizationService.canEditWorkflow(getAuthenticatedUser(), workflow);
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

    public IntegrationSaveResult getResult() {
        return result;
    }

    public void setResult(IntegrationSaveResult result) {
        this.result = result;
    }
}

package org.tdar.struts.action.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.IntegrationWorkflowService;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.IntegrationWorkflowWrapper;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.struts.WROProfile;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

/**
 *
 * @author jim
 */
@ParentPackage("secured")
@Namespace("/workspace")
@Component
@Scope("prototype")
@HttpsOnly
public class AngularIntegrationAction extends AbstractAuthenticatableAction implements Preparable, PersistableLoadingAction<DataIntegrationWorkflow>, Validateable {

    private static final long serialVersionUID = -2356381511354062946L;

    private int currentJsonVersion = 1;

    private Long id;
    private DataIntegrationWorkflow workflow;

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient SerializationService serializationService;
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient IntegrationWorkflowService integrationWorkflowService;

    private List<Resource> fullUserProjects = new ArrayList<>();
    private Collection<ResourceCollection> allResourceCollections = new ArrayList<>();

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
        prepareProjectStuff();
        prepareCollections();
    }

    @Actions({
            @Action(value = "add", results = {
                    @Result(name = SUCCESS, location = "edit.ftl")
            }),
            @Action(value = "integrate", results = {
                    @Result(name = SUCCESS, location = "ng-integrate.ftl")
            }),
            @Action(value = "integrate/{id}", results = {
                    @Result(name = SUCCESS, location = "ng-integrate.ftl")
            })
    })
    public String execute() {
        return SUCCESS;
    }

    public String getCategoriesJson() {
        String json = "[]";
        try {
            json = serializationService.convertToFilteredJson(genericService.findAll(CategoryVariable.class), JsonLookupFilter.class);
        } catch (IOException e) {
            addActionError(e.getMessage());
        }
        return json;
    }

    public String getWorkflowJson() {
        return workflowJson;
    }

    String getJson(Object obj) {
        String json = "[]";
        try {
            json = serializationService.convertToJson(obj);
        } catch (IOException e) {
            addActionError(e.getMessage());
        }
        return json;
    }

    public List<Resource> getFullUserProjects() {
        return fullUserProjects;
    }

    public String getFullUserProjectsJson() {
        return getJson(fullUserProjects);
    }

    public String getAllResourceCollectionsJson() {
        return getJson(allResourceCollections);
    }

    private void prepareProjectStuff() {
        // FIXME: Remove and replace with AJAX lookup / autocomplete
        fullUserProjects = new ArrayList<>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), false));
        Collections.sort(fullUserProjects);
    }

    private void prepareCollections() {
        // FIXME: Remove and replace with AJAX lookup / autocomplete
        allResourceCollections.addAll(resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser(), SharedCollection.class));
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

    public Boolean isEditable() {
        return authorizationService.canEditWorkflow(workflow, getAuthenticatedUser());
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
        if (data != null) {
            try {
                integrationWorkflowService.validateWorkflow(data, this);
            } catch (IntegrationDeserializationException e) {
                getLogger().debug(e.toString());
                getLogger().error("cannot validate", e);
            }
        }
    }
}

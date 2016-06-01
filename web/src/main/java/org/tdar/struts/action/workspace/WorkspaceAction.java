package org.tdar.struts.action.workspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.DataIntegrationService;
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
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient DataIntegrationService dataIntegrationService;

    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private transient IntegrationWorkflowService integrationWorkflowService;

    private List<Resource> bookmarkedResources;
    private Set<Ontology> sharedOntologies;
    private List<DataIntegrationWorkflow> workflows = new ArrayList<>();

    /**
     * Pass through actions that will go to <action-name>.ftl or <action-name>.jsp
     */
    @Override
    @Action(value = "list", results = { @Result(name = SUCCESS, location = "workspace.ftl") })
    public String execute() {
        Map<Ontology, List<DataTable>> suggestions = dataIntegrationService.getIntegrationSuggestions(getBookmarkedDataTables(), false);
        setSharedOntologies(suggestions.keySet());
        setWorkflows(integrationWorkflowService.getWorkflowsForUser(getAuthenticatedUser()));
        // in the future we could use the Map to prompt the user with suggestions
        return SUCCESS;
    }

    public List<Resource> getBookmarkedResources() {
        if (bookmarkedResources == null) {
            bookmarkedResources = bookmarkedResourceService.findBookmarkedResourcesByPerson(getAuthenticatedUser(),
                    Arrays.asList(Status.ACTIVE, Status.DRAFT));
        }

        for (Resource res : bookmarkedResources) {
            authorizationService.applyTransientViewableFlag(res, getAuthenticatedUser());
        }
        return bookmarkedResources;
    }

    public List<Dataset> getBookmarkedDatasets() {
        List<Dataset> datasets = new ArrayList<Dataset>();
        for (Resource resource : getBookmarkedResources()) {
            if ((resource instanceof Dataset) && resource.isActive()) {
                Dataset dataset = (Dataset) resource;
                datasets.add(dataset);
            }
        }
        return datasets;
    }

    public Set<DataTable> getBookmarkedDataTables() {
        Set<DataTable> dataTables = new HashSet<DataTable>();
        for (Dataset d : getBookmarkedDatasets()) {
            dataTables.addAll(d.getDataTables());
        }
        return dataTables;
    }

    public Set<Ontology> getSharedOntologies() {
        return sharedOntologies;
    }

    public void setSharedOntologies(Set<Ontology> sharedOntologies) {
        this.sharedOntologies = sharedOntologies;
    }

    public List<DataIntegrationWorkflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<DataIntegrationWorkflow> workflows) {
        this.workflows = workflows;
    }

}

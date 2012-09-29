package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;

/**
 * $Id$
 * 
 * Manages requests to create/delete/edit a Project and its associated metadata (including Datasets, etc).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/project")
@Component
@Scope("prototype")
public class ProjectController extends AbstractResourceController<Project> {

    private static final long serialVersionUID = -5625084702553576277L;

    @Autowired
    private transient ModsTransformer.ProjectTransformer projectModsTransformer;
    @Autowired
    private transient DcTransformer.ProjectTransformer projectDcTransformer;

    private List<Resource> recentlyEditedResources = new ArrayList<Resource>();
    private List<Project> emptyProjects = new ArrayList<Project>();

    private int maxRecentResources = 5;

    private String callback;

    @Override
    protected Project createResource() {
        return new Project();
    }

    /**
     * Projects contain no additional metadata beyond basic Resource metadata so saveBasicResourceMetadata() should work.
     */
    @Override
    protected String save(Project resource) {
        getLogger().trace("saving a project");
        super.saveBasicResourceMetadata();
        getLogger().trace("saved metadata -- about to call saveOrUPdate");
        getProjectService().saveOrUpdate(resource);
        getLogger().trace("finished calling saveorupdate");

        // reindex any child resources so that that searches will pick up any new keywords they should "inherit"
        getSearchIndexService().indexCollection(getProject().getInformationResources());
        return SUCCESS;
    }

    @Override
    protected Project loadResourceFromId(Long resourceId) {
        return getProjectService().find(resourceId);
    }

    @Override
    protected void delete(Project project) {
        getLogger().debug("Deleting project id = " + project.getId());
        // make sure that the fileService takes care of removing file from the
        // filestore on delete
        resource.setStatus(Status.DELETED);
        getProjectService().update(resource);
    }

    @Override
    public void loadListData() {
        super.loadListData();
        recentlyEditedResources = getProjectService().findRecentlyEditedResources(getAuthenticatedUser(), maxRecentResources);
        emptyProjects = getProjectService().findEmptyProjects(getAuthenticatedUser());
    }

    // FIXME: this belongs in the abstractResourcController, and there should be an abstract method that returns gives hints to json() on which fields to
    // serialize
    @Action(value = "json",
            interceptorRefs = { @InterceptorRef("unAuthenticatedStack") },
            results = { @Result(
                            name = SUCCESS,
                            location = "json.ftl",
                            params = { "contentType", "application/json" },
                            type = "freemarker"
                      ) }
            )
            public String json() {
        return SUCCESS;
    }

    public String getProjectAsJson() {
        getLogger().trace("getprojectasjson called");
        String json = "{}";
        try {
            Project project = getProject();
            if (project == null || project.isTransient()) {
                getLogger().trace("Trying to convert blank or null project to json: " + project);
                return json;
            }
            json = project.toJSON().toString();
        } catch (Exception ex) {
            addActionErrorWithException("There was an error retreiving project-level information for this resource.  Please reload the page " +
            		" or report this problem to an administrator if the problem persists.", ex);
        }
        getLogger().debug("returning json:" + json);
        return json;
    }

    public Project getProject() {
        return resource;
    }

    public void setProject(Project project) {
        this.resource = project;
    }

    public ModsTransformer<Project> getModsTransformer() {
        return projectModsTransformer;
    }

    @Override
    public DcTransformer<Project> getDcTransformer() {
        return projectDcTransformer;
    }

    public List<Resource> getRecentlyEditedResources() {
        return recentlyEditedResources;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    /**
     * @param emptyProjects the emptyProjects to set
     */
    public void setEmptyProjects(List<Project> emptyProjects) {
        this.emptyProjects = emptyProjects;
    }

    /**
     * @return the emptyProjects
     */
    public List<Project> getEmptyProjects() {
        return emptyProjects;
    }

}

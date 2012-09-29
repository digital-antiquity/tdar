package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
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

    private String callback;

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

        return SUCCESS;
    }

    @Override
    public void postSaveCleanup() {
        // reindex any child resources so that that searches will pick up any new keywords they should "inherit"
        logger.debug("reindexing project contents");
        getSearchIndexService().indexCollection(getProject().getInformationResources());
    }

    // FIXME: this belongs in the abstractResourcController, and there should be an abstract method that returns gives hints to json() on which fields to
    // serialize
    @Action(value = "json",
            results = { @Result(
                    name = SUCCESS,
                    location = "json.ftl",
                    params = { "contentType", "application/json" },
                    type = "freemarker"
                    ) }
            )
            @SkipValidation
            public String json() {
        return SUCCESS;
    }

    @Override
    public Collection<? extends Persistable> getDeleteIssues() {
        List<Resource> issues = new ArrayList<Resource>();
        if (getProject() != null && getProject().getInformationResources() != null) {
            for (Resource resource : getProject().getInformationResources()) {
                if (!resource.isDeleted()) {
                    issues.add(resource);
                }
            }
        }
        return issues;
    }

    @SkipValidation
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
        return getPersistable();
    }

    public ModsTransformer<Project> getModsTransformer() {
        return projectModsTransformer;
    }

    @Override
    public DcTransformer<Project> getDcTransformer() {
        return projectDcTransformer;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void setProject(Project project) {
        setPersistable(project);
    }

    public Class<Project> getPersistableClass() {
        return Project.class;
    }
}

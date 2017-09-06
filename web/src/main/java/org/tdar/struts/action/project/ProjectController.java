package org.tdar.struts.action.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.resource.AbstractResourceController;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;

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
    private transient ProjectService projectService;

    @Autowired
    private transient SearchIndexService searchIndexService;

    private String callback;
    private SortOption secondarySortField;
    private SortOption sortField;

    private InputStream jsonInputStream;

    /**
     * Projects contain no additional metadata beyond basic Resource metadata so saveBasicResourceMetadata() should work.
     */
    @Override
    protected String save(Project resource) {
        getLogger().trace("saving a project");
        saveBasicResourceMetadata();
        getLogger().trace("saved metadata -- about to call saveOrUPdate");
        getGenericService().saveOrUpdate(resource);
        getLogger().trace("finished calling saveorupdate");
        return SUCCESS;
    }

    @Override
    public void indexPersistable() throws SearchIndexException, IOException {
        if (isAsync()) {
            searchIndexService.indexProjectAsync(getPersistable());
        } else {
            searchIndexService.indexProject(getPersistable());
        }
    }

    @Action(value = "json/{id}",
            results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" }) })
    @SkipValidation
    @HttpForbiddenErrorResponseOnly
    public String json() {
        setJsonInputStream(new ByteArrayInputStream(projectService.getProjectAsJson(getProject(), getAuthenticatedUser(), getCallback()).getBytes()));
        return SUCCESS;
    }

    public Project getProject() {
        return getPersistable();
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

    @Override
    public Class<Project> getPersistableClass() {
        return Project.class;
    }

    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

    public void setSortField(SortOption sortField) {
        this.sortField = sortField;
    }

    public List<SortOption> getSortOptions() {
        List<SortOption> options = SortOption.getOptionsForContext(Resource.class);
        options.remove(SortOption.RESOURCE_TYPE);
        options.remove(SortOption.RESOURCE_TYPE_REVERSE);
        options.add(0, SortOption.RESOURCE_TYPE);
        options.add(1, SortOption.RESOURCE_TYPE_REVERSE);
        return options;
    }

    public List<DisplayOrientation> getResultsOrientations() {
        List<DisplayOrientation> options = Arrays.asList(DisplayOrientation.values());
        return options;
    }

    public InputStream getJsonInputStream() {
        return jsonInputStream;
    }

    public void setJsonInputStream(InputStream jsonInputStream) {
        this.jsonInputStream = jsonInputStream;
    }

    public SortOption getSecondarySortField() {
        return secondarySortField;
    }

    public SortOption getSortField() {
        return sortField;
    }

}

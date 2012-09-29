package org.tdar.struts.action.resource;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.AuthenticationAware;

/**
 * $Id$
 * 
 * Can probably remove this controller class.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/resource")
public class ResourceController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 7080916909862991142L;

    private final SortedMap<ResourceType, String> resourceTypes = new TreeMap<ResourceType, String>();

    // incoming data from /resource/add
    private ResourceType resourceType;
    private Long projectId;

    private Long resourceId;

    /**
     * Passthrough action, just loads add.ftl via conventions plugin.
     */
    @Action(value = "add")
    public String execute() {
        return SUCCESS;
    }

    /**
     * Used to edit an existing resource's resource type / document type / etc.
     * 
     * @return
     */
    @Action(value = "edit",
            results = {
                    @Result(name = INPUT, location = "add.ftl"),
                    @Result(name = "DATASET", type = "redirect", location = "/dataset/edit?resourceId=${resource.id}"),
                    @Result(name = "DOCUMENT", type = "redirect", location = "/document/edit?resourceId=${resource.id}"),
                    @Result(name = "ONTOLOGY", type = "redirect", location = "/ontology/edit?resourceId=${resource.id}"),
                    @Result(name = "IMAGE", type = "redirect", location = "/image/edit?resourceId=${resource.id}"),
                    @Result(name = "SENSORY_DATA", type = "redirect", location = "/sensory-data/edit?resourceId=${resource.id}"),
                    @Result(name = "CODING_SHEET", type = "redirect", location = "/coding-sheet/edit?resourceId=${resource.id}")
            })
    public String edit() {
        InformationResource informationResource = getInformationResourceService().find(resourceId);
        if (informationResource == null) {
            getLogger().error("trying to edit information resource but it was null.");
            addActionError("Information resource wasn't loaded properly, please file a bug report.  Thanks!");
            return INPUT;
        }
        if (resourceType == null) {
            addFieldError("resourceType", "Please enter a resource type.");
            return INPUT;
        }

        return resourceType.name();
    }

    /**
     * Used to edit an existing resource's resource type / document type / etc.
     * 
     * @return
     */
    @Action(value = "view",
            results = {
                    @Result(name = "DATASET", type = "redirect", location = "/dataset/view?id=${resourceId}"),
                    @Result(name = "DOCUMENT", type = "redirect", location = "/document/view?id=${resourceId}"),
                    @Result(name = "ONTOLOGY", type = "redirect", location = "/ontology/view?id=${resourceId}"),
                    @Result(name = "IMAGE", type = "redirect", location = "/image/view?id=${resourceId}"),
                    @Result(name = "SENSORY_DATA", type = "redirect", location = "/sensory-data/view?id=${resourceId}"),
                    @Result(name = "CODING_SHEET", type = "redirect", location = "/coding-sheet/view?id=${resourceId}")
            })
    public String view() {
        InformationResource informationResource = getInformationResourceService().find(resourceId);
        if (informationResource == null) {
            getLogger().error("trying to edit information resource but it was null.");
            addActionError("Information resource wasn't loaded properly, please file a bug report.  Thanks!");
            return NOT_FOUND;
        }

        return informationResource.getResourceType().name();
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Returns a sorted map of the allowable subset of resource types that can be
     * entered.
     */
    public SortedMap<ResourceType, String> getResourceTypes() {
        synchronized (resourceTypes) {
            if (resourceTypes.isEmpty()) {
                addResourceType(ResourceType.CODING_SHEET);
                addResourceType(ResourceType.DATASET);
                addResourceType(ResourceType.DOCUMENT);
                addResourceType(ResourceType.SENSORY_DATA);
                addResourceType(ResourceType.IMAGE);
                addResourceType(ResourceType.ONTOLOGY);
            }
        }
        return resourceTypes;
    }

    private void addResourceType(ResourceType resourceType) {
        resourceTypes.put(resourceType, resourceType.getLabel());
    }

    // FIXME: this is really ugly, simplify + refactor
    public Project getProject() {
        Project project = getProjectService().find(projectId);
        projectId = project.getId();
        return project;
    }

    public Long getProjectId() {
        if (projectId == null || projectId == -1L) {
            getProject();
        }
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getResourceId() {
        return resourceId;
    }
}

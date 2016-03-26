package org.tdar.struts.action.resource;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;


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

    public static final String BILLING = "billing";

    @Autowired
    private transient BillingAccountService accountService;

    // incoming data from /resource/add
    private ResourceType resourceType;
    private Long projectId;
    private Resource resource;

    private Long resourceId;

    /**
     * Passthrough action, just loads add.ftl via conventions plugin.
     */
    @Actions(value = {
            @Action(value = "add",
                    results = {
                            @Result(name = BILLING, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.CART_ADD),
                            @Result(name = CONTRIBUTOR, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.MY_PROFILE),
                            @Result(name = SUCCESS, location = "add.ftl")
                    }),
            @Action(value = "add/{projectId}",
                    results = {
                            @Result(name = BILLING, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.CART_ADD),
                            @Result(name = CONTRIBUTOR, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.MY_PROFILE),
                            @Result(name = SUCCESS, location = "add.ftl")
                    })

    })
    @Override
    public String execute() {
        if (!isContributor()) {
            addActionMessage(getText("resourceController.must_be_contributor"));
            return CONTRIBUTOR;
        }
        accountService.assignOrphanInvoicesIfNecessary(getAuthenticatedUser());
        if (!getTdarConfiguration().isPayPerIngestEnabled() || isAllowedToCreateResource()) {
            return SUCCESS;
        }
        addActionMessage(getText("resourceController.requires_funds"));
        return BILLING;
    }

    /**
     * Used to edit an existing resource's resource type / document type / etc.
     * 
     * @return
     */
    @Action(value = "{resourceId}/edit",
            results = {
                    @Result(name = INPUT, location = "add.ftl"),
                    @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = "/${resource.urlNamespace}/edit?id=${resource.id}")
            })
    public String edit() {
        resource = getGenericService().find(InformationResource.class, resourceId);
        if (resource == null) {
            getLogger().error("trying to edit information resource but it was null.");
            addActionError(getText("resourceController.invalid"));
            return INPUT;
        }
        if (resourceType == null) {
            addFieldError("resourceType", "Please enter a resource type.");
            return INPUT;
        }

        return SUCCESS;
    }

    public boolean isAllowedToCreateResource() {
        getLogger().trace("ppi: {}", getTdarConfiguration().isPayPerIngestEnabled());
        return (!getTdarConfiguration().isPayPerIngestEnabled() || accountService.hasSpaceInAnAccount(getAuthenticatedUser(), null));
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Long getProjectId() {
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

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    } 

}

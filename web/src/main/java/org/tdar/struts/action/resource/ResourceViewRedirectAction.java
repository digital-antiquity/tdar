package org.tdar.struts.action.resource;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.action.TdarActionSupport;

/**
 * $Id$
 * 
 * Can probably remove this controller class.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
@ParentPackage("default")
@Scope("prototype")
@Namespace("/resource")
public class ResourceViewRedirectAction extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = 7284484498129329713L;

    private Resource resource;
    private Long resourceId;

    /**
     * Used to edit an existing resource's resource type / document type / etc.
     * 
     * @return
     */
    @Action(value = "{resourceId}",
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = "${resource.detailUrl}"),
                    @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.FREEMARKERHTTP,
                    location = "/WEB-INF/content/errors/error.ftl",
                    params = { "status", "400" })
            })
    public String view() {
        setResource(getGenericService().find(Resource.class, getResourceId()));
        if (getResource() == null) {
            getLogger().error("trying to view information resource but it was null.");
            addActionError(getText("resourceController.not_found"));
            return NOT_FOUND;
        }
        return SUCCESS;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

}

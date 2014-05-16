package org.tdar.struts.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.resource.ResourceService;

@ParentPackage("secured")
@Namespace("/resource/duplicate")
@Component
@Scope("prototype")
public class DuplicateResourceController extends AuthenticationAware.Base {

    @Autowired
    private transient ImportService importService;

    @Autowired
    private transient ResourceService resourceService;

    private static final long serialVersionUID = -3844493016660189167L;
    private Long id;
    private Resource resource;
    private Resource copy;

    @Action(value="duplicate",results= {
            @Result(name = SUCCESS, type=TYPE_REDIRECT, location = "/${copy.resourceType.urlNamespace}/view?id=${copy.id}"),
            @Result(name = INPUT, type="freemarker", location = "/resource/duplicate_error.ftl")
    })
    public String execute() {
        if (!getAuthenticatedUser().isContributor()) {
            addActionError("resourceController.must_be_contribytor");
            return INPUT;
        }
        try {
            setCopy(importService.cloneResource(resource, getAuthenticatedUser()));
            addActionMessage(getText("duplicateResourceController.duplicate_success"));
        } catch (Exception e) {
            addActionErrorWithException(getText("duplicateResourceController.could_not_copy_resource"), e);
            return INPUT;
        }
        return SUCCESS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void validate() {
        if (Persistable.Base.isNullOrTransient(id)) {
            addFieldError("id", getText("duplicateResourceController.id_invalid"));
        }

        resource = resourceService.find(id);
        if (resource == null) {
            addFieldError("id", getText("duplicateResourceController.id_invalid_not_exist"));
        }
    }

    public Resource getCopy() {
        return copy;
    }

    public void setCopy(Resource copy) {
        this.copy = copy;
    }
}

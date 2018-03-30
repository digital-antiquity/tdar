package org.tdar.struts.action.export;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.struts_base.interceptor.annotation.PostOnly;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/export")
public class ResourceExportAction extends AbstractResourceExportAction {

    private static final long serialVersionUID = -3023487888719749700L;

    @Override
    @Action(value = "perform", results = {
            @Result(name = SUCCESS, location = "perform.ftl"),
            @Result(name = INPUT, location = "request.ftl")
    })
    @PostOnly
    public String execute() {
        resourceExportService.exportAsync(getExportProxy(), false, getAuthenticatedUser());
        return SUCCESS;
    }

}

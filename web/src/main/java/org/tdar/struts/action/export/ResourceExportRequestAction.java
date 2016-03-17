package org.tdar.struts.action.export;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/export")
public class ResourceExportRequestAction extends AbstractResourceExportAction {

    private static final long serialVersionUID = -7292280428686745482L;

    @Override
    @Action(value = "request", results = {
            @Result(name = SUCCESS, location = "request.ftl")
    })
    public String execute() {
        return SUCCESS;
    }
    
}

package org.tdar.struts.action.resource;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class ResourceComparisonAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 1996434590439580868L;
    private List<Long> ids;
    private List<Resource> resources;
    
    @Autowired
    private GenericService genericService;
    
    @Override
    public void prepare() throws Exception {
        resources = genericService.findAll(Resource.class, ids);
    }

    @Action(value = "compare", results = {
            @Result(name = SUCCESS, location = "compare.ftl")
    })
    @Override
    public String execute() {
        return SUCCESS;
    }
    
    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

}

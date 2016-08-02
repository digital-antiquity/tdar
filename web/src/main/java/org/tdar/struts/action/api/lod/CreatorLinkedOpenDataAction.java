package org.tdar.struts.action.api.lod;

import java.io.ByteArrayInputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/lod/creator")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpForbiddenErrorResponseOnly
public class CreatorLinkedOpenDataAction extends AbstractJsonApiAction implements Preparable  {

    private static final long serialVersionUID = 7428998317675870472L;
    private Long id;
    @Autowired
    private GenericService genericService;
    @Autowired
    private EntityService entityService;
    
    @Override
    public void prepare() throws Exception {
        Creator resource = genericService.find(Creator.class, id); 
        String message = entityService.getSchemaOrgJson(resource, null);
        setJsonInputStream(new ByteArrayInputStream(message.getBytes()));
    }
    
    @Action(value="{id}")
    @Override
    public String execute() throws Exception {
        return super.execute();
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

}

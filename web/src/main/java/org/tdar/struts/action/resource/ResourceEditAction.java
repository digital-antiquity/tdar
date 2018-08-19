package org.tdar.struts.action.resource;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

public class ResourceEditAction<P extends Persistable & Addressable> extends AbstractAuthenticatableAction implements Preparable {

    @Autowired
    private SerializationService serializationService;

    private static final long serialVersionUID = 1467806719681708555L;

    private Long id;
    private Resource resource = new Resource();
    private String json = "";

    @Override
    public void prepare() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(id)) {
            resource = getGenericService().find(Resource.class, id);
        }
        json = serializationService.convertToJson(resource);
    }

    @Override
    @Action(value = "edit", results = { @Result(name = SUCCESS, location = "edit.ftl") })
    public String execute() throws Exception {
        // TODO Auto-generated method stub
        return super.execute();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

}

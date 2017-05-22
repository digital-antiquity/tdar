package org.tdar.struts.action.api.lod;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/lod/collection")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpForbiddenErrorResponseOnly
public class CollectionLinkedOpenDataAction extends AbstractJsonApiAction implements Preparable  {

    private static final long serialVersionUID = 9142456920026569857L;
    private Long id;
    @Autowired
    private GenericService genericService;
    @Autowired
    private ResourceCollectionService collectionService;
    private Map<String,String> error = new HashMap<>();
    
    @Override
    public void prepare() throws Exception {
        error.put("status", getText("error.object_does_not_exist"));
        VisibleCollection resource = genericService.find(VisibleCollection.class, id); 
        if (resource == null) {
            addActionError("error.object_does_not_exist");
            setJsonObject(error);
            return;
        }
        String message = collectionService.getSchemaOrgJsonLD(resource);
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

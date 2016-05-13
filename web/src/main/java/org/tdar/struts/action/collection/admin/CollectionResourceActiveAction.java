package org.tdar.struts.action.collection.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin")
public class CollectionResourceActiveAction extends AbstractAuthenticatableAction implements Preparable {

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    
    private static final long serialVersionUID = -926906661391091555L;
    private Long id;
    private ResourceCollection collection;

    @Override
    public void prepare() throws Exception {
        setCollection(resourceCollectionService.find(id));
    }

    @Override
    @PostOnly
    @WriteableSession
    @Action(value = "makeActive/{id}", results={
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${collection.detailUrl}"),
    })
    public String execute() throws Exception {
        resourceCollectionService.makeResourcesInCollectionActive(getCollection(), getAuthenticatedUser(), null);
        return SUCCESS;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
        this.collection = collection;
    }

}

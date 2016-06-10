package org.tdar.struts.action.collection.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.struts.action.AbstractAuthenticatableAction;

import com.opensymphony.xwork2.Preparable;

@Scope("prototype")
public abstract class AbstractCollectionAdminAction extends AbstractAuthenticatableAction implements Preparable {

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    
    private static final long serialVersionUID = -926906661391091555L;
    private Long id;
    private ResourceCollection collection;

    @Override
    public void prepare() throws Exception {
        setCollection(resourceCollectionService.find(id));
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

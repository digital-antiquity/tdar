package org.tdar.struts.action.collection.admin;

import org.springframework.context.annotation.Scope;
import org.tdar.core.bean.collection.HasDisplayProperties;
import org.tdar.struts.action.AbstractAuthenticatableAction;

import com.opensymphony.xwork2.Preparable;

@Scope("prototype")
public abstract class AbstractCollectionAdminAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -926906661391091555L;
    private Long id;
    private HasDisplayProperties collection;

    @Override
    public void prepare() throws Exception {
        setCollection(getGenericService().find(HasDisplayProperties.class,id));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HasDisplayProperties getCollection() {
        return collection;
    }

    public void setCollection(HasDisplayProperties collection) {
        this.collection = collection;
    }

}

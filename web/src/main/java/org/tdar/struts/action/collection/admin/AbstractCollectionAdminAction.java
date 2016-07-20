package org.tdar.struts.action.collection.admin;

import org.springframework.context.annotation.Scope;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;

import com.opensymphony.xwork2.Preparable;

@Scope("prototype")
public abstract class AbstractCollectionAdminAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -926906661391091555L;
    private Long id;
    private VisibleCollection collection;


    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void validate() {
        if (!authorizationService.canEditCollection(getAuthenticatedUser(), getCollection())) {
            addActionError(getText("abstractAdminAction.cannot_edit"));
        }
    }

    @Override
    public void prepare() throws Exception {
        setCollection(getGenericService().find(VisibleCollection.class,id));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VisibleCollection getCollection() {
        return collection;
    }

    public void setCollection(VisibleCollection collection) {
        this.collection = collection;
    }

}

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
import org.tdar.search.service.index.SearchIndexService;
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
public class ReindexResourcesInCollectionAction extends AbstractAuthenticatableAction implements Preparable {

    /**
     * 
     */
    private static final long serialVersionUID = -5131633751574486076L;
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private SearchIndexService searchIndexService;

    private Long id;
    private ResourceCollection collection;
    private boolean async;

    @Override
    public void prepare() throws Exception {
        setCollection(resourceCollectionService.find(id));
    }

    @Override
    @PostOnly
    @WriteableSession
    @Action(value = "reindex/{id}", results={
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${collection.detailUrl}"),
    })
    public String execute() throws Exception {
        if (isAsync()) {
            searchIndexService.indexAllResourcesInCollectionSubTreeAsync(getCollection());
        } else {
            searchIndexService.indexAllResourcesInCollectionSubTree(getCollection());
        }
        return SUCCESS;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
        this.collection = collection;
    }

}

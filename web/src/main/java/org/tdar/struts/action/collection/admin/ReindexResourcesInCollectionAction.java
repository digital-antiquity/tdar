package org.tdar.struts.action.collection.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin/reindex")
public class ReindexResourcesInCollectionAction extends AbstractCollectionAdminAction implements Preparable {

    /**
     * 
     */
    private static final long serialVersionUID = -5131633751574486076L;

    @Autowired
    private SearchIndexService searchIndexService;

    private boolean async = true;

    @Override
    @PostOnly
    @WriteableSession
    @Action(value = "{id}", results={
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
    
    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }


}

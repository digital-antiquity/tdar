package org.tdar.struts.action.collection.admin;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection/admin/organize")
public class OrganizeCollectionAction extends AbstractCollectionAdminAction {

    private static final long serialVersionUID = 5856507473445291185L;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    private List<SharedCollection> tree;
    
    @Override
    @WriteableSession
    @Action(value = "{id}", results={
            @Result(name = SUCCESS, type = FREEMARKER, location = "../organize.ftl"),
    })
    public String execute() throws Exception {
        setTree(resourceCollectionService.findAllChildCollectionsOnly((SharedCollection)getCollection()));
        getTree().add((SharedCollection)getCollection());
        resourceCollectionService.reconcileCollectionTree(getTree(), getAuthenticatedUser(), PersistableUtils.extractIds(getTree()));
        return SUCCESS;
    }

    public List<SharedCollection> getTree() {
        return tree;
    }

    public void setTree(List<SharedCollection> tree) {
        this.tree = tree;
    }

}

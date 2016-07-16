package org.tdar.struts.action.collection.admin;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin/organize")
public class CollectionMoveAction extends AbstractCollectionAdminAction {

    private static final long serialVersionUID = 5856507473445291185L;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    private List<SharedCollection> tree;
    
    @Override
    @WriteableSession
    @Action(value = "{id}", results={
            @Result(name = SUCCESS, type = FREEMARKER, location = "../move.ftl"),
    })
    public String execute() throws Exception {
        setTree(resourceCollectionService.findAllChildCollectionsOnly((SharedCollection)getCollection(), SharedCollection.class));
        getTree().add((SharedCollection)getCollection());
        resourceCollectionService.reconcileCollectionTree(getTree(), getAuthenticatedUser(), PersistableUtils.extractIds(getTree()), SharedCollection.class);
        return SUCCESS;
    }

    public List<SharedCollection> getTree() {
        return tree;
    }

    public void setTree(List<SharedCollection> tree) {
        this.tree = tree;
    }

}

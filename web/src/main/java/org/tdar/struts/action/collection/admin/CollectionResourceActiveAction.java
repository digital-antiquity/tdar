package org.tdar.struts.action.collection.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin/makeActive")
public class CollectionResourceActiveAction extends AbstractCollectionAdminAction implements Preparable {

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    
    private static final long serialVersionUID = -926906661391091555L;

    @Override
    @PostOnly
    @WriteableSession
    @Action(value = "{id}", results={
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${collection.detailUrl}"),
    })
    public String execute() {
        try {
        resourceCollectionService.makeResourcesInCollectionActive(getCollection(), getAuthenticatedUser());
        } catch (Exception e) {
            addActionError(e.getMessage());
            return INPUT;
        }
        return SUCCESS;
    }
}

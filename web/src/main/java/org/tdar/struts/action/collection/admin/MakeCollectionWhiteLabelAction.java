package org.tdar.struts.action.collection.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.CustomizableCollection;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin/makeWhitelabel")
public class MakeCollectionWhiteLabelAction extends AbstractCollectionAdminAction implements Preparable {

    private static final long serialVersionUID = 4671830242931274023L;

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    
    @Override
    public void validate() {
        if (!(getCollection() instanceof CustomizableCollection)) {
            addActionError("makeWhiteLableAction.invalid_collection_type");
        }
        super.validate();
    }
    
    @Override
    @PostOnly
    @WriteableSession
    @Action(value = "{id}", results={
            @Result(name = SUCCESS, type = REDIRECT, location = "${collection.detailUrl}"),
    })
    public String execute() throws Exception {
        CustomizableCollection lc = (CustomizableCollection)getCollection();
        if (lc.getProperties() != null && lc.getProperties().getWhitelabel()) {
            return SUCCESS;
        }
        try {
            setCollection(resourceCollectionService.convertToWhitelabelCollection(lc));
            getLogger().debug(getCollection().getDetailUrl());
        } catch (Exception e) {
            getLogger().error("{}",e,e);
        }
        return SUCCESS;
    }
    
}

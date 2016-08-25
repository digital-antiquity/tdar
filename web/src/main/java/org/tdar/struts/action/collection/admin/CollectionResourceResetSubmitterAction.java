package org.tdar.struts.action.collection.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin/changeSubmitter")
public class CollectionResourceResetSubmitterAction extends AbstractCollectionAdminAction implements Preparable {


    private static final long serialVersionUID = 5948960771249423227L;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Autowired
    private GenericService genericService;

    private Long submitterId;
    private TdarUser submitter;
    
    @Override
    public void prepare() throws Exception {
        super.prepare();
        if (PersistableUtils.isNotNullOrTransient(submitterId)) {
        this.submitter = genericService.find(TdarUser.class,submitterId);
        if (submitter == null) {
            addActionError("CollectionResourceResetSubmitterAction.specify_submitter");
        }
        }
        if (this.submitter == null) {
            submitter = genericService.find(TdarUser.class, getTdarConfiguration().getAdminUserId());
        }
    }
    
    @Override
    @PostOnly
    @WriteableSession
    @Action(value = "{id}", results={
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${collection.detailUrl}"),
    })
    public String execute() {
        try {
        resourceCollectionService.changeSubmitter(getCollection(), submitter, getAuthenticatedUser());
        } catch (Exception e) {
            addActionError(e.getMessage());
            return INPUT;
        }
        return SUCCESS;
    }

    public Long getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(Long submitterId) {
        this.submitterId = submitterId;
    }
}

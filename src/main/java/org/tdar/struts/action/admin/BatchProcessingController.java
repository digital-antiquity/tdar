package org.tdar.struts.action.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.FileAccessRestriction;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.BatchActionService;
import org.tdar.core.service.batch.BatchAction;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

/**
 * $Id$
 * 
 * Manages requests to create/delete/edit a Project and its associated metadata (including Datasets, etc).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/admin/batch")
@Component
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Scope("prototype")
public class BatchProcessingController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 3323084509125780562L;
    private BatchAction batchAction;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient BatchActionService batchService;

    @Override
    @Action("batch")
    public String execute() {
        // removing duplicates
        return SUCCESS;
    }

    @Action("select-action")
    @PostOnly
    public String selectAction() {
        return SUCCESS;
    }

    @Action("confirm-action")
    @PostOnly
    public String confirmAction() {
        
        return SUCCESS;
    }

    @Action("complete-action")
    @PostOnly
    public String completeAction() {
        
        return SUCCESS;
    }

    public void prepare() {
    }

    public List<Status> getStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    public List<FileAccessRestriction> getRestrictions() {
        return new ArrayList<FileAccessRestriction>(Arrays.asList(FileAccessRestriction.values()));
    }

    public BatchAction getBatchAction() {
        return batchAction;
    }

    public void setBatchAction(BatchAction batchAction) {
        this.batchAction = batchAction;
    }

}

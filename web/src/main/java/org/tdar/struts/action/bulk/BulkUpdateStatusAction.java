package org.tdar.struts.action.bulk;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.bulk.BulkUpdateReceiver;
import org.tdar.core.service.bulk.BulkUploadService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.result.HasJsonDocumentResult;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Component
@HttpsOnly
@Scope("prototype")
@Namespace("/bulk")
public class BulkUpdateStatusAction extends AbstractAuthenticatableAction implements Preparable, HasJsonDocumentResult {

    private static final long serialVersionUID = -5855079741655022360L;

    @Autowired
    private transient BulkUploadService bulkUploadService;

    private Long ticketId;

    private BulkUpdateReceiver status = new BulkUpdateReceiver();

    @Override
    public void prepare() {
        BulkUpdateReceiver checkAsyncStatus = bulkUploadService.checkAsyncStatus(getTicketId());
        if (checkAsyncStatus != null) {
            setStatus(checkAsyncStatus);
        }
        if (getStatus() != null) {
            getLogger().debug("{} {}%", getStatus().getMessage(), getStatus().getPercentComplete());
        } 
    }
    
    @Override
    public Object getResultObject() {
        return getStatus();
    }

    @SkipValidation
    @Action(value = "checkstatus",
            results = { @Result(name = SUCCESS, type = JSONRESULT )})
    @PostOnly
    public String checkStatus() {
        return SUCCESS;
    }

    public Long getTicketId() {
        return ticketId;
    }


    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public BulkUpdateReceiver getStatus() {
        return status;
    }

    public void setStatus(BulkUpdateReceiver status) {
        this.status = status;
    }

}

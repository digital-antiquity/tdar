package org.tdar.struts.action.resource.request;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.external.EmailService;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;
/**
 * Process the Request to grant permissions (or reject)
 * @author abrin
 *
 */
@ParentPackage("secured")
@Namespace("/resource/request")
@Component
@Scope("prototype")
public class ProcessPermissonsAction extends AbstractProcessPermissonsAction implements Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = 4719778524052804432L;
    private boolean reject = false;
    private String comment;
    @Autowired
    private transient EmailService emailService;

    @Override
    public void prepare() throws Exception {
        super.prepare();
    }

    @Override
    public void validate() {
        super.validate();
        if (getPermission() == null) {
            addActionError("requestPermissionsController.specify_permission");
        }

        if (reject && StringUtils.isBlank(comment)) {
            addActionError("requestPermissionsController.comment_required");
        }
    }

    @Action(value = "process-access-request",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.REDIRECT, location = "/${resource.urlNamespace}/${resource.id}/${resource.slug}"),
                    @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" }),
                    @Result(name = INPUT, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" })
            })
    @PostOnly
    @WriteableSession
    @HttpsOnly
    public String processAccessRequest() throws TdarActionException {
        emailService.proccessPermissionsRequest(getRequestor(), getResource(), getAuthenticatedUser(), getComment(),isReject(), getType(), getPermission());
        if (isReject()) {
            addActionMessage("Access has been denied");
        } else {
            addActionMessage("Access has been granted");
        }
        return SUCCESS;
    }

    public boolean isReject() {
        return reject;
    }

    public void setReject(boolean reject) {
        this.reject = reject;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}

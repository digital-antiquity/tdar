package org.tdar.struts.action.account;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;

import com.opensymphony.xwork2.Validateable;

@ParentPackage("default")
@Namespace("/account")
@Component
@Scope("prototype")
@HttpsOnly
public class DisableAccountAction extends AbstractAuthenticatableAction implements Validateable {

    private static final long serialVersionUID = 8607783069295176021L;
    private String delete;
    private String deletionReason;

    @Autowired
    private AuthenticationService authenticationService;

    @Action(value = "delete",
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/"),
                    @Result(name = CONFIRM, location = "/WEB-INF/content/account/confirm-disable.ftl")
            })
    @SkipValidation
    public String execute() {
        if (!isPostRequest() || !DELETE.equals(getDelete())) {
            return CONFIRM;
        }

        getLogger().error("user is disabling account: {} -- {}", getAuthenticatedUser(), getDeletionReason());
        authenticationService.disableAccount(getSessionData(), getServletRequest(), getServletResponse(), getAuthenticatedUser());
        return SUCCESS;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

}

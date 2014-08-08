package org.tdar.struts.action.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class AdminEmailController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 7908324276339775315L;
    private List<Email> emails = new ArrayList<>();
    
    @Action("email")
    public String execute() {
        setEmails(getGenericService().findAll(Email.class));
        return SUCCESS;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }
    
    
}

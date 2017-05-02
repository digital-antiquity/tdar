package org.tdar.struts.action.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.service.external.EmailService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@HttpsOnly
public class AdminEmailController extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 7908324276339775315L;
    private List<Email> emails = new ArrayList<>();
    private List<Email> emailsToReview = new ArrayList<>();
    private List<Long> ids = new ArrayList<>();
    private Status emailAction;

    @Autowired
    private transient EmailService emailService;

    @Action("email")
    public String execute() {
        setEmails(getGenericService().findAll(Email.class));
        Collections.sort(emails, new Comparator<Email>() {

            @Override
            public int compare(Email o1, Email o2) {
                if (o1.getId() < o2.getId()) {
                    return 0;
                }
                else {
                    return 1;
                }
            }
        });

        for (Email email : emails) {
            if (email.getStatus().isInReview()) {
                emailsToReview.add(email);
            }
        }

        return SUCCESS;
    }

    @Action(value = "changeEmailStatus",
            results = {
                    @Result(name = SUCCESS, type=REDIRECT, location = "/admin/email"),
                    @Result(name = INPUT, location = "email.ftl") }
            )
            @PostOnly
            public String changeEmailStatus() {
        emailService.changeEmailStatus(getEmailAction(), emails);

        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {
        if (CollectionUtils.isNotEmpty(ids)) {
            if (getEmailAction() == null) {
                addActionError(getText("adminEmailController.please_specify_status"));
            }
            setEmails(getGenericService().findAll(Email.class, ids));
        }
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public List<Status> getEmailActions() {
        return Arrays.asList(Status.values());
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Status getEmailAction() {
        return emailAction;
    }

    public void setEmailAction(Status emailAction) {
        this.emailAction = emailAction;
    }

    public List<Email> getEmailsToReview() {
        return emailsToReview;
    }

    public void setEmailsToReview(List<Email> emailsToReview) {
        this.emailsToReview = emailsToReview;
    }

}

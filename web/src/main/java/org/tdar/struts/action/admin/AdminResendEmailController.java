package org.tdar.struts.action.admin;

import java.io.IOException;
import java.util.Date;

import javax.mail.MessagingException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.service.external.EmailService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

public class AdminResendEmailController extends AbstractAuthenticatableAction implements Preparable {

    private Long emailId;

    private Email email;

    @Autowired
    private transient EmailService emailService;

    @WriteableSession
    @Action(value = "confirmResendEmail",
            results = {
                    @Result(name = SUCCESS, location = "confirmResendEmail.ftl"),
            })
    public String confirmEmail() {

        return SUCCESS;
    }

    @WriteableSession
    @PostOnly
    @Action(value = "resendEmail",
            results = {
                    @Result(name = SUCCESS, type = REDIRECT, location = "/admin/email"),
            })
    public String resendEmail() {
        try {
            emailService.sendAwsHtmlMessage(email);
            email.setStatus(Status.SENT);
            email.setDateSent(new Date());
            getGenericService().saveOrUpdate(email);
        } catch (MailException | IOException | MessagingException me) {
            email.setStatus(Status.ERROR);
            email.setErrorMessage(me.getMessage());
            getLogger().error("email error: {} {}", email, me);
        }
        addActionMessage("Resent message " + getEmailId() + " to " + email.getTo());

        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {

        if (getEmailId() == null) {
            addActionError(getText("adminEmailController.please_specify_status"));
        }

        email = getGenericService().find(Email.class, getEmailId());
        email = emailService.dequeue(email);
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Long getEmailId() {
        return emailId;
    }

    public void setEmailId(Long emailId) {
        this.emailId = emailId;
    }

}

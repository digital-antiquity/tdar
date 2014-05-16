package org.tdar.struts.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.external.EmailService;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.utils.EmailMessageType;

@ParentPackage("secured")
@Namespace("/email")
@Component
@Scope("prototype")
public class EmailController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 2598289601940169922L;

    private AntiSpamHelper h;
    private Long fromId;
    private Long toId;
    private String subject;
    private String messageBody;
    private EmailMessageType type;

    @Autowired
    private EmailService emailService;

    @Action("deliver")
    public String execute() {
        h.checkForSpammers();
        emailService.constructEmail(fromId, toId, subject, messageBody, type);
        return SUCCESS;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public Long getFromId() {
        return fromId;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public Long getToId() {
        return toId;
    }

    public void setToId(Long toId) {
        this.toId = toId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public EmailMessageType getType() {
        return type;
    }

    public void setType(EmailMessageType type) {
        this.type = type;
    }

}

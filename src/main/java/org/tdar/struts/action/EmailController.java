package org.tdar.struts.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.EmailService;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.utils.EmailMessageType;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/email")
@Component
@Scope("prototype")
public class EmailController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = 2598289601940169922L;

    private AntiSpamHelper h = new AntiSpamHelper();
    private Long fromId;
    private Long toId;
    private TdarUser from;
    private TdarUser to;
    private String subject;
    private String messageBody;
    private EmailMessageType type;
    private Map<String, Object> jsonResult = new HashMap<>();

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient GenericService genericService;

    @Action(value = "deliver", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "jsonObject", "jsonResult" }),
            @Result(name = INPUT, type = JSONRESULT, params = { "jsonObject", "jsonResult", "statusCode", "500" })
    })
    @PostOnly
    public String execute() {
        emailService.constructEmail(fromId, toId, subject, messageBody, type);
        jsonResult.put("status", "QUEUED");
        
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

    @Override
    public void prepare() throws Exception {
        h.checkForSpammers();
        from = genericService.find(TdarUser.class, fromId);
        to = genericService.find(TdarUser.class, toId);
    }

    @Override
    public void validate() {
        if (Persistable.Base.isTransient(from)) {
            addActionError("emailController.from_not_found");
        }
        if (Persistable.Base.isTransient(to)) {
            addActionError("emailController.to_not_found");
        }
        if (StringUtils.isBlank(messageBody)) {
            addActionError("emailController.no_message");
        }
        if (type == null) {
            addActionError("emailController.no_type");
        }

    }

    public TdarUser getTo() {
        return to;
    }

    public void setTo(TdarUser to) {
        this.to = to;
    }

    public TdarUser getFrom() {
        return from;
    }

    public void setFrom(TdarUser from) {
        this.from = from;
    }

    public Map<String, Object> getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(Map<String, Object> jsonResult) {
        this.jsonResult = jsonResult;
    }

}

package org.tdar.struts.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.external.RecaptchaService;
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

    @Autowired
    private transient RecaptchaService recaptchaService;

    private AntiSpamHelper h = new AntiSpamHelper(recaptchaService);
    private Long fromId;
    private Long toId;
    private Person from;
    private Resource resource;
    private Long resourceId;
    private Person to;
    private String subject;
    private String messageBody;
    private EmailMessageType type;
    private Map<String, Object> jsonResult = new HashMap<>();

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient GenericService genericService;

    private Email email;

    @Action(value = "deliver", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "jsonObject", "jsonResult" }),
            @Result(name = INPUT, type = JSONRESULT, params = { "jsonObject", "jsonResult", "statusCode", "500" })
    })
    @PostOnly
    public String execute() {
        setEmail(emailService.constructEmail(from, to, resource, subject, messageBody, type));
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
        h.checkForSpammers(true);
        from = genericService.find(Person.class, fromId);
        to = genericService.find(Person.class, toId);
        resource = genericService.find(Resource.class, resourceId);
    }

    @Override
    public void validate() {
        if (Persistable.Base.isTransient(from)) {
            addActionError(getText("emailController.from_not_found"));
        }
        if (Persistable.Base.isTransient(to)) {
            addActionError(getText("emailController.to_not_found"));
        }
        if (StringUtils.isBlank(messageBody)) {
            addActionError(getText("emailController.no_message"));
        }
        if (Persistable.Base.isNotNullOrTransient(resourceId) && Persistable.Base.isNullOrTransient(resource)) {
            addActionError(getText("emailController.no_resource"));
        }
        if (type == null) {
            addActionError(getText("emailController.no_type"));
        } else if (getType().requiresResource() && Persistable.Base.isNullOrTransient(resource)) {
            addActionError(getText("emailController.no_resource"));
        }

    }

    public Person getTo() {
        return to;
    }

    public void setTo(Person to) {
        this.to = to;
    }

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
    }

    public Map<String, Object> getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(Map<String, Object> jsonResult) {
        this.jsonResult = jsonResult;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

}

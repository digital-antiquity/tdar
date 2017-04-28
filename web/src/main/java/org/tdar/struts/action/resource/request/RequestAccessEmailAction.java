package org.tdar.struts.action.resource.request;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ParameterAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.RequestCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.HasEmail;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;
/**
 * Sends email for the request-access / contact action
 * @author abrin
 *
 */
@ParentPackage("secured")
@Namespace("/resource/request")
@Component
@Scope("prototype")
public class RequestAccessEmailAction extends AbstractRequestAccessController implements Preparable, ParameterAware {

    private static final long serialVersionUID = 2598289601940169922L;
    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient RecaptchaService recaptchaService;

    private AntiSpamHelper h = new AntiSpamHelper();
    private Long fromId;
    private Long toId;
    private Person from;
    private Resource resource;
    private Long resourceId;
    private HasEmail to;
    private String subject;
    private String messageBody;
    private EmailMessageType type;

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient GenericService genericService;
    private Map<String, String[]> params;

    @Action(value = "deliver", results = {
            @Result(name = SUCCESS, type = REDIRECT, location="${resource.detailUrl}"),
            @Result(name = INPUT, type = REDIRECT, location=AbstractRequestAccessController.SUCCESS_REDIRECT_REQUEST_ACCESS)
    })
    @PostOnly
    public String execute() {
        // if we're in the SAA process, then override the "to" with the specified ID
        if (type == EmailMessageType.CUSTOM) {
            RequestCollection custom = resourceService.findCustom(getResource());
            to = custom.getContact();
        }
        emailService.constructEmail(from, to, resource, subject, messageBody, type, params);
        addActionMessage("Message Sent");

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
    public void prepare() {
    	super.prepare();
        h.checkForSpammers(recaptchaService, true, getServletRequest().getRemoteHost(), null, false);
        from = genericService.find(Person.class, fromId);
        to = genericService.find(Creator.class, toId);
    }

    @Override
    public void validate() {
        if (PersistableUtils.isTransient(from)) {
            addActionError(getText("emailController.from_not_found"));
        }
        if (PersistableUtils.isTransient(to)) {
            addActionError(getText("emailController.to_not_found"));
        }
        if (StringUtils.isBlank(messageBody)) {
            addActionError(getText("emailController.no_message"));
        }
        if (PersistableUtils.isNotNullOrTransient(resourceId) && PersistableUtils.isNullOrTransient(resource)) {
            addActionError(getText("emailController.no_resource"));
        }
        if (type == null) {
            addActionError(getText("emailController.no_type"));
        } else if (getType().requiresResource() && PersistableUtils.isNullOrTransient(resource)) {
            addActionError(getText("emailController.no_resource"));
        }

    }

    public HasEmail getTo() {
        return to;
    }

    public void setTo(HasEmail to) {
        this.to = to;
    }

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
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

    @Override
    public void setParameters(Map<String, String[]> arg0) {
        this.params = arg0;
    }

}

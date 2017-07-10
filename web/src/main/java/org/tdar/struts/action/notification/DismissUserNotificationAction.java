package org.tdar.struts.action.notification;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.service.UserNotificationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/notification")
@Component
@Scope("prototype")
public class DismissUserNotificationAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -1680185105953721985L;

    @Autowired
    private transient UserNotificationService userNotificationService;

    private Map<String, Object> jsonResult = new HashMap<>();
    private Long id;

    private UserNotification notification;

    @Action(value = "dismiss", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "jsonObject", "jsonResult" }),
            @Result(name = INPUT, type = JSONRESULT, params = { "jsonObject", "jsonResult", "statusCode", "500" })
    })
    @WriteableSession
    @PostOnly
    public String dismiss() {
        try {
            userNotificationService.dismiss(getAuthenticatedUser(), getNotification());
            jsonResult.put(SUCCESS, SUCCESS);
        } catch (Exception e) {
            addActionError(e.getLocalizedMessage());
            return INPUT;
        }
        return SUCCESS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void prepare() throws Exception {
        setNotification(userNotificationService.find(id));
    }

    public UserNotification getNotification() {
        return notification;
    }

    public void setNotification(UserNotification notification) {
        this.notification = notification;
    }

    public Map<String, Object> getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(Map<String, Object> jsonResult) {
        this.jsonResult = jsonResult;
    }
}

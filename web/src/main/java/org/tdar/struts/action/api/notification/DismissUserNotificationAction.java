package org.tdar.struts.action.api.notification;

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
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/api/notification")
@Component
@Scope("prototype")
public class DismissUserNotificationAction extends AbstractJsonApiAction implements Preparable {

    private static final long serialVersionUID = -1680185105953721985L;

    @Autowired
    private transient UserNotificationService userNotificationService;

    private Map<String, Object> resultObject = new HashMap<>();
    private Long id;

    private UserNotification notification;

    @Action(value = "dismiss")
    @WriteableSession
    @PostOnly
    public String dismiss() {
        try {
            userNotificationService.dismiss(getAuthenticatedUser(), getNotification());
            resultObject.put(SUCCESS, SUCCESS);
            setJsonObject(resultObject);
            getLogger().debug("id: {} notification: {}, user: {}", getId(), getNotification(), getAuthenticatedUser());
        } catch (Throwable e) {
            getLogger().error(e.getMessage(),e);
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
        try {
        setNotification(userNotificationService.find(id));
        } catch (Throwable t) {
            getLogger().error(t.getMessage(),t);
            addActionErrorWithException(t.getMessage(), t);
        }
    }

    public UserNotification getNotification() {
        return notification;
    }

    public void setNotification(UserNotification notification) {
        this.notification = notification;
    }

}

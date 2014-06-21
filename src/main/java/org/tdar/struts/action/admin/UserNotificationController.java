package org.tdar.struts.action.admin;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.util.UserNotification;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.UserNotificationService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

/**
 * Manages admin CRUD requests for UserNotifications.
 */
@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/notifications")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class UserNotificationController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 5844268857719107949L;
    
    private UserNotification notification = new UserNotification();
    private List<UserNotification> allNotifications;
    
    @Autowired
    private UserNotificationService userNotificationService;
    
    
    @Actions({
        @Action("index"),
        @Action("/admin/notifications")
    })
    public String execute() {
        allNotifications = userNotificationService.findAll();
        Collections.sort(allNotifications);
        return SUCCESS;
    }
    
    @Action("update")
    @PostOnly
    public String update() {
        return SUCCESS;
    }
    
    @Action("delete")
    @PostOnly
    public String delete() {
        return SUCCESS;
    }

    @Action("add")
    @PostOnly
    public String add() {
        return SUCCESS;
    }

    public UserNotification getNotification() {
        return notification;
    }

    public void setNotification(UserNotification notification) {
        this.notification = notification;
    }

    public List<UserNotification> getAllNotifications() {
        return allNotifications;
    }

}

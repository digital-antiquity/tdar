package org.tdar.struts.action.dashboard;

import java.io.IOException;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Manages requests to create/delete/edit a Project and its associated metadata
 * (including Datasets, etc).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/dashboard")
@Component
@Scope("prototype")
public class FilleListAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -224826703370233994L;

    private List<UserNotification> currentNotifications;

    @Override
    public void validate() {
        // if (PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
        // addActionError(getText("dashboardController.user_must_login"));
        // }
        // super.validate();
    }

    @Override
    @Action(value = "files", results = { @Result(name = SUCCESS, location = "files.ftl") })
    public String execute() {

        return SUCCESS;
    }

    @Override
    public void prepare() {
        // setupBookmarks();
    }

    public List<UserNotification> getCurrentNotifications() {
        return currentNotifications;
    }

    public void setCurrentNotifications(List<UserNotification> currentNotifications) {
        this.currentNotifications = currentNotifications;
    }

}

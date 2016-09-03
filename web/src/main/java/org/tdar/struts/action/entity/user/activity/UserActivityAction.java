package org.tdar.struts.action.entity.user.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/entity/user/activity")
public class UserActivityAction extends AbstractAuthenticatableAction implements Preparable, Validateable {

    private static final long serialVersionUID = 3403397540791227178L;
    private Long id;
    private TdarUser user;
    private List<ResourceRevisionLog> logs = new ArrayList<>();
    
    @Autowired
    private EntityService entityService;
    @Autowired
    private GenericService genericService;
    private Date from = DateTime.now().minusMonths(6).toDate();

    @Override
    public void prepare() throws Exception {
        this.user = genericService.find(TdarUser.class, id);
        if (user != null) {
            setLogs(entityService.findChangesForUser(user, getFrom() ));
        }
    }
    
    @Override
    @Action(value = "{id}", results = {
            @Result(name = SUCCESS, type = FREEMARKER, location = "activity.ftl"),
    })
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public void validate() {
        if (user == null) {
            addActionError("error.object_does_not_exist");
        }
    }

    public List<ResourceRevisionLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ResourceRevisionLog> logs) {
        this.logs = logs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }
    
}

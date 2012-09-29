package org.tdar.struts.action;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.external.auth.TdarGroup;
import org.tdar.struts.RequiresTdarUserGroup;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/system")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class AdminActivityController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 6261948544478872563L;

    @Autowired
    ScheduledProcessService scheduledProcessService;

    private Statistics sessionStatistics;
    private Boolean scheduledProcessesEnabled;

    private List<ScheduledProcess<Persistable>> allScheduledProcesses;

    private List<ScheduledProcess<Persistable>> scheduledProcessQueue;

    @Action(value = "activity")
    public String execute() {
        setScheduledProcessesEnabled(TdarConfiguration.getInstance().shouldRunPeriodicEvents());
        setSessionStatistics(getGenericService().getSessionStatistics());
        setAllScheduledProcesses(scheduledProcessService.getAllScheduledProcesses());
        setScheduledProcessQueue(scheduledProcessService.getScheduledProcessQueue());
        return SUCCESS;
    }

    public List<ScheduledProcess<Persistable>> getScheduledProcessQueue() {
        return scheduledProcessQueue;
    }

    public void setScheduledProcessQueue(List<ScheduledProcess<Persistable>> scheduledProcessQueue) {
        this.scheduledProcessQueue = scheduledProcessQueue;
    }

    public List<ScheduledProcess<Persistable>> getAllScheduledProcesses() {
        return allScheduledProcesses;
    }

    public void setAllScheduledProcesses(List<ScheduledProcess<Persistable>> allScheduledProcesses) {
        this.allScheduledProcesses = allScheduledProcesses;
    }

    public Statistics getSessionStatistics() {
        return sessionStatistics;
    }

    public void setSessionStatistics(Statistics sessionStatistics) {
        this.sessionStatistics = sessionStatistics;
    }

    public Boolean getScheduledProcessesEnabled() {
        return scheduledProcessesEnabled;
    }

    public void setScheduledProcessesEnabled(Boolean scheduledProcessesEnabled) {
        this.scheduledProcessesEnabled = scheduledProcessesEnabled;
    }

}

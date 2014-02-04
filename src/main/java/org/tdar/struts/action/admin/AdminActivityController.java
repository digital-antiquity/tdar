package org.tdar.struts.action.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.activity.IgnoreActivity;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/system")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
@IgnoreActivity
public class AdminActivityController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 6261948544478872563L;

    @Autowired
    ScheduledProcessService scheduledProcessService;

    private Statistics sessionStatistics;
    private Boolean scheduledProcessesEnabled;

    private List<ScheduledProcess<Persistable>> allScheduledProcesses;
    private Collection<ScheduledProcess<Persistable>> scheduledProcessQueue;

    private List<Activity> activityList = new ArrayList<Activity>();

    private HashMap<String, Integer> counters;
    private List<Person> activePeople;

    @Action(value = "active-users",
    interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
    results = {
            @Result(name = SUCCESS, location = "list-users.ftl",
                    params = { "contentType", "application/json" },
                    type = "freemarker"
                    ) 
            })
    public String listActiveUsers() {
        // FIXME: filter for localhost before enabling
//        setActivePeople(getAuthenticationAndAuthorizationService().getCurrentlyActiveUsers());
        return SUCCESS;
    }

    
    @Action(value = "activity")
    @Override
    public String execute() {
        setScheduledProcessesEnabled(TdarConfiguration.getInstance().shouldRunPeriodicEvents());
        setSessionStatistics(getGenericService().getSessionStatistics());
        setAllScheduledProcesses(scheduledProcessService.getAllScheduledProcesses());
        setScheduledProcessQueue(scheduledProcessService.getScheduledProcessQueue());
        getActivityList().addAll(ActivityManager.getInstance().getActivityQueueClone());
        Collections.sort(getActivityList(), new Comparator<Activity>() {

            @Override
            public int compare(Activity o1, Activity o2) {
                if (o1.getTotalTime() != -1L && o2.getTotalTime() != -1) {
                    return o1.getStartDate().compareTo(o2.getStartDate());
                }
                if (o1.getTotalTime() == -1L) {
                    return -1;
                }
                if (o2.getTotalTime() == -1L) {
                    return 1;
                }
                return 0;
            }

        });
        setCounters(new HashMap<String, Integer>());
        for (Activity activity : activityList) {
            Integer num = getCounters().get(activity.getSimpleBrowserName());
            if (num == null) {
                num = 1;
            } else {
                num++;
            }
            getCounters().put(activity.getSimpleBrowserName(), num);
        }

        setActivePeople(getAuthenticationAndAuthorizationService().getCurrentlyActiveUsers());

        return SUCCESS;
    }

    public Collection<ScheduledProcess<Persistable>> getScheduledProcessQueue() {
        return scheduledProcessQueue;
    }

    public void setScheduledProcessQueue(Set<ScheduledProcess<Persistable>> scheduledProcessQueue) {
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

    public List<Activity> getActivityList() {
        return activityList;
    }

    public void setActivityList(List<Activity> activityList) {
        this.activityList = activityList;
    }

    public HashMap<String, Integer> getCounters() {
        return counters;
    }

    public void setCounters(HashMap<String, Integer> counters) {
        this.counters = counters;
    }

    public List<Person> getActivePeople() {
        return activePeople;
    }

    public void setActivePeople(List<Person> activePeople) {
        this.activePeople = activePeople;
    }

}

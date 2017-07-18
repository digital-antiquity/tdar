package org.tdar.struts.action.admin;

import java.io.ByteArrayInputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.processes.ScheduledProcess;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.activity.IgnoreActivity;
import org.tdar.utils.json.JsonLookupFilter;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/system")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
@IgnoreActivity
public class AdminActivityController extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = 6261948544478872563L;

    @Autowired
    private transient ScheduledProcessService scheduledProcessService;

    @Autowired
    private transient SerializationService serializationService;

    @Autowired
    private transient AuthenticationService authenticationService;

    private Statistics sessionStatistics;
    private Boolean scheduledProcessesEnabled;

    private List<Class<? extends ScheduledProcess>> allScheduledProcesses;
    private Collection<Class<? extends ScheduledProcess>> scheduledProcessQueue;

    private List<Activity> activityList = new ArrayList<Activity>();

    private HashMap<String, Object> moreInfo = new HashMap<>();
    private HashMap<String, Integer> counters;
    private List<TdarUser> activePeople;

    private ByteArrayInputStream jsonInputStream;

    @Action(value = "active-users", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String listActiveUsers() {
        setJsonInputStream(new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(getActivePeople(), JsonLookupFilter.class, null)
                .getBytes()));
        return SUCCESS;
    }

    @Action(value = "activity")
    @Override
    public String execute() {
        setScheduledProcessesEnabled(TdarConfiguration.getInstance().shouldRunPeriodicEvents());
        setSessionStatistics(getGenericService().getSessionStatistics());
        setAllScheduledProcesses(scheduledProcessService.getManager().getAllTasks());
        setScheduledProcessQueue(scheduledProcessService.getScheduledProcessQueue());
        getActivityList().addAll(ActivityManager.getInstance().getActivityQueueClone());
        Collections.sort(getActivityList(), new Comparator<Activity>() {

            @Override
            public int compare(Activity o1, Activity o2) {
                if ((o1.getTotalTime() != -1L) && (o2.getTotalTime() != -1)) {
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

        setActivePeople(authenticationService.getCurrentlyActiveUsers());

        initSystemStats();
        return SUCCESS;
    }

    public void initSystemStats() {
        ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        getMoreInfo().put("Heap Memory", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        getMoreInfo().put("NonHeap Memory", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean : beans) {
            getLogger().trace("{}: {}", bean.getName(), bean.getUsage());
        }

        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            getLogger().trace("{}: {} {}", bean.getName(), bean.getCollectionCount(), bean.getCollectionTime());
        }

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        // What % CPU load this current JVM is taking, from 0.0-1.0
        getMoreInfo().put("system load", osBean.getSystemLoadAverage());
    }

    public Collection<Class<? extends ScheduledProcess>> getScheduledProcessQueue() {
        return scheduledProcessQueue;
    }

    public void setScheduledProcessQueue(Set<Class<? extends ScheduledProcess>> scheduledProcessQueue) {
        this.scheduledProcessQueue = scheduledProcessQueue;
    }

    public List<Class<? extends ScheduledProcess>> getAllScheduledProcesses() {
        return allScheduledProcesses;
    }

    public void setAllScheduledProcesses(Collection<Class<? extends ScheduledProcess>> allScheduledProcesses) {
        this.allScheduledProcesses = new ArrayList<>(allScheduledProcesses);
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

    public List<TdarUser> getActivePeople() {
        return activePeople;
    }

    public void setActivePeople(List<TdarUser> activePeople) {
        this.activePeople = activePeople;
    }

    public HashMap<String, Object> getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(HashMap<String, Object> moreInfo) {
        this.moreInfo = moreInfo;
    }

    public ByteArrayInputStream getJsonInputStream() {
        return jsonInputStream;
    }

    public void setJsonInputStream(ByteArrayInputStream jsonInputStream) {
        this.jsonInputStream = jsonInputStream;
    }

}

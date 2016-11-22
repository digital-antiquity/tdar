package org.tdar.core.service.processes.manager;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.processes.ScheduledProcess;

public class BaseProcessManager implements Serializable, ProcessManager {

    private static final long serialVersionUID = 5609551879252888600L;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected LinkedHashSet<ScheduledProcess> startupTasks = new LinkedHashSet<>();
    protected LinkedHashSet<Class<? extends ScheduledProcess>> scheduledProcessMap = new LinkedHashSet<Class<? extends ScheduledProcess>>();

    @Override
    public void addProcess(Class<? extends ScheduledProcess> cls) {
        scheduledProcessMap.add(cls);
    }

    @Override
    public LinkedHashSet<ScheduledProcess> getUpgradeTasks() {
        return startupTasks;
    }

    @Override
    public Set<Class<? extends ScheduledProcess>> getAllTasks() {
        return scheduledProcessMap;
    }

    @Override
    public void reset() {
        scheduledProcessMap.clear();
        startupTasks.clear();
    };

}
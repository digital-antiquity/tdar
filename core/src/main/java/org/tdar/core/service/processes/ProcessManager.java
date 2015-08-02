package org.tdar.core.service.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.configuration.TdarConfiguration;


public class ProcessManager implements Serializable {

	private static final long serialVersionUID = 7333013043608786215L;

	
    private final Logger logger = LoggerFactory.getLogger(getClass());
	private LinkedHashSet<ScheduledProcess> startupTasks = new LinkedHashSet<>();
    private Map<Class<? extends ScheduledProcess>, ScheduledProcess> scheduledProcessMap = new HashMap<Class<? extends ScheduledProcess>, ScheduledProcess>();

	public LinkedHashSet<ScheduledProcess> upgradeTasks(){ 
		return startupTasks;
	}

	public Map<Class<? extends ScheduledProcess>, ScheduledProcess> allTasks() {
		return scheduledProcessMap;
	}
	
	/**
     * Autowiring of Scheduled Processes, filter by those enabled and have not run. Also use @link TdarConfiguration to check whether the client supports
     * running them
     * 
     * @param processes
     */
    @Autowired
    public void setAllScheduledProcesses(List<ScheduledProcess> processes) {
        for (ScheduledProcess process : processes) {
            // if (!getTdarConfiguration().shouldRunPeriodicEvents()) {
            // scheduledProcessMap.clear();
            // logger.warn("current tdar configuration doesn't support running scheduled processes, skipping {}", processes);
            // return;
            // }
            if (!process.isEnabled()) {
                logger.warn("skipping disabled process {}", process);
                continue;
            }
            if (TdarConfiguration.getInstance().shouldRunPeriodicEvents() && process.isSingleRunProcess()) {
                logger.debug("adding {} to the process queue", process.getDisplayName());
                startupTasks.add(process);
            }
            else {
                // allScheduledProcesses.add(process);
                scheduledProcessMap.put(process.getClass(), process);
            }
        }

        logger.debug("ALL ENABLED SCHEDULED PROCESSES: {}", scheduledProcessMap.values());
    }


}

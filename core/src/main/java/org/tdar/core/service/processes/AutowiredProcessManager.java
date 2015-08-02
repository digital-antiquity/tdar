package org.tdar.core.service.processes;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.configuration.TdarConfiguration;


public class AutowiredProcessManager extends NullProcessManager {

	private static final long serialVersionUID = 7333013043608786215L;
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

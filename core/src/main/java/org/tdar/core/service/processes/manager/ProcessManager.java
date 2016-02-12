package org.tdar.core.service.processes.manager;

import java.util.LinkedHashSet;
import java.util.Set;

import org.tdar.core.service.processes.ScheduledProcess;

public interface ProcessManager {

	LinkedHashSet<ScheduledProcess> getUpgradeTasks();

	Set<Class<? extends ScheduledProcess>> getAllTasks();

	void addProcess(Class<? extends ScheduledProcess> cls);
	
	void reset();

}
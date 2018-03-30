package org.tdar.core.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.tdar.core.service.processes.ScheduledProcess;
import org.tdar.core.service.processes.manager.ProcessManager;

public interface ScheduledProcessService {

    final long ONE_HOUR_MS = 3600000;
    final long ONE_MIN_MS = 60000;
    final long TWO_MIN_MS = ONE_MIN_MS * 2;
    long FIVE_MIN_MS = ONE_MIN_MS * 5;

    /**
     * Check that our Authentication System (Crowd /LDAP ) is actually running
     */
    void cronCheckAuthService();

    /**
     * Cache the Crowd / LDAP group permissions for one hour
     * 
     */
    void cronClearPermissionsCache();

    /**
     * Scheduled processes have two separate flavors. (a) they run once (b) they
     * run regularly.
     * 
     * Regardless, we don't want long-running transactions in tDAR or a
     * transaction that affects tons of resources at the same time. To that end,
     * the ScheduledProcess Interface, and this task process is designed to
     * batch up tasks, and also run them at points that tDAR is not under heavy
     * load
     * 
     */
    void cronScheduledProcesses();

    List<String> runUpgradeTasks();

    void runNextScheduledProcessesInQueue();

    /**
     * Check if an @link UpgradeTask has been run or not.
     * 
     * @param name
     * @return
     */
    boolean hasRun(String name);

    /**
     * Add a @link ScheduledProcess to the Queue
     * 
     * @param process
     * @return
     */
    boolean queue(Class<? extends ScheduledProcess> cls);

    /**
     * get the scheduled process queue (used to make sure we don't run multiple
     * at once)
     * 
     * @return
     */
    Set<Class<? extends ScheduledProcess>> getScheduledProcessQueue();

    /**
     * Every few minutes, trim the activity queue so it doesn't get too big
     */
    void cronTrimActivityQueue();

    /**
     * Track startup of application (?)
     * 
     */
    void onApplicationEvent(ContextRefreshedEvent event);

    ProcessManager getManager();

    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;

    List<String> getCronEntries();

    /**
     * We don't actually perform configuration here. Instead we implement the SchedulingConfigurer to gain access
     * to the taskRegistrar so we can interrogate it later
     * 
     * @param taskRegistrar
     */
    void configureTasks(ScheduledTaskRegistrar taskRegistrar);

}
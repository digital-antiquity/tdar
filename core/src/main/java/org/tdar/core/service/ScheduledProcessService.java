/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.util.UpgradeTask;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.processes.AbstractPersistableScheduledProcess;
import org.tdar.core.service.processes.ScheduledProcess;
import org.tdar.core.service.processes.manager.ProcessManager;

import com.google.common.collect.Sets;

/**
 * 
 * This is a catch-all class that tracked all Scheduled, or "cronned" processes.
 * 
 * Spring scheduling cron expressions: Seconds Minutes Hours Day-of-Month Month
 * Day-of-Week Year (optional field)
 * 
 * *
 * <p>
 * Example patterns:
 * <ul>
 * <li>"0 0 * * * *" = the top of every hour of every day.</li>
 * <li>"*&#47;10 * * * * *" = every ten seconds.</li>
 * <li>"0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.</li>
 * <li>"0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.
 * </li>
 * <li>"0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays</li>
 * <li>"0 0 0 25 12 ?" = every Christmas Day at midnight</li>
 * </ul>
 * 
 * For more information on cron syntax, see
 * {@link http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06}
 * .
 * 
 * @author Adam Brin
 */
@Service
public class ScheduledProcessService implements  SchedulingConfigurer, ApplicationContextAware {

    public static final long ONE_HOUR_MS = 3600000;
    public static final long ONE_MIN_MS = 60000;
    public static final long FIVE_MIN_MS = ONE_MIN_MS * 5;
    public static final long TWO_MIN_MS = ONE_MIN_MS * 2;

    TdarConfiguration config = TdarConfiguration.getInstance();

    private transient final GenericService genericService;
    private transient final AuthenticationService authenticationService;
    private transient final ProcessManager manager;

    @Autowired
    public ScheduledProcessService(@Qualifier("genericService") GenericService gs,
            AuthenticationService auth, @Qualifier("processManager") ProcessManager pm) {
        this.genericService = gs;
        this.authenticationService = auth;
        this.manager = pm;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Set<Class<? extends ScheduledProcess>> scheduledProcessQueue = Sets.newConcurrentHashSet();
    private ApplicationContext applicationContext;
    private ScheduledTaskRegistrar taskRegistrar;


    /**
     * Check that our Authentication System (Crowd /LDAP ) is actually running
     */
    @Scheduled(fixedDelay = FIVE_MIN_MS)
    public void cronCheckAuthService() {
        if (!authenticationService.getProvider().isConfigured()) {
            logger.error("Unconfigured provider: {}", authenticationService.getProvider());
        }
    }


    /**
     * Cache the Crowd / LDAP group permissions for one hour
     * 
     */
    @Scheduled(fixedDelay = ONE_HOUR_MS)
    public void cronClearPermissionsCache() {
        authenticationService.clearPermissionsCache();
    }


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
    @Scheduled(fixedDelay = 10000)
    @Transactional(readOnly = false, noRollbackFor = { TdarRecoverableRuntimeException.class })
    public void cronScheduledProcesses() {
        if (CollectionUtils.isEmpty(getScheduledProcessQueue()) || !config.shouldRunPeriodicEvents()) {
            return;
        }
        runNextScheduledProcessesInQueue();
    }

    @Transactional(readOnly=false)
    public List<String> runUpgradeTasks() {
        List<String> tasksRun = new ArrayList<>();
        if (manager != null && CollectionUtils.isNotEmpty(manager.getUpgradeTasks())) {
            Iterator<ScheduledProcess> iterator = manager.getUpgradeTasks() .iterator();
            while (iterator.hasNext()) {
                ScheduledProcess process = iterator.next();
                boolean run = hasRun(process.getDisplayName());
                logger.debug("{} -- enabled:{} startup: {} completed: {}, hasRun: {}", process.getDisplayName(), process.isEnabled(), process.shouldRunAtStartup(), process.isCompleted(), run);
                if (process.isEnabled() && process.shouldRunAtStartup() && !process.isCompleted() && !run) {
                    if (process instanceof UpgradeTask && !((UpgradeTask) process).hasRun()) {
                        iterator.remove();
                        continue;
                    }
                    String threadName = Thread.currentThread().getName();
                    try {
                        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                        Thread.currentThread().setName(threadName + "-"+process.getClass().getSimpleName());
                        process.execute();
                        tasksRun.add(process.getDisplayName());
                    } catch (Throwable e) {
                        logger.error("an error ocurred when running {}", process.getDisplayName(), e);
                    } finally {
                        Thread.currentThread().setName(threadName);
                        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
                    }

                    complete(iterator, process);
                } else {
                    iterator.remove();
                }
            }
        }
        return tasksRun;
    }

    private void complete(Iterator<?> iterator, ScheduledProcess process) {
        logger.debug("process {} , completed: {} class: {}", process.getDisplayName(), process.isCompleted(), process.getClass().getName());
        if (process.isCompleted()) {
            process.cleanup();
            if (process.isSingleRunProcess()) {
                completedSuccessfully(process);
            }
            iterator.remove();
        }
    }
    public void runNextScheduledProcessesInQueue() {
        logger.debug("processes in Queue: {}", getScheduledProcessQueue());
        runUpgradeTasks();
        if (getScheduledProcessQueue().size() <= 0) {
            return;
        }

        Iterator<Class<? extends ScheduledProcess>> iterator = getScheduledProcessQueue().iterator();
        ScheduledProcess process = applicationContext.getBean(iterator.next());
        // FIXME: merge UpgradeTask and ScheduledProcess at some point, so that
        // UpgradeTask-s are
        // created / added / managed within a ScheduledProcess.execute()
        if (process == null) {
            iterator.remove();
            return;
        }
        // look in upgradeTasks to see what's there, if the task defined is not
        // there, then run the task, and then add it
        boolean run = hasRun(process.getDisplayName());
        if (process.isSingleRunProcess() && run) {
            logger.debug("process has already run once, removing {}", process);
            getScheduledProcessQueue().remove(process.getClass());
            return;
        }
        if (genericService.getActiveSessionCount() > config.getSessionCountLimitForBackgroundTasks()) {
            logger.debug("SKIPPING SCHEDULED PROCESSES, TOO MANY ACTIVE PROCESSES");
            logCurrentState();
            return;
        }

        if (!process.isEnabled()) {
            logger.debug("is not properly configured {}", process);
            getScheduledProcessQueue().remove(process.getClass());
            return;
        }

        if (process instanceof AbstractPersistableScheduledProcess<?>) {
            logger.info("beginning {} startId: {}", process.getDisplayName(),
                    ((AbstractPersistableScheduledProcess<?>) process).getLastId());
        } else {
            logger.info("beginning {}", process.getDisplayName());

        }
        String threadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            Thread.currentThread().setName(threadName + "-"+process.getClass().getSimpleName());
            process.execute();
        } catch (Throwable e) {
            logger.error("an error ocurred when running {}", process.getDisplayName(), e);
        } finally {
            Thread.currentThread().setName(threadName);
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }

        complete(iterator, process);
        logger.trace("processes in Queue: {}", getScheduledProcessQueue());
    }

    /**
     * Mark an @link UpgradeTask as having been run successfully
     * 
     * @param process
     */
    @Transactional(readOnly=false)
    private void completedSuccessfully(ScheduledProcess process) {
        UpgradeTask task = new UpgradeTask();
        task = genericService.markWritable(task);
        task.setName(process.getDisplayName());
        task.setRun(true);
        task.setRecordedDate(new Date());
        genericService.save(task);
        logger.info("completed " + task.getName());
    }

    /**
     * Check if an @link UpgradeTask has been run or not.
     * 
     * @param name
     * @return
     */
    @Transactional(readOnly=true)
    public boolean hasRun(String name) {
        UpgradeTask upgradeTask = new UpgradeTask();
        upgradeTask.setName(name);
        List<String> ignoreProperties = new ArrayList<String>();
        ignoreProperties.add("recordedDate");
        ignoreProperties.add("run");
        List<UpgradeTask> tasks = genericService.findByExample(UpgradeTask.class, upgradeTask, ignoreProperties, FindOptions.FIND_ALL);
        if ((tasks.size() > 0) && (tasks.get(0) != null)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a @link ScheduledProcess to the Queue
     * 
     * @param process
     * @return
     */
    public boolean queue(Class<? extends ScheduledProcess> cls) {
        return getScheduledProcessQueue().add(cls);
    }

    /**
     * get the scheduled process queue (used to make sure we don't run multiple
     * at once)
     * 
     * @return
     */
    public Set<Class<? extends ScheduledProcess>> getScheduledProcessQueue() {
        return scheduledProcessQueue;
    }

    /**
     * Every few minutes, trim the activity queue so it doesn't get too big
     */
    @Scheduled(fixedDelay = TWO_MIN_MS)
    public void cronTrimActivityQueue() {
        logger.trace("trimming activity queue");
        ActivityManager.getInstance().cleanup(System.currentTimeMillis() - TWO_MIN_MS);
        logger.trace("end trimming activity queue");
    }

    /**
     * Track startup of application (?)
     * 
     */
    @Transactional
    @EventListener()
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.debug("received app context event: " + event);
    }

    public ProcessManager getManager() {
        return manager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void logCurrentState() {
        try {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            long[] allThreadIds = threadMXBean.getAllThreadIds();
            ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(allThreadIds);
            long totalTime = 0;
            Map<Long, Long> totals = new HashMap<>();
            for (ThreadInfo info : threadInfo) {
                long id = info.getThreadId();
                long threadCpuTime = threadMXBean.getThreadUserTime(id);
                totalTime += threadCpuTime;
                totals.put(id, threadCpuTime);
            }
            for (ThreadInfo info : threadInfo) {
                long id = info.getThreadId();
                long percent = (100 * totals.get(id)) / totalTime;
                if (percent > 0) {
                    logger.debug("{} :: CPU: {}% {} ({})", id, percent, info.getThreadName(), info.getThreadState());
                    StackTraceElement[] st = info.getStackTrace();
                    for (StackTraceElement t : st) {
                        logger.debug("\t{} ", t);
                    }
                }
            }
        } catch (Throwable t) {
            logger.warn("exception in logging error state", t);
        }
    }

    public List<String> getCronEntries() {
        return taskRegistrar.getCronTaskList()
                .stream()
                .map(t -> t.getExpression())
                .collect(Collectors.toList());
    }

    /**
     * We don't actually perform configuration here. Instead we implement the SchedulingConfigurer to gain access
     * to the taskRegistrar so we can interrogate it later
     * 
     * @param taskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.taskRegistrar = taskRegistrar;
    }

}

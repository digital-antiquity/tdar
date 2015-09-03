/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.util.UpgradeTask;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.processes.AccountUsageHistoryLoggingTask;
import org.tdar.core.service.processes.CreatorAnalysisProcess;
import org.tdar.core.service.processes.DailyEmailProcess;
import org.tdar.core.service.processes.DailyStatisticsUpdate;
import org.tdar.core.service.processes.DoiProcess;
import org.tdar.core.service.processes.OccurranceStatisticsUpdateProcess;
import org.tdar.core.service.processes.RebuildHomepageCache;
import org.tdar.core.service.processes.SalesforceSyncProcess;
import org.tdar.core.service.processes.ScheduledProcess;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.processes.SitemapGeneratorProcess;
import org.tdar.core.service.processes.WeeklyFilestoreLoggingProcess;
import org.tdar.core.service.processes.WeeklyStatisticsLoggingProcess;
import org.tdar.core.service.search.SearchIndexService;

/**
 * 
 * This is a catch-all class that tracked all Scheduled, or "cronned" processes.
 * 
 * Spring scheduling cron expressions: Seconds Minutes Hours Day-of-Month Month Day-of-Week Year (optional field)
 * 
 * *
 * <p>
 * Example patterns:
 * <ul>
 * <li>"0 0 * * * *" = the top of every hour of every day.</li>
 * <li>"*&#47;10 * * * * *" = every ten seconds.</li>
 * <li>"0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.</li>
 * <li>"0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.</li>
 * <li>"0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays</li>
 * <li>"0 0 0 25 12 ?" = every Christmas Day at midnight</li>
 * </ul>
 * 
 * For more information on cron syntax, see {@link http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06}.
 * 
 * @author Adam Brin
 */
@Service
public class ScheduledProcessService implements ApplicationListener<ContextRefreshedEvent> {

    private static final long ONE_HOUR_MS = 3600000;
    private static final long ONE_MIN_MS = 60000;
    private static final long FIVE_MIN_MS = ONE_MIN_MS * 5;
    private static final long TWO_MIN_MS = ONE_MIN_MS * 2;

    TdarConfiguration config = TdarConfiguration.getInstance();

    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient RssService rssService;
    @Autowired
    private transient AuthenticationService authenticationService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // all scheduled processes configured on the system
    private Map<Class<?>, ScheduledProcess<Persistable>> scheduledProcessMap = new HashMap<Class<?>, ScheduledProcess<Persistable>>();
    // scheduled processes currently set to run in batches when spare cycles are available
    private LinkedHashSet<ScheduledProcess<Persistable>> scheduledProcessQueue = new LinkedHashSet<ScheduledProcess<Persistable>>();
    private boolean hasRunStartupProcesses;

    // * Spring scheduling cron expressions: Seconds Minutes Hours Day-of-Month Month Day-of-Week Year (optional field)

    /**
     * Once a week, on Sundays, generate some static, cached stats for use by the admin area and general system
     */
    @Scheduled(cron = "12 0 0 * * SUN")
    public void cronGenerateWeeklyStats() {
        queue(scheduledProcessMap.get(WeeklyStatisticsLoggingProcess.class));
        queue(scheduledProcessMap.get(CreatorAnalysisProcess.class));
    }

    /**
     * Send emails at midnight
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void cronDailyEmail() {
        logger.info("updating Daily Emails");
        queue(scheduledProcessMap.get(DailyEmailProcess.class));
        queue(scheduledProcessMap.get(SalesforceSyncProcess.class));
    }

    /**
     * Send emails at midnight
     */
    @Scheduled(cron = "0 15 0 * * *")
    public void cronDailyStats() {
        logger.info("updating Daily stats");
        queue(scheduledProcessMap.get(OccurranceStatisticsUpdateProcess.class));
        queue(scheduledProcessMap.get(DailyStatisticsUpdate.class));
    }

    /**
     * Check that our Authentication System (Crowd /LDAP ) is actually running
     */
    @Scheduled(fixedDelay = FIVE_MIN_MS)
    public void cronCheckAuthService() {
        if (!authenticationService.getProvider().isConfigured()) {
            logger.error("Unconfigured provider: {}", authenticationService.getProvider());
        }
        rssService.evictRssCache();
    }

    /**
     * Tell Lucene to Optimize it's indexes
     */
    @Scheduled(cron = "16 0 0 * * SUN")
    public void cronOptimizeSearchIndexes() {
        logger.info("Optimizing indexes");
        searchIndexService.optimizeAll();
    }

    /**
     * Cache the Crowd / LDAP group permissions for one hour
     * 
     */
    @Scheduled(fixedDelay = ONE_HOUR_MS)
    public void cronClearPermissionsCache() {
        authenticationService.clearPermissionsCache();
    }

    @Scheduled(fixedDelay = FIVE_MIN_MS)
    public void cronQueueEmail() {
        queue(scheduledProcessMap.get(SendEmailProcess.class));
    }

    /**
     * Generate DOIs
     */
    @Scheduled(cron = "16 15 0 * * *")
    public void cronUpdateDois() {
        logger.info("updating DOIs");
        queue(scheduledProcessMap.get(DoiProcess.class));
    }

    /**
     * Log Account Usage History
     */
    @Scheduled(cron = "0 0 1 0 * *")
    public void cronUpdateAccountUsageHistory() {
        logger.info("updating account usage history");
        queue(scheduledProcessMap.get(AccountUsageHistoryLoggingTask.class));
    }

    /**
     * Update the Sitemap.org sitemap files
     */
    @Scheduled(cron = "20 15 0 * * *")
    public void cronUpdateSitemap() {
        logger.info("updating Sitemaps");
        queue(scheduledProcessMap.get(SitemapGeneratorProcess.class));
    }

    /**
     * Update the Homepage's Featured Resources
     */
    @Scheduled(cron = "1 15 0 * * *")
    public void cronUpdateHomepage() {
        queue(scheduledProcessMap.get(RebuildHomepageCache.class));
    }

    /**
     * Verify the @link Filestore once a week
     * 
     * @throws IOException
     */
    @Scheduled(cron = "50 0 0 * * SUN")
    public void cronVerifyTdarFiles() throws IOException {
        queue(scheduledProcessMap.get(WeeklyFilestoreLoggingProcess.class));
    }

    /**
     * Autowiring of Scheduled Processes, filter by those enabled and have not run. Also use @link TdarConfiguration to check whether the client supports
     * running them
     * 
     * @param processes
     */
    @Autowired
    public void setAllScheduledProcesses(List<ScheduledProcess<?>> processes) {
        for (ScheduledProcess<?> process_ : processes) {
            @SuppressWarnings("unchecked")
            ScheduledProcess<Persistable> process = (ScheduledProcess<Persistable>) process_;
            // if (!getTdarConfiguration().shouldRunPeriodicEvents()) {
            // scheduledProcessMap.clear();
            // logger.warn("current tdar configuration doesn't support running scheduled processes, skipping {}", processes);
            // return;
            // }
            if (!process.isEnabled()) {
                logger.warn("skipping disabled process {}", process);
                continue;
            }
            if (config.shouldRunPeriodicEvents() && process.isSingleRunProcess()) {
                logger.debug("adding {} to the process queue {}", process.getDisplayName(), scheduledProcessQueue);
                queue(process);
            } else {
                // allScheduledProcesses.add(process);
                scheduledProcessMap.put(process.getClass(), process);
            }
        }

        logger.debug("ALL ENABLED SCHEDULED PROCESSES: {}", scheduledProcessMap.values());
    }

    /**
     * Scheduled processes have two separate flavors.
     * (a) they run once
     * (b) they run regularly.
     * 
     * Regardless, we don't want long-running transactions in tDAR or a transaction that affects tons
     * of resources at the same time. To that end, the ScheduledProcess Interface, and this task process
     * is designed to batch up tasks, and also run them at points that tDAR is not under heavy load
     * 
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional(readOnly = false, noRollbackFor = { TdarRecoverableRuntimeException.class })
    public void cronScheduledProcesses() {
        if (CollectionUtils.isEmpty(scheduledProcessQueue)) {
            return;
        }
        runNextScheduledProcessesInQueue();
    }

    protected void runNextScheduledProcessesInQueue() {
        logger.debug("processes in Queue: {}", scheduledProcessQueue);
        if (scheduledProcessQueue.size() <= 0) {
            return;
        }

        ScheduledProcess<Persistable> process = scheduledProcessQueue.iterator().next();
        // FIXME: merge UpgradeTask and ScheduledProcess at some point, so that UpgradeTask-s are
        // created / added / managed within a ScheduledProcess.execute()
        if (process == null) {
            return;
        }
        // look in upgradeTasks to see what's there, if the task defined is not
        // there, then run the task, and then add it
        UpgradeTask upgradeTask = checkIfRun(process.getDisplayName());
        if (process.isSingleRunProcess() && upgradeTask.hasRun()) {
            logger.debug("process has already run once, removing {}", process);
            scheduledProcessQueue.remove(process);
            return;
        }
        if (genericService.getActiveSessionCount() > config.getSessionCountLimitForBackgroundTasks()) {
            logger.debug("SKIPPING SCHEDULED PROCESSES, TOO MANY ACTIVE PROCESSES");
            logCurrentState();
            return;
        }
        logger.info("beginning {} startId: {}", process.getDisplayName(), process.getLastId());
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            process.execute();
        } catch (Throwable e) {
            logger.error("an error ocurred when running {}", process.getDisplayName(), e);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }

        if (process.isCompleted()) {
            process.cleanup();
            completedSuccessfully(upgradeTask);
            scheduledProcessQueue.remove(process);
        }
        logger.trace("processes in Queue: {}", scheduledProcessQueue);
    }

    private void logCurrentState() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] allThreadIds = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(allThreadIds);
        long totalTime = 0;
        Map<Long, Long> totals = new HashMap<>();
        for (ThreadInfo info : threadInfo) {
            if (info == null) {
                continue;
            }
            long id = info.getThreadId();
            long threadCpuTime = threadMXBean.getThreadUserTime(id);
            totalTime += threadCpuTime;
            totals.put(id, threadCpuTime);
        }
        for (ThreadInfo info : threadInfo) {
            if (info == null) {
                continue;
            }
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
    }

    /**
     * Mark an @link UpgradeTask as having been run successfully
     * 
     * @param upgradeTask
     */
    @Transactional
    private void completedSuccessfully(UpgradeTask upgradeTask) {
        upgradeTask.setRun(true);
        upgradeTask.setRecordedDate(new Date());
        genericService.save(upgradeTask);
        logger.info("completed " + upgradeTask.getName());
    }

    /**
     * Check if an @link UpgradeTask has been run or not.
     * 
     * @param name
     * @return
     */
    private UpgradeTask checkIfRun(String name) {
        UpgradeTask upgradeTask = new UpgradeTask();
        upgradeTask.setName(name);
        List<String> ignoreProperties = new ArrayList<String>();
        ignoreProperties.add("recordedDate");
        ignoreProperties.add("run");
        List<UpgradeTask> tasks = genericService.findByExample(UpgradeTask.class, upgradeTask, ignoreProperties, FindOptions.FIND_ALL);
        if ((tasks.size() > 0) && (tasks.get(0) != null)) {
            return tasks.get(0);
        } else {
            return upgradeTask;
        }
    }

    /**
     * Add a @link ScheduledProcess to the Queue
     * 
     * @param process
     * @return
     */
    public boolean queue(ScheduledProcess<Persistable> process) {
        if ((process == null) || !TdarConfiguration.getInstance().shouldRunPeriodicEvents()) {
            return false;
        }
        return scheduledProcessQueue.add(process);
    }

    /**
     * get the scheduled process queue (used to make sure we don't run multiple at once)
     * 
     * @return
     */
    public Set<ScheduledProcess<Persistable>> getScheduledProcessQueue() {
        return scheduledProcessQueue;
    }

    /**
     * Return all ScheduledProcesses
     * 
     * @return
     */
    public List<ScheduledProcess<Persistable>> getAllScheduledProcesses() {
        return new ArrayList<ScheduledProcess<Persistable>>(scheduledProcessMap.values());
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
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.debug("received app context event: " + event);
        if (hasRunStartupProcesses) {
            logger.trace("already run startup processes, aborting");
            return;
        }
        // FIXME: disabling for the interim, re-enable if we ever have processes that really do need to run at startup.
        // for (ScheduledProcess<Persistable> process: scheduledProcessMap.values()) {
        // if (process.shouldRunAtStartup()) {
        // logger.debug("executing startup process: " + process);
        // process.execute();
        // }
        // }
        hasRunStartupProcesses = true;

    }

    @Transactional
    public void queueTask(Class<? extends ScheduledProcess<? extends Persistable>> class1) {
        ScheduledProcess<Persistable> process = scheduledProcessMap.get(class1);
        if (process != null) {
            scheduledProcessQueue.add(process);
        }
    }

}

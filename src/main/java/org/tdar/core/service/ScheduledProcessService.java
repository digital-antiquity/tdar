/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.bean.util.UpgradeTask;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.processes.CreatorAnalysisProcess;
import org.tdar.core.service.processes.DoiProcess;
import org.tdar.core.service.processes.WeeklyFilestoreLoggingProcess;
import org.tdar.core.service.processes.OccurranceStatisticsUpdateProcess;
import org.tdar.core.service.processes.RebuildHomepageCache;
import org.tdar.core.service.processes.SitemapGeneratorProcess;
import org.tdar.core.service.processes.WeeklyStatisticsLoggingProcess;
import org.tdar.utils.MessageHelper;

/**
 * 
 * This is a catch-all class that tracked all Scheduled, or "cronned" processes.
 * 
 * Spring scheduling cron expressions: Seconds Minutes Hours Day-of-Month Month Day-of-Week Year (optional field)
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

    @Autowired
    private SearchIndexService searchIndexService;
    @Autowired
    private GenericService genericService;
    @Autowired
    private AuthenticationAndAuthorizationService authenticationService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // all scheduled processes configured on the system
    private Map<Class<?>, ScheduledProcess<Persistable>> scheduledProcessMap = new HashMap<Class<?>, ScheduledProcess<Persistable>>();
    // scheduled processes currently set to run in batches when spare cycles are available
    private LinkedHashSet<ScheduledProcess<Persistable>> scheduledProcessQueue = new LinkedHashSet<ScheduledProcess<Persistable>>();
    private boolean hasRunStartupProcesses;

    /**
     * Once a week, on Sundays, generate some static, cached stats for use by the admin area and general system
     */
    @Scheduled(cron = "12 0 0 * * SUN")
    public void generateWeeklyStats() {
        queue(scheduledProcessMap.get(WeeklyStatisticsLoggingProcess.class));
        queue(scheduledProcessMap.get(OccurranceStatisticsUpdateProcess.class));
        queue(scheduledProcessMap.get(CreatorAnalysisProcess.class));
    }

    /**
     * Check that our Authentication System (Crowd /LDAP ) is actually running
     */
    @Scheduled(fixedDelay = FIVE_MIN_MS)
    public void checkAuthService() {
        if (!authenticationService.getProvider().isConfigured()) {
            logger.error("Unconfigured provider: {}", authenticationService.getProvider());
        }
    }

    /**
     * Tell Lucene to Optimize it's indexes
     */
    @Scheduled(cron = "16 0 0 * * SUN")
    public void optimizeSearchIndexes() {
        logger.info("Optimizing indexes");
        searchIndexService.optimizeAll();
    }

    /**
     * Cache the Crowd / LDAP group permissions for one hour
     * 
     */
    @Scheduled(fixedDelay = ONE_HOUR_MS)
    public void clearPermissionsCache() {
        authenticationService.clearPermissionsCache();
    }

    /**
     * Generate DOIs
     */
    @Scheduled(cron = "16 15 0 * * *")
    public void updateDois() {
        logger.info("updating DOIs");
        queue(scheduledProcessMap.get(DoiProcess.class));
    }

    /**
     * Update the Sitemap.org sitemap files
     */
    @Scheduled(cron = "20 15 0 * * *")
    public void updateSitemap() {
        logger.info("updating Sitemaps");
        queue(scheduledProcessMap.get(SitemapGeneratorProcess.class));
    }

    /**
     * Update the Homepage's Featured Resources
     */
    @Scheduled(cron = "1 15 0 * * *")
    public void updateHomepage() {
        queue(scheduledProcessMap.get(RebuildHomepageCache.class));
    }

    /**
     * Verify the @link Filestore once a week
     * 
     * @throws IOException
     */
    @Scheduled(cron = "5 0 0 * * SUN")
    @Async
    public void verifyTdarFiles() throws IOException {
        queue(scheduledProcessMap.get(WeeklyFilestoreLoggingProcess.class));
    }

    private TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
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
            if (getTdarConfiguration().shouldRunPeriodicEvents() && process.isSingleRunProcess()) {
                logger.debug("adding {} to the process queue {}", process.getDisplayName(), scheduledProcessQueue);
                queue(process);
            }
            else {
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
    public void runScheduledProcesses() {
        if (CollectionUtils.isEmpty(scheduledProcessQueue)) {
            return;
        }
        logger.debug("processes in Queue: {}", scheduledProcessQueue);
        ScheduledProcess<Persistable> process = scheduledProcessQueue.iterator().next();
        // FIXME: merge UpgradeTask and ScheduledProcess at some point, so that UpgradeTask-s are
        // created / added / managed within a ScheduledProcess.execute()

        // look in upgradeTasks to see what's there, if the task defined is not
        // there, then run the task, and then add it
        UpgradeTask upgradeTask = checkIfRun(process.getDisplayName());
        if (process.isSingleRunProcess() && upgradeTask.hasRun()) {
            logger.debug("process has already run once, removing {}", process);
            scheduledProcessQueue.remove(process);
            return;
        }
        if (genericService.getActiveSessionCount() > getTdarConfiguration().getSessionCountLimitForBackgroundTasks()) {
            logger.debug("SKIPPING SCHEDULED PROCESSES, TOO MANY ACTIVE PROCESSES");
            return;
        }
        logger.info("beginning {} startId: {}", process.getDisplayName(), process.getLastId());
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            process.execute();
        } catch (Throwable e) {
            logger.error(MessageHelper.getMessage("scheduledProcessService.error_running", Arrays.asList(process.getDisplayName())), e);
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
        }
        else {
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
    public void trimActivityQueue() {
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
    public void queueTask(Class<? extends ScheduledProcess> class1) {
        ScheduledProcess<Persistable> process = scheduledProcessMap.get(class1);
        if (process != null) {
            scheduledProcessQueue.add(process);
        }
    }

}

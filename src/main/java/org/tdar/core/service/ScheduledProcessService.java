/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.bean.util.Statistic;
import org.tdar.core.bean.util.Statistic.StatisticType;
import org.tdar.core.bean.util.UpgradeTask;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.DoiProcess;
import org.tdar.core.service.processes.RebuildHomepageCache;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.TaintedFileException;

/**
 * 
 * This is a catch-all class that tracked all Scheduled, or "cronned" processes
 * 
 * @author Adam Brin
 * 
 */
@Service
public class ScheduledProcessService {

    private static final long ONE_HOUR_MS = 3600000;
    private static final long ONE_MIN_MS = 60000;
    private static final long FIVE_MIN_MS = ONE_MIN_MS * 5;
    private static final long TWO_MIN_MS = ONE_MIN_MS * 2;
    public static String BAR = "\r\n========================================================\r\n";
    @Autowired
    private InformationResourceFileVersionService informationResourceFileVersionService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SearchIndexService searchIndexService;
    @Autowired
    private GenericService genericService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private AuthenticationAndAuthorizationService authenticationService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // all scheduled processes configured on the system
    private List<ScheduledProcess<Persistable>> allScheduledProcesses = new ArrayList<ScheduledProcess<Persistable>>();
    // scheduled processes currently set to run in batches when spare cycles are available
    private List<ScheduledProcess<Persistable>> scheduledProcessQueue = new ArrayList<ScheduledProcess<Persistable>>();
    // a list of all ids to be processed by the current scheduled process (scheduledProcessQueue.get(0))
    private List<Long> persistableIdQueue;

    @Scheduled(cron = "12 0 0 * * SUN")
    public void generateWeeklyStats() {
        logger.info("generating weekly stats");
        List<Statistic> stats = new ArrayList<Statistic>();
        stats.add(generateStatistics(StatisticType.NUM_PROJECT, resourceService.countActiveResources(Project.class), ""));
        stats.add(generateStatistics(StatisticType.NUM_DOCUMENT, resourceService.countActiveResources(Document.class), ""));
        stats.add(generateStatistics(StatisticType.NUM_DATASET, resourceService.countActiveResources(Dataset.class), ""));
        stats.add(generateStatistics(StatisticType.NUM_CODING_SHEET, resourceService.countActiveResources(CodingSheet.class), ""));
        stats.add(generateStatistics(StatisticType.NUM_SENSORY_DATA, resourceService.countActiveResources(SensoryData.class), ""));
        stats.add(generateStatistics(StatisticType.NUM_ONTOLOGY, resourceService.countActiveResources(Ontology.class), ""));
        stats.add(generateStatistics(StatisticType.NUM_IMAGE, resourceService.countActiveResources(Image.class), ""));
        stats.add(generateStatistics(StatisticType.NUM_USERS, entityService.findAllRegisteredUsers(null).size(), ""));
        stats.add(generateStatistics(StatisticType.NUM_ACTUAL_CONTRIBUTORS, entityService.findNumberOfActualContributors(), ""));
        stats.add(generateStatistics(StatisticType.NUM_COLLECTIONS, resourceCollectionService.findAllResourceCollections().size(), ""));
        genericService.save(stats);
    }

    @Scheduled(cron = "16 0 0 * * SUN")
    public void optimizeSearchIndexes() {
        logger.info("Optimizing indexes");
        searchIndexService.optimizeAll();
    }

    @Scheduled(fixedDelay = ONE_HOUR_MS)
    public void clearPermissionsCache() {
        authenticationService.clearPermissionsCache();
    }

    @SuppressWarnings("unchecked")
    @Scheduled(cron = "16 15 0 * * *")
    public void updateDois() {
        logger.info("updating DOIs");
        for (ScheduledProcess<?> process : getAllScheduledProcesses()) {
            if (process instanceof DoiProcess && !getScheduledProcessQueue().contains(process)) {
                getScheduledProcessQueue().add((ScheduledProcess<Persistable>) process);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Scheduled(cron = "1 15 0 * * *")
    public void updateHomepage() {
        for (ScheduledProcess<?> process : getAllScheduledProcesses()) {
            if (process instanceof RebuildHomepageCache && !getScheduledProcessQueue().contains(process)) {
                getScheduledProcessQueue().add((ScheduledProcess<Persistable>) process);
            }
        }
    }

    protected Statistic generateStatistics(Statistic.StatisticType statisticType, Number value, String comment) {
        Statistic stat = new Statistic();
        stat.setRecordedDate(new Date());
        stat.setStatisticType(statisticType);
        stat.setComment(comment);
        stat.setValue(value.longValue());
        logger.info("stat: {}", stat);
        return stat;
    }

    @Scheduled(cron = "5 0 0 * * SUN")
    public void verifyTdarFiles() {
        if (!getTdarConfiguration().shouldRunPeriodicEvents()) {
            return;
        }
        logger.info("beginning automated verification of files");
        Filestore filestore = getTdarConfiguration().getFilestore();
        StringBuffer missing = new StringBuffer();
        StringBuffer tainted = new StringBuffer();
        StringBuffer other = new StringBuffer();
        StringBuffer subject = new StringBuffer("Problem Files Report");
        int count = 0;
        for (InformationResourceFileVersion version : informationResourceFileVersionService.findAll()) {
            try {
                filestore.verifyFile(version);
            } catch (FileNotFoundException e1) {
                missing.append(String.format(" - %s not found [%s]\r\n", version.getFilename(), version.getInformationResourceId()));
                count++;
                logger.debug("file not found ", e1);
            } catch (TaintedFileException e1) {
                count++;
                tainted.append(String.format(" - %s's checksum does not match the one stored [%s]\r\n", version.getFilename(),
                        version.getInformationResourceId()));
                logger.debug("file tainted", e1);
            } catch (Exception e1) {
                count++;
                tainted.append(String.format(" - %s had a problem [%s]\r\n", version.getFilename(), version.getInformationResourceId()));
                logger.debug("other error ", e1);
            }
        }

        if (missing.length() > 0)
            missing.insert(0, "\r\n" + BAR + "MISSING FILES" + BAR + "");
        if (tainted.length() > 0)
            tainted.insert(0, "\r\n" + BAR + "TAINTED FILES" + BAR + "");
        if (other.length() > 0)
            other.insert(0, "\r\n" + BAR + "OTHER PROBLEMS" + BAR + "");

        String end = " No issues found.";
        if (count == 0) {
            subject.append(" [NONE]");
        }
        else {
            subject.append(" [" + count + "]");
            missing.append(tainted).append(other);
            end = missing.toString();
        }

        String message = String.format("This is an automated message from tDAR reporting on files with issues.\r\nRun on: %s %s\n %s", TdarConfiguration
                .getInstance().getBaseUrl(), new Date(), end);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String filename = "verify-" + df.format(new Date()) + ".txt";
        filestore.storeLog(filename, message);

        logger.debug(subject + "[ " + getTdarConfiguration().getSystemAdminEmail() + " ]");
        logger.debug(message);
        emailService.send(message, getTdarConfiguration().getSystemAdminEmail(), subject.toString());
        logger.info("ending automated verification of files");
    }

    private TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    @Autowired
    public void setAllScheduledProcesses(List<ScheduledProcess<Persistable>> processes) {
        for (ScheduledProcess<Persistable> process : processes) {
            if (!getTdarConfiguration().shouldRunPeriodicEvents()) {
                getAllScheduledProcesses().clear();
            }
            if (process.isConfigured() && getTdarConfiguration().shouldRunPeriodicEvents()) {
                if (process.isShouldRunOnce()) {
                    logger.debug(String.format("adding %s to the process queue", process.getDisplayName()));
                    getScheduledProcessQueue().add(process);
                } else {
                    getAllScheduledProcesses().add(process);
                }
            }
        }
        logger.debug("ALL ENABLED SCHEDULED PROCESSES: {}", this.getAllScheduledProcesses());

    }

    public <P extends Persistable> List<Long> getNextBatch(ScheduledProcess<P> process) {
        if (CollectionUtils.isEmpty(persistableIdQueue)) {
            persistableIdQueue = process.getPersistableIdQueue();
        }
        int endIndex = Math.min(persistableIdQueue.size(), process.getBatchSize());
        List<Long> sublist = persistableIdQueue.subList(0, endIndex);
        // make a copy of the batch first
        ArrayList<Long> batch = new ArrayList<Long>(sublist);
        // sublist is a view of the backing list, clearing it modifies the backing list.
        sublist.clear();
        process.setLastId(batch.get(endIndex - 1));
        logger.trace("batch {}", batch);
        return batch;
    }

    /*
     * Scheduled processes have two separate flavors.
     * (a) they run once
     * (b) they run regularly.
     * 
     * Regardless, we don't want long-running transactions in tDAR or a transaction that affects tons
     * of resources at the same time. To that end, the ScheduledProcess Interface, and this task process
     * is designed to batch up tasks, and also run them at points that tDAR is not under heavy load
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional(readOnly = false, noRollbackFor = { TdarRecoverableRuntimeException.class })
    public void runScheduledProcesses() {
        if (!getTdarConfiguration().shouldRunPeriodicEvents() || CollectionUtils.isEmpty(scheduledProcessQueue)) {
            return;
        }
        logger.debug("processes in Queue: {}", scheduledProcessQueue);
        ScheduledProcess<Persistable> process = scheduledProcessQueue.get(0);
        // look in upgradeTasks to see what's there, if the task defined is not
        // there, then run the task, and then add it
        UpgradeTask upgradeTask = checkIfRun(process.getDisplayName());
        if (process.isShouldRunOnce() && upgradeTask.hasRun()) {
            scheduledProcessQueue.remove(process);
            return;
        }
        if (genericService.getActiveSessionCount() > getTdarConfiguration().getSessionCountLimitForBackgroundTasks()) {
            logger.debug("SKIPPING SCHEDULED PROCESSES, TOO MANY ACTIVE PROCESSES");
            return;
        }
        logger.info("beginning {} startId: {}", process.getDisplayName(), process.getLastId());
        try {
            process.processBatch(getNextBatch(process));
        } catch (Throwable e) {
            logger.error(String.format("an error ocurred while running the process: %s", process.getDisplayName()), e);
        }

        if (CollectionUtils.isEmpty(persistableIdQueue)) {
            process.cleanup();
            completedSuccessfully(upgradeTask);
            scheduledProcessQueue.remove(process);
            persistableIdQueue = null;
        }
        logger.trace("processes in Queue: {}", scheduledProcessQueue);
    }

    @Transactional
    private void completedSuccessfully(UpgradeTask upgradeTask) {
        upgradeTask.setRun(true);
        upgradeTask.setRecordedDate(new Date());
        genericService.save(upgradeTask);
        logger.info("completed " + upgradeTask.getName());
    }

    private UpgradeTask checkIfRun(String name) {
        UpgradeTask upgradeTask = new UpgradeTask();
        upgradeTask.setName(name);
        List<String> ignoreProperties = new ArrayList<String>();
        ignoreProperties.add("recordedDate");
        ignoreProperties.add("run");

        List<UpgradeTask> tasks = genericService.findByExample(UpgradeTask.class, upgradeTask, ignoreProperties, FindOptions.FIND_ALL);
        if (tasks.size() > 0 && tasks.get(0) != null) {
            return tasks.get(0);
        }
        else {
            return upgradeTask;
        }
    }

    public List<ScheduledProcess<Persistable>> getScheduledProcessQueue() {
        return scheduledProcessQueue;
    }

    public void setScheduledProcessQueue(List<ScheduledProcess<Persistable>> scheduledProcessQueue) {
        this.scheduledProcessQueue = scheduledProcessQueue;
    }

    public List<ScheduledProcess<Persistable>> getAllScheduledProcesses() {
        return allScheduledProcesses;
    }

    public List<Long> getPersistableIdQueue() {
        return persistableIdQueue;
    }

}

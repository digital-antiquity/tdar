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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.util.Statistic;
import org.tdar.core.bean.util.Statistic.StatisticType;
import org.tdar.core.bean.util.UpgradeTask;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.TaintedFileException;
import org.tdar.geosearch.GeoSearchService;

/**
 * @author Adam Brin
 * 
 */
@Service
public class ScheduledProcessService {

    public static String BAR = "\r\n========================================================\r\n";
    @Autowired
    InformationResourceFileVersionService informationResourceFileVersionService;
    @Autowired
    EmailService emailService;
    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    GenericService genericService;
    @Autowired
    ResourceService resourceService;
    @Autowired
    EntityService entityService;
    @Autowired
    GeoSearchService geoSearchService;
    @Autowired
    GeographicKeywordService geographicKeywordService;
    @Autowired
    SimpleCachingService simpleCachingService;

    private final Logger logger = Logger.getLogger(getClass());

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
        stats.add(generateStatistics(StatisticType.NUM_USERS, entityService.findAllRegisteredUsers().size(), ""));
        genericService.save(stats);
    }

    @Scheduled(cron = "16 0 0 * * SUN")
    public void optimizeSearchIndexes() {
        logger.info("Optimizing indexes");
        searchIndexService.optimizeAll();
    }

    @Scheduled(cron = "* 0 0 * * *")
    public void rebuildCaches() {
        logger.info("rebuilding caches");
        simpleCachingService.taintAllAndRebuild();
    }

    protected Statistic generateStatistics(Statistic.StatisticType statisticType, Number value, String comment) {
        Statistic stat = new Statistic();
        stat.setRecordedDate(new Date());
        stat.setStatisticType(statisticType);
        stat.setComment(comment);
        stat.setValue(value.longValue());
        logger.info(stat);
        return stat;
    }

    @Scheduled(cron = "5 0 0 * * SUN")
    public void verifyTdarFiles() {
        if (!TdarConfiguration.getInstance().shouldRunPeriodicEvents())
            return;
        logger.info("beginning automated verification of files");
        Filestore filestore = TdarConfiguration.getInstance().getFilestore();
        StringBuffer missing = new StringBuffer();
        StringBuffer tainted = new StringBuffer();
        StringBuffer other = new StringBuffer();
        String subject = "Problem Files Report";
        int count = 0;
        for (InformationResourceFileVersion version : informationResourceFileVersionService.findAll()) {
            try {
                filestore.verifyFile(version);
            } catch (FileNotFoundException e1) {
                missing.append(" - " + version.getFilename() + " not found [" + version.getInformationResourceId() + "]\r\n");
                count++;
                e1.printStackTrace();
            } catch (TaintedFileException e1) {
                count++;
                tainted.append(" - " + version.getFilename() + "'s checksum does not match the one stored [" + version.getInformationResourceId() + "]\r\n");
                e1.printStackTrace();
            } catch (Exception e1) {
                count++;
                other.append(" - " + version.getFilename() + " had a problem [" + version.getInformationResourceId() + "]\r\n");
                e1.printStackTrace();
            }
        }

        if (missing.length() > 0)
            missing.insert(0, "\r\n" + BAR + "MISSING FILES" + BAR + "");
        if (tainted.length() > 0)
            tainted.insert(0, "\r\n" + BAR + "TAINTED FILES" + BAR + "");
        if (other.length() > 0)
            other.insert(0, "\r\n" + BAR + "OTHER PROBLEMS" + BAR + "");

        String message = "This is an automated message from tDAR reporting on files with issues.\r\n";
        message += "Run on:" + new Date().toString();
        if (count == 0) {
            message += " No issues found.";
            subject += " [NONE]";
        } else {
            subject += " [" + count + "]";
            message += missing.toString() + tainted.toString() + other.toString();
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String filename = "verify-" + df.format(new Date()) + ".txt";
        filestore.storeLog(filename, message);
        logger.debug(subject + "[ " + TdarConfiguration.getInstance().getSystemAdminEmail() + " ]");
        logger.debug(message);
        emailService.send(message, TdarConfiguration.getInstance().getSystemAdminEmail(), subject);
        logger.info("ending automated verification of files");
    }

    /*
     * Methods that are added in here are effectively run once.
     */
    @Scheduled(fixedDelay = 1999999999)
    @Transactional(readOnly = false)
    public void upgradeTasks() {
        // look in upgradeTasks to see what's there, if the task defined is not
        // there, then run the task, and then add it
        // UpgradeTask upgradeTask = checkIfRun("latLongInitialIndex");
        /*
         * if (!upgradeTask.hasRun()) { // enable this when it's ready
         * logger.info("beginning latLong Generation");
         * List<LatitudeLongitudeBox> allLatLongBoxes = genericService.findAll(LatitudeLongitudeBox.class);
         * resourceService.processManagedKeywords(allLatLongBoxes);
         * if (geoSearchService.isEnabled()) {
         * completedSuccessfully(upgradeTask);
         * }
         * }
         */
    }

    @SuppressWarnings("unused")
    @Transactional
    private void completedSuccessfully(UpgradeTask upgradeTask) {
        upgradeTask.setRun(true);
        upgradeTask.setRecordedDate(new Date());
        genericService.save(upgradeTask);
        logger.info("completed " + upgradeTask.getName());
    }

    @SuppressWarnings("unused")
    private UpgradeTask checkIfRun(String name) {
        UpgradeTask upgradeTask = new UpgradeTask();
        upgradeTask.setName(name);
        List<String> ignoreProperties = new ArrayList<String>();
        ignoreProperties.add("recordedDate");
        ignoreProperties.add("run");

        List<UpgradeTask> tasks = genericService.findByExample(UpgradeTask.class, upgradeTask, ignoreProperties,FindOptions.FIND_ALL);
        if (tasks.size() > 0 && tasks.get(0) != null) {
            return tasks.get(0);
        } else {
            return upgradeTask;
        }
    }
}

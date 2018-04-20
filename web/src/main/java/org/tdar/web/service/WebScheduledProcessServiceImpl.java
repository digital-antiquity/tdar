package org.tdar.web.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.RssService;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.processes.AccountUsageHistoryLoggingTask;
import org.tdar.core.service.processes.OccurranceStatisticsUpdateProcess;
import org.tdar.core.service.processes.PollEmailBouncesProcess;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.processes.daily.DailyEmailProcess;
import org.tdar.core.service.processes.daily.DailyStatisticsUpdate;
import org.tdar.core.service.processes.daily.DoiProcess;
import org.tdar.core.service.processes.daily.EmbargoedFilesUpdateProcess;
import org.tdar.core.service.processes.daily.RebuildHomepageCache;
import org.tdar.core.service.processes.daily.SalesforceSyncProcess;
import org.tdar.core.service.processes.daily.SitemapGeneratorProcess;
import org.tdar.core.service.processes.weekly.WeeklyFilestoreLoggingProcess;
import org.tdar.core.service.processes.weekly.WeeklyStatisticsLoggingProcess;
import org.tdar.core.service.processes.weekly.WeeklyUserNotificationCleanup;

@Service
public class WebScheduledProcessServiceImpl implements WebScheduledProcessService {

    private transient final RssService rssService;
    private transient final ScheduledProcessService scheduledProcessService;
    TdarConfiguration config = TdarConfiguration.getInstance();

    @Autowired
    public WebScheduledProcessServiceImpl(
            RssService rss,
            @Qualifier("scheduledProcessService") ScheduledProcessService scheduledProcessService) {
        this.rssService = rss;
        this.scheduledProcessService = scheduledProcessService;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#evictCaches()
     */
    @Override
    @Scheduled(fixedDelay = ScheduledProcessService.FIVE_MIN_MS)
    public void evictCaches() {
        if (config.shouldRunPeriodicEvents()) {
            rssService.evictRssCache();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronQueueEmail()
     */
    @Override
    @Scheduled(fixedDelay = ScheduledProcessService.FIVE_MIN_MS)
    public void cronQueueEmail() {
        scheduledProcessService.queue(SendEmailProcess.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronQueueEmail()
     */
    @Override
    @Scheduled(fixedDelay = ScheduledProcessService.FIVE_MIN_MS)
    public void cronProcessEmailErrors() {
        scheduledProcessService.queue(PollEmailBouncesProcess.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronDailyEmail()
     */
    @Override
    @Scheduled(cron = "0 1 0 * * *")
    public void cronDailyEmail() {
        logger.info("updating Daily Emails");
        scheduledProcessService.queue(DailyEmailProcess.class);
        scheduledProcessService.queue(SalesforceSyncProcess.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronDailyStats()
     */
    @Override
    @Scheduled(cron = "0 15 0 * * *")
    public void cronDailyStats() {
        logger.info("updating Daily stats");
        scheduledProcessService.queue(DailyStatisticsUpdate.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronEmbargoNotices()
     */
    @Override
    @Scheduled(cron = "0 45 0 * * *")
    public void cronEmbargoNotices() {
        logger.info("updating Embargo notices");
        scheduledProcessService.queue(EmbargoedFilesUpdateProcess.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronUpdateDois()
     */
    @Override
    @Scheduled(cron = "16 15 0 * * *")
    public void cronUpdateDois() {
        logger.info("updating DOIs");
        scheduledProcessService.queue(DoiProcess.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronUpdateAccountUsageHistory()
     */
    @Override
    // * Spring scheduling cron expressions: Seconds Minutes Hours Day-of-Month Month Day-of-Week Year (optional field)
    @Scheduled(cron = "0 0 1 1 * *")
    public void cronUpdateAccountUsageHistory() {
        logger.info("updating account usage history");
        scheduledProcessService.queue(AccountUsageHistoryLoggingTask.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronUpdateSitemap()
     */
    @Override
    @Scheduled(cron = "20 15 0 * * *")
    public void cronUpdateSitemap() {
        logger.info("updating Sitemaps");
        scheduledProcessService.queue(SitemapGeneratorProcess.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronUpdateHomepage()
     */
    @Override
    @Scheduled(cron = "1 15 0 * * *")
    public void cronUpdateHomepage() {
        scheduledProcessService.queue(RebuildHomepageCache.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronVerifyTdarFiles()
     */
    @Override
    @Scheduled(cron = "50 0 0 * * SUN")
    public void cronVerifyTdarFiles() throws IOException {
        scheduledProcessService.queue(WeeklyFilestoreLoggingProcess.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#updateOcurrenceStats()
     */
    @Override
    @Scheduled(cron = "50 0 0 * * SAT")
    public void updateOcurrenceStats() throws IOException {
        scheduledProcessService.queue(OccurranceStatisticsUpdateProcess.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.WebScheduledProcessService#cronGenerateWeeklyStats()
     */
    @Override
    @Scheduled(cron = "12 0 0 * * SUN")
    public void cronGenerateWeeklyStats() {
        scheduledProcessService.queue(WeeklyStatisticsLoggingProcess.class);
    }

    @Scheduled(cron = "50 0 0 * * SAT")
    public void removeOldNotifications() throws IOException {
        scheduledProcessService.queue(WeeklyUserNotificationCleanup.class);
    }

}

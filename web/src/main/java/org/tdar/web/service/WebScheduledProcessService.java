package org.tdar.web.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.RssService;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.processes.AccountUsageHistoryLoggingTask;
import org.tdar.core.service.processes.OccurranceStatisticsUpdateProcess;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.processes.daily.DailyEmailProcess;
import org.tdar.core.service.processes.daily.DailyStatisticsUpdate;
import org.tdar.core.service.processes.daily.DoiProcess;
import org.tdar.core.service.processes.daily.EmbargoedFilesUpdateProcess;
import org.tdar.core.service.processes.daily.RebuildHomepageCache;
import org.tdar.core.service.processes.daily.SalesforceSyncProcess;
import org.tdar.core.service.processes.daily.SitemapGeneratorProcess;
import org.tdar.core.service.processes.manager.ProcessManager;
import org.tdar.core.service.processes.weekly.WeeklyFilestoreLoggingProcess;
import org.tdar.core.service.processes.weekly.WeeklyStatisticsLoggingProcess;

@Service
public class WebScheduledProcessService {

    private transient final GenericService genericService;
    private transient final RssService rssService;
    private transient final AuthenticationService authenticationService;
    private transient final ProcessManager manager;
    private transient final ScheduledProcessService scheduledProcessService;
    TdarConfiguration config = TdarConfiguration.getInstance();

    @Autowired
    public WebScheduledProcessService(@Qualifier("genericService") GenericService gs,
            RssService rss, AuthenticationService auth, @Qualifier("processManager") ProcessManager pm,
            @Qualifier("scheduledProcessService") ScheduledProcessService scheduledProcessService) {
        this.genericService = gs;
        this.rssService = rss;
        this.authenticationService = auth;
        this.manager = pm;
        this.scheduledProcessService = scheduledProcessService;
    }

    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(fixedDelay = ScheduledProcessService.FIVE_MIN_MS)
    public void evictCaches() {
        if (config.shouldRunPeriodicEvents()) {
            rssService.evictRssCache();
        }
    }
    
    @Scheduled(fixedDelay = ScheduledProcessService.FIVE_MIN_MS)
    public void cronQueueEmail() {
        scheduledProcessService.queue(SendEmailProcess.class);
    }


    /**
     * Send emails at midnight
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void cronDailyEmail() {
        logger.info("updating Daily Emails");
        scheduledProcessService.queue(DailyEmailProcess.class);
        scheduledProcessService.queue(SalesforceSyncProcess.class);
    }

    /**
     * Send emails at midnight
     */
    @Scheduled(cron = "0 15 0 * * *")
    public void cronDailyStats() {
        logger.info("updating Daily stats");
        scheduledProcessService.queue(DailyStatisticsUpdate.class);
    }

    /**
     * Send emails at midnight
     */
    @Scheduled(cron = "0 45 0 * * *")
    public void cronEmbargoNotices() {
        logger.info("updating Embargo notices");
        scheduledProcessService.queue(EmbargoedFilesUpdateProcess.class);
    }
    

    /**
     * Generate DOIs
     */
    @Scheduled(cron = "16 15 0 * * *")
    public void cronUpdateDois() {
        logger.info("updating DOIs");
        scheduledProcessService.queue(DoiProcess.class);
    }

    /**
     * Log Account Usage History
     */
    // * Spring scheduling cron expressions: Seconds Minutes Hours Day-of-Month Month Day-of-Week Year (optional field)
    @Scheduled(cron = "0 0 1 1 * *")
    public void cronUpdateAccountUsageHistory() {
        logger.info("updating account usage history");
        scheduledProcessService.queue(AccountUsageHistoryLoggingTask.class);
    }

    /**
     * Update the Sitemap.org sitemap files
     */
    @Scheduled(cron = "20 15 0 * * *")
    public void cronUpdateSitemap() {
        logger.info("updating Sitemaps");
        scheduledProcessService.queue(SitemapGeneratorProcess.class);
    }

    /**
     * Update the Homepage's Featured Resources
     */
    @Scheduled(cron = "1 15 0 * * *")
    public void cronUpdateHomepage() {
        scheduledProcessService.queue(RebuildHomepageCache.class);
    }

    /**
     * Verify the @link Filestore once a week
     * 
     * @throws IOException
     */
    @Scheduled(cron = "50 0 0 * * SUN")
    public void cronVerifyTdarFiles() throws IOException {
        scheduledProcessService.queue(WeeklyFilestoreLoggingProcess.class);
    }

    @Scheduled(cron = "50 0 0 * * SAT")
    public void updateOcurrenceStats() throws IOException {
        scheduledProcessService.queue(OccurranceStatisticsUpdateProcess.class);
    }

    /**
     * Once a week, on Sundays, generate some static, cached stats for use by
     * the admin area and general system
     */
    @Scheduled(cron = "12 0 0 * * SUN")
    public void cronGenerateWeeklyStats() {
        scheduledProcessService.queue(WeeklyStatisticsLoggingProcess.class);
    }
}

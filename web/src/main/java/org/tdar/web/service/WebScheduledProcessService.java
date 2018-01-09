package org.tdar.web.service;

import java.io.IOException;

public interface WebScheduledProcessService {

    void evictCaches();

    void cronQueueEmail();

    /**
     * Send emails at midnight
     */
    void cronDailyEmail();

    /**
     * Send emails at midnight
     */
    void cronDailyStats();

    /**
     * Send emails at midnight
     */
    void cronEmbargoNotices();

    /**
     * Generate DOIs
     */
    void cronUpdateDois();

    /**
     * Log Account Usage History
     */
    void cronUpdateAccountUsageHistory();

    /**
     * Update the Sitemap.org sitemap files
     */
    void cronUpdateSitemap();

    /**
     * Update the Homepage's Featured Resources
     */
    void cronUpdateHomepage();

    /**
     * Verify the @link Filestore once a week
     * 
     * @throws IOException
     */
    void cronVerifyTdarFiles() throws IOException;

    void updateOcurrenceStats() throws IOException;

    /**
     * Once a week, on Sundays, generate some static, cached stats for use by
     * the admin area and general system
     */
    void cronGenerateWeeklyStats();

}
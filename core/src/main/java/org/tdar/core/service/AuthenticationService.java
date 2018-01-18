package org.tdar.core.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.TextProvider;

public interface AuthenticationService {

    /**
     * Get Total @link Resource Statistics (counts) Grouped by week
     * 
     * @return
     */
    Map<Date, Map<StatisticType, Long>> getResourceStatistics();

    /**
     * Get total @link ResourceCollection Statistics (counts) grouped by week
     * 
     * @return
     */
    Map<Date, Map<StatisticType, Long>> getCollectionStatistics();

    /**
     * get real-time @link Resource Statistics
     * 
     * @return
     */
    Map<ResourceType, List<BigInteger>> getCurrentResourceStats();

    /**
     * Get current user statistics (grouped by week)
     * 
     * @return
     */
    Map<Date, Map<StatisticType, Long>> getUserStatistics();

    /**
     * Get File Average Statistics (for pie chart) by extension
     * 
     * @param types
     * @return
     */
    Map<String, List<Number>> getFileAverageStats(List<VersionType> types);

    /**
     * Get the repository size grouped by week
     * 
     * @return
     */
    Map<Date, Map<StatisticType, Long>> getRepositorySizes();

    /**
     * Get current @link Resource Counts limited to those with files (grouped by week)
     * 
     * @return
     */
    Map<Date, Map<StatisticType, Long>> getResourceStatisticsWithFiles();

    /**
     * Get user Login stats (# of logins by # of users)
     * 
     * @return
     */
    List<Pair<Long, Long>> getUserLoginStats();

    Map<String, Long> getFileStats(List<VersionType> types);

    void cleanupOldDailyStats(Date date);

    void generateAggregateDailyDownloadData(Date date);

    Number countWeeklyEmails();

    StatsResultObject getStatsForCollection(ResourceCollection collection, TextProvider provider, DateGranularity granularity);

    StatsResultObject getStatsForAccount(BillingAccount account, TextProvider provider, DateGranularity granularity);

    void generateMonthlyResourceStats(DateTime date);

    void initializeNewAggregateEntries(DateTime date);

    /**
     * Find the count of downloads for a specified @link InformationResourceFile for a given date range, limited by the minimum occurrence count.
     * 
     * @param granularity
     * @param start
     * @param end
     * @param minCount
     * @param iRFileId
     * @return
     */
    List<AggregateDownloadStatistic> getAggregateDownloadStatsForFile(DateGranularity granularity, Date start, Date end, Long minCount, Long iRFileId);

    /**
     * Find the count of views for the specified resources for a given date range, limited by the minimum occurrence count.
     * 
     * @param granularity
     * @param start
     * @param end
     * @param minCount
     * @param resourceIds
     * @return
     */
    List<AggregateDayViewStatistic> getUsageStatsForResource(Resource resource);

}
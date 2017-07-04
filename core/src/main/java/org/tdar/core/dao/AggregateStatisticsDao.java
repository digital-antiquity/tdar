package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * The AggregateStatisticsDao works with the two main aggregate statistics tables:
 * * resource_access_day_agg
 * * file_download_day_agg
 * 
 * These two tables are populated by the DailyStatisticsUpdate which runs daily as a scheduled process pulling the last day's data into the tables aggregating
 * it at the "day," with columns for the year and month which allow for indexing and faster querying. These tables can then be queried to get historic data. The
 * goal of these tables is mainly for performance. Each table has the following columns:
 * * Date Accessed
 * * Count (count of views that day)
 * * Month
 * * Year
 * * Reference Id (Resource or InformationResourceFile)
 * 
 * @author abrin
 *
 */
@Component
public class AggregateStatisticsDao extends GenericDao {



    private static final String YYYY_MM_DD = "yyyy-MM-dd";


    /**
     * Returns access and download statistics for the set of resources for every year since 2010. Downloads are aggregated to the Resource Id.
     * 
     * @param resourceIds
     * @param provider
     * @return
     */
    public StatsResultObject getAnnualStats(Persistable p, TextProvider provider) {
        int start = 2010;
        int end = DateTime.now().getYear();
        List<String> labelKeys = new ArrayList<>();
        String sql = buildAnnualQueryAndLabels(p, provider, start, end, labelKeys);
        getLogger().trace(sql);
        StatsResultObject results = populateResultsObject(p, labelKeys, sql);
        return results;
    }

    /**
     * Returns access and download statistics for the set of resources for the last 12 months from the date specified. Downloads are aggregated to the Resource
     * Id.
     * 
     * @param resourceIds
     * @param provider
     * @return
     */
    public StatsResultObject getMonthlyStats(Persistable p, TextProvider provider) {
        DateTime lastYear = DateTime.now().minusYears(1).plusMonths(1);
        List<String> labelKeys = new ArrayList<>();
        String sql = buildMonthQueryAndLabels(p, provider, lastYear.toDate(), labelKeys);
        StatsResultObject results = populateResultsObject(p, labelKeys, sql);
        return results;
    }

    /**
     * Returns access and download statistics for the set of resources for the last 7 days available (current day is not available as it's not in these tables).
     * Downloads are aggregated to the Resource Id.
     * 
     * @param ids
     * @param provider
     * @return
     */
    public StatsResultObject getDailyStats(Persistable p, TextProvider provider) {
        DateTime lastYear = DateTime.now().minusDays(8);
        List<String> labelKeys = new ArrayList<>();
        String sql = buildDayQueryAndLabels(provider, lastYear.toDate(), labelKeys, p);
        StatsResultObject results = populateResultsObject(p, labelKeys, sql);
        return results;
    }

    /**
     * Migrate daily download stats into aggregate tables
     * 
     * @param date
     */
    public void generateAggregateDailyDownloadData(Date date) {
        String sql = String.format(TdarNamedQueries.DAILY_DOWNLOAD_UPDATE, date);
        getCurrentSession().createSQLQuery(sql).executeUpdate();

    }

    /**
     * For the set of resourceIds, the SQL Query, and labels, run the query and populate the StatsResultObject that's passed back to the controller
     * 
     * @param resourceIds
     * @param labelKeys
     * @param sql
     * @return
     */
    @SuppressWarnings("unchecked")
    private StatsResultObject populateResultsObject(Persistable p, List<String> labelKeys, String sql) {
        getLogger().debug("run sql");
        getLogger().debug(sql);
        Query query = getCurrentSession().createSQLQuery(sql);
        query.setParameter("id", p.getId());
        StatsResultObject results = new StatsResultObject();
        List<Object[]> list = (List<Object[]>) query.list();
        getLogger().debug("done sql");
        for (Object[] row : list) {
            List<Number> numbers = new ArrayList<>();
            Resource resource = new Resource(((Number) row[0]).longValue(), (String) row[1], ResourceType.valueOf((String) row[2]), "",
                    Status.valueOf((String) row[3]));
            for (int j = 4; j < row.length; j++) {
                numbers.add((Number) row[j]);
            }
            results.addRowData(new ResourceStatWrapper(resource, numbers));
        }
        getLogger().debug("return");
        results.setRowLabels(labelKeys);
        return results;
    }

    /**
     * Generate aggregate stats for the 7 days starting with the date specified
     * 
     * @param provider
     * @param startYear
     * @param startMonth
     * @param startDay
     * @param labelKeys
     * @return
     */
    private String buildDayQueryAndLabels(TextProvider provider, Date start_, List<String> labelKeys, Persistable p) {
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        DateTime start = new DateTime(start_);
        for (int i = 0; i < 7; i++) {
            if (i != 0) {
                viewSubQuerypart.append(", ");
            }
            downloadSubQuerypart.append(", ");

            start = start.plusDays(1);
            String date = start.toString(YYYY_MM_DD);
            viewSubQuerypart.append(String.format(TdarNamedQueries.DAY_VIEW_PART, date, start.getDayOfMonth(), start.getYear(), start.getMonthOfYear()));
            downloadSubQuerypart.append(String.format(TdarNamedQueries.DAY_DOWNLAOD_PART, date));
            labelKeys.add(provider.getText("statisticsService.view_count_day", Arrays.asList(date)));
            labelDownloadKeys.add(provider.getText("statisticsService.download_count_day", Arrays.asList(date)));
        }

        labelKeys.addAll(labelDownloadKeys);

        String sql = constructAggregateQuery(p, viewSubQuerypart, downloadSubQuerypart);
        getLogger().trace(sql);
        return sql;
    }

    private String constructAggregateQuery(Persistable p, StringBuilder viewSubQuerypart, StringBuilder downloadSubQuerypart) {
        String sql = String.format(TdarNamedQueries.ANNUAL_ACCESS_SKELETON, viewSubQuerypart.toString(), downloadSubQuerypart.toString());
        if (p instanceof BillingAccount) {
            sql += " where res.account_id=:id";
        }
        if (p instanceof ResourceCollection) {
            sql += String.format(" left join collection_resource cr on cr.resource_id=res.id left join collection_parents cp on cr.collection_id=cp.collection_id where (cr.collection_id=:id or cp.parent_id=:id)");
        }
        return sql;
    }

    /**
     * Generate an aggregate query for the 12 months starting with the month and year specified
     * 
     * @param provider
     * @param startYear
     * @param startMonth
     * @param labelKeys
     * @return
     */
    private String buildMonthQueryAndLabels(Persistable p, TextProvider provider, Date start_, List<String> labelKeys) {
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        int count = 0;
        DateTime start = new DateTime(start_);
        while (count < 12) {
            if (count != 0) {
                viewSubQuerypart.append(", ");
            }
            count++;
            int month = start.getMonthOfYear();
            int year = start.getYear();
            downloadSubQuerypart.append(", ");
            viewSubQuerypart.append(String.format(TdarNamedQueries.MONTH_VIEW_PART, month, year));
            downloadSubQuerypart.append(String.format(TdarNamedQueries.MONTH_DOWNLOAD_PART, month, year));
            labelKeys.add(provider.getText("statisticsService.view_count_month", Arrays.asList(month, year)));
            labelDownloadKeys.add(provider.getText("statisticsService.download_count_month", Arrays.asList(month, year)));
            start = start.plusMonths(1);
        }
        labelKeys.addAll(labelDownloadKeys);

        String sql = constructAggregateQuery(p, viewSubQuerypart, downloadSubQuerypart);
        getLogger().trace(sql);
        return sql;
    }

    /**
     * Generate annual, aggregate usage stats for the range of specified years.
     * @param p 
     * 
     * @param provider
     * @param start
     * @param end
     * @param labelKeys
     * @return
     */
    private String buildAnnualQueryAndLabels(Persistable p, TextProvider provider, int start, int end, List<String> labelKeys) {
        int i = start;
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        while (i <= end) {
            if (i != start) {
                viewSubQuerypart.append(", ");
            }
            downloadSubQuerypart.append(", ");
            viewSubQuerypart.append(String.format(TdarNamedQueries.ANNUAL_VIEW_PART, i));
            downloadSubQuerypart.append(String.format(TdarNamedQueries.ANNUAL_DOWNLOAD_PART, i));
            labelKeys.add(provider.getText("statisticsService.view_count_annual", Arrays.asList(i)));
            labelDownloadKeys.add(provider.getText("statisticsService.download_count_annual", Arrays.asList(i)));
            i++;
        }
        labelKeys.addAll(labelDownloadKeys);

        String sql = constructAggregateQuery(p, viewSubQuerypart, downloadSubQuerypart);
        getLogger().trace(sql);
        return sql;
    }

    public void cleanupOldDailyStats(Date date) {
        String sql = String.format(TdarNamedQueries.DAILY_RESOURCE_STATS_CLEANUP, date);
        getLogger().trace(sql);
        getCurrentSession().createSQLQuery(sql).executeUpdate();

    }

    /**
     * Creates the entries in the year aggregate table so that all other tasks can be update statements
     * 
     * @param date
     */
    public void createNewAggregateEntries(DateTime date) {
        Query query = getCurrentSession().createSQLQuery(String.format(TdarNamedQueries.AGG_RESOURCE_SETUP_MONTH, date.getYear(), date.getMonthOfYear()));
        query.setParameter("date", date.withTimeAtStartOfDay().toDate());
        // if we're on the 1st of the month, than we need to generate entries for everything
        if (date.getDayOfMonth() == 1) {
            query.setParameter("date", DateTime.now().minusYears(100).toDate());
        }
        query.executeUpdate();
    }

    
    /**
     * not enabled ... the monthly currently works nicely
     * @param date
     */
    public void resetAnnualTable(DateTime date) {
        Query query = getCurrentSession().createSQLQuery(TdarNamedQueries.ANNUAL_RESOURCE_CLEANUP);
        query.setParameter("year", date.getYear());
        query.executeUpdate();
        query = getCurrentSession().createSQLQuery(TdarNamedQueries.ANNUAL_RESOURCE_UPDATE );
        query.setParameter("year", date.getYear());
        query.executeUpdate();
        
    }
    
    /**
     * inserts into the aggregate table the actual values for the last month
     * 
     * @param date
     */
    public void updateMonthly(DateTime date) {

        DateTime midnight = date.withTimeAtStartOfDay();
        String sql = String.format(TdarNamedQueries.AGG_RESOURCE_INSERT_MONTH, date.getDayOfMonth(), midnight.toString(YYYY_MM_DD),
                midnight.plusDays(1).toString(YYYY_MM_DD));
        Query query = getCurrentSession().createSQLQuery(sql);
        query.setParameter("month", date.getMonthOfYear());
        query.setParameter("year", date.getYear());
        getLogger().debug(sql);
        query.executeUpdate();

        sql = String.format(TdarNamedQueries.AGG_RESOURCE_INSERT_MONTH_BOT, date.getDayOfMonth(), midnight.toString(YYYY_MM_DD),
                midnight.plusDays(1).toString(YYYY_MM_DD));
        query = getCurrentSession().createSQLQuery(sql);
        query.setParameter("month", date.getMonthOfYear());
        query.setParameter("year", date.getYear());
        query.executeUpdate();

    }

    public List<Resource> getWeeklyPopularResources(int count) {
        List<Resource> resources = new ArrayList<>();
        DateTime end = new DateTime();
        DateTime start = end.minusDays(7);
        String sql = String.format(TdarNamedQueries.WEEKLY_POPULAR, start.toString(YYYY_MM_DD), end.toString(YYYY_MM_DD), count);
        Query query = getCurrentSession().createSQLQuery(sql);
        List list = query.list();
        for (Object o : list) {
            Object[] obj = (Object[])o;
            Resource resource = find(Resource.class, ((Number) obj[0]).longValue());
            if (PersistableUtils.isNotNullOrTransient(resource)) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public List<AggregateDayViewStatistic> getUsageStatsForResource(Resource resource) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.MONTHLY_USAGE_FOR_RESOURCE);
        query.setParameter("resourceId", resource.getId());
        return query.list();
    }


    @SuppressWarnings("unchecked")
    public List<AggregateDownloadStatistic> getDownloadStatsForFile(DateGranularity granularity, Date start, Date end,
            Long minCount, Long... irFileIds) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.FILE_DOWNLOAD_HISTORY);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setParameter("minCount", minCount);
        query.setParameterList("fileIds", Arrays.asList(irFileIds));
        return query.list();
    }

}

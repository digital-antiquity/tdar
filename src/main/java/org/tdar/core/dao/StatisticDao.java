package org.tdar.core.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.TextProvider;

@Component
public class StatisticDao extends Dao.HibernateBase<AggregateStatistic> {

    public StatisticDao() {
        super(AggregateStatistic.class);
    }

    @SuppressWarnings("unchecked")
    public Map<Date, Map<StatisticType, Long>> getStatistics(Date fromDate, Date toDate, StatisticType... types) {
        Query query = getCurrentSession().getNamedQuery(QUERY_USAGE_STATS);
        query.setDate("fromDate", fromDate);
        query.setDate("toDate", toDate);
        query.setParameterList("statTypes", types);
        Map<Date, Map<StatisticType, Long>> toReturn = new HashMap<Date, Map<StatisticType, Long>>();
        for (AggregateStatistic result : (List<AggregateStatistic>) query.list()) {
            Date date = result.getRecordedDate();
            if (!toReturn.containsKey(date)) {
                toReturn.put(date, new HashMap<StatisticType, Long>());
                for (StatisticType type : types) {
                    toReturn.get(date).put(type, 0L);
                }
            }
            Map<StatisticType, Long> stat = toReturn.get(date);
            stat.put(result.getStatisticType(), result.getValue());
        }

        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public Map<ResourceType, List<BigInteger>> getCurrentResourceStats() {
        Query query = getCurrentSession().createSQLQuery(QUERY_SQL_RAW_RESOURCE_STAT_LOOKUP);
        Map<ResourceType, List<BigInteger>> toReturn = new HashMap<ResourceType, List<BigInteger>>();
        for (Object[] result_ : (List<Object[]>) query.list()) {
            List<BigInteger> stat = new ArrayList<BigInteger>();
            toReturn.put(ResourceType.valueOf((String) result_[0]), stat);
            stat.add((BigInteger) result_[1]);
            stat.add((BigInteger) result_[2]);
            stat.add((BigInteger) result_[3]);
            stat.add((BigInteger) result_[4]);
        }
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Number>> getFileAverageStats(List<VersionType> types) {
        Query query = getCurrentSession().getNamedQuery(QUERY_FILE_STATS);
        query.setParameterList("types", types);
        Map<String, List<Number>> toReturn = new HashMap<String, List<Number>>();
        for (Object[] result_ : (List<Object[]>) query.list()) {
            List<Number> stat = new ArrayList<Number>();
            toReturn.put((String) result_[0], stat);
            stat.add((Double) result_[1]); // average
            stat.add((Long) result_[2]); // min
            stat.add((Long) result_[3]); // max
        }
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public List<Pair<Long, Long>> getUserLoginStats() {
        Query query = getCurrentSession().getNamedQuery(QUERY_LOGIN_STATS);
        List<Pair<Long, Long>> toReturn = new ArrayList<Pair<Long, Long>>();
        for (Object[] result_ : (List<Object[]>) query.list()) {
            Number total = (Number) result_[0];
            Number count = (Number) result_[1];
            toReturn.add(Pair.create(total.longValue(), count.longValue()));
        }
        return toReturn;
    }

    public Map<String, Long> getFileStats(List<VersionType> types) {
        Query query = getCurrentSession().getNamedQuery(QUERY_FILE_SIZE_TOTAL);
        query.setParameterList("types", types);
        Map<String, Long> toReturn = new HashMap<>();
        for (Object[] result_ : (List<Object[]>) query.list()) {
            String txt = StringUtils.upperCase((String) result_[0]);
            switch (txt) {
                case "JPEG":
                    txt = "JPG";
                    break;
                case "TIFF":
                    txt = "TIF";
                default:
                    break;
            }
            Long val = toReturn.get(txt);
            if (val == null) {
                val = 0L;
            }
            toReturn.put(txt, val + (Long) result_[1]);
        }
        return toReturn;
    }

    public void generateAggregateDailyDownloadData(Date date) {
        String sql = String.format(TdarNamedQueries.DAILY_DOWNLOAD_UPDATE, date);
        getCurrentSession().createSQLQuery(sql).executeUpdate();

    }

    public void generateAggregateDailyResourceData(Date date) {
        String sql = String.format(TdarNamedQueries.DAILY_RESOURCE_UPDATE, date);
        getLogger().trace(sql);
        getCurrentSession().createSQLQuery(sql).executeUpdate();

    }

    public Number countWeeklyEmails() {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.WEEKLY_EMAIL_STATS);
        query.setParameter("date", DateTime.now().minusDays(7).toDate());
        return (Number) query.uniqueResult();
    }

    public StatsResultObject getAnnualStats(Collection<Long> resourceIds, TextProvider provider) {
        int start = 2010;
        int end = DateTime.now().getYear();
        List<String> labelKeys = new ArrayList<>();
        String sql = buildAnnualQueryAndLabels(provider, start, end, labelKeys);
        StatsResultObject results = populateResultsObject(resourceIds, labelKeys, sql);
        return results;
    }

    
    public StatsResultObject getMonthlyStats(Collection<Long> resourceIds, TextProvider provider) {
        DateTime lastYear = DateTime.now().minusYears(1);
        int startYear = lastYear.getYear();
        int startMonth = lastYear.getMonthOfYear();
        List<String> labelKeys = new ArrayList<>();
        String sql = buildMonthQueryAndLabels(provider, startYear, startMonth, labelKeys);
        StatsResultObject results = populateResultsObject(resourceIds, labelKeys, sql);
        return results;
    }

    private StatsResultObject populateResultsObject(Collection<Long> resourceIds, List<String> labelKeys, String sql) {
        Query query = getCurrentSession().createSQLQuery(sql);
        query.setParameterList("ids", resourceIds);
        StatsResultObject results = new StatsResultObject();
        for (Object[] row : (List<Object[]>) query.list()) {
            List<Number> numbers = new ArrayList<>();
            Resource resource = new Resource(((Number)row[0]).longValue(), (String)row[1], ResourceType.valueOf((String)row[2]), "", Status.valueOf((String)row[3]));
            Pair<Resource, List<Number>> rowData = new Pair<Resource, List<Number>>(resource, numbers);
            for (int j = 4; j < row.length; j++) {
                numbers.add((Number)row[j]);
            }
            results.addRowData(rowData);
        }
        results.setRowLabels(labelKeys);
        return results;
    }
    
    private String buildDayQueryAndLabels(TextProvider provider, int startYear, int startMonth, int startDay, List<String> labelKeys) {
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        DateTime start = new DateTime(startYear, startMonth, startDay, 0, 0);
        for (int i=0; i < 7; i++) {
            start = start.plusDays(1);
            String date = start.toString("YYYY-MM-dd");
            viewSubQuerypart.append(String.format(TdarNamedQueries.DAY_VIEW_PART, date));
            downloadSubQuerypart.append(String.format(TdarNamedQueries.DAY_DOWNLAOD_PART, date));
            labelKeys.add(provider.getText("statisticsService.view_count_annual",Arrays.asList(date)));
            labelDownloadKeys.add(provider.getText("statisticsService.download_count_annual",Arrays.asList(date)));
        }
        
        labelKeys.add(provider.getText("statisticsService.view_count_annual_total"));
        labelKeys.addAll(labelDownloadKeys);
        labelKeys.add(provider.getText("statisticsService.download_count_annual_total"));
        
        String sql = String.format(TdarNamedQueries.ANNUAL_ACCESS_SKELETON, viewSubQuerypart.toString(), downloadSubQuerypart.toString());
        return sql;
    }

    
    private String buildMonthQueryAndLabels(TextProvider provider, int startYear, int startMonth, List<String> labelKeys) {
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        int i = startYear;
        int j = startMonth +1;
        while (i <= startYear +1) {
            while (j <= 12) {
                viewSubQuerypart.append(String.format(TdarNamedQueries.MONTH_VIEW_PART, i,j));
                downloadSubQuerypart.append(String.format(TdarNamedQueries.MONTH_DOWNLAOD_PART, i,j));
                labelKeys.add(provider.getText("statisticsService.view_count_month",Arrays.asList(i,j)));
                labelDownloadKeys.add(provider.getText("statisticsService.download_count_month",Arrays.asList(i,j)));
                if (j == startMonth) {
                    break;
                }
                j++;
            }
            j = 1;
            i++;
        }
        labelKeys.add(provider.getText("statisticsService.view_count_annual_total"));
        labelKeys.addAll(labelDownloadKeys);
        labelKeys.add(provider.getText("statisticsService.download_count_annual_total"));
        
        String sql = String.format(TdarNamedQueries.ANNUAL_ACCESS_SKELETON, viewSubQuerypart.toString(), downloadSubQuerypart.toString());
        return sql;
    }


    private String buildAnnualQueryAndLabels(TextProvider provider, int start, int end, List<String> labelKeys) {
        int i = start;
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        while (i <= end) {
            viewSubQuerypart.append(String.format(TdarNamedQueries.ANNUAL_VIEW_PART, i));
            downloadSubQuerypart.append(String.format(TdarNamedQueries.ANNUAL_DOWNLAOD_PART, i));
            labelKeys.add(provider.getText("statisticsService.view_count_annual",Arrays.asList(i)));
            labelDownloadKeys.add(provider.getText("statisticsService.download_count_annual",Arrays.asList(i)));
            i++;
        }
        labelKeys.add(provider.getText("statisticsService.view_count_annual_total"));
        labelKeys.addAll(labelDownloadKeys);
        labelKeys.add(provider.getText("statisticsService.download_count_annual_total"));
        
        String sql = String.format(TdarNamedQueries.ANNUAL_ACCESS_SKELETON, viewSubQuerypart.toString(), downloadSubQuerypart.toString());
        return sql;
    }

    public StatsResultObject getDailyStats(Collection<Long> ids, TextProvider provider) {
        DateTime lastYear = DateTime.now().minusDays(8);
        int startYear = lastYear.getYear();
        int startMonth = lastYear.getMonthOfYear();
        int startDay = lastYear.getDayOfMonth();
        List<String> labelKeys = new ArrayList<>();
        String sql = buildDayQueryAndLabels(provider, startYear, startMonth,startDay, labelKeys);
        StatsResultObject results = populateResultsObject(ids, labelKeys, sql);
        return results;
    }
}

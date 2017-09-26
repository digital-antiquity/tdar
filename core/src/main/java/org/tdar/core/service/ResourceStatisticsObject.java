package org.tdar.core.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.DailyTotal;

import com.opensymphony.xwork2.TextProvider;

public class ResourceStatisticsObject {

    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private List<AggregateDayViewStatistic> usageStatsForResource = new ArrayList<>();
    private Map<String, List<AggregateDownloadStatistic>> downloadStats = new HashMap<>();

    private TreeSet<Integer> yearLabels = new TreeSet<>();
    private TreeSet<String> monthLabels = new TreeSet<>();
    private TreeSet<String> dayLabels = new TreeSet<>();
    private Map<String, List<Long>> allByYear;
    private Map<String, List<Long>> allByMonth;
    private Map<String, List<Long>> allByDay;
    private SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD);
    Set<String> keys = new HashSet<>();
    private String graphJson;

    private Map<String, Map<String, Object>> map = new HashMap<>();
    private Map<String, DailyTotal> allMap = new TreeMap<>();
    private Map<String, DailyTotal> monthlyMap = new HashMap<>();
    private Map<String, DailyTotal> annualMap = new HashMap<>();

    public ResourceStatisticsObject(TextProvider provider, List<AggregateDayViewStatistic> usageStatsForResources,
            Map<String, List<AggregateDownloadStatistic>> downloadStats, Resource resource) {

        DateTime lastYear = DateTime.now().minusDays(255).withDayOfMonth(1);
        DateTime lastWeek = DateTime.now().minusDays(7);

        
        List<DailyTotal> last7 = new ArrayList<>();
        List<String> filenames = new ArrayList<>();
        
        if (resource instanceof InformationResource) {
            for (InformationResourceFile file : ((InformationResource) resource).getInformationResourceFiles()) {
                filenames.add(file.getFilename());
            }
        }
        for (AggregateDayViewStatistic stat : usageStatsForResources) {
            String key = String.format("%s-%s", stat.getMonth(),stat.getYear());
            String ykey = String.format("%s", stat.getYear());
            for (DailyTotal total : stat.getDailyTotals()) {
                if (total.getDate().after(lastWeek.toDate())) {
                    last7.add(total);
                }
                
                total.setTotalDownloads(new ArrayList<>(filenames.size()));
                getAllMap().put(total.getDateString(), total);

                if (total.getDate().after(lastYear.toDate())) {
                    DailyTotal mtotal = getMonthlyMap().getOrDefault(key, new DailyTotal(0, 0, key, null));
                    mtotal.setTotal(mtotal.getTotal() + total.getTotal());
                    mtotal.setTotalBot(mtotal.getTotalBot() + total.getTotalBot());
                    mtotal.setTotalDownloads(new ArrayList<>(filenames.size()));
                    getMonthlyMap().put(key, mtotal);
                }
            }
            DailyTotal atotal = getAnnualMap().getOrDefault(ykey, new DailyTotal(0, 0, ykey, null));
            getAnnualMap().put(ykey, atotal);
            atotal.setTotal(atotal.getTotal() + stat.getTotal().intValue());
            atotal.setTotalBot(atotal.getTotalBot() + stat.getTotal_bot().intValue());
            atotal.setTotalDownloads(new ArrayList<>(filenames.size()));
        }

        for (int i=0; i< filenames.size(); i++) {
            String filename = filenames.get(i);
            for (AggregateDownloadStatistic stat : downloadStats.get(filename)) {
                String key = format.format(stat.getAggregateDate());
                String ykey = Integer.toBinaryString(stat.getYear());
                DailyTotal dailyTotal = getAllMap().get(key);
                dailyTotal.getTotalDownloads().set(i, stat.getCount().intValue());
                if (dailyTotal.getDate().after(lastYear.toDate())) {
                    DailyTotal mtotal = getMonthlyMap().getOrDefault(key, new DailyTotal(0, 0, key, null));
                    mtotal.getTotalDownloads().set(i, mtotal.getTotalDownloads().get(i)  + stat.getCount().intValue() );
                }
                DailyTotal ytotal = getMonthlyMap().getOrDefault(ykey, new DailyTotal(0, 0, key, null));
                ytotal.getTotalDownloads().set(i, ytotal.getTotalDownloads().get(i)  + stat.getCount().intValue() );
            }
        }
        String viewsText = provider.getText("resourceStatisticsController.views");

    }

    /**
     * Sort the labels alphabetically, and then pass them to the "unwrap" function so they're sorted alphabetically there too
     * 
     * @param byYear
     * @param byMonth
     * @param byDay
     */
    private void sortAndSetupForViewLayer(Map<Integer, Map<String, Long>> byYear, Map<String, Map<String, Long>> byMonth,
            Map<String, Map<String, Long>> byDay) {
        keys.addAll(getDownloadStats().keySet());

        yearLabels.addAll(byYear.keySet());
        monthLabels.addAll(byMonth.keySet());
        dayLabels.addAll(byDay.keySet());

        setAllByYear(unwrapByKey(byYear, getYearLabels()));
        setAllByMonth(unwrapByKey(byMonth, getMonthLabels()));
        setAllByDay(unwrapByKey(byDay, getDayLabels()));
    }

    /**
     * build a map that has the view statistics ordered properly
     * 
     * @param byYear
     * @param byMonth
     * @param byDay
     * @param lastYear
     * @param lastWeek
     * @param map
     */
    private void incrementDownloadStatistics(Map<Integer, Map<String, Long>> byYear, Map<String, Map<String, Long>> byMonth,
            Map<String, Map<String, Long>> byDay, DateTime lastYear, DateTime lastWeek) {
        for (Entry<String, List<AggregateDownloadStatistic>> entry : getDownloadStats().entrySet()) {
            for (AggregateDownloadStatistic stat : entry.getValue()) {
                Date aggregateDate = stat.getAggregateDate();
                String dayString = format.format(aggregateDate);
                map.putIfAbsent(dayString, new HashMap<>());

                if (StringUtils.isNotBlank(entry.getKey())) {
                    map.get(dayString).put(entry.getKey(), stat.getCount());
                    map.get(dayString).put("date", aggregateDate);
                }
                incrementKey(byYear, stat.getYear(), stat.getCount(), entry.getKey());
                if (lastYear.isBefore(aggregateDate.getTime())) {
                    incrementKey(byMonth, formatMonth(stat.getYear(), stat.getMonth()), stat.getCount(), entry.getKey());
                }
                if (lastWeek.isBefore(aggregateDate.getTime())) {
                    incrementKey(byDay, dayString, stat.getCount(), entry.getKey());
                }
            }
        }
    }

    private void incrementViewStatistics(Map<Integer, Map<String, Long>> byYear, Map<String, Map<String, Long>> byMonth, Map<String, Map<String, Long>> byDay,
            String viewsText, DateTime lastYear, DateTime lastWeek) {
        for (AggregateDayViewStatistic s : getUsageStatsForResource()) {
            incrementKey(byYear, s.getYear(), s.getTotal(), viewsText);
            for (DailyTotal t : s.getDailyTotals()) {
                DateTime date = new DateTime(t.getDate());
                Date date2 = date.toDate();
                String dayString = date.toString(YYYY_MM_DD);

                map.putIfAbsent(dayString, new HashMap<String, Object>());
                Map<String, Object> submap = map.get(dayString);
                submap.put(viewsText, t.getTotal());
                submap.put("date", dayString);

                if (lastYear.isBefore(date)) {
                    incrementKey(byMonth, formatMonth(s.getYear(), s.getMonth()), s.getTotal(), viewsText);
                }
                if (lastWeek.isBefore(date)) {
                    incrementKey(byDay, dayString, t.getTotal().longValue(), viewsText);
                }
                keys.add(viewsText);
            }

        }
    }

    public String getCategoryKeys() {
        return StringUtils.join(keys, ",");
    }

    private void setupLabels(Map<String, Map<String, Long>> byMonth, Map<String, Map<String, Long>> byDay, DateTime lastYear, DateTime lastWeek) {
        // ** setup with all months and days **
        DateTime dt = lastWeek;
        while (dt.isBeforeNow()) {
            String key = format.format(dt.toDate());
            byDay.put(key, new HashMap<>());
            getDayLabels().add(key);
            dt = dt.plusDays(1);
        }

        dt = lastYear;
        while (dt.isBeforeNow()) {
            String key = formatMonth(dt.getYear(), dt.getMonthOfYear());
            byMonth.put(key, new HashMap<>());
            getMonthLabels().add(key);
            dt = dt.plusMonths(1);
        }
    }

    private String formatMonth(Integer year, Integer month) {
        return String.format("%s-%02d", year, month);
    }

    private <T> Map<String, List<Long>> unwrapByKey(Map<T, Map<String, Long>> by, Set<T> order) {
        Map<String, List<Long>> toReturn = new HashMap<>();
        for (String key : keys) {
            List<Long> list = new ArrayList<>();
            for (T itm : order) {
                for (T year : by.keySet()) {
                    if (itm != year) {
                        continue;
                    }
                    Map<String, Long> map = by.get(year);
                    list.add(map.getOrDefault(key, 0L));
                }

            }
            toReturn.put(key, list);
        }
        return toReturn;
    }

    private <T> void incrementKey(Map<T, Map<String, Long>> parent, T subKey, Long count_, String k) {
        Map<String, Long> by = parent.getOrDefault(subKey, new HashMap<>());
        long count = 0l;
        if (count_ != null) {
            count = count_;
        }
        by.put(k, by.getOrDefault(k, 0L) + count);
        parent.put(subKey, by);
    }

    public Map<String, Map<String, Object>> getMap() {
        return map;
    }

    public void setMap(Map<String, Map<String, Object>> map) {
        this.map = map;
    }

    public List<AggregateDayViewStatistic> getUsageStatsForResource() {
        return usageStatsForResource;
    }

    public void setUsageStatsForResource(List<AggregateDayViewStatistic> usageStatsForResource) {
        this.usageStatsForResource = usageStatsForResource;
    }

    public Map<String, List<AggregateDownloadStatistic>> getDownloadStats() {
        return downloadStats;
    }

    public void setDownloadStats(Map<String, List<AggregateDownloadStatistic>> downloadStats) {
        this.downloadStats = downloadStats;
    }

    public TreeSet<Integer> getYearLabels() {
        return yearLabels;
    }

    public TreeSet<String> getMonthLabels() {
        return monthLabels;
    }

    public TreeSet<String> getDayLabels() {
        return dayLabels;
    }

    public Map<String, List<Long>> getAllByYear() {
        return allByYear;
    }

    public void setAllByYear(Map<String, List<Long>> allByYear) {
        this.allByYear = allByYear;
    }

    public Map<String, List<Long>> getAllByMonth() {
        return allByMonth;
    }

    public void setAllByMonth(Map<String, List<Long>> allByMonth) {
        this.allByMonth = allByMonth;
    }

    public Map<String, List<Long>> getAllByDay() {
        return allByDay;
    }

    public void setAllByDay(Map<String, List<Long>> allByDay) {
        this.allByDay = allByDay;
    }

    public String getGraphJson() {
        return graphJson;
    }

    public void setGraphJson(String graphJson) {
        this.graphJson = graphJson;
    }

    public Map<String, DailyTotal> getAnnualMap() {
        return annualMap;
    }

    public void setAnnualMap(Map<String, DailyTotal> annualMap) {
        this.annualMap = annualMap;
    }

    public Map<String, DailyTotal> getMonthlyMap() {
        return monthlyMap;
    }

    public void setMonthlyMap(Map<String, DailyTotal> monthlyMap) {
        this.monthlyMap = monthlyMap;
    }

    public Map<String, DailyTotal> getAllMap() {
        return allMap;
    }

    public void setAllMap(Map<String, DailyTotal> allMap) {
        this.allMap = allMap;
    }

}

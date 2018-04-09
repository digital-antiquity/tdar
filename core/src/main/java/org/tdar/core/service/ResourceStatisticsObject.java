package org.tdar.core.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.DailyTotal;

import com.opensymphony.xwork2.TextProvider;

public class ResourceStatisticsObject {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD);

    private Map<String, DailyTotal> allMap = new TreeMap<>();
    private Map<String, DailyTotal> monthlyMap = new TreeMap<>();
    private Map<String, DailyTotal> annualMap = new TreeMap<>();
    private Map<String, DailyTotal> dailyMap = new TreeMap<>();
    private List<String> filenames = new ArrayList<>();

    public ResourceStatisticsObject(TextProvider provider, List<AggregateDayViewStatistic> usageStatsForResources,
            Map<String, List<AggregateDownloadStatistic>> downloadStats, Resource resource) {

        DateTime lastYear = DateTime.now().minusDays(255).withDayOfMonth(1);
        DateTime lastWeek = DateTime.now().minusDays(7);

        Date lastWeekDate = lastWeek.toDate();
        Date lastYearDate = lastYear.toDate();

        if (resource instanceof InformationResource) {
            for (InformationResourceFile file : ((InformationResource) resource).getInformationResourceFiles()) {
                getFilenames().add(file.getFilename());
            }
        }

        setupDailyUsage(usageStatsForResources, lastWeekDate, lastYearDate);
        setupDownloadUsage(downloadStats, lastYearDate);

    }

    private void setupDownloadUsage(Map<String, List<AggregateDownloadStatistic>> downloadStats, Date lastYearDate) {
        for (int i = 0; i < getFilenames().size(); i++) {
            String filename = getFilenames().get(i);
            for (AggregateDownloadStatistic stat : downloadStats.get(filename)) {
                String key = format.format(stat.getAggregateDate());
                String mkey = String.format("%02d-%s", stat.getMonth(), stat.getYear());
                String ykey = Integer.toString(stat.getYear());
                DailyTotal dailyTotal = getOrCreate(stat, key, getAllMap());
                dailyTotal.addTotalDownload(i, stat.getCount());
                if (dailyTotal.getDate().after(lastYearDate)) {
                    DailyTotal mtotal = getOrCreate(stat, mkey, getMonthlyMap());
                    mtotal.addTotalDownload(i, stat.getCount());
                }
                DailyTotal ytotal = getOrCreate(stat, ykey, getAnnualMap());
                ytotal.addTotalDownload(i, stat.getCount());
            }
        }
    }

    private DailyTotal getOrCreate(AggregateDownloadStatistic stat, String mkey, Map<String, DailyTotal> map) {
        DailyTotal mtotal = map.get(mkey);
        if (mtotal == null) {
            mtotal = createEmptyDailyTotal(mkey);
            mtotal.setDate(stat.getAggregateDate());
            map.put(mkey, mtotal);
        }
        return mtotal;
    }

    private void setupDailyUsage(List<AggregateDayViewStatistic> usageStatsForResources, Date lastWeekDate, Date lastYearDate) {
        for (AggregateDayViewStatistic stat : usageStatsForResources) {
            String key = String.format("%02d-%s", stat.getMonth(), stat.getYear());
            String ykey = String.format("%s", stat.getYear());
            for (DailyTotal total : stat.getDailyTotals()) {
                if (total.getDate().after(lastWeekDate)) {
                    getDailyMap().put(total.getDateString(), total);
                }

                total.setTotalDownloads(createDownloadList());
                getAllMap().put(total.getDateString(), total);

                if (total.getDate().after(lastYearDate)) {
                    DailyTotal mtotal = getMonthlyMap().getOrDefault(key, createEmptyDailyTotal(key));
                    mtotal.addTotals(total.getTotal(), total.getTotalBot(), createDownloadList());
                    getMonthlyMap().put(key, mtotal);
                }
            }
            DailyTotal atotal = getAnnualMap().get(ykey);
            if (atotal == null) {
                atotal = createEmptyDailyTotal(ykey);
                getAnnualMap().put(ykey, atotal);
            }

            // FIXME: this is not great, but the assumption in the DB is that the total here is Everything
            Long total_ = 0L;
            if (stat.getTotal() != null) {
                total_ += stat.getTotal();
            }
            if (stat.getTotalBot() != null) {
                total_ += stat.getTotalBot();
            }
            atotal.addTotals(total_, stat.getTotalBot(), createDownloadList());
            // logger.debug("{}:{} -- {} ", stat.getYear(), stat.getMonth(), total_);
        }
    }

    private DailyTotal createEmptyDailyTotal(String key) {
        return new DailyTotal(0, 0, key, null, createDownloadList());
    }

    private List<Integer> createDownloadList() {
        List<Integer> lst = new ArrayList<>();
        for (int i = 0; i < filenames.size(); i++) {
            lst.add(0);
        }

        return lst;
    }

    public SimpleDateFormat getFormat() {
        return format;
    }

    public void setFormat(SimpleDateFormat format) {
        this.format = format;
    }

    public Map<String, DailyTotal> getAllMap() {
        return allMap;
    }

    public void setAllMap(Map<String, DailyTotal> allMap) {
        this.allMap = allMap;
    }

    public Map<String, DailyTotal> getMonthlyMap() {
        return monthlyMap;
    }

    public void setMonthlyMap(Map<String, DailyTotal> monthlyMap) {
        this.monthlyMap = monthlyMap;
    }

    public Map<String, DailyTotal> getAnnualMap() {
        return annualMap;
    }

    public void setAnnualMap(Map<String, DailyTotal> annualMap) {
        this.annualMap = annualMap;
    }

    public Map<String, DailyTotal> getDailyMap() {
        return dailyMap;
    }

    public void setDailyMap(Map<String, DailyTotal> dailyMap) {
        this.dailyMap = dailyMap;
    }

    public List<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

}

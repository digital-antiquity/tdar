package org.tdar.struts.action.resource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.DailyTotal;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.StatisticsService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource/usage")
public class ResourceUsageAction extends AbstractAuthenticatableAction implements Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = 97924349900255693L;
    private List<AggregateDayViewStatistic> usageStatsForResources = new ArrayList<>();
    private Map<String, List<AggregateDownloadStatistic>> downloadStats = new HashMap<>();

    private Resource resource;
    private Long id;

    @Autowired
    private StatisticsService StatisticsService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private SerializationService serializationService;

    @Override
    @Action(value = "{id}", results = {
            @Result(name = SUCCESS, location = "../usage.ftl")
    })
    public String execute() throws TdarActionException {
        return SUCCESS;
    }

    List<Integer> yearLabels = new ArrayList<>();
    List<String> monthLabels = new ArrayList<>();
    List<String> dayLabels = new ArrayList<>();
    private Map<String, List<Long>> allByYear;
    private Map<String, List<Long>> allByMonth;
    private Map<String, List<Long>> allByDay;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Set<String> keys = new HashSet<>();
    private String graphJson;

    protected void setupAggregateStats() {
        Map<Integer, Map<String, Long>> byYear = new HashMap<>();
        Map<String, Map<String, Long>> byMonth = new HashMap<>();
        Map<String, Map<String, Long>> byDay = new HashMap<>();
        String viewsText = getText("resourceStatisticsController.views");
        DateTime lastYear = DateTime.now().minusDays(255).withDayOfMonth(1);
        DateTime lastWeek = DateTime.now().minusDays(7);
        Map<Date, Map<String, Object>> map = new HashMap<>();


        setupLabels(byMonth, byDay, lastYear, lastWeek);

        incrementViewStatistics(byYear, byMonth, byDay, viewsText, lastYear, lastWeek, map);
        incrementDownloadStatistics(byYear, byMonth, byDay, lastYear, lastWeek, map);

        try {
            setGraphJson(serializationService.convertToJson(map.values()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sortAndSetupForViewLayer(byYear, byMonth, byDay);

    }

    /**
     * Sort the labels alphabetically, and then pass them to the "unwrap" function so they're sorted alphabetically there too
     * @param byYear
     * @param byMonth
     * @param byDay
     */
    private void sortAndSetupForViewLayer(Map<Integer, Map<String, Long>> byYear, Map<String, Map<String, Long>> byMonth, Map<String, Map<String, Long>> byDay) {
        keys.addAll(getDownloadStats().keySet());

        yearLabels = new ArrayList<>(byYear.keySet());
        monthLabels = new ArrayList<>(byMonth.keySet());
        dayLabels = new ArrayList<>(byDay.keySet());
        Collections.sort(yearLabels);
        Collections.sort(monthLabels);
        Collections.sort(dayLabels);

        allByYear = unwrapByKey(byYear, yearLabels);
        allByMonth = unwrapByKey(byMonth, monthLabels);
        allByDay = unwrapByKey(byDay, dayLabels);
    }

    /**
     * build a map that has the view statistics ordered properly
     * @param byYear
     * @param byMonth
     * @param byDay
     * @param lastYear
     * @param lastWeek
     * @param map
     */
    private void incrementDownloadStatistics(Map<Integer, Map<String, Long>> byYear, Map<String, Map<String, Long>> byMonth, Map<String, Map<String, Long>> byDay,
            DateTime lastYear, DateTime lastWeek, Map<Date, Map<String, Object>> map) {
        for (Entry<String, List<AggregateDownloadStatistic>> entry : getDownloadStats().entrySet()) {
            for (AggregateDownloadStatistic stat : entry.getValue()) {
                Date aggregateDate = stat.getAggregateDate();
                if (!map.containsKey(aggregateDate)) {
                    map.put(aggregateDate, new HashMap<>());
                }
                if (StringUtils.isNotBlank(entry.getKey())) {
                    map.get(aggregateDate).put(entry.getKey(), stat.getCount());
                    map.get(aggregateDate).put("date", aggregateDate);
                }
                incrementKey(byYear, stat.getYear(), stat.getCount(), entry.getKey());
                if (lastYear.isBefore(aggregateDate.getTime())) {
                    incrementKey(byMonth, formatMonth(stat.getYear(), stat.getMonth()), stat.getCount(), entry.getKey());
                }
                if (lastWeek.isBefore(aggregateDate.getTime())) {
                    incrementKey(byDay, format.format(aggregateDate), stat.getCount(), entry.getKey());
                }
            }
        }
    }

    private void incrementViewStatistics(Map<Integer, Map<String, Long>> byYear, Map<String, Map<String, Long>> byMonth, Map<String, Map<String, Long>> byDay,
            String viewsText, DateTime lastYear, DateTime lastWeek, Map<Date, Map<String, Object>> map) {
        for (AggregateDayViewStatistic s : getUsageStatsForResources()) {
          incrementKey(byYear, s.getYear(), s.getTotal(), viewsText);
          for (DailyTotal t : s.getDailyTotals()) {
              DateTime date = DateTime.parse(t.getDate());
              Date date2 = date.toDate();
            if (!map.containsKey(date2)) {
                  map.put(date2, new HashMap<String, Object>());
              }
              Map<String, Object> submap = map.get(date2);
              submap.put(viewsText, t.getTotal());
              submap.put("date", date);

              if (lastYear.isBefore(date)) {
                  incrementKey(byMonth, formatMonth(s.getYear(), s.getMonth()), s.getTotal(), viewsText);
              }
              if (lastWeek.isBefore(date)) {
                  incrementKey(byDay, format.format(date), t.getTotal().longValue(), viewsText);
              }
              keys.add(viewsText);
          }

        }
    }

    private void setupLabels(Map<String, Map<String, Long>> byMonth, Map<String, Map<String, Long>> byDay, DateTime lastYear, DateTime lastWeek) {
        // ** setup with all months and days ** 
        DateTime dt = lastWeek;
        while (dt.isBeforeNow()) {
            String key = format.format(dt.toDate());
            byDay.put(key, new HashMap<>());
            dayLabels.add(key);
            dt = dt.plusDays(1);
        }

        dt = lastYear;
        while (dt.isBeforeNow()) {
            String key = formatMonth(dt.getYear(), dt.getMonthOfYear());
            byMonth.put(key, new HashMap<>());
            monthLabels.add(key);
            dt = dt.plusMonths(1);
        }
    }

    private String formatMonth(Integer year, Integer month) {
        return String.format("%s-%02d",year, month);
    }

    private <T> Map<String, List<Long>> unwrapByKey(Map<T, Map<String, Long>> by, List<T> order) {
        Map<String, List<Long>> toReturn = new HashMap<>();
        for (String key : keys) {
            List<Long> list = new ArrayList<>();
            for (T itm : order) {
                for (T year : by.keySet()) {
                    if (itm != year) {
                        continue;
                    }
                    Map<String, Long> map = by.get(year);
                    Long val = 0L;
                    if (map.containsKey(key)) {
                        val = map.get(key);
                    }
                    list.add(val);
                }

            }
            toReturn.put(key, list);
        }
        return toReturn;
    }

    private <T> void incrementKey(Map<T, Map<String, Long>> parent, T subKey, Long count, String k) {
        Map<String, Long> by = parent.get(subKey);
        if (by == null) {
            by = new HashMap<>();
        }
        Long v = by.get(k);
        if (by.get(k) == null) {
            v = 0L;
        }
        v += count;
        by.put(k, v);
        parent.put(subKey, by);
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);
        setUsageStatsForResources(StatisticsService.getUsageStatsForResource(getResource()));
        if (getResource() instanceof InformationResource) {
            for (InformationResourceFile file : ((InformationResource) getResource()).getInformationResourceFiles()) {
                getDownloadStats().put(file.getFilename(),
                        StatisticsService.getAggregateDownloadStatsForFile(DateGranularity.WEEK, new Date(0L), new Date(), 1L, file.getId()));
            }
        }
        setupAggregateStats();
    }

    public String getCategoryKeys() {
        return StringUtils.join(keys, ",");
    }

    public List<AggregateDayViewStatistic> getUsageStatsForResources() {
        return usageStatsForResources;
    }

    public void setUsageStatsForResources(List<AggregateDayViewStatistic> list) {
        this.usageStatsForResources = list;
    }

    public Map<String, List<AggregateDownloadStatistic>> getDownloadStats() {
        return downloadStats;
    }

    public void setDownloadStats(Map<String, List<AggregateDownloadStatistic>> downloadStats) {
        this.downloadStats = downloadStats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), GeneralPermissions.MODIFY_METADATA);
    }

    @Override
    public Resource getPersistable() {
        return resource;
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    @Override
    public void setPersistable(Resource persistable) {
        this.resource = persistable;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANY_RESOURCE;
    }

    public List<Integer> getYearLabels() {
        return yearLabels;
    }

    public void setYearLabels(List<Integer> yearLabels) {
        this.yearLabels = yearLabels;
    }

    public List<String> getMonthLabels() {
        return monthLabels;
    }

    public void setMonthLabels(List<String> monthLabels) {
        this.monthLabels = monthLabels;
    }

    public List<String> getDayLabels() {
        return dayLabels;
    }

    public void setDayLabels(List<String> dayLabels) {
        this.dayLabels = dayLabels;
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

}

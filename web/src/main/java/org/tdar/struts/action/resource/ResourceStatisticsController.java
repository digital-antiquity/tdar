package org.tdar.struts.action.resource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.AggregateViewStatistic;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource/usage")
public class ResourceStatisticsController extends AuthenticationAware.Base implements Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = 97924349900255693L;
    private List<AggregateViewStatistic> usageStatsForResources = new ArrayList<>();
    private Map<String, List<AggregateDownloadStatistic>> downloadStats = new HashMap<>();

    private Resource resource;
    private Long id;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private SerializationService serializationService;

    @Override
    @Action(value = "{id}", results = {
            @Result(name = SUCCESS, location = "../stats.ftl")
    })
    public String execute() throws TdarActionException {
        setUsageStatsForResources(resourceService.getUsageStatsForResources(DateGranularity.WEEK, new Date(0L), new Date(), 1L,
                Arrays.asList(getResource().getId())));
        if (getResource() instanceof InformationResource) {
            for (InformationResourceFile file : ((InformationResource) getResource()).getInformationResourceFiles()) {
                getDownloadStats().put(file.getFilename(), resourceService.getAggregateDownloadStatsForFile(DateGranularity.WEEK, new Date(0L), new Date(), 1L, file.getId()));
            }
        }
        setupAggregateStats();
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
        DateTime lastYear = DateTime.now().minusDays(255);
        DateTime lastWeek = DateTime.now().minusDays(7);
        Map<Date, Map<String, Object>> map = new HashMap<>();
        for (AggregateViewStatistic stat : getUsageStatsForResources()) {
            incrementKey(byYear, stat.getYear(), stat.getCount(), viewsText);
            
            Date date = stat.getAggregateDate();
            if (!map.containsKey(date)) {
                map.put(date, new HashMap<String,Object>());
            }
            Map<String,Object> submap = map.get(date);
            submap.put(viewsText, stat.getCount());
            submap.put("date", date);
            
            if (lastYear.isBefore(date.getTime())) {
                incrementKey(byMonth, stat.getYear() + "-" + stat.getMonth(), stat.getCount(), viewsText);
            }
            if (lastWeek.isBefore(date.getTime())) {
                incrementKey(byDay, format.format(date), stat.getCount(), viewsText);
            }
            keys.add(viewsText);
        }

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
                    incrementKey(byMonth, stat.getYear() + "-" + stat.getMonth(), stat.getCount(), entry.getKey());
                }
                if (lastWeek.isBefore(aggregateDate.getTime())) {
                    incrementKey(byDay, format.format(aggregateDate), stat.getCount(), entry.getKey());
                }
            }
        }
        try {
           setGraphJson(serializationService.convertToJson(map.values()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        keys.addAll(getDownloadStats().keySet());

        allByYear = unwrapByKey(byYear);
        yearLabels = new ArrayList<>(byYear.keySet());
        monthLabels = new ArrayList<>(byMonth.keySet());
        dayLabels = new ArrayList<>(byDay.keySet());
        allByMonth = unwrapByKey(byMonth);
        allByDay = unwrapByKey(byDay);

    }

    private <T> Map<String, List<Long>> unwrapByKey(Map<T, Map<String, Long>> by) {
        Map<String, List<Long>> toReturn = new HashMap<>();
        for (String key : keys) {
            List<Long> list = new ArrayList<>();
            for (T year : by.keySet()) {
                Map<String, Long> map = by.get(year);
                Long val = 0L;
                if (map.containsKey(key)) {
                    val = map.get(key);
                }
                list.add(val);
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
    }

    public String getCategoryKeys() {
        return StringUtils.join(keys,",");
    }

    public List<AggregateViewStatistic> getUsageStatsForResources() {
        return usageStatsForResources;
    }

    public void setUsageStatsForResources(List<AggregateViewStatistic> usageStatsForResources) {
        this.usageStatsForResources = usageStatsForResources;
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

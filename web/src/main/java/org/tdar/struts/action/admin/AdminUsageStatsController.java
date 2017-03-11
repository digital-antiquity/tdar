package org.tdar.struts.action.admin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.AggregateViewStatistic;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Administrative actions (that shouldn't be available for wide use).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/admin/usage")
@Component
@Scope("prototype")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@HttpsOnly
public class AdminUsageStatsController extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 6455397601247694602L;
    private String dateStart;
    private String dateEnd;
    private DateGranularity granularity = DateGranularity.DAY;
    private List<AggregateDownloadStatistic> downloadStats;
    private List<AggregateViewStatistic> usageStats;
    DateTime end = new DateTime();
    DateTime start = end.minusDays(7);
    private Long minCount = 5L;

    @Autowired
    private transient ResourceService resourceService;

    @Action("stats")
    @Override
    public String execute() {
        setUsageStats(resourceService.getAggregateUsageStats(granularity, start.toDate(), end.toDate(), minCount));
        return SUCCESS;
    }

    public void prepare() {
        if (StringUtils.isNotBlank(dateEnd)) {
            end = DateTime.parse(dateEnd);
        }
        if (StringUtils.isNotBlank(dateStart)) {
            start = DateTime.parse(dateStart);
        }
    }

    @Action("downloads")
    public String downloadStats() {
        setDownloadStats(resourceService.getAggregateDownloadStats(granularity, start.toDate(), end.toDate(), minCount));
        getLogger().debug("{}", downloadStats);
        return SUCCESS;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public DateGranularity getGranularity() {
        return granularity;
    }

    public void setGranularity(DateGranularity granularity) {
        this.granularity = granularity;
    }

    public List<AggregateViewStatistic> getUsageStats() {
        return usageStats;
    }

    public void setUsageStats(List<AggregateViewStatistic> usageStats) {
        this.usageStats = usageStats;
    }

    public List<AggregateDownloadStatistic> getDownloadStats() {
        return downloadStats;
    }

    public void setDownloadStats(List<AggregateDownloadStatistic> downloadStats) {
        this.downloadStats = downloadStats;
    }

    public Long getMinCount() {
        return minCount;
    }

    public void setMinCount(Long minCount) {
        this.minCount = minCount;
    }

}

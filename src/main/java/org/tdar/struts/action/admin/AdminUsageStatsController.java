package org.tdar.struts.action.admin;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.struts.RequiresTdarUserGroup;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.data.AggregateDownloadStatistic;
import org.tdar.struts.data.AggregateViewStatistic;
import org.tdar.struts.data.DateGranularity;

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
public class AdminUsageStatsController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 6455397601247694602L;
    private String dateStart;
    private String dateEnd;
    private DateGranularity granularity = DateGranularity.DAY;
    private List<AggregateDownloadStatistic> downloadStats;
    private List<AggregateViewStatistic> usageStats;

    @Actions({
            @Action("stats")
    })
    @Override
    public String execute() {
        DateTime end = new DateTime();
        DateTime start = end.minusDays(7);
        if (StringUtils.isNotBlank(dateEnd)) {
            DateTime.parse(dateEnd);
        }
        if (StringUtils.isNotBlank(dateStart)) {
            DateTime.parse(dateStart);
        }
        setUsageStats(getResourceService().getAggregateUsageStats(granularity, start.toDate(), end.toDate(), 1L));
        setDownloadStats(getResourceService().getAggregateDownloadStats(granularity, start.toDate(), end.toDate(), 0L));
        for (AggregateDownloadStatistic download : getDownloadStats()) {
            InformationResourceFile irf = getGenericService().find(InformationResourceFile.class, download.getInformationResourceFileId());
            download.setInformationResource(irf.getInformationResource());
        }
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

}

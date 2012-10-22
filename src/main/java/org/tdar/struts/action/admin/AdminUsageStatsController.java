package org.tdar.struts.action.admin;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.service.external.auth.TdarGroup;
import org.tdar.struts.RequiresTdarUserGroup;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.data.DateGranularity;
import org.tdar.utils.Pair;

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
    private DateGranularity granularity;
    

    @Actions({
            @Action("stats")
    })
    public String execute() {
        
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

}

package org.tdar.struts.action.admin;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.StatisticsService;
import org.tdar.core.service.processes.daily.RebuildHomepageCache;
import org.tdar.core.service.processes.daily.SitemapGeneratorProcess;
import org.tdar.core.service.processes.weekly.WeeklyStatisticsLoggingProcess;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.Pair;
import org.tdar.web.service.WebScheduledProcessService;

/**
 * $Id$
 * 
 * Administrative actions (that shouldn't be available for wide use).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/admin")
@Component
@Scope("prototype")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class AdminController extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = 4385039298623767568L;

    @Autowired
    private transient ScheduledProcessService scheduledProcessService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient StatisticsService StatisticsService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    WebScheduledProcessService webScheduledProcessService;

    private List<ResourceRevisionLog> resourceRevisionLogs;

    private List<Pair<CultureKeyword, Integer>> uncontrolledCultureKeywordStats;
    private List<Pair<CultureKeyword, Integer>> controlledCultureKeywordStats;
    private List<Pair<GeographicKeyword, Integer>> geographicKeywordStats;
    private List<Pair<InvestigationType, Integer>> investigationTypeStats;
    private List<Pair<MaterialKeyword, Integer>> materialKeywordStats;
    private List<Pair<OtherKeyword, Integer>> otherKeywordStats;
    private List<Pair<SiteNameKeyword, Integer>> siteNameKeywordStats;
    private List<Pair<SiteTypeKeyword, Integer>> controlledSiteTypeKeywordStats;
    private List<Pair<SiteTypeKeyword, Integer>> uncontrolledSiteTypeKeywordStats;
    private List<Pair<TemporalKeyword, Integer>> temporalKeywordStats;
    private List<Resource> recentlyUpdatedResources;
    private Map<ResourceType, List<BigInteger>> currentResourceStats;

    private Map<Date, Map<StatisticType, Long>> historicalResourceStats;
    private Map<Date, Map<StatisticType, Long>> historicalResourceStatsWithFiles;
    private Map<Date, Map<StatisticType, Long>> historicalCollectionStats;
    private Map<Date, Map<StatisticType, Long>> historicalRepositorySizes;

    private List<TdarUser> recentLogins;

    @Actions({
            @Action("contributors"),
            @Action("internal"),
            @Action("activity")
    })
    @Override
    public String execute() {
        setCurrentResourceStats(StatisticsService.getCurrentResourceStats());
        setHistoricalRepositorySizes(StatisticsService.getRepositorySizes());
        setRecentlyUpdatedResources(resourceService.findRecentlyUpdatedItemsInLastXDays(7));
        setRecentLogins(getEntityService().showRecentLogins());
        return SUCCESS;
    }

    @Action("resource")
    public String resourceInfo() {
        setHistoricalResourceStats(StatisticsService.getResourceStatistics());
        setHistoricalResourceStatsWithFiles(StatisticsService.getResourceStatisticsWithFiles());
        setHistoricalCollectionStats(StatisticsService.getCollectionStatistics());
        return SUCCESS;
    }

    @Action(value = "verifyFilestore", results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/admin")
    })
    @PostOnly
    @WriteableSession
    public String verifyFilestore() throws IOException {
        webScheduledProcessService.cronVerifyTdarFiles();
        getActionMessages().add("Running ... this may take a while");
        return SUCCESS;
    }

    @Action(value = "updateDois", results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/admin")
    })
    @PostOnly
    @WriteableSession
    public String updateDois() throws IOException {
        webScheduledProcessService.cronUpdateDois();
        getActionMessages().add("Running ... this may take a while");
        return SUCCESS;
    }

    @Action(value = "runWeekly", results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/admin")
    })
    @PostOnly
    @WriteableSession
    public String runWeekly() throws IOException {
        scheduledProcessService.queue(WeeklyStatisticsLoggingProcess.class);
        getActionMessages().add("Running ... this may take a while");
        return SUCCESS;
    }

    @Action(value = "rebuildCaches", results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/admin")
    })
    @PostOnly
    @WriteableSession
    public String rebuildCaches() {
        scheduledProcessService.queue(SitemapGeneratorProcess.class);
        scheduledProcessService.queue(RebuildHomepageCache.class);
        getActionMessages().add("Scheduled... check admin activity controller to test");
        return SUCCESS;
    }

    public List<ResourceRevisionLog> getResourceRevisionLogs() {
        if (resourceRevisionLogs == null) {
            resourceRevisionLogs = getGenericService().findAllSorted(ResourceRevisionLog.class, "timestamp desc");
        }
        return resourceRevisionLogs;
    }

    @Action("keyword-stats")
    public String viewKeywordStats() {
        return SUCCESS;
    }

    // @Action(value = "fix-pluralization", results = {
    // @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/admin/internal") })
    // @WriteableSession
    // @PostOnly
    // public String cleanupPluralization() {
    // authorityManagementService.cleanupKeywordDups(getAuthenticatedUser());
    // return SUCCESS;
    // }
    //
    // @Action(value = "fix-institutions", results = {
    // @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/admin/internal") })
    // @WriteableSession
    // public String cleanupInstitutionNames() {
    // authorityManagementService.cleanupInstitutionsWithSpaces(getAuthenticatedUser());
    // return SUCCESS;
    // }

    public List<Pair<CultureKeyword, Integer>> getUncontrolledCultureKeywordStats() {
        if (uncontrolledCultureKeywordStats == null) {
            uncontrolledCultureKeywordStats = genericKeywordService.getUncontrolledCultureKeywordStats();
        }
        return uncontrolledCultureKeywordStats;
    }

    public List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats() {
        if (controlledCultureKeywordStats == null) {
            controlledCultureKeywordStats = genericKeywordService.getControlledCultureKeywordStats();
        }
        return controlledCultureKeywordStats;
    }

    public List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats() {
        if (geographicKeywordStats == null) {
            geographicKeywordStats = genericKeywordService.getGeographicKeywordStats();
        }
        return geographicKeywordStats;
    }

    public List<Pair<InvestigationType, Integer>> getInvestigationTypeStats() {
        if (investigationTypeStats == null) {
            investigationTypeStats = genericKeywordService.getInvestigationTypeStats();
        }
        return investigationTypeStats;
    }

    public List<Pair<MaterialKeyword, Integer>> getMaterialKeywordStats() {
        if (materialKeywordStats == null) {
            materialKeywordStats = genericKeywordService.getMaterialKeywordStats();
        }
        return materialKeywordStats;
    }

    public List<Pair<OtherKeyword, Integer>> getOtherKeywordStats() {
        if (otherKeywordStats == null) {
            otherKeywordStats = genericKeywordService.getOtherKeywordStats();
        }
        return otherKeywordStats;
    }

    public List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats() {
        if (siteNameKeywordStats == null) {
            siteNameKeywordStats = genericKeywordService.getSiteNameKeywordStats();
        }
        return siteNameKeywordStats;
    }

    public List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats() {
        if (controlledSiteTypeKeywordStats == null) {
            controlledSiteTypeKeywordStats = genericKeywordService.getControlledSiteTypeKeywordStats();
        }
        return controlledSiteTypeKeywordStats;
    }

    public List<Pair<SiteTypeKeyword, Integer>> getUncontrolledSiteTypeKeywordStats() {
        if (uncontrolledSiteTypeKeywordStats == null) {
            uncontrolledSiteTypeKeywordStats = genericKeywordService.getUncontrolledSiteTypeKeywordStats();
        }
        return uncontrolledSiteTypeKeywordStats;
    }

    public List<Pair<TemporalKeyword, Integer>> getTemporalKeywordStats() {
        if (temporalKeywordStats == null) {
            temporalKeywordStats = genericKeywordService.getTemporalKeywordStats();
        }
        return temporalKeywordStats;
    }

    public List<Resource> getRecentlyUpdatedResources() {
        return recentlyUpdatedResources;
    }

    public void setRecentlyUpdatedResources(List<Resource> recentlyUpdatedResources) {
        this.recentlyUpdatedResources = recentlyUpdatedResources;
    }

    public Map<ResourceType, List<BigInteger>> getCurrentResourceStats() {
        return currentResourceStats;
    }

    public void setCurrentResourceStats(Map<ResourceType, List<BigInteger>> map) {
        this.currentResourceStats = map;
    }

    public Map<Date, Map<StatisticType, Long>> getHistoricalResourceStats() {
        return historicalResourceStats;
    }

    public void setHistoricalResourceStats(Map<Date, Map<StatisticType, Long>> map) {
        this.historicalResourceStats = map;
    }

    public Map<Date, Map<StatisticType, Long>> getHistoricalCollectionStats() {
        return historicalCollectionStats;
    }

    public void setHistoricalCollectionStats(Map<Date, Map<StatisticType, Long>> historicalCollectionStats) {
        this.historicalCollectionStats = historicalCollectionStats;
    }

    public Map<Date, Map<StatisticType, Long>> getHistoricalRepositorySizes() {
        return historicalRepositorySizes;
    }

    public void setHistoricalRepositorySizes(Map<Date, Map<StatisticType, Long>> historicalRepositorySizes) {
        this.historicalRepositorySizes = historicalRepositorySizes;
    }

    public Map<Date, Map<StatisticType, Long>> getHistoricalResourceStatsWithFiles() {
        return historicalResourceStatsWithFiles;
    }

    public void setHistoricalResourceStatsWithFiles(Map<Date, Map<StatisticType, Long>> historicalResourceStatsWithFiles) {
        this.historicalResourceStatsWithFiles = historicalResourceStatsWithFiles;
    }

    public EntityService getEntityService() {
        return entityService;
    }

    public void setEntityService(EntityService entityService) {
        this.entityService = entityService;
    }

    public List<TdarUser> getRecentLogins() {
        return recentLogins;
    }

    public void setRecentLogins(List<TdarUser> showRecentLogins) {
        this.recentLogins = showRecentLogins;
    }

}

package org.tdar.struts.action.admin;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
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
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.processes.RebuildHomepageCache;
import org.tdar.core.service.processes.SitemapGeneratorProcess;
import org.tdar.core.service.processes.UpgradeResourceCollectionPermissions;
import org.tdar.core.service.processes.WeeklyStatisticsLoggingProcess;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
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
@Namespace("/admin")
@Component
@Scope("prototype")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class AdminController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 4385039298623767568L;

    @Autowired
    private ScheduledProcessService scheduledProcessService;

    @Autowired
    private UpgradeResourceCollectionPermissions urcp;
    
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
    private Map<String, Float> extensionStats;
    private List<Person> recentUsers;
    private List<Pair<Long, Long>> userLoginStats;
    private List<Resource> recentlyUpdatedResources;
    private Map<ResourceType, List<BigInteger>> currentResourceStats;

    private Map<Date, Map<StatisticType, Long>> historicalResourceStats;
    private Map<Date, Map<StatisticType, Long>> historicalResourceStatsWithFiles;
    private Map<Date, Map<StatisticType, Long>> historicalCollectionStats;
    private Map<Date, Map<StatisticType, Long>> historicalUserStats;
    private List<InformationResourceFile> files;
    private Map<Date, Map<StatisticType, Long>> historicalRepositorySizes;
    private List<Person> recentLogins;

    private Map<String, List<Number>> fileAverageStats;
    private Map<String, Long> fileStats;

    private Map<String, List<Number>> fileUploadedAverageStats;

    @Actions({
            @Action("contributors"),
            @Action("internal"),
            @Action("activity")
    })
    @Override
    public String execute() {
        setCurrentResourceStats(getStatisticService().getCurrentResourceStats());
        setHistoricalRepositorySizes(getStatisticService().getRepositorySizes());
        setRecentlyUpdatedResources(getResourceService().findRecentlyUpdatedItemsInLastXDays(7));
        setRecentLogins(getEntityService().showRecentLogins());
        return SUCCESS;
    }

    @Action("resource")
    public String resourceInfo() {
        setHistoricalResourceStats(getStatisticService().getResourceStatistics());
        setHistoricalResourceStatsWithFiles(getStatisticService().getResourceStatisticsWithFiles());
        setHistoricalCollectionStats(getStatisticService().getCollectionStatistics());
        return SUCCESS;
    }

    @Action("file-info")
    public String fileInfo() {
        setFileAverageStats(getStatisticService().getFileAverageStats(Arrays.asList(VersionType.values())));
        setFileStats(getStatisticService().getFileStats(Arrays.asList(VersionType.values())));
        setFileUploadedAverageStats(getStatisticService().getFileAverageStats(Arrays.asList(VersionType.UPLOADED, VersionType.UPLOADED_ARCHIVAL, VersionType.UPLOADED_TEXT, VersionType.ARCHIVAL)));
        setExtensionStats(getInformationResourceFileService().getAdminFileExtensionStats());
        setFiles(getInformationResourceFileService().findFilesWithStatus(FileStatus.PROCESSING_ERROR, FileStatus.PROCESSING_WARNING));
        return SUCCESS;
    }

    @Action(value = "verifyFilestore", results = {
            @Result(name = SUCCESS, type = "redirect", location = "/admin")
    })
    public String verifyFilestore() throws IOException {
        scheduledProcessService.verifyTdarFiles();
        getActionMessages().add("Running ... this may take a while");
        return SUCCESS;
    }

    @Action(value = "updateDois", results = {
            @Result(name = SUCCESS, type = "redirect", location = "/admin")
    })
    public String updateDois() throws IOException {
        scheduledProcessService.updateDois();
        getActionMessages().add("Running ... this may take a while");
        return SUCCESS;
    }

    @Action(value = "runWeekly", results = {
            @Result(name = SUCCESS, type = "redirect", location = "/admin")
    })
    public String runWeekly() throws IOException {
        scheduledProcessService.queueTask(WeeklyStatisticsLoggingProcess.class);
        getActionMessages().add("Running ... this may take a while");
        return SUCCESS;
    }

    @Action(value = "rebuildCaches", results = {
            @Result(name = SUCCESS, type = "redirect", location = "/admin")
    })
    public String rebuildCaches() {
        scheduledProcessService.queueTask(SitemapGeneratorProcess.class);
        scheduledProcessService.queueTask(RebuildHomepageCache.class);
        getActionMessages().add("Scheduled... check admin activity controller to test");
        return SUCCESS;
    }

    @Action(value = "buildCollectionTree", results = {
            @Result(name = SUCCESS, type = "redirect", location = "/admin")
    })
    public String buildCollectionTree() {
        getLogger().debug("manually running 'build collection tree'");
        urcp.execute();
        return SUCCESS;
    }

    @Action("user")
    public String userInfo() {
        setHistoricalUserStats(getStatisticService().getUserStatistics());
        setRecentUsers(getEntityService().findAllRegisteredUsers(10));
        setUserLoginStats(getStatisticService().getUserLoginStats());
        return SUCCESS;
    }


    @Action("user-mailchimp")
    public String userMailchipInfo() {
        setRecentUsers(getEntityService().findAllRegisteredUsers());
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

    public List<Pair<CultureKeyword, Integer>> getUncontrolledCultureKeywordStats() {
        if (uncontrolledCultureKeywordStats == null) {
            uncontrolledCultureKeywordStats = getGenericKeywordService().getUncontrolledCultureKeywordStats();
        }
        return uncontrolledCultureKeywordStats;
    }

    public List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats() {
        if (controlledCultureKeywordStats == null) {
            controlledCultureKeywordStats = getGenericKeywordService().getControlledCultureKeywordStats();
        }
        return controlledCultureKeywordStats;
    }

    public List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats() {
        if (geographicKeywordStats == null) {
            geographicKeywordStats = getGenericKeywordService().getGeographicKeywordStats();
        }
        return geographicKeywordStats;
    }

    public List<Pair<InvestigationType, Integer>> getInvestigationTypeStats() {
        if (investigationTypeStats == null) {
            investigationTypeStats = getGenericKeywordService().getInvestigationTypeStats();
        }
        return investigationTypeStats;
    }

    public List<Pair<MaterialKeyword, Integer>> getMaterialKeywordStats() {
        if (materialKeywordStats == null) {
            materialKeywordStats = getGenericKeywordService().getMaterialKeywordStats();
        }
        return materialKeywordStats;
    }

    public List<Pair<OtherKeyword, Integer>> getOtherKeywordStats() {
        if (otherKeywordStats == null) {
            otherKeywordStats = getGenericKeywordService().getOtherKeywordStats();
        }
        return otherKeywordStats;
    }

    public List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats() {
        if (siteNameKeywordStats == null) {
            siteNameKeywordStats = getGenericKeywordService().getSiteNameKeywordStats();
        }
        return siteNameKeywordStats;
    }

    public List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats() {
        if (controlledSiteTypeKeywordStats == null) {
            controlledSiteTypeKeywordStats = getGenericKeywordService().getControlledSiteTypeKeywordStats();
        }
        return controlledSiteTypeKeywordStats;
    }

    public List<Pair<SiteTypeKeyword, Integer>> getUncontrolledSiteTypeKeywordStats() {
        if (uncontrolledSiteTypeKeywordStats == null) {
            uncontrolledSiteTypeKeywordStats = getGenericKeywordService().getUncontrolledSiteTypeKeywordStats();
        }
        return uncontrolledSiteTypeKeywordStats;
    }

    public List<Pair<TemporalKeyword, Integer>> getTemporalKeywordStats() {
        if (temporalKeywordStats == null) {
            temporalKeywordStats = getGenericKeywordService().getTemporalKeywordStats();
        }
        return temporalKeywordStats;
    }

    public Map<String, Float> getExtensionStats() {
        return extensionStats;
    }

    public void setExtensionStats(Map<String, Float> map) {
        this.extensionStats = map;
    }

    public List<Resource> getRecentlyUpdatedResources() {
        return recentlyUpdatedResources;
    }

    public void setRecentlyUpdatedResources(List<Resource> recentlyUpdatedResources) {
        this.recentlyUpdatedResources = recentlyUpdatedResources;
    }

    public List<Person> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<Person> recentUsers) {
        this.recentUsers = recentUsers;
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

    public Map<Date, Map<StatisticType, Long>> getHistoricalUserStats() {
        return historicalUserStats;
    }

    public void setHistoricalUserStats(Map<Date, Map<StatisticType, Long>> historicalUserStats) {
        this.historicalUserStats = historicalUserStats;
    }

    public Map<Date, Map<StatisticType, Long>> getHistoricalCollectionStats() {
        return historicalCollectionStats;
    }

    public void setHistoricalCollectionStats(Map<Date, Map<StatisticType, Long>> historicalCollectionStats) {
        this.historicalCollectionStats = historicalCollectionStats;
    }

    public List<Person> getRecentLogins() {
        return recentLogins;
    }

    public void setRecentLogins(List<Person> recentLogins) {
        this.recentLogins = recentLogins;
    }

    public Map<String, List<Number>> getFileAverageStats() {
        return fileAverageStats;
    }

    public void setFileAverageStats(Map<String, List<Number>> fileAverageStats) {
        this.fileAverageStats = fileAverageStats;
    }

    public Map<Date, Map<StatisticType, Long>> getHistoricalRepositorySizes() {
        return historicalRepositorySizes;
    }

    public void setHistoricalRepositorySizes(Map<Date, Map<StatisticType, Long>> historicalRepositorySizes) {
        this.historicalRepositorySizes = historicalRepositorySizes;
    }

    public Map<String, List<Number>> getFileUploadedAverageStats() {
        return fileUploadedAverageStats;
    }

    public void setFileUploadedAverageStats(Map<String, List<Number>> fileUploadedAverageStats) {
        this.fileUploadedAverageStats = fileUploadedAverageStats;
    }

    public Map<Date, Map<StatisticType, Long>> getHistoricalResourceStatsWithFiles() {
        return historicalResourceStatsWithFiles;
    }

    public void setHistoricalResourceStatsWithFiles(Map<Date, Map<StatisticType, Long>> historicalResourceStatsWithFiles) {
        this.historicalResourceStatsWithFiles = historicalResourceStatsWithFiles;
    }

    public List<Pair<Long, Long>> getUserLoginStats() {
        return userLoginStats;
    }

    public void setUserLoginStats(List<Pair<Long, Long>> userLoginStats) {
        this.userLoginStats = userLoginStats;
    }

    public List<InformationResourceFile> getFiles() {
        return files;
    }

    public void setFiles(List<InformationResourceFile> files) {
        this.files = files;
    }

    public Map<String, Long> getFileStats() {
        return fileStats;
    }

    public void setFileStats(Map<String, Long> map) {
        this.fileStats = map;
    }

}

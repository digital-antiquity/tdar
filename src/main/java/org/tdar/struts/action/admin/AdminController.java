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
import org.tdar.core.bean.entity.TdarUser;
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
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.AuthorityManagementService;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.StatisticService;
import org.tdar.core.service.processes.CreatorAnalysisProcess;
import org.tdar.core.service.processes.RebuildHomepageCache;
import org.tdar.core.service.processes.SitemapGeneratorProcess;
import org.tdar.core.service.processes.WeeklyStatisticsLoggingProcess;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.ResourceService;
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
    private transient ScheduledProcessService scheduledProcessService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    @Autowired
    private transient StatisticService statisticService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient AuthorityManagementService authorityManagementService;

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
    private List<TdarUser> recentUsers;
    private List<Pair<Long, Long>> userLoginStats;
    private List<Resource> recentlyUpdatedResources;
    private Map<ResourceType, List<BigInteger>> currentResourceStats;

    private Map<Date, Map<StatisticType, Long>> historicalResourceStats;
    private Map<Date, Map<StatisticType, Long>> historicalResourceStatsWithFiles;
    private Map<Date, Map<StatisticType, Long>> historicalCollectionStats;
    private Map<Date, Map<StatisticType, Long>> historicalUserStats;
    private List<InformationResourceFile> files;
    private Map<Date, Map<StatisticType, Long>> historicalRepositorySizes;
    private List<TdarUser> recentLogins;

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
        setCurrentResourceStats(statisticService.getCurrentResourceStats());
        setHistoricalRepositorySizes(statisticService.getRepositorySizes());
        setRecentlyUpdatedResources(resourceService.findRecentlyUpdatedItemsInLastXDays(7));
        setRecentLogins(entityService.showRecentLogins());
        return SUCCESS;
    }

    @Action("resource")
    public String resourceInfo() {
        setHistoricalResourceStats(statisticService.getResourceStatistics());
        setHistoricalResourceStatsWithFiles(statisticService.getResourceStatisticsWithFiles());
        setHistoricalCollectionStats(statisticService.getCollectionStatistics());
        return SUCCESS;
    }

    @Action("file-info")
    public String fileInfo() {
        setFileAverageStats(statisticService.getFileAverageStats(Arrays.asList(VersionType.values())));
        setFileStats(statisticService.getFileStats(Arrays.asList(VersionType.values())));
        setFileUploadedAverageStats(statisticService.getFileAverageStats(
                Arrays.asList(VersionType.UPLOADED, VersionType.UPLOADED_ARCHIVAL, VersionType.UPLOADED_TEXT, VersionType.ARCHIVAL)));
        setExtensionStats(informationResourceFileService.getAdminFileExtensionStats());
        setFiles(informationResourceFileService.findFilesWithStatus(FileStatus.PROCESSING_ERROR, FileStatus.PROCESSING_WARNING));
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

    @Action(value = "buildCreators", results = {
            @Result(name = SUCCESS, type = "redirect", location = "/admin")
    })
    public String buildCreators() {
        getLogger().debug("manually running 'build creator'");
        scheduledProcessService.queueTask(CreatorAnalysisProcess.class);
        return SUCCESS;
    }

    @Action("user")
    public String userInfo() {
        setHistoricalUserStats(statisticService.getUserStatistics());
        setRecentUsers(entityService.findAllRegisteredUsers(10));
        setUserLoginStats(statisticService.getUserLoginStats());
        return SUCCESS;
    }

    @Action("user-mailchimp")
    public String userMailchipInfo() {
        setRecentUsers(entityService.findAllRegisteredUsers());
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
            uncontrolledCultureKeywordStats = genericKeywordService.getUncontrolledCultureKeywordStats();
        }
        return uncontrolledCultureKeywordStats;
    }

    public List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats() {
        if (controlledCultureKeywordStats == null) {
            controlledCultureKeywordStats = genericKeywordService.getControlledCultureKeywordStats();
        }
        authorityManagementService.findPluralDups(CultureKeyword.class, getAuthenticatedUser(), true);
        return controlledCultureKeywordStats;
    }

    public List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats() {
        if (geographicKeywordStats == null) {
            geographicKeywordStats = genericKeywordService.getGeographicKeywordStats();
        }
        authorityManagementService.findPluralDups(GeographicKeyword.class, getAuthenticatedUser(), true);
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
        authorityManagementService.findPluralDups(OtherKeyword.class, getAuthenticatedUser(), true);
        return otherKeywordStats;
    }

    public List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats() {
        if (siteNameKeywordStats == null) {
            siteNameKeywordStats = genericKeywordService.getSiteNameKeywordStats();
        }
        authorityManagementService.findPluralDups(SiteNameKeyword.class, getAuthenticatedUser(), true);
        return siteNameKeywordStats;
    }

    public List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats() {
        if (controlledSiteTypeKeywordStats == null) {
            controlledSiteTypeKeywordStats = genericKeywordService.getControlledSiteTypeKeywordStats();
        }
        authorityManagementService.findPluralDups(SiteTypeKeyword.class, getAuthenticatedUser(), true);
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
        authorityManagementService.findPluralDups(TemporalKeyword.class, getAuthenticatedUser(), true);
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

    public List<TdarUser> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<TdarUser> recentUsers) {
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

    public List<TdarUser> getRecentLogins() {
        return recentLogins;
    }

    public void setRecentLogins(List<TdarUser> recentLogins) {
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

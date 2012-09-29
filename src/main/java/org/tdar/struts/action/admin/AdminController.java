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
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.util.Statistic.StatisticType;
import org.tdar.core.service.external.auth.TdarGroup;
import org.tdar.struts.RequiresTdarUserGroup;
import org.tdar.struts.action.AuthenticationAware;
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
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class AdminController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 4385039298623767568L;

    private List<Project> allProjects;

    private List<ContributorRequest> pendingContributorRequests;

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
    private List<Resource> recentlyUpdatedResources;
    private Map<ResourceType, List<BigInteger>> currentResourceStats;

    private Map<Date, Map<StatisticType, Long>> historicalResourceStats;
    private Map<Date, Map<StatisticType, Long>> historicalCollectionStats;
    private Map<Date, Map<StatisticType, Long>> historicalUserStats;
    private List<Person> recentLogins;

    private Map<String, List<Number>> fileAverageStats;

    @Actions({
            @Action("contributors"),
            @Action("internal"),
            @Action("activity")
    })
    public String execute() {
        setCurrentResourceStats(getStatisticService().getCurrentResourceStats());
        setRecentlyUpdatedResources(getDatasetService().findRecentlyUpdatedItemsInLastXDays(7));
        setRecentLogins(getEntityService().showRecentLogins());
        return SUCCESS;
    }

    @Action("resource")
    public String resourceInfo() {
        setFileAverageStats(getStatisticService().getFileAverageStats());
        setExtensionStats(getInformationResourceFileService().getAdminFileExtensionStats());
        setHistoricalResourceStats(getStatisticService().getResourceStatistics());
        setHistoricalCollectionStats(getStatisticService().getCollectionStatistics());
        return SUCCESS;
    }

    @Action("user")
    public String userInfo() {
        setHistoricalUserStats(getStatisticService().getUserStatistics());
        setRecentUsers(getEntityService().findAllRegisteredUsers(10));
        return SUCCESS;
    }

    public List<Project> getAllProjects() {
        if (allProjects == null) {
            allProjects = getProjectService().findAllSorted();
        }
        return allProjects;
    }

    public List<ContributorRequest> getPendingContributorRequests() {
        if (pendingContributorRequests == null) {
            pendingContributorRequests = getEntityService().findAllPendingContributorRequests();
        }
        return pendingContributorRequests;
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

}

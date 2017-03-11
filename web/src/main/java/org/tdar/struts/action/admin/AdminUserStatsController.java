package org.tdar.struts.action.admin;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.AgreementTypes;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.StatisticService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
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
@HttpsOnly
public class AdminUserStatsController extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = 757363810823550815L;

    @Autowired
    private transient StatisticService statisticService;

    @Autowired
    private transient EntityService entityService;

    private List<TdarUser> recentUsers;
    private List<Pair<Long, Long>> userLoginStats;

    private Map<Date, Map<StatisticType, Long>> historicalUserStats;
    private List<TdarUser> recentLogins;
    private Map<UserAffiliation, Long> affiliationCounts;
    private Map<UserAffiliation, Long> contributorAffiliationCounts;
    private Map<AgreementTypes, Long> agreementCounts;
    private Set<Long> contributorIds = new HashSet<>();

    @Action("user")
    public String userInfo() {
        try {
            setHistoricalUserStats(statisticService.getUserStatistics());
            setRecentUsers(entityService.findAllRegisteredUsers(10));
            setUserLoginStats(statisticService.getUserLoginStats());
            setAffiliationCounts(entityService.getAffiliationCounts());
            setContributorAffiliationCounts(entityService.getAffiliationCounts(true));
            setContributorIds(entityService.findAllContributorIds());
            setAgreementCounts(entityService.getAgreementCounts());
        } catch (Exception e) {
            getLogger().error("error in userInfo", e);
        }
        return SUCCESS;
    }

    public List<TdarUser> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<TdarUser> recentUsers) {
        this.recentUsers = recentUsers;
    }

    public Map<Date, Map<StatisticType, Long>> getHistoricalUserStats() {
        return historicalUserStats;
    }

    public void setHistoricalUserStats(Map<Date, Map<StatisticType, Long>> historicalUserStats) {
        this.historicalUserStats = historicalUserStats;
    }

    public List<TdarUser> getRecentLogins() {
        return recentLogins;
    }

    public void setRecentLogins(List<TdarUser> recentLogins) {
        this.recentLogins = recentLogins;
    }

    public List<Pair<Long, Long>> getUserLoginStats() {
        return userLoginStats;
    }

    public void setUserLoginStats(List<Pair<Long, Long>> userLoginStats) {
        this.userLoginStats = userLoginStats;
    }

    public Map<UserAffiliation, Long> getAffiliationCounts() {
        return affiliationCounts;
    }

    public void setAffiliationCounts(Map<UserAffiliation, Long> affiliationCounts) {
        this.affiliationCounts = affiliationCounts;
    }

    public Map<AgreementTypes, Long> getAgreementCounts() {
        return agreementCounts;
    }

    public void setAgreementCounts(Map<AgreementTypes, Long> agreementCounts) {
        this.agreementCounts = agreementCounts;
    }

    public Map<UserAffiliation, Long> getContributorAffiliationCounts() {
        return contributorAffiliationCounts;
    }

    public void setContributorAffiliationCounts(Map<UserAffiliation, Long> contributorAffiliationCounts) {
        this.contributorAffiliationCounts = contributorAffiliationCounts;
    }

    public Set<Long> getContributorIds() {
        return contributorIds;
    }

    public void setContributorIds(Set<Long> contributorIds) {
        this.contributorIds = contributorIds;
    }

}

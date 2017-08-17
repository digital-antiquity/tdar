package org.tdar.struts.action.resource;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.ResourceStatisticsObject;
import org.tdar.core.service.StatisticsService;
import org.tdar.core.service.external.AuthorizationService;
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

    private Resource resource;
    private Long id;

    @Autowired
    private StatisticsService statisticService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    @Action(value = "{id}", results = {
            @Result(name = SUCCESS, location = "../usage.ftl")
    })
    public String execute() throws TdarActionException {
        return SUCCESS;
    }

    private ResourceStatisticsObject stats;



    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);
        setStats(statisticService.getUsageStatsObjectForResource(this, getResource()));
    }

    public List<AggregateDayViewStatistic> getUsageStatsForResources() {
        return stats.getUsageStatsForResource();
    }


    public Map<String, List<AggregateDownloadStatistic>> getDownloadStats() {
        return stats.getDownloadStats();
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

    public TreeSet<Integer> getYearLabels() {
        return stats.getYearLabels();
    }

    public TreeSet<String> getMonthLabels() {
        return stats.getMonthLabels();
    }

    public TreeSet<String> getDayLabels() {
        return stats.getDayLabels();
    }

    public Map<String, List<Long>> getAllByYear() {
        return stats.getAllByYear();
    }

    public Map<String, List<Long>> getAllByMonth() {
        return stats.getAllByMonth();
    }

    public Map<String, List<Long>> getAllByDay() {
        return stats.getAllByDay();
    }

    public String getGraphJson() {
        return stats.getGraphJson();
    }

    public String getCategoryKeys() {
        return stats.getCategoryKeys();
    }
    public ResourceStatisticsObject getStats() {
        return stats;
    }

    public void setStats(ResourceStatisticsObject stats) {
        this.stats = stats;
    }

}

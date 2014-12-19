package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.AggregateViewStatistic;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.UsageStats;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class ResourceAdminController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = -2071449250711089300L;
    public static final String ADMIN = "admin";
    private List<ResourceRevisionLog> resourceLogEntries;

    private List<AggregateViewStatistic> usageStatsForResources = new ArrayList<>();
    private Map<String, List<AggregateDownloadStatistic>> downloadStats = new HashMap<>();
    private List<ResourceRevisionLog> logEntries;
    private Set<ResourceCollection> effectiveResourceCollections = new HashSet<>();

    private Resource resource;
    private Long id;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Autowired
    private SerializationService serializationService;

    @Action(value = ADMIN, results = {
            @Result(name = SUCCESS, location = "../resource/admin.ftl")
    })
    public String viewAdmin() throws TdarActionException {
        setResourceLogEntries(resourceService.getLogsForResource(getResource()));
        setUsageStatsForResources(resourceService.getUsageStatsForResources(DateGranularity.WEEK, new Date(0L), new Date(), 1L,
                Arrays.asList(getResource().getId())));
        if (getResource() instanceof InformationResource) {
            int i = 0;
            for (InformationResourceFile file : ((InformationResource) getResource()).getInformationResourceFiles()) {
                i++;
                getDownloadStats().put(String.format("%s. %s", i, file.getFilename()),
                        resourceService.getAggregateDownloadStatsForFile(DateGranularity.WEEK, new Date(0L), new Date(), 1L, file.getId()));
            }
        }
        getEffectiveResourceCollections().addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(getResource()));
        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {
        if (Persistable.Base.isNotNullOrTransient(getId())) {
            setResource(resourceService.find(getId()));
        } else {
            addActionError(getText("resourceAdminController.valid_resource_required"));
        }
    }

    public String getJsonStats() {
        String json = "null";
        // FIXME: what is the goal of this null check; shouldn't the UsageStats object handle this? Also, why bail if only one is null?
        if ((usageStatsForResources == null) || (downloadStats == null)) {
            return json;
        }

        try {
            json = serializationService.convertToJson(new UsageStats(usageStatsForResources, downloadStats));
        } catch (IOException e) {
            getLogger().error("failed to convert stats to json", e);
            json = String.format("{'error': '%s'}", StringEscapeUtils.escapeEcmaScript(e.getMessage()));
        }
        return json;
    }

    public List<ResourceRevisionLog> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<ResourceRevisionLog> logEntries) {
        this.logEntries = logEntries;
    }

    public List<ResourceRevisionLog> getResourceLogEntries() {
        return resourceLogEntries;
    }

    public void setResourceLogEntries(List<ResourceRevisionLog> resourceLogEntries) {
        this.resourceLogEntries = resourceLogEntries;
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

    public Set<ResourceCollection> getEffectiveResourceCollections() {
        return effectiveResourceCollections;
    }

    public void setEffectiveResourceCollections(Set<ResourceCollection> effectiveResourceCollections) {
        this.effectiveResourceCollections = effectiveResourceCollections;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        List<GeneralPermissions> permissions = new ArrayList<GeneralPermissions>();
        for (GeneralPermissions permission : GeneralPermissions.values()) {
            if ((permission.getContext() == null) || Resource.class.isAssignableFrom(permission.getContext())) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

}

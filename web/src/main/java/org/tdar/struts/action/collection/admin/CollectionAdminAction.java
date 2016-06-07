package org.tdar.struts.action.collection.admin;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin")
public class CollectionAdminAction extends AbstractCollectionAdminAction implements Preparable {

    private static final long serialVersionUID = -4060598709570483884L;
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private ResourceService resourceService;
    private Set<ResourceCollection> findAllChildCollections;
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        resourceCollectionService.buildCollectionTreeForController(getCollection(), getAuthenticatedUser(), CollectionType.SHARED);
        findAllChildCollections = getCollection().getTransientChildren();

        List<Long> collectionIds = PersistableUtils.extractIds(getCollection().getTransientChildren());
        collectionIds.add(getId());
        setUploadedResourceAccessStatistic(resourceService.getSpaceUsageForCollections(collectionIds, Arrays.asList(Status.ACTIVE, Status.DRAFT)));
        
    }

    @Override
    @Action(value = "{id}", results={
            @Result(name = SUCCESS, type = FREEMARKER, location = "index.ftl"),
    })
    public String execute() throws Exception {
        return SUCCESS;
    }

    public ResourceSpaceUsageStatistic getUploadedResourceAccessStatistic() {
        return uploadedResourceAccessStatistic;
    }

    public void setUploadedResourceAccessStatistic(ResourceSpaceUsageStatistic uploadedResourceAccessStatistic) {
        this.uploadedResourceAccessStatistic = uploadedResourceAccessStatistic;
    }
}

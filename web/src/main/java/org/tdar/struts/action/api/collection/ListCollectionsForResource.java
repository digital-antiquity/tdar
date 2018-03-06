package org.tdar.struts.action.api.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_USERS)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class ListCollectionsForResource extends AbstractJsonApiAction implements Preparable, Validateable {

    private static final long serialVersionUID = 1344077793459231299L;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    private Resource resource;
    private Permissions permission = Permissions.REMOVE_FROM_COLLECTION;
    private Long resourceId;

    @Action(value = "resourcecollections", results = { @Result(name = SUCCESS, type = TdarActionSupport.JSONRESULT) })
    @Transactional(readOnly = true)
    /**
     * For a given resource, returns a JSON result containing all of the associated managed and unmanaged collections that
     * the resource is part of.
     * 
     * @return
     * @throws Exception
     */
    public String listCollectionsForResource() throws Exception {
        TdarUser user = getAuthenticatedUser();
        ArrayList<ResourceCollection> managed = new ArrayList<ResourceCollection>();
        ArrayList<ResourceCollection> unmanaged = new ArrayList<ResourceCollection>();
        getLogger().debug("listCollectionsForResource: {}", resource);

        addToCollectionList(resource.getManagedResourceCollections(), user, managed);
        addToCollectionList(resource.getUnmanagedResourceCollections(), user, unmanaged);

        Map<String, ArrayList<ResourceCollection>> result = new HashMap<String, ArrayList<ResourceCollection>>();
        result.put("managed", managed);
        result.put("unmanaged", unmanaged);
        setJsonObject(result, JsonLookupFilter.class);
        return SUCCESS;
    }

    private void addToCollectionList(Collection<ResourceCollection> collections, TdarUser user, ArrayList<ResourceCollection> managed) {
        for (ResourceCollection resourceCollection : collections) {
            getLogger().debug("Checking collection {}", resourceCollection.getName());
            if (testCollection(user, resourceCollection)) {
                getLogger().debug("Adding collection {}", resourceCollection.getName());
                managed.add(resourceCollection);
            }
        }
    }

    private boolean testCollection(TdarUser user, ResourceCollection resourceCollection) {
        if (permission == Permissions.ADD_TO_COLLECTION) {
            return authorizationService.canAddToCollection(user, resourceCollection);
        }
        if (permission == Permissions.REMOVE_FROM_COLLECTION) {
            return authorizationService.canRemoveFromCollection(user, resourceCollection);
        }
        return authorizationService.canEdit(user, resourceCollection);
    }

    @Override
    public void validate() {
        super.validate();

        if (PersistableUtils.isNullOrTransient(resource) || !authorizationService.canView(getAuthenticatedUser(), resource)) {
            addActionError("addResourceToCollectionAction.no_edit_permission");
        }
    }

    @Override
    public void prepare() throws Exception {
        resource = getGenericService().find(Resource.class, resourceId);
    }

    public ResourceCollectionService getResourceCollectionService() {
        return resourceCollectionService;
    }

    public void setResourceCollectionService(ResourceCollectionService resourceCollectionService) {
        this.resourceCollectionService = resourceCollectionService;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Permissions getPermission() {
        return permission;
    }

    public void setPermission(Permissions permission) {
        this.permission = permission;
    }

}

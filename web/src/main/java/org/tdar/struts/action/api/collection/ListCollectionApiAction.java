package org.tdar.struts.action.api.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class ListCollectionApiAction extends AbstractJsonApiAction implements Preparable {

    private static final long serialVersionUID = 8858444149054259631L;

    private Long id;
    private ResourceCollection collection;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    public void prepare() {
        collection = getGenericService().find(ResourceCollection.class, getId());
    }

    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNotNullOrTransient(getId())) {
            if (collection == null) {
                addActionError("issue with collection");
            }

            if (!authorizationService.canViewCollection(collection, getAuthenticatedUser())) {
                addActionError("unauthorized");
            }
        }

    }

    @Action("tree")
    @Override
    public String execute() throws Exception {
        List<ResourceCollection> allChildCollections  = new ArrayList<>();
        if (collection != null) {
            allChildCollections = resourceCollectionService.getAllChildCollections(collection);
        } else {
            allChildCollections.addAll(resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser()));
        }
        getLogger().trace("accessible collections");
        List<Long> collectionIds = PersistableUtils.extractIds(allChildCollections);
        resourceCollectionService.reconcileCollectionTree(allChildCollections, getAuthenticatedUser(), collectionIds);
        getLogger().trace("reconcile tree2");
        Collections.sort(allChildCollections);
        List<Map<String,Object>> result = new ArrayList<>();
        processCollectionTree(allChildCollections, result);
        setJsonObject(result);
        return SUCCESS;
    }

    /**
     * return a basic JSON object tree with id/name/children
     * @param allChildCollections
     * @param result
     */
    private void processCollectionTree(Collection<ResourceCollection> allChildCollections, List<Map<String, Object>> root) {
        for (ResourceCollection col : allChildCollections) {
            Map<String, Object> result = new HashMap<>();
            root.add(result);
            result.put("id", col.getId());
            result.put("name", col.getName());
            List<Map<String, Object>> children = new ArrayList<>();
            processCollectionTree(col.getTransientChildren(), children);
            if (!CollectionUtils.isEmpty(children)) {
                result.put("children", children);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

package org.tdar.struts.action.api.lookup;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.bean.ResourceLookupObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

/**
 * $Id$
 * <p>
 * Handles ajax requests for people
 * 
 * @version $Rev$
 */
@Namespace("/api/lookup")
@ParentPackage("default")
@Component
@Scope("prototype")
public class ResourceLookupAction extends AbstractLookupController<Resource> {

    private static final long serialVersionUID = 1328807454084572934L;

    public static final String SELECTED_RESULTS = "selectedResults";
    public static final String MANAGED_RESULTS 	= "managedResourceResults";
    public static final String UNMANAGED_RESULTS = "unmanagedResourceResults";

    private Long projectId;
    private List<Long> collectionId;
    private String term;
    private String query;
    private Long sortCategoryId;
    private boolean includeCompleteRecord = false;
    private Permissions permission = Permissions.VIEW_ALL;

    private boolean parentCollectionsIncluded = true;

    private Long selectResourcesFromCollectionid;

    @Autowired
    private ResourceSearchService resourceSearchService;

    @Action(value = "resource", results = {
            @Result(name = SUCCESS, type = JSONRESULT)
    })
    public String lookupResource() throws SolrServerException, IOException {
        setLookupSource(LookupSource.RESOURCE);
        setMode("resourceLookup");
        // if we're doing a coding sheet lookup, make sure that we have access to all of the information here
        if (!isIncludeCompleteRecord() || (getAuthenticatedUser() == null)) {
            setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
            getLogger().info("using projection {}, {}", isIncludeCompleteRecord(), getAuthenticatedUser());
        }

        ResourceLookupObject look = new ResourceLookupObject();
        look.setTerm(term);
        look.setProjectId(projectId);
        look.setGeneralQuery(query);
        
        if (CollectionUtils.isNotEmpty(collectionId)) {
            for (Long id : collectionId) {
                //ResourceCollection rc = getGenericService().find(ResourceCollection.class, id);
                look.getCollectionIds().add(id);                    
            }
        }
        look.setCategoryId(sortCategoryId);
        look.setUseSubmitterContext(isUseSubmitterContext());
        look.setReservedSearchParameters(getReservedSearchParameters());
        look.setPermission(permission);
        cleanupResourceTypes();
        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }

        try {
            // includeComplete?
            resourceSearchService.lookupResource(getAuthenticatedUser(), look, this, this);
            getLogger().trace("resultObjects: {}", getResults());
        } catch (SearchException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        if (PersistableUtils.isNotNullOrTransient(getSelectResourcesFromCollectionid())) {
            ResourceCollection collectionContainer = getGenericService().find(ResourceCollection.class, getSelectResourcesFromCollectionid());
            if (collectionContainer != null) {
                Set<Long> resourceIds = new HashSet<Long>();
                Set<Long> managedResourceIds = new HashSet<Long>();
                Set<Long> unmanagedResourceIds = new HashSet<Long>();
                for (Indexable result_ : getResults()) {
                    Resource resource = (Resource) result_;
                    
                    //This endpoint is used to display results in a collection for the datatable.
                    //Should this include all resources including the ones in draft? 
                    if (resource != null && resource.isViewable()) {
                        if(resource.getManagedResourceCollections().contains(collectionContainer)){
                        	managedResourceIds.add(resource.getId()); 
                        	resourceIds.add(resource.getId());
                        }
                        
                        if(resource.getUnmanagedResourceCollections().contains(collectionContainer)){
                        	unmanagedResourceIds.add(resource.getId());
                        	 resourceIds.add(resource.getId());
                        }
                    }
                }
                getResult().put(SELECTED_RESULTS, resourceIds);
                getResult().put(MANAGED_RESULTS, managedResourceIds);
                getResult().put(UNMANAGED_RESULTS, unmanagedResourceIds);
            }
        }

        if (isIncludeCompleteRecord()) {
            jsonifyResult(null);
        } else {
            jsonifyResult(JsonLookupFilter.class);
        }
        return SUCCESS;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getSortCategoryId() {
        return sortCategoryId;
    }

    public void setSortCategoryId(Long sortCategoryId) {
        this.sortCategoryId = sortCategoryId;
    }

    public boolean isIncludeCompleteRecord() {
        return includeCompleteRecord;
    }

    public void setIncludeCompleteRecord(boolean includeCompleteRecord) {
        this.includeCompleteRecord = includeCompleteRecord;
    }

    public Permissions getPermission() {
        return permission;
    }

    public void setPermission(Permissions permission) {
        this.permission = permission;
    }

    public Long getSelectResourcesFromCollectionid() {
        return selectResourcesFromCollectionid;
    }

    public void setSelectResourcesFromCollectionid(Long selectResourcesFromCollectionid) {
        this.selectResourcesFromCollectionid = selectResourcesFromCollectionid;
    }

    public boolean isParentCollectionsIncluded() {
        return parentCollectionsIncluded;
    }

    public void setParentCollectionsIncluded(boolean parentCollectionsIncluded) {
        this.parentCollectionsIncluded = parentCollectionsIncluded;
    }

    @Override
    public DisplayOrientation getOrientation() {
        return DisplayOrientation.LIST;
    }

    public List<Long> getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(List<Long> collectionIds) {
        this.collectionId = collectionIds;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}

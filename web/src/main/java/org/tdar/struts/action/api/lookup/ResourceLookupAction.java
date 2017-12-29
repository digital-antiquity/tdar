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

    //Inputs
    private Long projectId;
    private List<Long> collectionId;
    private String term;
    private String query;
    private Long sortCategoryId;
    private boolean includeCompleteRecord = false;
    private Permissions permission = Permissions.VIEW_ALL;
    private boolean parentCollectionsIncluded = true;
    
    /**
     * This is used for the TdarDatatable to request resources that are in the collection.
     * This refers to the collection being edited. 
     */
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

        
        //Set the search parameters 
        ResourceLookupObject lookupParameters = new ResourceLookupObject();
        lookupParameters.setTerm(term);
        lookupParameters.setProjectId(projectId);
        lookupParameters.setGeneralQuery(query);
        lookupParameters.setCategoryId(sortCategoryId);
        lookupParameters.setUseSubmitterContext(isUseSubmitterContext());
        lookupParameters.setReservedSearchParameters(getReservedSearchParameters());
        lookupParameters.setPermission(permission);
        addCollectionIdsToLookup(lookupParameters);
        cleanupResourceTypes();
        if (getSortField() != SortOption.RELEVANCE) 
        	setSecondarySortField(SortOption.TITLE);

        
        //Run the search.
        try {
            resourceSearchService.lookupResource(getAuthenticatedUser(), lookupParameters, this, this);
            getLogger().trace("resultObjects: {}", getResults());
        } catch (SearchException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        //Add the search results.
        processSearchResults();
        
        
        //Return the results to the browser. 
        if (isIncludeCompleteRecord()) {
        	//If the full record is needed, then don't filter the JSON results. 
            jsonifyResult(null);
        } else {
        	//The filter will specify which fields to serialize. 
            jsonifyResult(JsonLookupFilter.class);
        }
        return SUCCESS;
    }

	private void processSearchResults() {
		//The results may contain lots of different resources, some that may be part of the collection and some that arent. 
		//If a selectResourcesFromCollectionid is specified, then it means that only the resources belonging to that
		//Collection should be returned in the page result.
		//
		//This means that the collection will be checked to make it exists, and if it does then the 
		//search results will be iterated to check if the collection is part of the resources managed/unmanaged collection.
		//if it is, then it will be added accordingly. 
		if (PersistableUtils.isNotNullOrTransient(getSelectResourcesFromCollectionid())) {
            ResourceCollection collection = getGenericService().find(ResourceCollection.class, getSelectResourcesFromCollectionid());
            if (collection != null) {
                Set<Long> resourceIds = new HashSet<Long>();
                Set<Long> managedResourceIds = new HashSet<Long>();
                Set<Long> unmanagedResourceIds = new HashSet<Long>();
                
                //Loop through the results, and add any them accordingly. 
                for (Indexable result_ : getResults()) {
                    Resource resource = (Resource) result_;
                    if (resource != null && resource.isViewable()) {
                        if(resource.getManagedResourceCollections().contains(collection)){
                        	managedResourceIds.add(resource.getId()); 
                        	resourceIds.add(resource.getId());
                        }
                        
                        if(resource.getUnmanagedResourceCollections().contains(collection)){
                        	unmanagedResourceIds.add(resource.getId());
                        	resourceIds.add(resource.getId());
                        }
                    }
                }
                
                //This is what gets serialized as JSON. 
                getResult().put(SELECTED_RESULTS, resourceIds);
                getResult().put(MANAGED_RESULTS, managedResourceIds);
                getResult().put(UNMANAGED_RESULTS, unmanagedResourceIds);
            }
        }
	}

	private void addCollectionIdsToLookup(ResourceLookupObject lookupParameters) {
		if (CollectionUtils.isNotEmpty(collectionId)) {
            for (Long id : collectionId) {
                //ResourceCollection rc = getGenericService().find(ResourceCollection.class, id);
                lookupParameters.getCollectionIds().add(id);                    
            }
        }
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

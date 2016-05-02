package org.tdar.struts.action.lookup;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
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
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.bean.ResourceLookupObject;
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
@Namespace("/lookup")
@ParentPackage("default")
@Component
@Scope("prototype")
public class ResourceLookupAction extends AbstractLookupController<Resource> {

    private static final long serialVersionUID = 1328807454084572934L;

    public static final String SELECTED_RESULTS = "selectedResults";

    private Long projectId;
    private List<Long> collectionId;
    private String term;
    private String query;
    private Long sortCategoryId;
    private boolean includeCompleteRecord = false;
    private GeneralPermissions permission = GeneralPermissions.VIEW_ALL;

    private boolean parentCollectionsIncluded = true;

    private Long selectResourcesFromCollectionid;

    @Autowired
    private ResourceSearchService resourceSearchService;

    @Action(value = "resource", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
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
        	look.getCollectionIds().addAll(collectionId);
        }
        look.setCategoryId(sortCategoryId);
        look.setReservedSearchParameters(getReservedSearchParameters());
        look.setPermission(permission);

        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }

        try {
            // includeComplete?
            resourceSearchService.lookupResource(getAuthenticatedUser(), look, this, this);
            getLogger().trace("jsonResults: {}", getResults());
        } catch (ParseException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        if (PersistableUtils.isNotNullOrTransient(getSelectResourcesFromCollectionid())) {
            ResourceCollection collectionContainer = getGenericService().find(ResourceCollection.class, getSelectResourcesFromCollectionid());
            if (collectionContainer != null) {
                Set<Long> resourceIds = new HashSet<Long>();
                for (Indexable result_ : getResults()) {
                    Resource resource = (Resource) result_;
                    if (resource != null && resource.isViewable() && resource.getResourceCollections().contains(collectionContainer)) {
                        resourceIds.add(resource.getId());
                    }
                }
                getResult().put(SELECTED_RESULTS, resourceIds);
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

    public GeneralPermissions getPermission() {
        return permission;
    }

    public void setPermission(GeneralPermissions permission) {
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

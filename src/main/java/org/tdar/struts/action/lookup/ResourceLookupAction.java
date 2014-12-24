package org.tdar.struts.action.lookup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.CategoryTermQueryPart;
import org.tdar.search.query.part.ProjectIdLookupQueryPart;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.data.FacetGroup;
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
    private Long collectionId;
    private String term;
    private String title;
    private Long sortCategoryId;
    private boolean includeCompleteRecord = false;
    private GeneralPermissions permission = GeneralPermissions.VIEW_ALL;

    private Long selectResourcesFromCollectionid;


    @Action(value = "resource", results = {
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
            })
    public String lookupResource() {
        QueryBuilder q = new ResourceQueryBuilder();
        setLookupSource(LookupSource.RESOURCE);
        setMode("resourceLookup");
        // if we're doing a coding sheet lookup, make sure that we have access to all of the information here
        if (!isIncludeCompleteRecord() || (getAuthenticatedUser() == null)) {
            getLogger().info("using projection {}, {}", isIncludeCompleteRecord(), getAuthenticatedUser());
            setProjectionModel(ProjectionModel.RESOURCE_PROXY);
        }

        q.append(new CategoryTermQueryPart(getTerm(), getSortCategoryId()));

        if (Persistable.Base.isNotNullOrTransient(getProjectId())) {
            q.append(new ProjectIdLookupQueryPart(getProjectId()));
        }

        appendIf(Persistable.Base.isNotNullOrTransient(getCollectionId()), q, QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, getCollectionId());

        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }

        q.append(processReservedTerms(this));
        try {
            handleSearch(q);
            getLogger().trace("jsonResults: {}", getResults());
        } catch (ParseException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        if (Persistable.Base.isNotNullOrTransient(getSelectResourcesFromCollectionid())) {
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
    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
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
    public Long getCollectionId() {
        return collectionId;
    }
    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
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
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

}

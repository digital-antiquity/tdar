package org.tdar.search.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.utils.PersistableUtils;

public class ResourceLookupObject implements Serializable {


    private static final long serialVersionUID = 5762691918094910192L;
    private String term;
    private String generalQuery;
    private Long projectId;
    private Boolean includeParent;
    private List<Long> collectionIds = new ArrayList<Long>();
    private List<Long> shareIds = new ArrayList<Long>();
    private Long categoryId;
    private GeneralPermissions permission;
    private boolean useSubmitterContext;
    private ReservedSearchParameters reservedSearchParameters;
    private SearchParameters searchParameters = new SearchParameters();
    
    public ResourceLookupObject() {
    }
    
    public ResourceLookupObject(String term, Long projectId, Boolean includeParent, Long collectionId, Long categoryId, GeneralPermissions permission, ReservedSearchParameters reservedSearchParameters) {
        this.term = term;
        this.projectId = projectId;
        this.includeParent = includeParent;
        if (PersistableUtils.isNotNullOrTransient(collectionId)) {
        	collectionIds.add(collectionId);
        }
        this.categoryId = categoryId;
        this.permission = permission;
        this.setReservedSearchParameters(reservedSearchParameters);
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

    public Boolean getIncludeParent() {
        return includeParent;
    }

    public void setIncludeParent(Boolean includeParent) {
        this.includeParent = includeParent;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public GeneralPermissions getPermission() {
        return permission;
    }

    public void setPermission(GeneralPermissions permission) {
        this.permission = permission;
    }

    public ReservedSearchParameters getReservedSearchParameters() {
        if (reservedSearchParameters == null) {
            reservedSearchParameters = new ReservedSearchParameters();
        }
        return reservedSearchParameters;
    }

    public void setReservedSearchParameters(ReservedSearchParameters reservedSearchParameters) {
        this.reservedSearchParameters = reservedSearchParameters;
    }

    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

	public List<Long> getCollectionIds() {
		return collectionIds;
	}

	public void setCollectionIds(List<Long> collectionIds) {
		this.collectionIds = collectionIds;
	}

	public String getGeneralQuery() {
		return generalQuery;
	}

	public void setGeneralQuery(String generalQuery) {
		this.generalQuery = generalQuery;
	}

    public boolean isUseSubmitterContext() {
        return useSubmitterContext;
    }

    public void setUseSubmitterContext(boolean useSubmitterContext) {
        this.useSubmitterContext = useSubmitterContext;
    }

    public List<Long> getShareIds() {
        return shareIds;
    }

    public void setShareIds(List<Long> shareIds) {
        this.shareIds = shareIds;
    }
    
}

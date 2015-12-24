package org.tdar.search.bean;

import java.io.Serializable;

import org.tdar.core.bean.entity.permissions.GeneralPermissions;

public class ResourceLookupObject implements Serializable {


    private static final long serialVersionUID = 5762691918094910192L;
    private String term;
    private Long projectId;
    private Boolean includeParent;
    private Long collectionId;
    private Long categoryId;
    private GeneralPermissions permission;
    private ReservedSearchParameters reservedSearchParameters;
    private SearchParameters searchParameters = new SearchParameters();
    
    public ResourceLookupObject() {
    }
    
    public ResourceLookupObject(String term, Long projectId, Boolean includeParent, Long collectionId, Long categoryId, GeneralPermissions permission, ReservedSearchParameters reservedSearchParameters) {
        this.term = term;
        this.projectId = projectId;
        this.includeParent = includeParent;
        this.collectionId = collectionId;
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

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
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
    
}

package org.tdar.struts.action.workspace.ajax;

/**
 * Simple pojo for storing Integration search parameters
 */
public class IntegrationSearchFilter {

    private Long projectId = -1L;
    private Long collectionId = -1L;
    private Long categoryId = -1L;
    private Boolean isBookmarked = true;
    private Boolean isAbleToIntegrate = false;

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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getIsBookmarked() {
        return isBookmarked;
    }

    public void setIsBookmarked(Boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }

    public Boolean getIsAbleToIntegrate() {
        return isAbleToIntegrate;
    }

    public void setIsAbleToIntegrate(Boolean isAbleToIntegrate) {
        this.isAbleToIntegrate = isAbleToIntegrate;
    }
}

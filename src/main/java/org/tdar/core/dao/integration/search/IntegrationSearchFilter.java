package org.tdar.core.dao.integration.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tdar.core.bean.entity.TdarUser;

/**
 * Simple pojo for storing Integration search parameters
 */
public class IntegrationSearchFilter implements Serializable {

    private static final long serialVersionUID = 260423502820324444L;

    private TdarUser authorizedUser;

    private Long projectId = -1L;
    private Long collectionId = -1L;
    private Long categoryVariableId = -1L;
    private List<Long> ontologyIds = new ArrayList<>();
    private List<Long> dataTableIds = new ArrayList<>();
    private boolean bookmarked = false;
    private boolean ableToIntegrate = false;
    private String title;

    private int maxResults;
    private int firstResult;

    public IntegrationSearchFilter(int maxResults, int firstResult) {
        this.maxResults = maxResults;
        this.firstResult = firstResult;
    }

    public Boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(Boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public TdarUser getAuthorizedUser() {
        return authorizedUser;
    }

    public void setAuthorizedUser(TdarUser authorizedUser) {
        this.authorizedUser = authorizedUser;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("project", projectId)
                .append("collection", collectionId)
                .append("bookmarked", bookmarked)
                .append("isAbleToIntegrate", ableToIntegrate)
                .append("ontologies", ontologyIds)
                .append("datatables", dataTableIds)
                .append("authuser", authorizedUser).build();
    }

    public boolean isAbleToIntegrate() {
        return ableToIntegrate;
    }

    public void setAbleToIntegrate(boolean ableToIntegrate) {
        this.ableToIntegrate = ableToIntegrate;
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

    public boolean hasOntologies() {
        return false;
    }

    public List<Long> getOntologyIds() {
        return ontologyIds;
    }

    public List<Long> getPaddedOntologyIds() {
        if (ontologyIds.isEmpty()) {
            return paddedIdList();
        }
        return ontologyIds;
    }

    public void setOntologyIds(List<Long> ontologyIds) {
        this.ontologyIds = ontologyIds;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    public List<Long> getDataTableIds() {
        return dataTableIds;
    }

    public List<Long> getPaddedDataTableIds() {
        if (dataTableIds.isEmpty()) {
            return paddedIdList();
        }
        return dataTableIds;
    }

    private List<Long> paddedIdList() {
        List<Long> padding = new ArrayList<>();
        padding.add(null);
        return padding;
    }

    public void setDataTableIds(List<Long> dataTableIds) {
        this.dataTableIds = dataTableIds;
    }

    public Long getCategoryVariableId() {
        return categoryVariableId;
    }

    public void setCategoryVariableId(Long categoryVariableId) {
        this.categoryVariableId = categoryVariableId;
    }

    public boolean isHasOntologies() {
        return !getOntologyIds().isEmpty();
    }

    public long getSubmitterId() {
        return getAuthorizedUser().getId();
    }

    public boolean isHasDatasets() {
        return !getDataTableIds().isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // fixme: better way?
    public String getTitleLookup() {
        if (StringUtils.isBlank(title)) {
            return "%";
        }
        return "%" + title.toLowerCase() + "%";
    }
}

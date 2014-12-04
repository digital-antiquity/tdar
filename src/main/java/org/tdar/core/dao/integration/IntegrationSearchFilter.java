package org.tdar.core.dao.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.utils.PaginationHelper;

/**
 * Simple pojo for storing Integration search parameters
 */
public class IntegrationSearchFilter implements Serializable {
    private static final long serialVersionUID = -6417523572672914499L;
    private TdarUser authorizedUser;

    private Long projectId = -1L;
    private Long collectionId = -1L;
    private Long categoryVariableId = -1L;
    private List<Long> ontologyIds = new ArrayList<>();
    private List<Long> dataTableIds = new ArrayList<>();
    private boolean bookmarked = false;
    private boolean ableToIntegrate = false;

    //TODO: consider paginationHelper object instead
    private int maxResults = 100;
    private int firstResult = 0;


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

    public void setDataTableIds(List<Long> dataTableIds) {
        this.dataTableIds = dataTableIds;
    }

    public Long getCategoryVariableId() {
        return categoryVariableId;
    }

    public void setCategoryVariableId(Long categoryVariableId) {
        this.categoryVariableId = categoryVariableId;
    }
}

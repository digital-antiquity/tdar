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
public class AbstractIntegrationSearchFilter implements Serializable {
    private static final long serialVersionUID = -6417523572672914499L;
    private TdarUser authorizedUser;

    private Long projectId = -1L;
    private Long collectionId = -1L;
    private boolean bookmarked = false;
    private String title;

    //if true, include the total result count in the search results (increases execution time)
    private boolean includeResultCount = true;

    /*
     * + "(:hasOntologies=false or ont.id in :paddedOntologyIds ) and "
     * + "(:bookmarked=false or ds.id in (select b.resource.id from BookmarkedResource b where b.person.id=:submitterId) )"),
     */
    private int maxResults;
    private int firstResult;

    public AbstractIntegrationSearchFilter(int maxResults, int firstResult) {
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
                .append("authuser", authorizedUser).build();
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

    public List<Long> paddedIdList() {
        List<Long> padding = new ArrayList<>();
        padding.add(null);
        return padding;
    }

    public long getSubmitterId() {
        return getAuthorizedUser().getId();
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

    public boolean isIncludeResultCount() {
        return includeResultCount;
    }

    public void setIncludeResultCount(boolean includeResultCount) {
        this.includeResultCount = includeResultCount;
    }
}

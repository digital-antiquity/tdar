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
    public static final int DEFAULT_RECORDS_PER_PAGE = 500;
    private TdarUser authorizedUser;

    private Long projectId = -1L;
    private Long collectionId = -1L;
    private boolean bookmarked = false;
    private String title;

    /*
     * + "(:hasOntologies=false or ont.id in :paddedOntologyIds ) and "
     * + "(:bookmarked=false or ds.id in (select b.resource.id from BookmarkedResource b where b.person.id=:submitterId) )"),
     */
    private int recordsPerPage = DEFAULT_RECORDS_PER_PAGE;
    private int startRecord = 0;

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

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public int getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    public List<Long> paddedIdList() {
        List<Long> padding = new ArrayList<>();
        padding.add(null);
        return padding;
    }

    @SuppressWarnings("unused")
    @Deprecated
    // "ignore, required for hibernate"
    private void setSubmitterId(Long id) {

    }

    public Long getSubmitterId() {
        return getAuthorizedUser().getId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @SuppressWarnings("unused")
    @Deprecated
    // "ignore, required for hibernate"
    private void setTitleLookup(String title) {

    }

    // fixme: better way?
    public String getTitleLookup() {
        if (StringUtils.isBlank(title)) {
            return "%";
        }
        return "%" + title.toLowerCase() + "%";
    }
}

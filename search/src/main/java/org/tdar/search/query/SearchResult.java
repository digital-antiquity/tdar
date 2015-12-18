package org.tdar.search.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.TdarUser;

public class SearchResult implements SearchResultHandler<Indexable>, Serializable {

    private static final long serialVersionUID = 8370261049894410532L;
    private SortOption sortField;
    private SortOption secondarySortField;
    private int totalRecords = 0;
    private int startRecord = 0;
    private int recordsPerPage = getDefaultRecordsPerPage();
    private boolean debug;
    private List<Indexable> results;
    private String mode;
    private boolean reindexing;
    private TdarUser authenticatedUser;
    private String searchTitle;
    private String searchDescription;
    private ProjectionModel projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
    private Map<String, Class<? extends Persistable>> facetMap = new HashMap<>();

    public void facetBy(String facetField, Class<? extends Persistable> facetClass) {
        facetMap.put(facetField, facetClass);
    }

    public Map<String, Class<? extends Persistable>> getFacetMap() {
        return facetMap;
    }

    @Override
    public SortOption getSortField() {
        return sortField;
    }

    @Override
    public void setSortField(SortOption sortField) {
        this.sortField = sortField;
    }

    @Override
    public SortOption getSecondarySortField() {
        return secondarySortField;
    }

    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

    @Override
    public int getTotalRecords() {
        return totalRecords;
    }

    @Override
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    @Override
    public int getStartRecord() {
        return startRecord;
    }

    @Override
    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    @Override
    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public List<Indexable> getResults() {
        return results;
    }

    @Override
    public void setResults(List<Indexable> results) {
        this.results = results;
    }

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public boolean isReindexing() {
        return reindexing;
    }

    public void setReindexing(boolean reindexing) {
        this.reindexing = reindexing;
    }

    @Override
    public TdarUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(TdarUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    @Override
    public String getSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

    @Override
    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
    }

    @Override
    public int getPrevPageStartRecord() {
        return startRecord - recordsPerPage;
    }

    @Override
    public ProjectionModel getProjectionModel() {
        return projectionModel;
    }

    public void setProjectionModel(ProjectionModel projectionModel) {
        this.projectionModel = projectionModel;
    }

    @Override
    public int getDefaultRecordsPerPage() {
        return DEFAULT_RESULT_SIZE;
    }

    public boolean hasMore() {
        return (getTotalRecords() > getStartRecord() + getRecordsPerPage());
    }

}

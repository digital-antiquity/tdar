package org.tdar.search.query;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.struts.data.FacetGroup;

public class SearchResult implements SearchResultHandler<Indexable>,Serializable {

    private static final long serialVersionUID = 8370261049894410532L;
    private SortOption sortField;
    private SortOption secondarySortField;
    private int resultSize;
    private int totalRecords;
    private int startRecord;
    private int recordsPerPage;
    private boolean debug;
    private boolean showAll;
    private List<Indexable> results;
    private String mode;
    private boolean reindexing;
    private Person authenticatedUser;
    private String searchTitle;
    private String searchDescription;

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

    public int getResultSize() {
        return resultSize;
    }

    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
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
    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
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
    public Person getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(Person authenticatedUser) {
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getPrevPageStartRecord() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<String> getProjections() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FacetGroup<? extends Facetable>> getFacetFields() {
        // TODO Auto-generated method stub
        return null;
    }

}

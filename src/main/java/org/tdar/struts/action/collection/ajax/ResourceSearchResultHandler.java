package org.tdar.struts.action.collection.ajax;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.struts.data.FacetGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jimdevos on 3/23/15.
 */
public class ResourceSearchResultHandler implements SearchResultHandler<Resource> {

    public static final int DEFAULT_START_RECORD = 0;
    public static final int DEFAULT_RECORDS_PER_PAGE = 25;

    private SortOption sortOption1 = SortOption.ID;
    private SortOption sortOption2 = SortOption.TITLE;
    private int totalRecords = 0;
    private List<Resource> results = new ArrayList<>();
    private String mode = "CollectionBrowse";
    private TdarUser authenticatedUser = null;
    private int startRecord = DEFAULT_START_RECORD;
    private int recordsPerPage = DEFAULT_RECORDS_PER_PAGE;

    public ResourceSearchResultHandler(TdarUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public SortOption getSecondarySortField() {
        return sortOption2;
    }

    @Override
    public ProjectionModel getProjectionModel() {
        return ProjectionModel.RESOURCE_PROXY;
    }

    @Override
    public void setTotalRecords(int resultSize) {
        totalRecords = resultSize;
    }

    @Override
    public int getTotalRecords() {
        return totalRecords;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public boolean isShowAll() {
        return true;
    }

    @Override
    public void setResults(List<Resource> toReturn) {
        this.results = toReturn;
    }

    @Override
    public List<Resource> getResults() {
        return results;
    }

    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public boolean isReindexing() {
        return false;
    }

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public TdarUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    @Override
    public String getSearchTitle() {
        return "";
    }

    @Override
    public String getSearchDescription() {
        return "";
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
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return Collections.emptyList();
    }

    @Override
    public int getDefaultRecordsPerPage() {
        return DEFAULT_RECORDS_PER_PAGE;
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    @Override
    public SortOption getSortField() {
        return null;
    }

    @Override
    public void setSortField(SortOption sortField) {
        sortOption1 = sortField;
    }

    @Override
    public int getStartRecord() {
        return 0;
    }

    @Override
    public void setStartRecord(int startRecord) {

    }

    @Override
    public int getRecordsPerPage() {
        return 0;
    }
}

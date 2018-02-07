package org.tdar.search.query;

import org.tdar.core.bean.SortOption;

public interface SimpleSearchResultHandler {

    void setRecordsPerPage(int recordsPerPage);

    SortOption getSortField();

    void setSortField(SortOption sortField);

    /**
     * Gets the index of the first record which the SearchService should return in this page of results.
     * 
     * @return the index of the first record which the SearchService should return
     */

    int getStartRecord();

    void setStartRecord(int startRecord);

    /**
     * Retrieve the number of records which the SearchService should return in this page of results.
     * 
     * @return the number of records to return.
     */
    int getRecordsPerPage();

    
}

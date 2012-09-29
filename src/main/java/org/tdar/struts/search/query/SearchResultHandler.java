package org.tdar.struts.search.query;

import java.util.List;

import org.hibernate.search.FullTextQuery;
import org.tdar.core.bean.Indexable;
import org.tdar.search.query.SortOption;

/* further abstracting some of the functions of the search result handler 
 * so it can be pushed into the service layer
 * 
 */
public interface SearchResultHandler {

    public static final int DEFAULT_START = 0;
    public static final int DEFAULT_RESULT_SIZE = 25;

    SortOption getSortField();

    SortOption getSecondarySortField();

    void setTotalRecords(int resultSize);

    int getTotalRecords();

    int getStartRecord();

    void setStartRecord(int startRecord);

    int getRecordsPerPage();

    void setRecordsPerPage(int recordsPerPage);

    void addFacets(FullTextQuery ftq);

    boolean isDebug();

    boolean isShowAll();

    void setResults(List<Indexable> toReturn);

    List<Indexable> getResults();
    
    void setMode(String mode);
    
    String getMode();

}

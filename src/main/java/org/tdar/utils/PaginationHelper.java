package org.tdar.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * $Id$
 * 
 * zero-based pagination helper 
 * 
 * @author Jim
 * @version $Rev$
 */
public class PaginationHelper {
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    private int totalNumberOfItems;
    private int itemsPerPage;
    // sliding window size (number of page elements to include)
    private int windowSize;
    private int currentPage;
    
    //stuff we derive
    private int pageCount;

    public PaginationHelper(int itemCount, int itemsPerPage, int maxVisiblePages, int currentPage) {
        this.totalNumberOfItems = itemCount;
        this.itemsPerPage = itemsPerPage;
        this.windowSize = maxVisiblePages;
        this.currentPage = currentPage;
        
        //derive the rest
        double dpc = (float)(itemCount) / itemsPerPage;
        this.pageCount = (int)Math.ceil(dpc);
    }
    
    public boolean hasPrevious() {
        return currentPage > 0;
    }
    
    public boolean hasNext() {
        return currentPage < pageCount;
    }
    
    public int getWindowInterval() {
        return windowSize / 2;
    }
    
    public int getMinimumPageNumber() {
        
        if(pageCount <= windowSize) return 0;
        
        //index of current page within window (same as Math.floor(windowSize/2f))
        int cpi = windowSize / 2;
        
        //distance of cpi to window end (not always windowSize/2)
        int distw = windowSize - cpi;
        
        //distance of currentpage to last page
        int distl = pageCount - currentPage;
        
        
        if (distl < distw ) {
            cpi = windowSize - distl;
        } else if (currentPage < cpi) {
            cpi = currentPage;
        }
        return currentPage - cpi;
    }
    
    public boolean isCurrentPageNearBeginning() {
        return currentPage < getWindowInterval();
    }
    
    public boolean isCurrentPageNearEnd() {
        return (pageCount - currentPage < getWindowInterval());        
    }
    
    public int getMaximumPageNumber() {
        return Math.min(pageCount - 1, getMinimumPageNumber() + windowSize - 1);
    }

    public int getPageCount() {
        return pageCount;
    }

    //recommended  width (in characters) to alot for each page number
    public int getSuggestedPadding() {
        return Integer.toString(pageCount).length();
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }
    
    public int getPageNumber(int windowIndex) {
        return getMinimumPageNumber() + windowIndex;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < getWindowSize(); i++) {
            int pad = getSuggestedPadding();
            String fmtsel = "[%" + pad + "s]";
            String fmtreg = " %" + pad + "s ";
            int pageNumber = getPageNumber(i);
            String fmt = getCurrentPage() == pageNumber ? fmtsel : fmtreg;
            sb.append(String.format(fmt, pageNumber));
        }
        String fmt = "{<%s> ic:%4s ipp:%3s pc:%3s cp:%3s min:%3s max:%3s}";
        String str = String.format(fmt, sb.toString(), totalNumberOfItems, itemsPerPage, pageCount, currentPage, getMinimumPageNumber(), getMaximumPageNumber());
        return str;
    }
    
    public static PaginationHelper withStartRecord(int totalItems, int itemsPerPage, int maxVisiblePages, int startRecord) {
        //lock the startRecord to the first record in a page 
        int currentPage = startRecord / itemsPerPage;
        return new PaginationHelper(totalItems, itemsPerPage, maxVisiblePages, currentPage);
    }

    /**
     * @return the totalNumberOfItems
     */
    public int getTotalNumberOfItems() {
        return totalNumberOfItems;
    }

    /**
     * @return the itemsPerPage
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }

}

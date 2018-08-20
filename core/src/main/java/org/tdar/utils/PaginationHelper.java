package org.tdar.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Indexable;
import org.tdar.search.query.SearchResultHandler;

/**
 * $Id$
 * 
 * zero-based pagination helper
 * 
 * @author Jim
 * @version $Rev$
 */
public class PaginationHelper {
    public static final int DEFAULT_ITEMS_PER_WINDOW = 15;

    @SuppressWarnings("unused")
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private int totalNumberOfItems;
    private int itemsPerPage;
    // sliding window size (number of page elements to include)
    private int windowSize;
    private int currentPage;

    // stuff we derive
    private int pageCount;

    // min pageNumber in the window
    private int minimumPageNumber;

    // max page in the window
    private int maximumPageNumber;

    // index of current page within window
    private int currentPageIndex;

    private int padding;

    public static PaginationHelper withStartRecord(int totalItems, int itemsPerPage, int maxVisiblePages, int startRecord) {
        // lock the startRecord to the first record in a page
        int currentPage = startRecord / itemsPerPage;
        return new PaginationHelper(totalItems, itemsPerPage, maxVisiblePages, currentPage);
    }

    public static <I extends Indexable> PaginationHelper withSearchResults(SearchResultHandler<I> results) {
        return withStartRecord(results.getTotalRecords(), results.getRecordsPerPage(), DEFAULT_ITEMS_PER_WINDOW, results.getStartRecord());
    }

    public PaginationHelper(int itemCount, int itemsPerPage, int maxVisiblePages, int currentPage) {
        this.totalNumberOfItems = itemCount;
        this.itemsPerPage = itemsPerPage;
        this.windowSize = maxVisiblePages;
        this.currentPage = currentPage;

        // derive the rest
        double dpc = (float) (itemCount) / itemsPerPage;
        this.pageCount = (int) Math.ceil(dpc);

        if (pageCount <= windowSize) {
            minimumPageNumber = 0;
            maximumPageNumber = pageCount - 1;
            currentPageIndex = currentPage;
        } else {
            // average case, cpi in the middle (when windowSize is odd) or right-of-middle (if windowSize is even)
            currentPageIndex = windowSize / 2;

            // distance of cpi to window end (not always windowSize/2) + 1
            int distw = windowSize - currentPageIndex;
            // distance of currentpage to last page + 1
            int distl = pageCount - currentPage;

            // adjust cpi if current page is near the end
            if (distl < distw) {
                currentPageIndex = windowSize - distl;

                // adjust cpi if currentPage is near the start
            } else if (currentPage < currentPageIndex) {
                currentPageIndex = currentPage;
            }

            minimumPageNumber = currentPage - currentPageIndex;
            maximumPageNumber = (minimumPageNumber + windowSize) - 1;
        }

        padding = Integer.toString(maximumPageNumber).length();
    }

    public boolean hasPrevious() {
        return currentPage > 0;
    }

    public boolean hasNext() {
        return currentPage < (pageCount - 1);
    }

    public int getMinimumPageNumber() {
        return minimumPageNumber;
    }

    public int getCurrentPageWindowIndex() {
        return currentPageIndex;
    }

    public int getMaximumPageNumber() {
        // return Math.min(pageCount - 1, getMinimumPageNumber() + windowSize - 1);
        return maximumPageNumber;
    }

    public int getPageCount() {
        return pageCount;
    }

    // recommended width (in characters) to alot for each page number
    public int getSuggestedPadding() {
        return padding;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageNumber(int windowIndex) {
        return minimumPageNumber + windowIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getWindowSize(); i++) {
            String fmtsel = "[%" + padding + "s]";
            String fmtreg = " %" + padding + "s ";
            int pageNumber = getPageNumber(i);
            String fmt = currentPageIndex == i ? fmtsel : fmtreg;
            sb.append(String.format(fmt, pageNumber));
        }
        String fmt = "{<%s> ic:%4s ipp:%3s pc:%3s cp:%3s min:%3s max:%3s}";
        String str = String.format(fmt, sb.toString(), totalNumberOfItems, itemsPerPage, pageCount, currentPage, minimumPageNumber, maximumPageNumber);
        return str;
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

    public int getFirstItem() {
        return firstItemOnPage(currentPage);
    }

    public int getLastItem() {
        int firstItem = firstItemOnPage(currentPage);
        int lastItem = (firstItem + itemsPerPage) - 1;
        if (lastItem > (totalNumberOfItems - 1)) {
            lastItem = totalNumberOfItems - 1;
        }
        return lastItem;
    }

    public int firstItemOnPage(int page) {
        return page * itemsPerPage;
    }

    public int getStartRecord() {
        if (getTotalNumberOfItems() == 0) {
            return 0;
        } else {
            return getFirstItem() + 1;
        }
    }

    public int getNextPage() {
        return getCurrentPage() + 1;
    }

    public int getPreviousPage() {
        return getCurrentPage() - 1;
    }

    public int getNextPageStartRecord() {
        return getItemsPerPage() * getNextPage();
    }

    public int getLastPage() {
        return getItemsPerPage() * (getPageCount() - 1);
    }

    public int getPreviousPageStartRecord() {
        return getItemsPerPage() * getPreviousPage();
    }

    public int getEndRecord() {
        if (getTotalNumberOfItems() == 0) {
            return 0;
        } else {
            return getLastItem() + 1;
        }
    }

}

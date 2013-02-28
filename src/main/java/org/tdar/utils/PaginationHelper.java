package org.tdar.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaginationHelper {
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    
    private int itemCount;
    private int itemsPerPage;
    private int visiblePageCount;
    private int currentPage;
    private SummaryProvider summaryProvider;

    //where to put snip-indicator when presenting subset of resultPages (pageCount > maxVisiblePages)
    private static int SNIP_OFFSET = 2;
    
    //stuff we derive
    private int pageCount;
    
    //distance from current page to last page
    private int distEnd;
    
    //distance from first page to current page
    private int distStart;
    
    //page number of the first page following the left snip
    private int sbLeft;
    
    //page number of the last page preceding the right snip
    private int sbRight;
    
    boolean snipped = false;
    
    
    private static SummaryProvider NULL_PROVIDER = new SummaryProvider() {
        public String getStartSummary(int pageNumber) {
            return null;
        }

        public String getEndSummary(int pageNumber) {
            return null;
        }
    };
    
    public static interface SummaryProvider {
        public String getStartSummary(int pageNumber);
        public String getEndSummary(int pageNumber);
    }
    
    public PaginationHelper(int itemCount, int itemsPerPage,  int visiblePages, int currentPage) {
        this(itemCount, itemsPerPage, visiblePages, currentPage, NULL_PROVIDER);
    }
    
    public PaginationHelper(int itemCount, int itemsPerPage, int maxVisiblePages, int currentPage, SummaryProvider summaryProvider) {
        this.itemCount = itemCount;
        this.itemsPerPage = itemsPerPage;
        this.visiblePageCount = maxVisiblePages;
        this.currentPage = currentPage;
        this.summaryProvider = summaryProvider;
        
        //derive the rest
        
        snipped = pageCount <= visiblePageCount;
        
        double dpc = (float)(itemCount) / itemsPerPage;
        this.pageCount = (int)Math.ceil(dpc);
        
        this.distEnd = (pageCount - 1) - currentPage;
        this.distStart = currentPage;
        
        sbLeft = 0;
        sbRight = pageCount - 1;
        
        
        
        /* My snipping 'logic':
         *     -If # of result pages exceeds how many pages we display on the screen, we snip the list of pages
         *          + we 'snip' out pages based on proximity of the current page to the first page and proximity to last page 
         *          + the list of pages will always show the first few pages of results, and the last few pages of results
         *          + 
         */
        if(isSnippedLeft() && isSnippedRight()) {
            sbLeft = currentPage - (maxVisiblePages / 2 - 1);
            sbRight = currentPage + (maxVisiblePages / 2 - 1);
        } else if(isSnippedLeft()){
            //if only left side is snipped,  the pagenum after the snip will be maxpage - the snip offset - 1 slot for rendering the gap
            //sbLeft = (pageCount - (maxVisiblePages - SNIP_OFFSET - 1)) - 1;
            sbLeft  = (pageCount - maxVisiblePages) + SNIP_OFFSET + 1;
        } else if(isSnippedRight()) {
            sbRight = (pageCount - SNIP_OFFSET - 1) -1;
        }
    }
   
    
    public boolean isSnippedLeft() {
        return distStart > visiblePageCount ; 
    }
    
    public boolean isSnippedRight() {
        return distEnd > visiblePageCount;
    }
   
    //page number of the page displayed by the left snip
    public int getSectionBorderLeft() {
        return sbLeft;
    }
    
    public int getSectionBorderRight() {
        return sbRight;
    }
    
    public int getFirstItem(int pageNumber) {
        if(pageNumber > pageCount) return -1;
        return itemsPerPage * pageNumber;
    }
    
    public int getLastItem(int pageNumber) {
        if(pageNumber > pageCount) return -1;
        int lastItem = itemsPerPage * pageNumber + itemsPerPage - 1;
        if(lastItem >= itemCount) lastItem = itemCount - 1;
        return lastItem;
    }

    public int getPageCount() {
        return pageCount;
    }
    
    public int getSnipIndexLeft() {
        return SNIP_OFFSET;
    }
    
    
    public int getSnipIndexRight() {
        return pageCount - 1 - SNIP_OFFSET;
    }
    
    //recommended  width (in characters) to alot for each page number
    public int getSuggestedPadding() {
        return Integer.toString(pageCount).length();
    }

    public int getVisiblePageCount() {
        return visiblePageCount;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }
    
    public int getPageNumber(int idx) {
        if(!snipped) return idx;
        if(idx < SNIP_OFFSET ) return idx;
        //if idx is to the right of the right snip
        if(idx >= visiblePageCount - 1 - SNIP_OFFSET) return (pageCount - 1) - ((visiblePageCount - 1) - idx);
        
        //if idx is left of the right snip, and there is no left snip
        if(!isSnippedLeft()) return idx;
        
        //idx is between the the left & right snip..
        return sbLeft + (idx - (SNIP_OFFSET + 1));
    }
    
    public String toString() {
        return render(this);
                
    }
    
    public boolean isSnipIndex(int idx) {
        return (isSnippedLeft() && getSnipIndexLeft() == idx) || 
        (isSnippedRight() && (getSnipIndexRight() == idx));
    }
    
    static String render(PaginationHelper ph) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < ph.getVisiblePageCount(); i++) {
            int pad = ph.getSuggestedPadding();
            String fmtsel = "[%" + pad + "s]";
            String fmtreg = " %" + pad + "s ";
            String fmt = ph.getCurrentPage() == ph.getPageNumber(i) ? fmtsel : fmtreg;
            String page;
            if(
                    (ph.isSnippedLeft() && ph.getSnipIndexLeft() == i) || 
                    (ph.isSnippedRight() && (ph.getSnipIndexRight() == i))
            )  {
                page = String.format(fmt,  "..");
            } else {
                page = String.format(fmt,  ph.getPageNumber(i) + 1);
            }
            sb.append(page);
        }
        
        return sb.toString();
    }
    
    public static PaginationHelper withStartRecord(int totalItems, int itemsPerPage, int maxVisiblePages, int startRecord) {
        //lock the startRecord to the first record in a page 
        int currentPage = startRecord / itemsPerPage;
        return new PaginationHelper(totalItems, itemsPerPage, maxVisiblePages, currentPage);
    }

}

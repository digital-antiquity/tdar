package org.tdar.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;


public class PaginationHelperTest {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testLowRange() {
        for (int i = 0; i < 5; i++) {
            PaginationHelper ph = newPaginationHelper(5000, 10, 10, i);
            assertEquals("minimum page should still be 0", 0, ph.getMinimumPageNumber());
            assertEquals("max page should be 9", 9, ph.getMaximumPageNumber());
        }
        for (int i = 5; i < 20; i++) {
            PaginationHelper ph = newPaginationHelper(5000, 10, 10, i);
            assertEquals("minimum page should be i - window size / 2", i - ph.getWindowSize()/2, ph.getMinimumPageNumber());
        }
    }
    
    @Test
    public void testHighRange() {
        for (int i = 500; i > 494; --i) {
            PaginationHelper ph = newPaginationHelper(5000, 10, 10, i);
            assertEquals("minimum page number should be 490", 490, ph.getMinimumPageNumber());
            assertEquals("maximum page number should be 499", 499, ph.getMaximumPageNumber());            
        }
        PaginationHelper ph = newPaginationHelper(5000, 10, 10, 494);
        assertEquals("maximum page number should be 498", 498, ph.getMaximumPageNumber());
    }
    
    @Test
    public void testPageCount() {
        assertEquals("should have 200 pages", 200, (newPaginationHelper(5000, 25, 20, 0)).getPageCount());
        assertEquals("should have 200 pages", 200, (newPaginationHelper(4976, 25, 20, 0)).getPageCount());
    }
    
    private PaginationHelper newPaginationHelper(int itemCount, int itemsPerPage,  int visiblePages, int currentPage) {
        PaginationHelper ph = new PaginationHelper(itemCount, itemsPerPage, visiblePages, currentPage);
        Exception ex = new Exception();
        ex.fillInStackTrace();
        StackTraceElement[] st = ex.getStackTrace();
        logger.debug("{}:{}:: {}",new Object[] {st[1].getMethodName(), st[1].getLineNumber(), ph});
        return ph;
    }
    
}

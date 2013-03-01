package org.tdar.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaginationHelperTest {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    //jim is too lazy to use matchers
    static interface Checker {
        void check(PaginationHelper ph);
    }
    
    //return new ph and log the method/line that created it
    private PaginationHelper newPaginationHelper(int itemCount, int itemsPerPage,  int visiblePages, int currentPage) {
        PaginationHelper ph = new PaginationHelper(itemCount, itemsPerPage, visiblePages, currentPage);
        Exception ex = new Exception();
        ex.fillInStackTrace();
        StackTraceElement[] st = ex.getStackTrace();
        logger.debug("{}:{}:: {}",new Object[] {st[1].getMethodName(), st[1].getLineNumber(), ph});
        return ph;
    }
    
    //go through all page numbers and run a check
    public void checkPages(int itemCount, int itemsPerPage, int windowSize, Checker checker) {
        int expectedPageCount = (int)(Math.ceil((double)itemCount / (double)itemsPerPage));
        for(int i = 0; i < expectedPageCount; i++) {
            PaginationHelper ph = newPaginationHelper(itemCount, itemsPerPage, windowSize, i);
            if(checker!=null)  {
                checker.check(ph);
            }
        }
    }
    
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
    
    @Test 
    public void testOddWindowSize() {
        checkPages(5000, 25, 11, new Checker() {
            public void check(PaginationHelper ph) {
                assertEquals("pagecount should alwasys be 200", 200, ph.getPageCount());
            }
        });
    }
    
    @Test 
    public void testOddWindow() {
        checkPages(2500, 25, 11, new Checker() {
            public void check(PaginationHelper ph) {
                //average case,  current page in the middle
                int expectedMin = ph.getCurrentPage() - 5;
                int expectedMax = expectedMin + 10;
                
                //corner case:  current page near end
                if(ph.getCurrentPage() >= 95) {
                    expectedMin = 89;
                    expectedMax = 99;
                    
                //corner case: current page near start
                } else if(ph.getCurrentPage() < 6) {
                    expectedMin = 0;
                    expectedMax = 10;
                }
                
                int actualMin = ph.getMinimumPageNumber();
                int actualMax = ph.getMaximumPageNumber();
                assertEquals("first page in window should be " + expectedMin, expectedMin, actualMin);
                assertEquals("last page in window should be " + expectedMax, expectedMax, actualMax);
            }
        });
    }
    
    @Test
    public void testEvenWindow() {
        checkPages(2500, 25, 10, new Checker() {
            public void check(PaginationHelper ph) {
                //average case,  current page in the middle
                int expectedMin = ph.getCurrentPage() - 5;
                int expectedMax = expectedMin + 9;
                
                //corner case:  current page near end
                if(ph.getCurrentPage() >= 96) {
                    expectedMin = 90;
                    expectedMax = 99;
                    
                //corner case: current page near start
                } else if(ph.getCurrentPage() < 5) {
                    expectedMin = 0;
                    expectedMax = 9;
                }
                
                int actualMin = ph.getMinimumPageNumber();
                int actualMax = ph.getMaximumPageNumber();
                assertEquals("first page in window should be " + expectedMin, expectedMin, actualMin);
                assertEquals("last page in window should be " + expectedMax, expectedMax, actualMax);
            }
        });
    }
    
    
    @Test 
    public void testPageCountLessThanWindowSize() {
        checkPages(50, 10, 10, new Checker() {
            public void check(PaginationHelper ph) {
                assertEquals("page count should be 5", 5, ph.getPageCount());
                assertEquals("window should not exceed pagecount", 0, ph.getMinimumPageNumber());
                assertEquals("window should not exceed pagecount", 4, ph.getMaximumPageNumber());
            }
        });
    }

    
    
}

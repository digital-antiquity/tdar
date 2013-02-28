package org.tdar.utils;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

@Ignore
public class PaginationHelperTest {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testNoSnip() {
        PaginationHelper ph = new PaginationHelper(400, 50, 10, 4);
        assertFalse("no pages snipped", ph.isSnippedLeft());
        assertFalse("no pages snipped", ph.isSnippedRight());
        assertEquals(400 / 50, ph.getPageCount());
    }
    
    @Test
    public void testRightSnip() {
        int itemsPerPage = 50;
        int itemCount =  5000;
        int visiblePages = 10;
        int currentPage = 0;
        PaginationHelper ph = newPaginationHelper(itemCount, itemsPerPage, visiblePages, currentPage);
        
        assertTrue("right side should be snipped", ph.isSnippedRight());
        assertFalse("left side should not be snipped", ph.isSnippedLeft());
        
        assertEquals("first page should be 0", 0, ph.getPageNumber(0));
    }
    
    
    @Test
    public void testLeftSnip() {
        PaginationHelper ph = newPaginationHelper(5000, 25, 20, 5000/25);
        assertTrue("should be snipped on the left", ph.isSnippedLeft());
    }
    
    @Test
    public void testLeftSnipLastPage() {
        PaginationHelper ph = newPaginationHelper(5000, 25, 10, 199);
        assertEquals("page count", 200, ph.getPageCount());
        assertEquals("last page number", 199, ph.getPageNumber(9));
    }
    
    @Test
    public void testPageAfterLeftSnip() {
        PaginationHelper ph = newPaginationHelper(5000, 25, 10, 199);
        assertEquals("page count", 200, ph.getPageCount());
        assertTrue("results should be snipped on left", ph.isSnippedLeft());
        int expected =  193;
        int actual = ph.getPageNumber(3);
        assertEquals("page after left-snip ", expected, actual );
        assertEquals("first page should be 0", 0, ph.getPageNumber(0));
    }
    @Test 
    public void testPageAfterLeftSnip2() {
        PaginationHelper ph = newPaginationHelper(4976, 25, 20, 199);
        int expected = 183;
        int actual = ph.getPageNumber(3);
        assertEquals("page after left-snip ", expected, actual );
    }
    
    
    @Test
    public void testPageBeforeRightSnip() {
        fail("not implemenned");
    }
    
    @Test
    public void testPageCount() {
        assertEquals("should have 200 pages", 200, (newPaginationHelper(5000, 25, 20, 0)).getPageCount());
        assertEquals("should have 200 pages", 200, (newPaginationHelper(4976, 25, 20, 0)).getPageCount());
    }
    
    PaginationHelper newPaginationHelper(int itemCount, int itemsPerPage,  int visiblePages, int currentPage) {
        PaginationHelper ph = new PaginationHelper(itemCount, itemsPerPage, visiblePages, currentPage);
        Exception ex = new Exception();
        ex.fillInStackTrace();
        StackTraceElement[] st = ex.getStackTrace();
        logger.debug("{}:{}:: {}",new Object[] {st[1].getMethodName(), st[1].getLineNumber(), ph});
        return ph;
    }
    
}

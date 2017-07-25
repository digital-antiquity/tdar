package org.tdar.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaginationHelperTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // return new ph and log the method/line that created it
    private PaginationHelper newPaginationHelper(int itemCount, int itemsPerPage, int visiblePages, int currentPage) {
        PaginationHelper ph = new PaginationHelper(itemCount, itemsPerPage, visiblePages, currentPage);
        Exception ex = new Exception();
        ex.fillInStackTrace();
        StackTraceElement[] st = ex.getStackTrace();
        logger.trace("{}:{}:: {}", new Object[] { st[1].getMethodName(), st[1].getLineNumber(), ph });
        return ph;
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
            assertEquals("minimum page should be i - window size / 2", i - (ph.getWindowSize() / 2), ph.getMinimumPageNumber());
        }
    }

    @Test
    public void testPaginationMethodsCalledByPaginationMacroLinks() {
        int recordsPerPage = 2;
        int max = 5000;
        for (int i = 10; i < (max + 1); i++) {
            int currentPage = i + (int) (Math.random() * ((max - i) + 1));
            int prevPage = currentPage - 1;
            int nextPage = currentPage + 1;
            PaginationHelper ph = newPaginationHelper(i, recordsPerPage, 10, currentPage);

            assertTrue(String.format("last page %s should be less than %s ", ph.getLastPage(), i), ph.getLastPage() < i);
            assertEquals(String.format("prev page %s should be %s ", ph.getPreviousPage(), prevPage), prevPage, ph.getPreviousPage());
            assertEquals(String.format("prev page start %s should be %s ", ph.getPreviousPageStartRecord(), prevPage * recordsPerPage), prevPage
                    * recordsPerPage, ph.getPreviousPageStartRecord());
            assertEquals(String.format("next page start %s should be %s ", ph.getNextPageStartRecord(), nextPage * recordsPerPage), nextPage
                    * recordsPerPage, ph.getNextPageStartRecord());
            assertEquals(String.format("next page %s should be %s ", ph.getNextPage(), nextPage), nextPage, ph.getNextPage());
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
        for (int i = 0; i < pagecount(5000, 25); i++) {
            PaginationHelper ph = newPaginationHelper(5000, 25, 11, i);
            assertEquals("pagecount should alwasys be 200", 200, ph.getPageCount());
        }
    }

    @Test
    public void testOddWindow() {
        for (int i = 0; i < pagecount(2500, 25); i++) {
            PaginationHelper ph = newPaginationHelper(2500, 25, 11, i);
            // average case, current page in the middle
            int expectedMin = ph.getCurrentPage() - 5;
            int expectedMax = expectedMin + 10;

            // corner case: current page near end
            if (ph.getCurrentPage() >= 95) {
                expectedMin = 89;
                expectedMax = 99;

                // corner case: current page near start
            } else if (ph.getCurrentPage() < 6) {
                expectedMin = 0;
                expectedMax = 10;
            }

            int actualMin = ph.getMinimumPageNumber();
            int actualMax = ph.getMaximumPageNumber();
            assertEquals("first page in window should be " + expectedMin, expectedMin, actualMin);
            assertEquals("last page in window should be " + expectedMax, expectedMax, actualMax);
        }
    }

    @Test
    public void testEvenWindow() {
        for (int i = 0; i < pagecount(2500, 25); i++) {
            PaginationHelper ph = newPaginationHelper(2500, 25, 10, i);
            // average case, current page in the middle
            int expectedMin = ph.getCurrentPage() - 5;
            int expectedMax = expectedMin + 9;

            // corner case: current page near end
            if (ph.getCurrentPage() >= 96) {
                expectedMin = 90;
                expectedMax = 99;

                // corner case: current page near start
            } else if (ph.getCurrentPage() < 5) {
                expectedMin = 0;
                expectedMax = 9;
            }

            int actualMin = ph.getMinimumPageNumber();
            int actualMax = ph.getMaximumPageNumber();
            assertEquals("first page in window should be " + expectedMin, expectedMin, actualMin);
            assertEquals("last page in window should be " + expectedMax, expectedMax, actualMax);
        }
    }

    @Test
    public void testPageCountLessThanWindowSize() {
        for (int i = 0; i < pagecount(49, 10); i++) {
            PaginationHelper ph = newPaginationHelper(50, 10, 10, i);
            assertEquals("page count should be 5", 5, ph.getPageCount());
            assertEquals("window should not exceed pagecount", 0, ph.getMinimumPageNumber());
            assertEquals("window should not exceed pagecount", 4, ph.getMaximumPageNumber());
        }
    }

    @Test
    public void testPageNumberAndCurrentPageIndex() {
        for (int items = 0; items < 500; items += 10) {
            for (int itemsPerPage = 10; itemsPerPage < 30; itemsPerPage++) {
                for (int windowSize = 9; windowSize < 15; windowSize++) {
                    for (int currentPage = 0; currentPage < pagecount(items, itemsPerPage); currentPage++) {
                        PaginationHelper ph = newPaginationHelper(items, itemsPerPage, windowSize, currentPage);
                        assertEquals("minpage + cpi should be " + currentPage, currentPage, ph.getMinimumPageNumber() + ph.getCurrentPageWindowIndex());
                        for (int i = 0; i < windowSize; i++) {
                            int expectedPage = ph.getMinimumPageNumber() + i;
                            int actualPage = ph.getPageNumber(i);
                            assertEquals("page number at window[" + i + "] should be " + expectedPage, expectedPage, actualPage);
                        }
                    }
                }
            }
        }
    }

    // expected page count
    private int pagecount(int items, int itemsPerPage) {
        return (int) Math.ceil((float) items / (float) itemsPerPage);
    }

}

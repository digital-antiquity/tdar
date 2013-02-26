package org.tdar.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;


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
    public void testOneSnip() {
        int itemsPerPage = 50;
        int itemCount =  5000;
        int visiblePages = 10;
        int currentPage = 0;
        PaginationHelper ph = new PaginationHelper(itemCount, itemsPerPage, visiblePages, currentPage);
        
        logger.debug(render(ph));
        assertFalse("right side should be snipped", ph.isSnippedRight());
        assertTrue("left side should not be snipped", ph.isSnippedLeft());
    }
    
    private String render(PaginationHelper ph) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < ph.getVisiblePageCount(); i++) {
            int pad = ph.getSuggestedPadding();
            String fmtsel = "[%" + pad + "s]";
            String fmtreg = " %" + pad + "s ";
            String fmt = ph.getCurrentPage() == i ? fmtsel : fmtreg;
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
}

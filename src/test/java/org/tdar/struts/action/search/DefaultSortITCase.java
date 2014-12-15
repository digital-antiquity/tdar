package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.TdarActionException;

import com.opensymphony.xwork2.interceptor.annotations.Before;

public class DefaultSortITCase extends AbstractSearchControllerITCase {

    @Before
    protected void init() {
        super.reindex();
    }

    @Test
    public void testAdvancedSearchDefaultSort() throws TdarActionException {
        controller = generateNewInitializedController(AdvancedSearchController.class);
        controller.search();
        logger.info("sorting by: {} ", controller.getSortField());
        assertEquals(SortOption.getDefaultSortOption(), controller.getSortField());
    }

    @Test
    public void testRSSDefaultSort() throws TdarActionException {
        RSSSearchAction controller_ = null;
        try {
        controller_ = generateNewInitializedController(RSSSearchAction.class);
            controller_.viewRss();
        } catch (Exception e) {
            logger.error("exception in rss", e);
        }
        logger.info("sorting by: {} ", controller_.getSortField());
        assertEquals(SortOption.ID_REVERSE, controller_.getSortField());
    }
}

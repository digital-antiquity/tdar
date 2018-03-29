package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.tdar.core.bean.SortOption;
import org.tdar.struts.action.api.search.RSSSearchAction;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.interceptor.annotations.Before;

public class DefaultSortITCase extends AbstractSearchControllerITCase {

    @Before
    protected void init() {
        super.reindex();
    }

    @Test
    public void testAdvancedSearchDefaultSort() throws TdarActionException {
        AdvancedSearchController controller = generateNewInitializedController(AdvancedSearchController.class);
        controller.search();
        logger.info("sorting by: {} ", controller.getSortField());
        assertEquals(SortOption.getDefaultSortOption(), controller.getSortField());
    }

    @Test
    public void testRSSDefaultSort() throws TdarActionException {
        RSSSearchAction controller_ = null;
        boolean fail = false;
        try {
            controller_ = generateNewInitializedController(RSSSearchAction.class);
            controller_.viewRss();
        } catch (Exception e) {
            logger.error("exception in rss", e);
            fail = true;
        }

        logger.info("sorting by: {} ", controller_.getSortField());
        assertEquals(SortOption.ID_REVERSE, controller_.getSortField());
        assertFalse(fail);
    }
}

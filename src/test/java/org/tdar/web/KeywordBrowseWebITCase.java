/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import org.junit.Test;

public class KeywordBrowseWebITCase extends AbstractAnonymousWebTestCase {
    private static final String SUCCESS_URL = "/browse/investigation-type/8/Records-Search-Inventory-Checking";
    boolean indexed = false;

    public void reindexOnce() {
        if (indexed) {
            return;
        }

        reindexUnauthenticated();
        indexed = true;
    }

    @Test
    public void testSlugViewTaintedUrl() {
        reindexOnce();
        gotoPage("/browse/investigation-type/8/Records-Search-Inventory-Testing");
        assertCurrentUrlContains(SUCCESS_URL);
    }

    @Test
    public void testSlugViewNoSlug() {
        reindexOnce();
        gotoPage("/browse/investigation-type/8");
        assertCurrentUrlContains(SUCCESS_URL);
    }

    @Test
    public void testSlugViewPagination() {
        reindexOnce();
        gotoPage("/browse/investigation-type/12?startRecord=2&recordsPerPage=1");
        assertCurrentUrlContains("/browse/investigation-type/12/Systematic-Survey?startRecord=2&recordsPerPage=1");
    }

}

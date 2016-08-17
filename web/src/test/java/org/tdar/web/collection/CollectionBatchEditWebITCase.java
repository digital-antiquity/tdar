package org.tdar.web.collection;

import org.junit.Test;
import org.tdar.web.AbstractEditorAuthenticatedWebTestCase;

public class CollectionBatchEditWebITCase extends AbstractEditorAuthenticatedWebTestCase {

    private static final String _12345 = "12345";
    private static final String HORP_DESCRIPTION = "horp description";
    private static final String HORP_TITLE = "HORP title";

    @Test
    public void testBatchEdit() {
        gotoPage("/collection/admin/batch/1002");
        setInput("titles[1]", HORP_TITLE);
        setInput("descriptions[1]", HORP_DESCRIPTION);
        setInput("dates[1]", _12345);
        logger.debug(getPageText());
        submitForm();
        gotoPage("/collection/1002/display_orientationgrid?orientation=LIST_FULL");
        assertTextPresentInPage(HORP_TITLE);
        assertTextPresentInPage(HORP_DESCRIPTION);
        assertTextPresentInPage(_12345);
        assertTextNotPresent("HARP");

        logger.debug(getPageText());
    }
}

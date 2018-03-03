package org.tdar.web.collection;

import org.junit.Test;
import org.tdar.web.AbstractEditorAuthenticatedWebTestCase;

public class CollectionCompareWebITCase extends AbstractEditorAuthenticatedWebTestCase {

    private static final String HORP_DESCRIPTION = "horp description";
    private static final String HORP_TITLE = "HORP title";

    @Test
    public void testCompare() {
        gotoPage("/resource/compare?collectionId=1004");
        logger.debug(getPageText());
        assertNoErrorTextPresent();
    }
}

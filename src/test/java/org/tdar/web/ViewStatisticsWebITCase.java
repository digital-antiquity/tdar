package org.tdar.web;

import java.net.URL;

import org.junit.Test;

public class ViewStatisticsWebITCase extends AbstractAuthenticatedWebTestCase {

    public static final String TEST_TITLE = "This is a test title";

    @Test
    public void testViewStatisticsIncremented() {
        createDocumentAndUploadFile(TEST_TITLE);
        
        logger.info(getPageCode());
        assertTextPresent("1 time");
        URL url = internalPage.getUrl();
        gotoPage(url.toString());
        assertTextPresent("2 time(s)");

    }

}

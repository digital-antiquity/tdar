package org.tdar.web;

import java.net.URL;

import org.junit.Test;

public class ViewStatisticsWebITCase extends AbstractAuthenticatedWebTestCase {

    public static final String TEST_TITLE = "This is a test title";

    @Test
    public void testViewStatisticsIncremented() {
        createDocumentAndUploadFile(TEST_TITLE);
        String pageCode = getPageCode();
        String docUrl = pageCode.substring(pageCode.indexOf("/filestore/"));
        docUrl = docUrl.substring(0, docUrl.indexOf("\""));
        logger.info(docUrl);
        assertTextPresentInPage("1 time");
        URL url = internalPage.getUrl();
        gotoPage(url.toString());
        assertTextPresentInPage("2 time(s)");

        gotoPage(docUrl);
        gotoPage(url.toString());
        assertTextPresentInPage("downloaded 1 time");
        gotoPage(docUrl);
        gotoPage(url.toString());
        assertTextPresentInPage("downloaded 2 times");
    }

}

package org.tdar.web;

import java.net.URL;

import org.junit.Test;

public class ViewStatisticsWebITCase extends AbstractAuthenticatedWebTestCase {

    public static final String TEST_TITLE = "This is a test title";

    @Test
    public void testViewStatisticsIncremented() {
        logout();
        loginAdmin();
        createDocumentAndUploadFile(TEST_TITLE);
        login();
        String pageCode = getPageCode();
        String docUrl = pageCode.substring(pageCode.indexOf("/filestore/"));
        docUrl = docUrl.substring(0, docUrl.indexOf("\""));
        logger.info(docUrl);
        URL url = internalPage.getUrl();
        //start at 0
        gotoPage(url.toString());
        assertTextPresent("1 time");
        gotoPage(url.toString());
        assertTextPresent("2 time(s)");

        gotoPage(docUrl);
        gotoPage(url.toString());
        assertTextPresent("downloaded 1 time");
        gotoPage(docUrl);
        gotoPage(url.toString());
        assertTextPresent("downloaded 2 times");
    }

}

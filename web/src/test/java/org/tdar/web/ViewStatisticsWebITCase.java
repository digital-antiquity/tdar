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
        String pageCode = getPageCode();
        URL url = internalPage.getUrl();
        logger.debug(getPageCode());
        String docUrl = pageCode.substring(pageCode.indexOf("/filestore/"));
        docUrl = docUrl.substring(0, docUrl.indexOf("\""));
        logout();
        login();
        logger.info(docUrl);
        // start at 0
        gotoPage(url.toString());
        gotoPageWithoutErrorCheck(docUrl);
        logout();
        loginAdmin();
        gotoPage(url.toString());
        assertTextPresent("1 time");
        assertTextPresent("downloaded 1 time");
        logout();
        login();
        gotoPage(url.toString());
        gotoPageWithoutErrorCheck(docUrl);
        logout();
        loginAdmin();
        gotoPage(url.toString());
        assertTextPresent("2 time(s)");
        assertTextPresent("downloaded 2 times");
    }

}

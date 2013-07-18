package org.tdar.web.functional;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * tests for business rules surrounding encrypted and non-encrypted pages o
 */
public class HttpsSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {

    @Before
    @Override
    public void login() {
        // override the login because we don't need to index
        gotoPage("/login");
        find("#loginUsername").sendKeys(CONFIG.getAdminUsername());
        find("#loginPassword").sendKeys(CONFIG.getPassword());
        find("#btnLogin").click();
    }

    @Test
    public void testAttemptToVisitDocumentEdit() throws InterruptedException {
        // try to go to document-add page with non-ssl
        gotoPage(getBaseUrl(false), "/document/add");
        Thread.sleep(500);
        takeScreenshot("final screenshot");
        logger.debug("on page: {}", getCurrentUrl());

        assertThat(getCurrentUrl(), startsWith("https"));
    }
}

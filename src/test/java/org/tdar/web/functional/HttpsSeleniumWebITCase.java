package org.tdar.web.functional;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * tests for business rules surrounding encrypted and non-encrypted pages o
 */
public class HttpsSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Test
    public void testAttemptToVisitDocumentEdit() throws InterruptedException {
        // try to go to document-add page with non-ssl
        gotoPage(getBaseUrl(false), "/document/add");
        find("#loginUsername").sendKeys(CONFIG.getAdminUsername());
        find("#loginPassword").sendKeys(CONFIG.getAdminPassword());
        find("#btnLogin").click();
        Thread.sleep(500);
        takeScreenshot("final screenshot");
        logger.debug("on page: {}", getCurrentUrl());

        assertThat(getCurrentUrl(), startsWith("https"));
    }
}

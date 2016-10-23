package org.tdar.functional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: jimdevos
 * Date: 7/1/13
 */
public class EmailSeleniumWebITCase extends AbstractAdminSeleniumWebITCase {
    private static final String EMAIL_LINK = "Submit Correction";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Ignore @Test
    public void sendTest() throws InterruptedException {
        String url = "/project/3805";
        gotoPage(url);
        waitFor("body");
        logger.debug("on page: {}", url);
        find(By.partialLinkText(EMAIL_LINK)).click();
        waitForPageload();
        find("#messageBody").sendKeys("This is a test email");
        find(By.name("send")).click();
        waitForPageload();
        assertThat(getText(), containsString("Message Sent"));
        // we could do this implicitly by going to any other page but this makes the test faster
        reportJavascriptErrors();
    }

    @Ignore("")
    @Test
    public void sendTestErrorNoBody() throws InterruptedException {
        String url = "/project/3805";
        gotoPage(url);
        waitFor("body");
        logger.debug("on page: {}", url);
        find(By.partialLinkText(EMAIL_LINK)).click();
        waitForPageload();
        waitFor(By.name("send")).click();
        assertTrue(getText().contains("required"));
        // we could do this implicitly by going to any other page but this makes the test faster
        reportJavascriptErrors();
    }

    @Ignore("happens too fast")
    public void sendTestErrorSpam() throws InterruptedException {
        String url = "/project/3805";
        gotoPage(url);
        waitFor("body");
        logger.debug("on page: {}", url);
        find(By.partialLinkText(EMAIL_LINK)).first().click();
        waitForPageload();
        find(By.name("send")).click();
        Thread.sleep(2000);
        reportJavascriptErrors();
        assertTrue(getText().contains("An error occurred"));
        // we could do this implicitly by going to any other page but this makes the test faster
        reportJavascriptErrors();
    }
}

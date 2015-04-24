package org.tdar.web.functional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: jimdevos
 * Date: 7/1/13
 */
public class EmailSeleniumWebITCase extends AbstractAdminSeleniumWebITCase {
    private static final String EMAIL_LINK = "Submit Correction";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void sendTest() throws InterruptedException {
        String url = "/project/3805";
        gotoPage(url);
        waitFor("body");
        logger.debug("on page: {}", url);
        find(By.partialLinkText(EMAIL_LINK)).click();
        waitFor("div.modal.hide.fade.in");  //modal adds .in  after animation complete (maybe?)
        find("#messageBody").sendKeys("This is a test email");
        find(By.name("send")).click();

        WebElement statusModal = waitFor(visibilityOf(find("#emailStatusModal").first()));
        assertThat(statusModal.getText(), containsString("Your message has been sent"));
        find(By.id("email-close-button")).click();
        // we could do this implicitly by going to any other page but this makes the test faster
        reportJavascriptErrors();
    }

    @Test
    public void sendTestErrorNoBody() throws InterruptedException {
        String url = "/project/3805";
        gotoPage(url);
        waitFor("body");
        logger.debug("on page: {}", url);
        find(By.partialLinkText(EMAIL_LINK)).first().click();
        waitFor(By.name("send")).click();
        waitFor( visibilityOf( find("#emailErrorContainer").first()));
        assertTrue(getText().contains("An error occurred"));
        find(By.id("email-close-button")).click();
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
        find(By.name("messageBody")).sendKeys("This is a test email");
        find(By.name("send")).click();
        Thread.sleep(2000);
        reportJavascriptErrors();
        assertTrue(getText().contains("An error occurred"));
        find(By.id("email-close-button")).click();
        // we could do this implicitly by going to any other page but this makes the test faster
        reportJavascriptErrors();
    }
}

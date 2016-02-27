package org.tdar.functional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;
import static org.tdar.TestConstants.TEST_DOCUMENT;
import static org.tdar.struts.action.TdarActionSupport.CONFIRM;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.functional.util.WebElementSelection;

/**
 * Created by adam on 7/21/14.
 */
public class DownloadSeleniumWebITCase extends AbstractSeleniumWebITCase {

    Logger logger = LoggerFactory.getLogger(getClass());
    String documentViewUrl;

    @Before
    public void createDocument() {
        loginAdmin();
        gotoPage("/document/add");
        setFieldByName("document.title", "My Sample Document");
        setFieldByName("document.documentType", "OTHER");
        setFieldByName("document.description", "A resource description");
        setFieldByName("document.date", "1923");
        setFieldByName("projectId", "-1");
        uploadFileAsync(FileAccessRestriction.PUBLIC, new File(TEST_DOCUMENT));
        submitForm();
        documentViewUrl = getCurrentUrl();
        // if something went wrong we'd probably be on /document/save.action
        assertThat(documentViewUrl, containsString("/document/"));
        assertThat(documentViewUrl, not(containsString("/document/save")));
        logout();
    }

    @Test
    public void testVisitorDownload() throws InterruptedException {
        // start at the cart page, and click one of the suggested packages
        logout();
        gotoPage(documentViewUrl);
        find(".download-file").get(0).click();
        waitForPageload();
        // now we are on the review form (w/ registration/login forms)
        // fill out required user registration fields and submit form
        assertThat(getCurrentUrl(), containsString("download"));
        UserRegistration reg = createUserRegistration("bob");
        reg.setConfirmEmail(reg.getConfirmEmail().toUpperCase());
        fillOutRegistration(reg);
        // wait for spam check
        Thread.sleep(3000);
        WebElementSelection buttons = find("#registrationForm [type=submit]");
        buttons.first().click();
        Thread.sleep(1000);

        // now we are on the "choose billing account" page. just click through to next page
        waitFor(titleContains("Download: "));
        dismissModal();
        assertThat(getCurrentUrl(), containsString(CONFIRM));
    }

    @Test
    public void testLoginDownload() {
        // go to document-view page (while logged-out) and click on one of the file download links
        logout();
        gotoPage(documentViewUrl);
        find(".download-file").first().click();

        // we should now be on the download authentication page.
        assertThat(getCurrentUrl(), containsString("download"));

        // fill out the "login" form and submit
        find("#loginUsername").val(CONFIG.getUsername());
        find("#loginPassword").val(CONFIG.getPassword());
        submitForm("#loginForm [type=submit]");

        // At this point we need to hurry before the autodownload starts (in 4 seconds or so). Confirm we're on the right page and then bail out!
        waitFor(titleContains("Download: "));
        assertThat(getCurrentUrl(), containsString(CONFIRM));
    }

    @Override
    public boolean testRequiresLucene() {
        return true;
    }
}

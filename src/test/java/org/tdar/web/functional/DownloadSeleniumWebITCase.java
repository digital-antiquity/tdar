package org.tdar.web.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.tdar.TestConstants.TEST_DOCUMENT;

import java.io.File;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.struts.action.download.AbstractDownloadController;
import org.tdar.struts.data.UserRegistration;

/**
 * Created by adam on 7/21/14.
 */
public class DownloadSeleniumWebITCase extends AbstractSeleniumWebITCase {

    Logger logger = LoggerFactory.getLogger(getClass());

    // handle of window created at beginning of test
    String startWindow = null;

    private static String url;
    
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
        url = getCurrentUrl();
        logout();

    }


    @Test
    public void testVisitorDownload() throws InterruptedException {
        //start at the cart page, and click one of the suggested packages
        logout();
        gotoPage(url);
        find(".download-file").get(0).click();

        //now we are on the review form (w/ registration/login forms)
        //fill out required user registration fields and submit form
        assertThat(getCurrentUrl(), StringContains.containsString("download"));
        UserRegistration reg = createUserRegistration("bob");
        fillOutRegistration(reg);
        // wait for spam check
        Thread.sleep(4000);
        submitForm("#registrationForm .submitButton");


        //now we are on the "choose billing account" page. just click through to next page
        waitFor(ExpectedConditions.titleContains("Download: "));
        dismissModal();
        assertThat(getCurrentUrl(), StringContains.containsString(AbstractDownloadController.CONFIRM));
    }

    @Test
    public void testLoginDownload() throws InterruptedException {
        // Starting page
        // go to the cart page and make sure we are logged out
        logout();
        gotoPage(url);
        find(".download-file").get(0).click();

        //now we are on the review form (w/ registration/login forms)
        //fill out required user registration fields and submit form
        assertThat(getCurrentUrl(), StringContains.containsString("download"));

        find("#loginUsername").val(CONFIG.getUsername());
        find("#loginPassword").val(CONFIG.getPassword());
        Thread.sleep(4000);
        submitForm("#loginForm [type=submit]");

        waitFor(ExpectedConditions.titleContains("Download: "));
        dismissModal();
        assertThat(getCurrentUrl(), StringContains.containsString(AbstractDownloadController.CONFIRM));
    }

}

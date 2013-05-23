package org.tdar.web.functional;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;

public class FileUploadITCase extends FunctionalWebTestCase {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    
    @Test 
    public void testDocumentUpload() {
        login();
        gotoPage("/document/add");
        
        File testDocument = new File(TestConstants.TEST_DOCUMENT);
        assertTrue("file exists", testDocument.exists());
        String path = testDocument.getAbsolutePath();
        find("#resourceRegistrationTitle").val("testing file upload");
        find("#dateCreated").val("2002");
        find("#projectId").val("-1");
        find("#resourceDescription").val("this is a test");
        
        //selenium will not operate on elements it cannot 'see', so we need to unhide the actual file upload input
        WebElement fileInput = find("#fileAsyncUpload").get(0);
        fileInput.sendKeys(path);
        //find("#fileAsyncUpload").sendKeys(path);
        
        //once the upload is complete the delete button will appear.
        waitFor(".delete-button");
        
        find("#submitButton").submit();
        
        
        logger.debug("document text \n\n {} \n\n", getText());
    }
    
    //the nuclear option:  disable all CSS so that the real file input control is visible
    //http://stackoverflow.com/questions/3526361/firefoxdriver-how-to-disable-javascript-css-and-make-sendkeys-type-instantly
    @Override
    protected FirefoxProfile newFirefoxProfile() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("permissions.default.stylesheet", 2);
        //profile.setPreference("permissions.default.image", 2);
        return profile;
    }
}


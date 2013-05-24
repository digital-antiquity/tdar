package org.tdar.web.functional;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openqa.selenium.WebElement;
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
        clearFileInputStyles();
        WebElement fileInput = find("#fileAsyncUpload").first();
        fileInput.sendKeys(path);
        //find("#fileAsyncUpload").sendKeys(path);
        
        //once the upload is complete the delete button will appear.
        waitFor(".delete-button");
        
        find("#submitButton").submit();
        
        
        logger.debug("document text \n\n {} \n\n", getText());
    }
    
    private void clearFileInputStyles() {
        WebElement input = find("#fileAsyncUpload").first();
        setStyle(input, "position", "static");
        setStyle(input, "top", "auto");
        setStyle(input, "right", "auto");
        setStyle(input, "margin", 0);
        setStyle(input, "opacity", 1);
        setStyle(input, "transform", "none");
        setStyle(input, "direction", "ltr");
        setStyle(input, "cursor", "auto");
    }
    
    //FIXME: extend this to any webelement value, e.g. setPropVal(elem, "style", "display", "none")  or setPropVal(elem, "disabled", false);
    private void setStyle(WebElement elem, String property, Object value){
        executeJavascript("arguments[0].style[arguments[1]]=arguments[2]", elem, property, value);
    }
    
    //the nuclear option:  disable all CSS so that the real file input control is visible
    //http://stackoverflow.com/questions/3526361/firefoxdriver-how-to-disable-javascript-css-and-make-sendkeys-type-instantly
    protected FirefoxProfile _newFirefoxProfile() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("permissions.default.stylesheet", 2);
        //profile.setPreference("permissions.default.image", 2);
        return profile;
    }
}


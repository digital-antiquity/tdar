package org.tdar.web.functional;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import static org.junit.Assert.assertTrue;

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
        find("#fileAsyncUpload").sendKeys(path);
        
        //once the upload is complete the delete button will appear.
        waitFor(".delete-button");
        
        find("#submitButton").submit();
        
        
        logger.debug("document text \n\n {} \n\n", getText());
    }
}

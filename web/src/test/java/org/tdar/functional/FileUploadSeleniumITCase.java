package org.tdar.functional;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;

public class FileUploadSeleniumITCase extends AbstractBasicSeleniumWebITCase {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testDocumentUpload() {
        gotoPage("/document/add");

        File testDocument = new File(TestConstants.TEST_DOCUMENT);
        assertTrue("file exists", testDocument.exists());
        String path = testDocument.getAbsolutePath();
        find("#resourceRegistrationTitle").val("testing file upload");
        find("#dateCreated").val("2002");
        find("#projectId").val("-1");
        find("#resourceDescription").val("this is a test");

        // selenium will not operate on elements it cannot 'see', so we need to unhide the actual file upload input
        WebElement fileInput = find("#fileAsyncUpload").first();
        fileInput.sendKeys(path);

        // once the upload is complete the delete button will appear.
        waitFor(".delete-button");

        submitForm();
        assertThat(getDriver().getCurrentUrl(), not(endsWith("action")));
        assertThat(getDriver().getCurrentUrl(), not(endsWith("edit")));

        logger.trace("document text \n\n {} \n\n", getText());
    }

}

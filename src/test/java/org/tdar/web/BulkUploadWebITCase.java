/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Status;

/**
 * @author Adam Brin
 * 
 */
public class BulkUploadWebITCase extends AbstractAuthenticatedWebTestCase {

    @Test
    public void testInvalidBulkUpload() {
        testBulkUploadController("image_manifest2.xlsx");
    }

    @Test
    public void testValidBulkUpload() {
        testBulkUploadController("image_manifest.xlsx");
    }

    public void testBulkUploadController(String filename) {

        String ticketId = getPersonalFilestoreTicketId();
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        for (File uploadedFile : FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, true)) {
            uploadFileToPersonalFilestore(ticketId, uploadedFile.getAbsolutePath());
        }

        gotoPage("/batch/add");
        setInput("projectId", "");
        setInput("status", Status.ACTIVE.name());
        setInput("uploadedFiles", TestConstants.TEST_BULK_DIR + filename);
//        setInput("sourceCollections[0].text", "source collection text");
//        setInput("relatedComparativeCollections[0].text", "related comparative collection text");
        
        int i = 0;
        for (File uploadedFile : FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, true)) {
            addFileProxyFields(i, false, uploadedFile.getName());
            i++;
        }
        if (filename.contains("2")) {
            setInput("resourceCollections[0].name", "template name");
        }
        setInput("ticketId", ticketId);
        submitForm();
        assertTrue(internalPage.getUrl().toString().contains("save.action"));
        assertTextPresentIgnoreCase("Processing bulk upload");
        assertTextPresentInCode("getJSON(\"checkstatus");
        String statusPage = "/batch/checkstatus?ticketId=" + ticketId;
        gotoPage(statusPage);
        logger.info(getPageCode());
        int count = 0;
        while (!getPageCode().contains("\"percentDone\" : 100")) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail("InterruptedException during bulk upload.  sorry.");
            }
            gotoPage(statusPage);
            logger.info(getPageCode());
            if (count == 1000) {
                fail("we went through 1000 iterations of waiting for the upload to be imported... assuming something is wrong");
            }
            count++;
        }
        if (filename.contains("2")) {
            assertTrue(getPageCode().contains("resource creator is not"));
        } else {
            assertFalse(getPageCode().contains("resource creator is not"));
        }
    }
}

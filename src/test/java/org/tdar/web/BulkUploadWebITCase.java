/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.web;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Status;
import java.util.List;

/**
 * @author Adam Brin
 * 
 */
public class BulkUploadWebITCase extends AbstractAuthenticatedWebTestCase {

    @Test
    public void testBulkUploadController() {

        String ticketId = getPersonalFilestoreTicketId();
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        List<File> files = (List<File>) FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, true);
        for (File uploadedFile : files) {
            uploadFileToPersonalFilestore(ticketId, uploadedFile.getAbsolutePath());
        }

        gotoPage("/batch/add");
        setInput("projectId", "");
        setInput("status", Status.ACTIVE.name());
        setInput("uploadedFiles", TestConstants.TEST_BULK_DIR + "image_manifest.xlsx");

        setInput("ticketId", ticketId);
        submitForm();
        String statusPage = "/batch/checkstatus?ticketId="+ticketId;
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
    }
}

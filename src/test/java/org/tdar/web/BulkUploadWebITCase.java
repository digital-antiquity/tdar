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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
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
        testBulkUploadController("image_manifest2.xlsx", null);
    }

    @Test
    public void testValidBulkUpload() {
        Map<String, String> extra = new HashMap<String, String>();
        extra.put("investigationTypeIds", "1");
        extra.put("latitudeLongitudeBoxes[0].maximumLatitude", "41.83228739643032");
        extra.put("latitudeLongitudeBoxes[0].maximumLongitude", "-71.39860153198242");
        extra.put("latitudeLongitudeBoxes[0].minimumLatitude", "41.82608370627639");
        extra.put("latitudeLongitudeBoxes[0].minimumLongitude", "-71.41018867492676");
        extra.put(PROJECT_ID_FIELDNAME, "3805");
        extra.put("resource.inheritingInvestigationInformation","true");
        testBulkUploadController("image_manifest.xlsx", extra);
    }

    
    @Test
    public void testValidBulkUpload2() {
        Map<String, String> extra = new HashMap<String, String>();
        extra.put("investigationTypeIds", "1");
        extra.put("coverageDates[0].startDate", "1200");
        extra.put("coverageDates[0].endDate", "1500");
        extra.put("coverageDates[0].dateType", "CALENDAR_DATE");
        extra.put("resourceNotes[0].type", "GENERAL");
        extra.put("resourceNotes[0].note", "A Moose once bit my sister...");
        extra.put("resourceProviderInstitutionName", "Digital Antiquity4");
        extra.put("sourceCollections[0].text", "ASU Museum Collection1");
        extra.put("sourceCollections[1].text", "test Museum Collection1");
        extra.put("latitudeLongitudeBoxes[0].maximumLatitude", "41.83228739643032");
        extra.put("latitudeLongitudeBoxes[0].maximumLongitude", "-71.39860153198242");
        extra.put("latitudeLongitudeBoxes[0].minimumLatitude", "41.82608370627639");
        extra.put("latitudeLongitudeBoxes[0].minimumLongitude", "-71.41018867492676");
        extra.put(PROJECT_ID_FIELDNAME, "3805");
//        extra.put("resource.inheritingInvestigationInformation","true");
        testBulkUploadController("image_manifest.xlsx", extra);
    }

    public void testBulkUploadController(String filename, Map<String, String> extra) {

        String ticketId = getPersonalFilestoreTicketId();
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        for (File uploadedFile : FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, true)) {
            uploadFileToPersonalFilestore(ticketId, uploadedFile.getAbsolutePath());
        }

        gotoPage("/batch/add");
        setInput("status", Status.ACTIVE.name());
        setInput("uploadedFiles", TestConstants.TEST_BULK_DIR + filename);
        if (extra != null) {
            for (String key : extra.keySet()) {
                setInput(key, extra.get(key));
            }
        }
        if (extra != null && !extra.containsKey(PROJECT_ID_FIELDNAME)) {
            setInput("projectId", "");
            }
     
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

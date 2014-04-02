/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.web;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class BulkUploadWebITCase extends AbstractAuthenticatedWebTestCase {

    @Test
    public void testInvalidBulkUpload() {
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        Collection<File> listFiles = FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false);
        Map<String, String> extra = new HashMap<String, String>();
        extra.put("resourceCollections[0].name", "template name");
        testBulkUploadController("image_manifest2.xlsx", listFiles, extra, false);
        assertTrue(getPageCode().contains("resource creator is not"));

    }

    @Test
    public void testBulkUploadDups() {
        File testImagesDirectory = new File(TestConstants.TEST_BULK_DIR + "/" + "TDAR-2380");
        Collection<File> listFiles = FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, true);
        testBulkUploadController("TDAR-2380/tdar-bulk-upload-template.xls", listFiles, null, false);
        assertFalse(getPageCode().contains("Plate 01"));
        assertFalse(getPageCode().contains("Plate 02"));
        assertFalse(getPageCode().contains("Plate 03"));
        assertTrue(getPageCode().contains("Plate 04"));
        assertTrue(getPageCode().contains("Plate 05"));
        assertEquals(33, StringUtils.countMatches(getPageCode(), "Filename \\\"Color Plate"));
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.CREDIT_CARD })
    public void testValidBulkUpload() throws MalformedURLException {
        String accountId = "";
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            gotoPage("/cart/add");
            setInput("invoice.numberOfMb", "200");
            setInput("invoice.numberOfFiles", "20");
            submitForm();
            setInput("invoice.paymentMethod", "CREDIT_CARD");
            String invoiceId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL);
            accountId = addInvoiceToNewAccount(invoiceId, null, "my first account");
        }

        Map<String, String> extra = new HashMap<String, String>();
        extra.put("investigationTypeIds", "1");
        extra.put("latitudeLongitudeBoxes[0].maximumLatitude", "41.83228739643032");
        extra.put("latitudeLongitudeBoxes[0].maximumLongitude", "-71.39860153198242");
        extra.put("latitudeLongitudeBoxes[0].minimumLatitude", "41.82608370627639");
        extra.put("latitudeLongitudeBoxes[0].minimumLongitude", "-71.41018867492676");
        extra.put(PROJECT_ID_FIELDNAME, "3805");
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            extra.put("accountId", accountId);
        }
        extra.put("resource.inheritingInvestigationInformation", "true");
        extra.put("resourceProviderInstitutionName", "Digital Antiquity");
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        Collection<File> listFiles = FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false);
        testBulkUploadController("image_manifest.xlsx", listFiles, extra, true);
        assertFalse(getPageCode().contains("resource creator is not"));
    }

    
    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR})
    @Ignore
    public void testExtraFile() throws MalformedURLException {
        Map<String, String> extra = new HashMap<String, String>();
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        Collection<File> listFiles = new ArrayList<File>(FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, true));
        listFiles.add(new File(TestConstants.TEST_DOCUMENT_DIR,TestConstants.TEST_DOCUMENT_NAME));
        testBulkUploadController("image_manifest.xlsx", listFiles, extra, true);
        logger.debug(getPageBodyCode());
        assertTrue(getPageCode().contains(TestConstants.TEST_DOCUMENT_NAME));
    }

    

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    public void testValidBulkUploadWithConfidentialSelfSimple() throws MalformedURLException {
        String accountId = "";
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            gotoPage("/cart/add");
            setInput("invoice.numberOfMb", "200");
            setInput("invoice.numberOfFiles", "20");
            submitForm();
            setInput("invoice.paymentMethod", "CREDIT_CARD");
            String invoiceId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL);
            accountId = addInvoiceToNewAccount(invoiceId, null, "my first account");
        }

        Map<String, String> extra = new HashMap<String, String>();
        extra.put("creditProxies[0].person.id", getUserId().toString());
        extra.put("creditProxies[0].person.firstName", getUser().getFirstName());
        extra.put("creditProxies[0].person.lastName", getUser().getLastName());
        extra.put("creditProxies[0].person.institution.name", getUser().getInstitutionName());
        extra.put("creditProxies[0].role", ResourceCreatorRole.CONTACT.name());
        extra.put(PROJECT_ID_FIELDNAME, "3805");
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            extra.put("accountId", accountId);
        }
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        Collection<File> listFiles = FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false);
        testBulkUploadController("image_manifest_simple.xlsx", listFiles, extra, true);
        assertFalse(getPageCode().contains("resource creator is not"));
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    public void testValidBulkUploadWithConfidentialSelf() throws MalformedURLException {
        String accountId = "";
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            gotoPage("/cart/add");
            setInput("invoice.numberOfMb", "200");
            setInput("invoice.numberOfFiles", "20");
            submitForm();
            setInput("invoice.paymentMethod", "CREDIT_CARD");
            String invoiceId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL);
            accountId = addInvoiceToNewAccount(invoiceId, null, "my first account");
        }

        Map<String, String> extra = new HashMap<String, String>();
        extra.put("investigationTypeIds", "1");
        extra.put("latitudeLongitudeBoxes[0].maximumLatitude", "41.83228739643032");
        extra.put("latitudeLongitudeBoxes[0].maximumLongitude", "-71.39860153198242");
        extra.put("latitudeLongitudeBoxes[0].minimumLatitude", "41.82608370627639");
        extra.put("latitudeLongitudeBoxes[0].minimumLongitude", "-71.41018867492676");
        extra.put("creditProxies[0].person.id", getUserId().toString());
        extra.put("creditProxies[0].person.firstName", getUser().getFirstName());
        extra.put("creditProxies[0].person.lastName", getUser().getLastName());
        extra.put("creditProxies[0].person.institution.name", getUser().getInstitutionName());
        extra.put("creditProxies[0].role", ResourceCreatorRole.CONTACT.name());
        extra.put(PROJECT_ID_FIELDNAME, "3805");
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            extra.put("accountId", accountId);
        }
        extra.put("resource.inheritingInvestigationInformation", "true");
        extra.put("resourceProviderInstitutionName", "Digital Antiquity");
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        Collection<File> listFiles = FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false);
        testBulkUploadController("image_manifest.xlsx", listFiles, extra, true);
        assertFalse(getPageCode().contains("resource creator is not"));
    }

    @Test
    // RunWithTdarConfiguration.TDAR,
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    public void testValidBulkUploadWithProject() throws MalformedURLException {

        String accountId = "";
        Map<String, String> extra = new HashMap<String, String>();
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            gotoPage("/cart/add");
            setInput("invoice.numberOfMb", "200");
            setInput("invoice.numberOfFiles", "20");
            submitForm();
            setInput("invoice.paymentMethod", "CREDIT_CARD");
            String invoiceId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL);
            accountId = addInvoiceToNewAccount(invoiceId, null, "my first account");
            extra.put("accountId", accountId);
        }

        ResourceType rt = ResourceType.PROJECT;
        final String path = "/" + rt.getUrlNamespace() + "/add";
        gotoPage(path);
        setInput(String.format("%s.%s", rt.getFieldName(), "title"), "test");
        setInput(String.format("%s.%s", rt.getFieldName(), "description"), "test");
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            try {
                setInput("accountId", accountId);
            } catch (Exception e) {
                // hasInput = false;
            }
        }
        submitForm();
        Long projectId = extractTdarIdFromCurrentURL();

        extra.put("investigationTypeIds", "1");
        extra.put("coverageDates[0].startDate", "1200");
        extra.put("coverageDates[0].endDate", "1500");
        extra.put("coverageDates[0].dateType", "CALENDAR_DATE");
        extra.put("resourceNotes[0].type", "GENERAL");
        extra.put("resourceNotes[0].note", "A Moose once bit my sister...");
        extra.put("resourceProviderInstitutionName", "Digital Antiquity4");
        extra.put("authorshipProxies[0].person.id", "1");
        extra.put("authorshipProxies[0].person.lastName", "Lee");
        extra.put("authorshipProxies[0].person.firstName", "Allen");
        extra.put("sourceCollections[0].text", "ASU Museum Collection1");
        extra.put("sourceCollections[1].text", "test Museum Collection1");
        extra.put("latitudeLongitudeBoxes[0].maximumLatitude", "41.83228739643032");
        extra.put("latitudeLongitudeBoxes[0].maximumLongitude", "-71.39860153198242");
        extra.put("latitudeLongitudeBoxes[0].minimumLatitude", "41.82608370627639");
        extra.put("latitudeLongitudeBoxes[0].minimumLongitude", "-71.41018867492676");
        extra.put(PROJECT_ID_FIELDNAME, projectId.toString());
        // extra.put("resource.inheritingInvestigationInformation","true");
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        Collection<File> listFiles = FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false);
        testBulkUploadController("image_manifest.xlsx", listFiles, extra, true);
        assertFalse(getPageCode().contains("could not save xml record"));
        assertFalse(getPageCode().contains("resource creator is not"));

    }

    public void testBulkUploadController(String filename, Collection<File> listFiles, Map<String, String> extra, boolean expectSuccess) {

        String ticketId = getPersonalFilestoreTicketId();
        for (File uploadedFile : listFiles) {
            uploadFileToPersonalFilestore(ticketId, uploadedFile.getAbsolutePath());
        }

        gotoPage("/batch/add");
        setInput("status", Status.ACTIVE.name());
        setInput("uploadedFiles", TestConstants.TEST_BULK_DIR + filename);
        if (extra != null) {
            boolean hasInput = true;
            try {
                getInput("accountId");
            } catch (Exception e) {
                hasInput = false;
            }
            for (String key : extra.keySet()) {
                if (!key.equals("accountId") || hasInput) {
                    setInput(key, extra.get(key));
                }
            }
        }
        if (extra != null && !extra.containsKey(PROJECT_ID_FIELDNAME)) {
            setInput("projectId", "");
        }

        int i = 0;
        for (File uploadedFile : listFiles) {
            addFileProxyFields(i, FileAccessRestriction.PUBLIC, uploadedFile.getName());
            i++;
        }
        if (filename.contains("2")) {
            setInput("resourceCollections[0].name", "template name");
        }
        setInput("ticketId", ticketId);
        submitForm();
        assertTrue(internalPage.getUrl().toString().contains("save.action"));
        assertTextPresentIgnoreCase("Bulk Upload Status");
        // logger.info(getPageCode());
        assertTextPresentInCode("$.ajax");
        String statusPage = "/batch/checkstatus?ticketId=" + ticketId;
        gotoPage(statusPage);
        logger.info(getPageCode());
        int count = 0;
        // fixme: parse this json and get the actual number,
        while (!getPageCode().contains("\"percentDone\":100")) {
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
        if (expectSuccess && !getPageCode().contains("errors\":\"\"")) {
        	Assert.fail(getPageBodyCode());
        }
    }
}

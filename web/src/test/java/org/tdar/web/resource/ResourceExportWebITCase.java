package org.tdar.web.resource;

import java.net.MalformedURLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.UrlConstants;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.web.AbstractAuthenticatedWebTestCase;

@RunWith(MultipleWebTdarConfigurationRunner.class)
public class ResourceExportWebITCase extends AbstractAuthenticatedWebTestCase {

    public static String IMAGE_TITLE_FIELDNAME = "image.title";
    public static String DESCRIPTION_FIELDNAME = "image.description";
    public static final String TEST_IMAGE_NAME = "handbook_of_archaeology.jpg";
    public static final String TEST_IMAGE = TestConstants.TEST_IMAGE_DIR + TEST_IMAGE_NAME;
    public static String IMAGE_TITLE = "a thumb test";
    public static String DESCRIPTION = "this is a test";

    @SuppressWarnings("unused")
    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    public void testValidBulkUploadWithConfidentialSelfSimple() throws MalformedURLException {
        String accountId = "";
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            gotoPage(UrlConstants.CART_ADD);
            setInput("invoice.numberOfMb", "200");
            setInput("invoice.numberOfFiles", "20");
            submitForm();
            selectAnyAccount();
            // setInput("invoice.paymentMethod", "CREDIT_CARD");
            accountId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL).get(ACCOUNT_ID);
        }

        // simulate an async file upload
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TEST_IMAGE);

        gotoPage("/image/add");
        setInput("accountId", accountId);
        setInput("ticketId", ticketId);
        setInput(IMAGE_TITLE_FIELDNAME, IMAGE_TITLE);
        setInput(DESCRIPTION_FIELDNAME, DESCRIPTION);
        setInput("image.date", "1984");
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        // FIXME: need to create input
        addFileProxyFields(0, FileAccessRestriction.CONFIDENTIAL, TEST_IMAGE_NAME);
        // setInput("resourceAvailability", "Public");
        submitForm();

        gotoPage("/dashboard");
        clickLinkWithText("Export");
        logger.debug(getPageText());
        setInput("accountId", accountId);
        submitForm("Submit");
        logger.debug(getPageText());
    }
}

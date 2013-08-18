package org.tdar.web;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.utils.TestConfiguration;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
public class AccountUsageWebITCase extends AbstractWebTestCase {

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    private static float BYTES_PER_MEGABYTE = 1048576F;

    @Test
    public void testCartWithAccountFilling() throws MalformedURLException {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "user124");
        testLogin(personmap, true);
        assertTextPresent("Create a new project");

        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "20");
        setInput("invoice.numberOfFiles", "2");
        submitForm();
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        String invoiceId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL);
        String accountId = addInvoiceToNewAccount(invoiceId, null, "my first account");
        assertTrue(accountId != "-1");

        createDocumentAndUploadFile("my first document");
        createDocumentAndUploadFile("my second document");
        gotoPage("/document/add");
        assertTextPresent("What would you like to put into tDAR");
        gotoPage("/resource/add");
        assertTextPresent("What would you like to put into tDAR");
        logger.info(getPageText());
        gotoPage("/logout");
    }

    @Test
    public void testCartWithCoupon() throws MalformedURLException {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "user1124");
        testLogin(personmap, true);
        assertTextPresent("Create a new project");

        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "20");
        setInput("invoice.numberOfFiles", "2");
        submitForm();
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        String invoiceId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL);
        String accountId = addInvoiceToNewAccount(invoiceId, null, "my first account");
        assertTrue(accountId != "-1");
        logger.info(getCurrentUrlPath());

        setInput("numberOfFiles", "1");
        submitForm("Create Voucher");
        String code = getHtmlPage().getDocumentElement().querySelector("td.voucherCode").getFirstChild().toString();
        logger.info("=======================================================\n" + code);
        gotoPage("/cart/add");
        setInput("invoice.numberOfFiles", "1");
        setInput("code", code);
        submitForm();
        invoiceId = testAccountPollingResponse("0", TransactionStatus.TRANSACTION_SUCCESSFUL);

        gotoPage("/logout");
    }

    @Test
    public void testAccountListWhenEditingAsAdmin() throws Exception {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "bobloblaw123");
        testLogin(personmap, true);

        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "20");
        setInput("invoice.numberOfFiles", "2");
        submitForm();
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        String invoiceId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL);
        String accountName = "loblaw account";
        String accountId = addInvoiceToNewAccount(invoiceId, null, accountName);

        createDocumentAndUploadFile("my first document");
        logger.debug("page url is: {}", internalPage.getUrl());

        Long docid = extractTdarIdFromCurrentURL();
        String viewUrl = internalPage.getUrl().getPath();
        gotoPage("/logout");

        login(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());

        gotoPage("/document/" + docid + "/edit");
        assertTextPresent(accountName);
    }

    @Test
    /**
     * This test tries to recreate a very specific set of steps that cause tdar to incorrectly flag a resource.  Basically you 
     * create a draft citation, and then add an attachment in an edit pass.   If you perform this step 2-3 times the system may flag the resource,
     * even if the billing account has plenty of space remaining.
     * @throws Exception
     */
    public void testUploadOnSecondEditProperAccountDecriment() throws Exception {
        // create 2 accounts w/ 10 files & 4x the MB that we need
        File file = new File(TestConstants.TEST_DOCUMENT);
        int spaceNeeded = (int) Math.ceil((file.length() / BYTES_PER_MEGABYTE) * 4);
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "bobloblaw234");
        testLogin(personmap, true);

        // the 2nd account is not used. We only add it to ensure the edit renders a select dropdown which more faithfully recreates the precondition described
        // in the ticket
        int acct1Id = createNewAccountWithInvoice("test account one", 10, spaceNeeded);
        int acct2Id = createNewAccountWithInvoice("test account two", 10, spaceNeeded);
        assertTrue(acct1Id > 0);
        assertTrue(acct2Id > 0);

        createDocumentThenUploadFile("document one", acct1Id, TestConstants.TEST_DOCUMENT_NAME);
        createDocumentThenUploadFile("document two", acct1Id, TestConstants.TEST_DOCUMENT_NAME);
        createDocumentThenUploadFile("document three", acct1Id, TestConstants.TEST_DOCUMENT_NAME);
    }

    public int createNewAccountWithInvoice(String accountName, int files, int mb) throws Exception {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", files);
        setInput("invoice.numberOfFiles", mb);
        submitForm();
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        String invoiceId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL);
        String accountId = addInvoiceToNewAccount(invoiceId, null, accountName);
        return Integer.parseInt(accountId);
    }

    public void createDocumentThenUploadFile(String title, int accountId, String filename) {
        gotoPage("/document/add");
        assertTextPresentInPage("Create a new Document");
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        setInput("document.title", title);
        setInput("document.description", "Abstract");
        setInput("document.date", "2012");
        setInput("projectId", TestConstants.NO_ASSOCIATED_PROJECT);
        setInput("accountId", accountId);
        setInput("status", Status.DRAFT);
        submitForm();
        String url = getCurrentUrlPath();

        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, filename);
        gotoPage(url);
        clickLinkWithText("edit");
        setInput("ticketId", ticketId);
        addFileProxyFields(0, FileAccessRestriction.PUBLIC, filename);
        setInput("accountId", accountId);
        submitForm();

        // make sure we're on the view page
        assertPageTitleEquals(title);
        assertTextPresentInPage(filename);

        // make sure were not flagged.
        String flaggedText = "Flagged for account overage";
        assertTextNotPresent(flaggedText);
    }

}

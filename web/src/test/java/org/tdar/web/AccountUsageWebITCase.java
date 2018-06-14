package org.tdar.web;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.URLConstants;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.utils.TestConfiguration;

@RunWith(MultipleWebTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
public class AccountUsageWebITCase extends AbstractWebTestCase {

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    private static float BYTES_PER_MEGABYTE = 1048576F;

    @Test
    public void testCartWithAccountFilling() throws MalformedURLException {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "user" + System.currentTimeMillis());
        personmap.remove("reg.contributorReason");
        testRegister(personmap, TERMS.BOTH, true);
        assertTextPresent("Start a new Project");

        gotoPage(URLConstants.CART_ADD);
        setInput("invoice.numberOfMb", "20");
        setInput("invoice.numberOfFiles", "2");
        submitForm();
        // setInput("invoice.paymentMethod", "CREDIT_CARD");
        String accountId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL).get(ACCOUNT_ID);
        assertTrue(accountId != "-1");

        createDocumentAndUploadFile("my first document", Long.parseLong(accountId));
        createDocumentAndUploadFile("my second document", Long.parseLong(accountId));
        gotoPage("/document/add");
        assertTextPresent("Pricing");
        gotoPage("/resource/add");
        assertTextPresent("Pricing");
        logger.info(getPageText());
        logout();
    }

    @Test
    public void testCartWithCoupon() throws MalformedURLException {
        Map<String, String> personmap = new HashMap<>();
        setupBasicUser(personmap, "user1124" + System.currentTimeMillis());
        personmap.remove("reg.contributorReason");
        testRegister(personmap, TERMS.BOTH, true);
        assertTextPresent("Start a new Project");

        gotoPage(URLConstants.CART_ADD);
        setInput("invoice.numberOfMb", "20");
        setInput("invoice.numberOfFiles", "2");
        submitForm();
        // setInput("invoice.paymentMethod", "CREDIT_CARD");
        String accountId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL).get(ACCOUNT_ID);
        assertTrue(accountId != "-1");
        logger.info(getCurrentUrlPath());
        gotoPage("/billing/" + accountId);
        setInput("numberOfFiles", "1");
        submitForm("Create Voucher");

        String code = getHtmlPage().getDocumentElement().querySelector("td.voucherCode").getFirstChild().toString();

        logger.info("coupon code is:" + code);
        gotoPage(URLConstants.CART_ADD);
        setInput("invoice.numberOfFiles", "1");
        setInput("code", code);
        submitForm();

        // sanity check: after submitting the cart form we should wind up on the review page.
        assertThat(getCurrentUrlPath(), containsString("/review"));

        accountId = testAccountPollingResponse("0", TransactionStatus.TRANSACTION_SUCCESSFUL).get(ACCOUNT_ID);
        logout();
    }

    @Test
    public void testAccountListWhenEditingAsAdmin() throws Exception {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "bobloblaw123" + System.currentTimeMillis());
        personmap.remove("reg.contributorReason");
        testRegister(personmap, TERMS.BOTH, true);

        gotoPage(URLConstants.CART_ADD);
        setInput("invoice.numberOfMb", "20");
        setInput("invoice.numberOfFiles", "2");
        submitForm();
        logger.debug("curernt page:{}", getCurrentUrlPath());
        // setInput("invoice.paymentMethod", "CREDIT_CARD");
        String accountId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL).get(ACCOUNT_ID);

        createDocumentAndUploadFile("my first document", Long.parseLong(accountId));
        logger.debug("page url is: {}", internalPage.getUrl());

        Long docid = extractTdarIdFromCurrentURL();
        internalPage.getUrl().getPath();
        logout();

        login(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());

        gotoPage("/document/" + docid + "/edit");
        assertTextPresent("Default account for");
    }


    @Test
    public void testAccountAddUser() throws Exception {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "bobloblaw123" + System.currentTimeMillis());
        personmap.remove("reg.contributorReason");
        testRegister(personmap, TERMS.BOTH, true);

        gotoPage(URLConstants.CART_ADD);
        setInput("invoice.numberOfMb", "20");
        setInput("invoice.numberOfFiles", "2");
        submitForm();
        logger.debug("curernt page:{}", getCurrentUrlPath());
        // setInput("invoice.paymentMethod", "CREDIT_CARD");
        String accountId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL).get(ACCOUNT_ID);
        gotoPage("/billing/"+ accountId + "/edit");
        setInput("proxies[5].id",TestConfiguration.getInstance().getUserId());
        setInput("proxies[5].permission",Permissions.USE_ACCOUNT);
        submitForm();
        logger.debug(getPageText());
        assertTextPresentInPage("test user");
        assertTextPresentInPage("Charge to Billing Account");
//        assertTextPresentInPage("test user  test@tdar.org   Charge to Billing Account");
    }

    @Test
    /**
     * This test tries to recreate a very specific set of steps that cause tdar to incorrectly flag a resource. Basically you
     * create a draft citation, and then add an attachment in an edit pass. If you perform this step 2-3 times the system may flag the resource,
     * even if the billing account has plenty of space remaining.
     * 
     * @throws Exception
     */
    public void testUploadOnSecondEditProperAccountDecrement() throws Exception {
        // create 2 accounts w/ 10 files & 4x the MB that we need
        File file = TestConstants.getFile(TestConstants.TEST_DOCUMENT);
        int spaceNeeded = (int) Math.ceil((file.length() / BYTES_PER_MEGABYTE) * 4);
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "bobloblaw234" + System.currentTimeMillis());
        // personmap.remove("reg.contributorReason");
        testRegister(personmap, TERMS.BOTH, true);
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
        gotoPage(URLConstants.CART_ADD);
        setInput("invoice.numberOfMb", files);
        setInput("invoice.numberOfFiles", mb);
        logger.debug("about to submit form:{}", getForm());
        submitForm();
        setInputIfExists("invoice.paymentMethod", "CREDIT_CARD");
        setInputIfExists("account.id", "-1");
        setInputIfExists("account.name", "generated account (2)");
        logger.debug(getCurrentUrlPath());
        logger.debug(getPageText());
        String accountId = testAccountPollingResponse("11000", TransactionStatus.TRANSACTION_SUCCESSFUL).get(ACCOUNT_ID);
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
        setInputIfExists("accountId", Integer.toString(accountId));
        setInput("status", Status.DRAFT);
        submitForm();
        String url = getCurrentUrlPath();

        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, filename);
        gotoPage(url);
        clickLinkWithText("edit");
        setInput("ticketId", ticketId);
        addFileProxyFields(0, FileAccessRestriction.PUBLIC, filename);
        setInputIfExists("accountId", Integer.toString(accountId));
        submitForm();

        // make sure we're on the view page
        assertPageTitleContains(title);
        assertTextPresentInPage(filename);

        // make sure were not flagged.
        String flaggedText = "Flagged for account overage";
        assertTextNotPresent(flaggedText);
    }

}

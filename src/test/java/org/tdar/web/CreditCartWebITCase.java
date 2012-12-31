package org.tdar.web;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.TestConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.CartController;

import com.gargoylesoftware.htmlunit.WebWindow;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { "src/test/resources/tdar.cc.properties" })
public class CreditCartWebITCase extends AbstractAuthenticatedWebTestCase {

    private static final String MY_TEST_ACCOUNT = "my test account";
    private static final String THIS_IS_A_TEST_DESCIPTION = "this is a test desciption";

    @Test
    public void testCartIncomplete() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "0");
        setInput("invoice.numberOfFiles", "0");
        submitForm();
        assertCurrentUrlContains("save.action");
        assertTextPresentInCode("505 USD");
        assertTextPresentInCode(CartController.SPECIFY_SOMETHING);
    }

    @Test
    public void testCartFilesNoMB() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "0");
        setInput("invoice.numberOfFiles", "100");
        submitForm();
        assertTextPresent("50-500:100:$31:$3,100");
        assertTextPresent("total:$3,100");
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        testResponse("310000", TransactionStatus.TRANSACTION_SUCCESSFUL);

    }

    @Test
    public void testCartMBNoFiles() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "100");
        setInput("invoice.numberOfFiles", "0");
        submitForm();
        assertTextPresent("100 mb:1:$50:$50");
        assertTextPresent("total:$50");
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        testResponse("5000", TransactionStatus.TRANSACTION_SUCCESSFUL);
    }

    @Test
    public void testCartSuccess() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        submitForm();

        assertTextPresent("100 mb:19:$50:$950");
        assertTextPresent("5- 19:10:$40:$400");
        assertTextPresent("total:$1,350");
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        testResponse("135000", TransactionStatus.TRANSACTION_SUCCESSFUL);
    }

    @Test
    public void testCartWithAccount() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        submitForm();

        setInput("invoice.paymentMethod", "CREDIT_CARD");
        String invoiceId = testResponse("135000", TransactionStatus.TRANSACTION_SUCCESSFUL);
        String accountId = addInvoiceToNewAccount(invoiceId, null);
        assertTrue(accountId != "-1");
    }

    @Test
    public void testAddCartToAccount() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        submitForm();

        setInput("invoice.paymentMethod", "CREDIT_CARD");
        String invoiceId = testResponse("135000", TransactionStatus.TRANSACTION_SUCCESSFUL);
        String accountId = addInvoiceToNewAccount(invoiceId, null);
        assertTrue(accountId != "-1");
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "10000");
        setInput("invoice.numberOfFiles", "12");
        submitForm();
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        String invoiceId2 = testResponse("543000", TransactionStatus.TRANSACTION_SUCCESSFUL);
        String account = addInvoiceToNewAccount(invoiceId2, accountId);
        assertEquals(account, accountId);
        assertTextPresent("10,000");
        assertTextPresent("2,000");
        assertTextPresent("10");
        assertTextPresent("12");
        assertTextPresent("$5,430");
        assertTextPresent("$1,350");
        logger.info(getPageText());
        
    }

    private String addInvoiceToNewAccount(String invoiceId, String accountId) {
        if (accountId != null) {
            gotoPage("/billing/choose?invoiceId=" + invoiceId + "&accountId=" + accountId);
            setInput("id", accountId);
        } else {
            gotoPage("/billing/add?invoiceId=" + invoiceId);
        }
        setInput("account.name", MY_TEST_ACCOUNT);
        setInput("account.description", THIS_IS_A_TEST_DESCIPTION);
        List<Person> users = entityService.findAllRegisteredUsers(3);
        List<Long> userIds = Persistable.Base.extractIds(users);
        for (int i = 0; i < userIds.size(); i++) {
            setInput("authorizedMembers[" + i + "].id", Long.toString(userIds.get(i)));
        }
        submitForm();
        assertAccountPageCorrect(users, userIds);
        clickLinkOnPage("edit");
        String id = getInput("id").getAttribute("value");
        submitForm();
        assertAccountPageCorrect(users, userIds);
        return id;
    }

    private void assertAccountPageCorrect(List<Person> users, List<Long> userIds) {
        assertTextPresent(MY_TEST_ACCOUNT);
        assertTextPresent(THIS_IS_A_TEST_DESCIPTION);
        for (int i = 0; i < userIds.size(); i++) {
            assertTextPresent(users.get(i).getProperName());
        }
        assertTextPresent(getUser().getProperName());
    }

    @Test
    public void testCartError() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        setInput("extraItemName", "error");
        setInput("extraItemQuantity", "1");

        submitForm();

        assertTextPresent("100 mb:19:$50:$950");
        assertTextPresent("5- 19:10:$40:$400");
        assertTextPresent("total:$1,405.21");
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        testResponse("140521", TransactionStatus.TRANSACTION_FAILED);
    }

    @Test
    public void testCartUnknown() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        setInput("extraItemName", "unknown");
        setInput("extraItemQuantity", "1");

        submitForm();

        assertTextPresent("100 mb:19:$50:$950");
        assertTextPresent("5- 19:10:$40:$400");
        assertTextPresent("total:$1,405.31");
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        testResponse("140531", TransactionStatus.TRANSACTION_FAILED);
    }

    @Test
    public void testCartDecline() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        setInput("extraItemName", "decline");
        setInput("extraItemQuantity", "1");

        submitForm();

        assertTextPresent("100 mb:19:$50:$950");
        assertTextPresent("5- 19:10:$40:$400");
        assertTextPresent("total:$1,405.11");
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        testResponse("140511", TransactionStatus.TRANSACTION_FAILED);
    }

    private String testResponse(String total, TransactionStatus expectedResponse) throws MalformedURLException {
        assertCurrentUrlContains("/simple");

        String invoiceid = getInput("id").getAttribute("value");
        submitForm();
        assertCurrentUrlContains("process-payment-request");
        clickLinkWithText("click here");
        URL polingUrl = new URL(getBaseUrl() + "/cart/polling-check?id=" + invoiceid);
        String response = getPollingRequest(polingUrl);
        assertTrue(response.contains(TransactionStatus.PENDING_TRANSACTION.name()));
        checkInput(NelnetTransactionItem.getInvoiceIdKey(), invoiceid);
        checkInput(NelnetTransactionItem.getUserIdKey(), Long.toString(getUserId()));
        checkInput(NelnetTransactionItem.AMOUNT_DUE.name(), total);
        clickElementWithId("process-payment_0");
        response = getPollingRequest(polingUrl);
        assertTrue(response.contains(expectedResponse.name()));
        return invoiceid;
    }

    private String getPollingRequest(URL polingUrl) {
        WebWindow openWindow = webClient.openWindow(polingUrl, "polling" + System.currentTimeMillis());
        return openWindow.getEnclosedPage().getWebResponse().getContentAsString();
    }

}

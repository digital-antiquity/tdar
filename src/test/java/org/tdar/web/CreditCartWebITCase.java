package org.tdar.web;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

import com.gargoylesoftware.htmlunit.WebWindow;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { "src/test/resources/tdar.cc.properties" })
public class CreditCartWebITCase extends AbstractAuthenticatedWebTestCase {

    @Test
    public void testCart() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        submitForm();
        logger.info(getPageCode());

        assertTextPresent("100 mb:19:$50:$950");
        assertTextPresent("5- 19:10:$40:$400");
        assertTextPresent("total:$1,350");
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        testResponse("135000", TransactionStatus.TRANSACTION_SUCCESSFUL);
    }

    @Test
    public void testCartError() throws MalformedURLException {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        setInput("extraItemName", "error");
        setInput("extraItemQuantity", "1");

        submitForm();
        logger.info(getPageCode());

        assertTextPresent("100 mb:19:$50:$950");
        assertTextPresent("5- 19:10:$40:$400");
        assertTextPresent("total:$1,405.21");
        setInput("invoice.paymentMethod", "CREDIT_CARD");
        testResponse("140521", TransactionStatus.TRANSACTION_FAILED);
    }

    private void testResponse(String total, TransactionStatus expectedResponse) throws MalformedURLException {
        assertCurrentUrlContains("/simple");

        String invoiceid = getInput("invoice.id").getAttribute("value");
        submitForm();
        assertCurrentUrlContains("process-payment-request");
        clickLinkWithText("click here");
        logger.info(getPageCode());
        URL polingUrl = new URL(getBaseUrl() + "/cart/polling-check?id=" + invoiceid);
        String response = getPollingRequest(polingUrl);
        assertTrue(response.contains(TransactionStatus.PENDING_TRANSACTION.name()));
        checkInput(NelnetTransactionItem.getInvoiceIdKey(), invoiceid);
        checkInput(NelnetTransactionItem.getUserIdKey(), Long.toString(getUserId()));
        checkInput(NelnetTransactionItem.AMOUNT_DUE.name(), total);
        clickElementWithId("process-payment_0");
        response = getPollingRequest(polingUrl);
        assertTrue(response.contains(expectedResponse.name()));
    }

    private String getPollingRequest(URL polingUrl) {
        WebWindow openWindow = webClient.openWindow(polingUrl, "polling" + System.currentTimeMillis());
        return openWindow.getEnclosedPage().getWebResponse().getContentAsString();
    }

}

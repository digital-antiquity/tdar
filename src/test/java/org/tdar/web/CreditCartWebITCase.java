package org.tdar.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { "src/test/resources/tdar.cc.properties" })
public class CreditCartWebITCase extends AbstractAuthenticatedWebTestCase {

    @Test
    public void testCart() {
        gotoPage("/cart/add");
        setInput("invoice.numberOfMb", "2000");
        setInput("invoice.numberOfFiles", "10");
        submitForm();
        logger.info(getPageCode());

        assertTextPresent("100 mb:19:$50:$950");
        assertTextPresent("5- 19:10:$40:$400");
        assertTextPresent("total:$1,350");
        assertCurrentUrlContains("/simple");

        setInput("invoice.paymentMethod", "CREDIT_CARD");
        String invoiceid = getInput("invoice.id").getAttribute("value");
        submitForm();
        assertCurrentUrlContains("process-payment-request");
        clickLinkWithText("click here");
        logger.info(getPageCode());
        
        checkInput(NelnetTransactionItem.getInvoiceIdKey(), invoiceid);
        checkInput(NelnetTransactionItem.getUserIdKey(), Long.toString(getUserId()));
        clickElementWithId("process-payment_0");
    }

}

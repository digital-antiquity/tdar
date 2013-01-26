package org.tdar.web;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;


@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { "src/test/resources/tdar.cc.properties" })
public class AccountUsageWebITCase extends AbstractWebTestCase {

    

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
        assertTextPresent("You must create an account");
        gotoPage("/resource/add");
        assertTextPresent("You must create an account");
        logger.info(getPageText());
        gotoPage("/logout");
    }
    
    
}

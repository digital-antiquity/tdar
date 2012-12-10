package org.tdar.core.bean;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.GenericService;

public class InvoiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    AccountService accountService;

    @Autowired
    GenericService genericService;

    @Test
    @Rollback
    public void testInvoicePricingBasic() {
        Invoice invoice = new Invoice();
        long numberOfFiles = 10L;
        BillingItem item = setupBillingItme(invoice, numberOfFiles);
        Assert.assertTrue(item.getActivity().getMinAllowedNumberOfFiles() < numberOfFiles);
    }

    @Test
    @Rollback
    public void testInvoicePricingInBetween() {
        /* expect that this is activity 2 -- not 1 */
        Invoice invoice = new Invoice();
        long numberOfFiles = 4L;
        BillingItem item = setupBillingItme(invoice, numberOfFiles);
        Assert.assertFalse(item.getActivity().getMinAllowedNumberOfFiles() < numberOfFiles);
        Assert.assertEquals(5L, item.getActivity().getMinAllowedNumberOfFiles().longValue());
    }

    
    
    private BillingItem setupBillingItme(Invoice invoice, long numberOfFiles) {
        invoice.setNumberOfFiles(numberOfFiles);
        BillingItem billingItem = accountService.calculateCheapestActivities(invoice);
        logger.info("{} {}", billingItem, billingItem.getActivity().getMinAllowedNumberOfFiles());
        return billingItem;
    }
}

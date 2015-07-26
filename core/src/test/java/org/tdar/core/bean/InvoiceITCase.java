package org.tdar.core.bean;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.InvoiceService;

public class InvoiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    GenericService genericService;

    @Autowired
    SerializationService serializationService;

    @Test
    @Rollback
    public void testInvoicePricingBasic() {
        Invoice invoice = new Invoice();
        long numberOfFiles = 10L;
        List<BillingItem> items = setupBillingItem(invoice, numberOfFiles, 10L);
        Assert.assertEquals(1, items.size());
        Assert.assertTrue(items.get(0).getActivity().getMinAllowedNumberOfFiles() < numberOfFiles);
    }

    @Test
    @Rollback
    public void testInvoicePricingTiny() {
        Invoice invoice = new Invoice();
        long numberOfFiles = 2L;
        List<BillingItem> items = setupBillingItem(invoice, numberOfFiles, 0L);
        Assert.assertEquals(1, items.size());
        assertNotEquals(items.get(0).getActivity().getMinAllowedNumberOfFiles(), 0);
        assertNotEquals(items.get(0).getActivity().getNumberOfFiles().intValue(), 0);
        Assert.assertEquals(items.get(0).getActivity().getNumberOfFiles().intValue(), 1);
    }

    @Test
    @Rollback
    public void testInvoicePricingMBOnly() {
        Invoice invoice = new Invoice();
        long numberOfFiles = 0L;
        List<BillingItem> items = setupBillingItem(invoice, numberOfFiles, 100L);
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(items.get(0).getActivity().getMinAllowedNumberOfFiles().intValue(), 0);
        Assert.assertEquals(items.get(0).getActivity().getNumberOfMb().intValue(), 100);
    }

    @Test
    @Rollback
    public void testInvoicePricingInBetweenLevels() {
        /* expect that this is activity 2 -- not 1 */
        Invoice invoice = new Invoice();
        long numberOfFiles = 4L;
        List<BillingItem> items = setupBillingItem(invoice, numberOfFiles, 2L);
        Assert.assertEquals(1, items.size());
        Assert.assertFalse(items.get(0).getActivity().getMinAllowedNumberOfFiles() < numberOfFiles);
        Assert.assertEquals(5L, items.get(0).getActivity().getMinAllowedNumberOfFiles().longValue());
    }

    @Test
    @Rollback
    public void testBillingItem() {
        BillingItem item = new BillingItem();
        AbstractIntegrationTestCase.assertInvalid(item, null);
        item.setQuantity(1);
        AbstractIntegrationTestCase.assertInvalid(item, null);
        item.setActivity(new BillingActivity());
        assertTrue(item.isValid());
        assertTrue(item.isValidForController());
    }

    @Test
    @Rollback
    public void testJsonStatus() throws IOException {
        Invoice invoice = new Invoice();
        invoice.setTransactionStatus(TransactionStatus.PREPARED);
        String json = serializationService.convertToJson(invoice);
        assertTrue("status in json", json.indexOf(TransactionStatus.PREPARED.name()) > -1);

    }

    private List<BillingItem> setupBillingItem(Invoice invoice, long numberOfFiles, long numberOfMb) {
        invoice.setNumberOfFiles(numberOfFiles);
        invoice.setNumberOfMb(numberOfMb);
        List<BillingItem> billingItems = invoiceService.calculateCheapestActivities(invoice).getItems();
        logger.info("{} ", billingItems);
        return billingItems;
    }
}

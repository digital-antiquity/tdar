package org.tdar.core.bean;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.dao.external.payment.PaymentMethod;

public class AccountITCase extends AbstractIntegrationTestCase {

    @Test
    @Rollback
    public void testInvoiceBillingItemIncrements() {
        BillingActivityModel model = new BillingActivityModel();
        List<BillingItem> items = new ArrayList<BillingItem>();
        genericService.saveOrUpdate(model);
        items.add(new BillingItem(new BillingActivity("1 hour", 10f, 10, 0L, 0L, 0L, model), 2));
        // public BillingActivity(String name, Float price, Integer numHours, Long numberOfResources, Long numberOfFiles, Long numberOfMb) {
        items.add(new BillingItem(new BillingActivity("1 resource", 1f, 0, 1L, 0L, 0L, model), 2));
        items.add(new BillingItem(new BillingActivity("1 file", 100f, 0, 0L, 2L, 0L, model), 2));
        items.add(new BillingItem(new BillingActivity("1 mb", .1f, 0, 0L, 0L, 3L, model), 2));
        Invoice invoice = new Invoice(getUser(), PaymentMethod.INVOICE, 10L, 0L, items);
        Account account = new Account("my account");
        account.getInvoices().add(invoice);
        // genericService.saveOrUpdate(invoice);

        assertEquals(4L, invoice.getTotalNumberOfFiles().longValue());
        assertEquals(2L, invoice.getTotalResources().longValue());
        assertEquals(6L, invoice.getTotalSpace().longValue());
        assertEquals(222.2, invoice.getCalculatedCost().floatValue(), 1);
        assertEquals(null, invoice.getTotal());

        // account is empty because invoice is not finalized
        assertEquals(0L, account.getTotalNumberOfSpace().longValue());
        assertEquals(0L, account.getTotalNumberOfResources().longValue());
        assertEquals(0L, account.getTotalNumberOfFiles().longValue());

        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.finalize();
        assertEquals(222.2, invoice.getTotal().floatValue(), 1);
        assertEquals(4L, account.getTotalNumberOfFiles().longValue());
        assertEquals(2L, account.getTotalNumberOfResources().longValue());
        assertEquals(6L, account.getTotalNumberOfSpace().longValue());

    }

    
}

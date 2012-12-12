package org.tdar.struts.action;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;

public class BillingAccountControllerITCase extends AbstractResourceControllerITCase {

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    @Test
    @Rollback
    public void testAccountControllerChoicesNoAccount() throws TdarActionException {
        // test fence for Invoice
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.prepare();
        String msg = null;
        try {
            assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertEquals(BillingAccountController.INVOICE_IS_REQURIED, msg);
    }

    @Test
    @Rollback
    public void testAccountControllerChoicesOkNewAccount() throws TdarActionException {
        Invoice invoice = new Invoice(getUser(), PaymentMethod.INVOICE, 10L, 0L, null);
        genericService.saveOrUpdate(invoice);

        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());

    }

    @Test
    @Rollback
    public void testAccountControllerChoicesNoRightsToAssign() throws TdarActionException {
        BillingAccountController controller = generateNewController(BillingAccountController.class);
        Invoice invoice = new Invoice(getUser(), PaymentMethod.INVOICE, 10L, 0L, null);
        genericService.save(invoice);
        String msg = null;
        init(controller, createAndSaveNewPerson());
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        try {
            assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertEquals(BillingAccountController.RIGHTS_TO_ASSIGN_THIS_INVOICE, msg);
    }

    @Test
    @Rollback
    public void testAccountControllerChoicesSelectAccounts() throws TdarActionException {
        Invoice invoice = new Invoice(getUser(), PaymentMethod.INVOICE, 10L, 0L, null);
        genericService.save(invoice);
        Account account = createAccount(getAdminUser());
        BillingAccountController controller = generateNewController(BillingAccountController.class);
        init(controller, getAdminUser());
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        assertEquals(BillingAccountController.SUCCESS, controller.selectAccount());
        assertTrue(controller.getAccounts().contains(account));
    }
}

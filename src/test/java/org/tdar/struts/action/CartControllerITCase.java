package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;

public class CartControllerITCase extends AbstractResourceControllerITCase {

    @Test
    @Rollback
    public void testCartBasicInvalid() throws TdarActionException {
        CartController controller = generateNewInitializedController(CartController.class);
        controller.prepare();
        String result = controller.add();
        assertEquals(TdarActionSupport.SUCCESS, result);
        controller = generateNewInitializedController(CartController.class);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        String save = null;
        TdarActionException tae = null;
        try {
            save = controller.save();
        } catch (TdarActionException tdara) {
            tae = tdara;
        }

        assertTrue(controller.getActionErrors().contains(CartController.SPECIFY_SOMETHING));
        assertEquals(CartController.INPUT, save);
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testCartBasicValid() throws TdarActionException {
        CartController controller = generateNewInitializedController(CartController.class);
        createAndTestInvoiceQuantity(controller, 10L);
    }

    @Test
    @Rollback
    public void testCartPrematurePayment() throws TdarActionException {
        CartController controller = generateNewInitializedController(CartController.class);
        Long invoiceId = createAndTestInvoiceQuantity(controller, 10L);
        controller = generateNewInitializedController(CartController.class);
        controller.setId(invoiceId);
        controller.prepare();
        assertEquals(CartController.ERROR, controller.processPayment());

        controller = generateNewInitializedController(CartController.class);
        controller.setId(invoiceId);
        controller.prepare();
        assertEquals(CartController.ERROR, controller.addPaymentMethod());
    }

    @Test
    @Rollback
    public void testCartBasicAddress() throws TdarActionException {
        CartController controller = generateNewInitializedController(CartController.class);
        setupAndTestBillingAddress(controller);
    }

    @Test
    @Rollback
    public void testCartPayment() throws TdarActionException {
        CartController controller = generateNewInitializedController(CartController.class);
        Long invoiceId = setupAndTestBillingAddress(controller);
        controller = generateNewInitializedController(CartController.class);
        controller.setId(invoiceId);
        controller.prepare();
        String response = controller.addPaymentMethod();
        assertEquals(CartController.SUCCESS, response);
        controller = generateNewInitializedController(CartController.class);
        controller.setId(invoiceId);
        controller.prepare();
        controller.getInvoice().setBillingPhone("1234567890");
        controller.getInvoice().setPaymentMethod(PaymentMethod.CREDIT_CARD);
        response =controller.processPayment();
        assertEquals(CartController.POLLING, response);
    }

    @Test
    @Rollback
    public void testPaymentPermissions() {
        CartController controller = generateNewController(CartController.class);
        init(controller, getBasicUser());
        assertTrue(controller.getAllPaymentMethods().size() == 1);
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.CREDIT_CARD));

        controller = generateNewController(CartController.class);
        init(controller, getAdminUser());
        assertTrue(controller.getAllPaymentMethods().size() > 1);
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.CREDIT_CARD));
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.MANUAL));
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.INVOICE));

    }
    
    private Long setupAndTestBillingAddress(CartController controller) throws TdarActionException {
        Address address = new Address(AddressType.BILLING, "street", "Tempe", "arizona", "q234", "united states");
        Address address2 = new Address(AddressType.MAILING, "2street", "notsurewhere", "california", "q234", "united states");
        getUser().getAddresses().add(address);
        getUser().getAddresses().add(address2);
        genericService.save(getUser());
        Long invoiceId = createAndTestInvoiceQuantity(controller, 10L);
        controller = generateNewInitializedController(CartController.class);
        controller.setId(invoiceId);
        controller.prepare();
        controller.chooseAddress();
        assertNull(controller.getInvoice().getAddress());
        controller.getInvoice().setAddress(address);
        String saveAddress = controller.saveAddress();
        assertEquals(CartController.SUCCESS_ADD_PAY, saveAddress);
        Invoice invoice = genericService.find(Invoice.class, controller.getId());
        assertNotNull(invoice);
        assertNotNull(invoice.getAddress());
        return invoiceId;
    }

    private Long createAndTestInvoiceQuantity(CartController controller, Long numberOfFiles) throws TdarActionException {
        controller.prepare();
        String result = controller.add();
        assertEquals(TdarActionSupport.SUCCESS, result);
        controller = generateNewInitializedController(CartController.class);
        controller.prepare();
        controller.getInvoice().setNumberOfFiles(numberOfFiles);
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();

        assertEquals(CartController.SUCCESS, save);
        assertEquals(CartController.SUCCESS_ADD_ADDRESS, controller.getSaveSuccessPath());
        return controller.getInvoice().getId();
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

}

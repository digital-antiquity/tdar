package org.tdar.struts.action;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
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
        Invoice invoice = createAndTestInvoiceQuantity(controller, 10L);
        controller = generateNewInitializedController(CartController.class);
        controller.setId(invoice.getId());
        controller.prepare();
        assertEquals(CartController.ERROR, controller.processPayment());

        controller = generateNewInitializedController(CartController.class);
        controller.setId(invoice.getId());
        controller.prepare();
        assertEquals(CartController.ERROR, controller.addPaymentMethod());

        
    }

    @Test
    @Rollback
    public void testCartBasicAddress1() throws TdarActionException {
        CartController controller = generateNewInitializedController(CartController.class);
        Address address = new Address();
        address.setCity("Tempe");
        address.setCountry("united states");
        address.setPostal("q234");
        address.setType(AddressType.BILLING);
        getUser().getAddresses().add(address);
        Invoice invoice = createAndTestInvoiceQuantity(controller, 10L);
        controller = generateNewInitializedController(CartController.class);
        controller.setInvoice(invoice);
        controller.prepare();
        controller.chooseAddress();
        assertNull(controller.getInvoice().getAddress());
        
    }

    private Invoice createAndTestInvoiceQuantity(CartController controller, Long numberOfFiles) throws TdarActionException {
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
        return controller.getInvoice();
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

}

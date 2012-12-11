package org.tdar.struts.action;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
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
        assertEquals(CartController.INPUT,save);
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testCartBasicValid() throws TdarActionException {
        CartController controller = generateNewInitializedController(CartController.class);
        controller.prepare();
        String result = controller.add();
        assertEquals(TdarActionSupport.SUCCESS, result);
        controller = generateNewInitializedController(CartController.class);
        controller.prepare();
        controller.getInvoice().setNumberOfFiles(10L);
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();

        assertEquals(CartController.SUCCESS,save);
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

}

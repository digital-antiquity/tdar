package org.tdar.struts.action;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;

public class CartControllerITCase extends AbstractResourceControllerITCase {

    @Test
    @Rollback
    public void testCart() throws TdarActionException {
        CartController controller = generateNewInitializedController(CartController.class);
        String result = controller.add();
        assertEquals(TdarActionSupport.SUCCESS, result);
        
    }
    
    
    
    
    
    
    @Override
    protected TdarActionSupport getController() {
        return null;
    }

}

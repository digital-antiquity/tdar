package org.tdar.struts.action;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;

import static org.junit.Assert.*;

/**
 * $Id$
 * 
 * Tests AccountController's action methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class RedirectMessageITCase extends AbstractControllerITCase {

    @Autowired
    private UserAccountController controller;

    @Autowired
    private AuthenticationAndAuthorizationService authService;

    @Test
    @Rollback
    public void testMessageStore() {
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        String execute = setupValidUserInController(controller);
        Person p = controller.getPerson();
        assertEquals("expecting result to be 'success'", "success", execute);
        assertNotNull("person id should not be null", p.getId());
        assertNotNull("person should have set insitution", p.getInstitution());
        assertEquals("insitution should match", p.getInstitution().getName(), TESTING_AUTH_INSTIUTION);
        assertTrue("person should be registered", p.isRegistered());
        boolean deleteUser = authService.getAuthenticationProvider().deleteUser(p);
        assertTrue("could not delete user", deleteUser);
        assertFalse("there should be an action message on successful creation ", controller.getActionMessages().isEmpty());
        assertEquals(UserAccountController.SUCCESSFUL_REGISTRATION_MESSAGE, controller.getActionMessages().iterator().next());
        assertTrue("no errors expected", controller.getActionErrors().isEmpty());
    }
    
   
    @Override
    protected TdarActionSupport getController() {
        return controller;
    }
}

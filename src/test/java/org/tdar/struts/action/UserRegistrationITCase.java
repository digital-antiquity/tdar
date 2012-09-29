package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.CrowdService;

/**
 * $Id$
 * 
 * Exercises the DatasetController's action methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class UserRegistrationITCase extends AbstractControllerITCase {

    private static final String TESTING_EMAIL = "test2asd@test2.com";
    private static final String TESTING_AUTH_INSTIUTION = "testing auth instiution";

    @Autowired
    private AccountController controller;

    @Autowired
    private CrowdService crowdService;

    @Test
    @Rollback(true)
    public void testDuplicateUser() {
        Person p = new Person();
        p.setEmail("allen.lee@asu.edu");
        controller.setPerson(p);
        String execute = controller.create();
        assertTrue("Expected controller to return an error, email exists", execute.equalsIgnoreCase("error"));
        logger.info(execute + " : " + controller.getActionMessages());
        assertEquals("expecting valid message", controller.getActionErrors().iterator().next(), AccountController.ERROR_ALREADY_REGISTERED);

    }

    @Test
    @Rollback(true)
    public void testExistingAuthorWithoutLogin() {
        Person p = new Person();
        p.setEmail("tiffany.clark@asu.edu");
        p.setFirstName("Tiffany");
        p.setLastName("Clark");

        // cleanup crowd if we need to...
        crowdService.deleteUser(p);

        controller.setPassword("password");
        controller.setPerson(p);
        controller.setServletRequest(getServletRequest());
        controller.setServletResponse(getServletResponse());
        String execute = controller.create();
        assertEquals("expecting result to be 'success'", "success", execute);

        boolean deleteUser = crowdService.deleteUser(p);
        assertTrue("could not delete user", deleteUser);
    }

    @Test
    @Rollback(true)
    public void testNewUser() {
        Person p = new Person();
        p.setEmail("testuser@testuser.com");
        p.setFirstName("Testing auth");
        p.setLastName("User");
        p.setPhone("212 000 0000");
        p.setContributor(true);
        p.setContributorReason("because");
        p.setRpa(true);

        // cleanup crowd if we need to...
        crowdService.deleteUser(p);

        controller.setRequestingContributorAccess(true);
        controller.setInstitutionName(TESTING_AUTH_INSTIUTION);
        controller.setPassword("password");
        controller.setPerson(p);
        controller.setServletRequest(getServletRequest());
        controller.setServletResponse(getServletResponse());
        String execute = controller.create();
        assertEquals("expecting result to be 'success'", "success", execute);
        assertNotNull("person id should not be null", p.getId());
        assertNotNull("person should have set insitution", p.getInstitution());
        assertEquals("insitution should match", p.getInstitution().getName(), TESTING_AUTH_INSTIUTION);
        assertTrue("person should be registered", p.isRegistered());
        boolean deleteUser = crowdService.deleteUser(p);
        assertTrue("could not delete user", deleteUser);

    }

    @Test
    public void testEmailRegistration() {
        assertFalse("email should not exist", controller.isEmailRegistered("testuser@testuser.com"));
        assertFalse("email should exist but not be registered", controller.isEmailRegistered("tiffany.clark@asu.edu"));
        assertTrue("email should exist and be registered", controller.isEmailRegistered("admin@tdar.org"));
    }

    @Test
    @Rollback(true)
    public void testPrepareValidate() {
        controller = generateNewController(AccountController.class);
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail("test@tdar.org");
        assertTrue(p.getId().equals(-1L));
        controller.validate();
//        assertEquals("expecting user existing", AccountController.ERROR_ALREADY_REGISTERED, getFirstFieldError());
        assertErrorPresent(AccountController.ERROR_ALREADY_REGISTERED);
    }

    private String getFirstFieldError() {
        return controller.getActionErrors().iterator().next();
    }
    
    private void assertErrorPresent(String expectedError) {
        Set<String> errorSet = new HashSet<String>(controller.getActionErrors());
        assertTrue("Expecting action error:" + expectedError, errorSet.contains(expectedError));
    }
    
    
    private void assertErrorNotPresent(String error) {
        Set<String> errorSet = new HashSet<String>(controller.getActionErrors());
        assertFalse("Expecting action error:" + error, errorSet.contains(error));
    }
    

    @Test
    @Rollback(true)
    public void testPrepareValidate2() {
        controller = generateNewController(AccountController.class);
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setPerson(p);
        controller.validate();
        assertErrorPresent(AccountController.ERROR_CHOOSE_PASSWORD);
        assertErrorPresent(AccountController.ERROR_CONFIRM_EMAIL);
        assertErrorNotPresent(AccountController.ERROR_CONFIRM_PASSWORD);
        
    }

    @Test
    @Rollback(true)
    public void testPrepareValidate25() {
        controller = generateNewController(AccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.validate();
//        assertEquals("expecting confirm email", AccountController.ERROR_CONFIRM_EMAIL, getFirstFieldError());
        assertErrorPresent(AccountController.ERROR_CONFIRM_EMAIL);
    }

    @Test
    @Rollback(true)
    public void testPrepareValidate3() {
        controller = generateNewController(AccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setConfirmEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.validate();
//        assertEquals("expecting confirm password", AccountController.ERROR_CONFIRM_PASSWORD, getFirstFieldError());
        assertErrorPresent(AccountController.ERROR_CONFIRM_PASSWORD);
    }

    @Test
    @Rollback(true)
    public void testPrepareValidate4() {
        controller = generateNewController(AccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setConfirmEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.setConfirmPassword("password_");
        controller.validate();
//        assertEquals("expecting matching passwords", AccountController.ERROR_PASSWORDS_DONT_MATCH, getFirstFieldError());
        assertErrorPresent(AccountController.ERROR_PASSWORDS_DONT_MATCH);
    }

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    @Override
    protected Person getUser() {
        return null;
    }
}

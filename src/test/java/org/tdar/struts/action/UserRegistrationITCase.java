package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;

/**
 * $Id$
 * 
 * Tests AccountController's action methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class UserRegistrationITCase extends AbstractControllerITCase {

    private static final String REASON = "because";
    private static final String TESTING_EMAIL = "test2asd@test2.com";
    private static final String TESTING_AUTH_INSTIUTION = "testing auth instiution";

    @Autowired
    private AccountController controller;

    @Autowired
    private AuthenticationAndAuthorizationService authService;

    @Test
    @Rollback
    public void testDuplicateUser() {
        Person p = new Person();
        p.setEmail("allen.lee@asu.edu");
        controller.setPerson(p);
        String execute = controller.create();
        assertEquals("Expected controller to return an error, email exists", TdarActionSupport.ERROR, execute);
        logger.info(execute + " : " + controller.getActionMessages());
        assertEquals("expecting valid message", controller.getActionErrors().iterator().next(), AccountController.ERROR_ALREADY_REGISTERED);

    }

    @Test
    @Rollback
    public void testExistingAuthorWithoutLogin() {
        Person p = new Person();
        p.setEmail("tiffany.clark@asu.edu");
        p.setFirstName("Tiffany");
        p.setLastName("Clark");

        // cleanup crowd if we need to...
        authService.getAuthenticationProvider().deleteUser(p);

        controller.setPassword("password");
        controller.setPerson(p);
        controller.setServletRequest(getServletRequest());
        controller.setServletResponse(getServletResponse());
        String execute = controller.create();
        assertEquals(TdarActionSupport.SUCCESS, execute);

        boolean deleteUser = authService.getAuthenticationProvider().deleteUser(p);
        assertTrue("could not delete user", deleteUser);
    }

    @Test
    @Rollback
    public void testNewUser() {
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        String execute = setupValidUserInController();
        Person p = controller.getPerson();
        assertEquals("expecting result to be 'success'", "success", execute);
        assertNotNull("person id should not be null", p.getId());
        assertNotNull("person should have set insitution", p.getInstitution());
        assertEquals("insitution should match", p.getInstitution().getName(), TESTING_AUTH_INSTIUTION);
        assertTrue("person should be registered", p.isRegistered());
        boolean deleteUser = authService.getAuthenticationProvider().deleteUser(p);
        assertTrue("could not delete user", deleteUser);
        assertTrue("no errors expected", controller.getActionErrors().size() == 0);
    }

    @Test
    @Rollback(false)
    public void testEmailWithPlusSign() {
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        String execute = setupValidUserInController("test+user@gmail.com");
        final Person p = controller.getPerson();
        final ContributorRequest request = controller.getContributorRequest();
        final AuthenticationToken token = controller.getSessionData().getAuthenticationToken();
        assertNotNull(request);
        assertEquals(p, request.getApplicant());
        assertEquals(p, token.getPerson());
        assertEquals(REASON, request.getContributorReason());
        assertEquals("expecting result to be 'success'", "success", execute);
        assertNotNull("person id should not be null", p.getId());
        assertNotNull("person should have set insitution", p.getInstitution());
        assertEquals("insitution should match", p.getInstitution().getName(), TESTING_AUTH_INSTIUTION);
        assertTrue("person should be registered", p.isRegistered());
        assertTrue("no errors expected", controller.getActionErrors().isEmpty());
        assertTrue("email should contain plus sign", p.getEmail().contains("+"));
        setVerifyTransactionCallback(new TransactionCallback<Person>() {
            @Override
            public Person doInTransaction(TransactionStatus status) {
                LoginAction loginAction = generateNewInitializedController(LoginAction.class);
                loginAction.setLoginEmail(p.getEmail());
                loginAction.setLoginPassword("password");
                assertEquals(TdarActionSupport.AUTHENTICATED, loginAction.authenticate());
                Person person = genericService.find(Person.class, p.getId());
                boolean deleteUser = authService.getAuthenticationProvider().deleteUser(person);
                assertTrue("could not delete user", deleteUser);
                genericService.delete(genericService.findAll(AuthenticationToken.class));
                genericService.delete(request);
                genericService.synchronize();
                genericService.delete(person);
                return null;
            }
        });
    }

    @Test
    @Rollback
    public void testUserCreatedTooFast() {
        controller.setTimeCheck(System.currentTimeMillis() - 1000);
        String execute = setupValidUserInController();
        assertEquals("expecting result to be 'success'", "success", execute);
        String firstError = getFirstFieldError();
        assertTrue(firstError.equals(AccountController.COULD_NOT_AUTHENTICATE_AT_THIS_TIME));
    }

    @Test
    @Rollback
    public void testUserCreatedTooSlow() {
        controller.setTimeCheck(System.currentTimeMillis() + 1000 * 61);
        String execute = setupValidUserInController();
        assertEquals("expecting result to be 'success'", "success", execute);
        String firstError = getFirstFieldError();
        assertTrue(firstError.equals(AccountController.COULD_NOT_AUTHENTICATE_AT_THIS_TIME));
    }

    @Test
    @Rollback
    public void testUserCreatedFallsIntoHoneypot() {
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        controller.setComment("could you help me?  I love your site");
        String execute = setupValidUserInController();
        assertEquals("expecting result to be 'success'", "success", execute);
        String firstError = getFirstFieldError();
        assertTrue(firstError.equals(AccountController.COULD_NOT_AUTHENTICATE_AT_THIS_TIME));
    }

    private String setupValidUserInController() {
        return setupValidUserInController("testuser@example.com");
    }

    private String setupValidUserInController(String email) {
        Person p = new Person();
        p.setEmail(email);
        p.setFirstName("Testing auth");
        p.setLastName("User");
        p.setPhone("212 000 0000");
        p.setContributor(true);
        p.setContributorReason(REASON);
        p.setRpa(true);

        // cleanup crowd if we need to...
        authService.getAuthenticationProvider().deleteUser(p);

        controller.setRequestingContributorAccess(true);
        controller.setInstitutionName(TESTING_AUTH_INSTIUTION);
        controller.setPassword("password");
        controller.setConfirmPassword("password");
        controller.setConfirmEmail(p.getEmail());
        controller.setPerson(p);
        controller.setServletRequest(getServletRequest());
        controller.setServletResponse(getServletResponse());
        controller.validate();
        String execute = controller.create();

        return execute;
    }

    @Test
    public void testEmailRegistration() {
        assertFalse("email should not exist", controller.isEmailRegistered("testuser@testuser.com"));
        assertFalse("email should exist but not be registered", controller.isEmailRegistered("tiffany.clark@asu.edu"));
        assertTrue("email should exist and be registered", controller.isEmailRegistered("admin@tdar.org"));
    }

    @Test
    @Rollback
    public void testPrepareValidate() {
        controller = generateNewController(AccountController.class);
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail("test@tdar.org");
        assertTrue(p.getId().equals(-1L));
        controller.validate();
        assertTrue("expecting user existing", controller.getActionErrors().contains(AccountController.ERROR_ALREADY_REGISTERED));
    }

    private String getFirstFieldError() {
        return controller.getActionErrors().iterator().next();
    }

    @Test
    @Rollback
    public void testPrepareValidate2() {
        controller = generateNewController(AccountController.class);
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setPerson(p);
        controller.validate();
        assertTrue("expecting password", controller.getActionErrors().contains(AccountController.ERROR_CHOOSE_PASSWORD));
    }

    @Test
    @Rollback
    public void testPrepareValidate25() {
        controller = generateNewController(AccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.validate();
        assertTrue("expecting confirm email", controller.getActionErrors().contains(AccountController.ERROR_CONFIRM_EMAIL));
    }

    @Test
    @Rollback
    public void testPrepareValidate3() {
        controller = generateNewController(AccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setConfirmEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.validate();
        assertTrue("expecting confirm password", controller.getActionErrors().contains(AccountController.ERROR_CONFIRM_PASSWORD));
    }

    @Test
    @Rollback
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
        assertTrue("expecting matching passwords", controller.getActionErrors().contains(AccountController.ERROR_PASSWORDS_DONT_MATCH));
    }

    @Test
    @Rollback
    public void testPrepareValidateWithCaseDifferenceInEmail() {
        controller = generateNewController(AccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        p.setFirstName("First");
        p.setLastName("last");
        controller.setTimeCheck(System.currentTimeMillis() - 5000);
        controller.setConfirmEmail(TESTING_EMAIL.toUpperCase());
        controller.setPassword("password");
        controller.setConfirmPassword("password_");
        controller.validate();
        assertTrue("expecting matching passwords", controller.getActionErrors().contains(AccountController.ERROR_PASSWORDS_DONT_MATCH));
        assertEquals(1, controller.getActionErrors().size());
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

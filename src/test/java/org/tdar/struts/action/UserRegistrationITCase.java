package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.MockMailSender;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.web.SessionData;

import freemarker.template.Configuration;

/**
 * $Id$
 * 
 * Tests AccountController's action methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 * @param <E>
 */
public class UserRegistrationITCase extends AbstractControllerITCase {

    static final String REASON = "because";
    private static final String TESTING_EMAIL = "test2asd@test2.com";
    static final String TESTING_AUTH_INSTIUTION = "testing auth instiution";

    @Autowired
    private Configuration freemarkerConfiguration;

    @Autowired
    AuthenticationAndAuthorizationService authService;

    private List<Person> crowdPeople = new ArrayList<Person>();

    @After
    public void deleteCreateUsersFromCrowd() {
        for (Person person : crowdPeople) {
            if (StringUtils.isNotBlank(person.getEmail())) {
                if (!authService.getAuthenticationProvider().deleteUser(person)) {
                    logger.warn("Could not remove user {} after running test {}.{}()",
                            new Object[] { person, getClass().getSimpleName(), testName.getMethodName() });
                }
            }
        }
        crowdPeople.clear();
    }

    @Test
    @Rollback
    public void testDuplicateUser() {
        Person p = new Person();
        p.setUsername("allen.lee@asu.edu");
        p.setEmail("allen.lee1@asu.edu");
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        String execute = controller.create();
        assertEquals("Expected controller to return an error, email exists", TdarActionSupport.ERROR, execute);
        logger.info(execute + " : " + controller.getActionMessages());
        assertEquals("expecting valid message", UserAccountController.ERROR_USERNAME_ALREADY_REGISTERED, controller.getActionErrors().iterator().next());
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testDuplicateEmail() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        Person p = new Person();
        p.setUsername("allen.lee");
        p.setEmail("allen.lee@asu.edu");
        controller.setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        String execute = controller.create();
        assertEquals("Expected controller to return an error, email exists", TdarActionSupport.ERROR, execute);
        logger.info(execute + " : " + controller.getActionMessages());
        assertEquals("expecting valid message", UserAccountController.ERROR_DUPLICATE_EMAIL, controller.getActionErrors().iterator().next());
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testExistingAuthorWithoutLogin() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        Person p = new Person();
        p.setEmail("tiffany.clark@asu.edu");
        p.setUsername("tiffany.clark@asu.edu");
        p.setFirstName("Tiffany");
        p.setLastName("Clark");

        // cleanup crowd if we need to...
        authService.getAuthenticationProvider().deleteUser(p);

        controller.setPassword("password");
        controller.setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        controller.setServletResponse(getServletResponse());
        String execute = controller.create();
        assertEquals(TdarActionSupport.SUCCESS, execute);

        boolean deleteUser = authService.getAuthenticationProvider().deleteUser(p);
        assertTrue("could not delete user", deleteUser);
    }

    @Test
    @Rollback
    public void testExistingDraftUserWithoutLogin() {
        String email = "tiffany.clark@asu.edu";
        Person p = testCreatePerson(email, Status.ACTIVE, TdarActionSupport.SUCCESS);
        assertEquals(Status.ACTIVE, p.getStatus());

        p = testCreatePerson(email, Status.DRAFT, TdarActionSupport.SUCCESS);
        assertEquals(Status.ACTIVE, p.getStatus());

        p = testCreatePerson(email, Status.DELETED, TdarActionSupport.ERROR);
        assertEquals(Status.DELETED, p.getStatus());

        p = testCreatePerson(email, Status.FLAGGED, TdarActionSupport.ERROR);
        assertEquals(Status.FLAGGED, p.getStatus());
        setIgnoreActionErrors(true);
    }

    private Person testCreatePerson(String email, Status status, String success) {
        Person p = new Person();
        p.setEmail(email);
        p.setUsername(email);
        p.setFirstName("Tiffany");
        p.setLastName("Clark");

        Person findByEmail = entityService.findByEmail(email);
        findByEmail.setStatus(status);
        findByEmail.setUsername(null);
        findByEmail.setRegistered(false);
        genericService.saveOrUpdate(findByEmail);
        genericService.synchronize();
        findByEmail = null;
        // cleanup crowd if we need to...
        authService.getAuthenticationProvider().deleteUser(p);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        controller.setPassword("password");
        controller.setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        controller.setServletResponse(getServletResponse());
        String execute = controller.create();
        assertEquals(success, execute);

        findByEmail = entityService.findByEmail(email);
        genericService.refresh(findByEmail);

        boolean deleteUser = authService.getAuthenticationProvider().deleteUser(p);
        assertTrue("could not delete user", deleteUser);

        return findByEmail;
    }

    @Test
    @Rollback
    public void testNewUser() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

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
        assertTrue("no errors expected", controller.getActionErrors().size() == 0);
    }

    @Test
    @Rollback
    public void testInvalidUsers() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        List<String> emails = Arrays.asList("a", "a b", "adam brin", "abcd1234!", "http://", "!#####/bin/ls");
        for (String email : emails) {
            logger.info("TRYING =======> {}", email);
            controller.setTimeCheck(System.currentTimeMillis() - 10000);
            String execute = setupValidUserInController(controller, email);
            // assertFalse("user " + email + " succeeded??", TdarActionSupport.SUCCESS.equals(execute));
            logger.info("errors:{}", controller.getActionErrors());
            assertTrue(controller.getActionErrors().size() > 0);
        }
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testValidUsers() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        List<String> emails = Arrays.asList("aaaa-bbbbbb.ccccccc-ddddd@eeeeeee.ffff.hh");
        
        for (String email : emails) {
            assertTrue(authenticationAndAuthorizationService.isValidEmail(email));
            assertTrue(authenticationAndAuthorizationService.isValidUsername(email));
            logger.info("TRYING =======> {}", email);
            controller.setTimeCheck(System.currentTimeMillis() - 10000);
            String execute = setupValidUserInController(controller, email);
            // assertFalse("user " + email + " succeeded??", TdarActionSupport.SUCCESS.equals(execute));
            logger.info("errors:{}", controller.getActionErrors());
            assertFalse(controller.getActionErrors().size() > 0);
        }
    }

    @Test
    @Rollback
    public void testRegistrationEmailSent() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        setupValidUserInController(controller);
        Map<String, Object> welcomeEmailValues = controller.getWelcomeEmailValues();
        MockMailSender mms = (MockMailSender) controller.getEmailService().getMailSender();
        ArrayList<SimpleMailMessage> messages = mms.getMessages();
        // we assume that the message sent was the registration one. If it wasn't we will soon find out...
        assertTrue("Registration email was not sent.", messages.size() == 1);
        String messageText = messages.get(0).getText();
        assertTrue(StringUtils.isNotBlank(messageText));
    }

    @Test
    @Rollback(false)
    public void testEmailWithPlusSign() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        String email = "test+user@gmail.com";
        Person findByEmail = entityService.findByEmail(email);
        if (findByEmail != null) { // this should rarely happen, but it'll clear out the test before we run it if the last time it failed...
            genericService.delete(entityService.findContributorRequest(findByEmail));
            genericService.delete(findByEmail);
            authService.getAuthenticationProvider().deleteUser(findByEmail);
        }
        String execute = setupValidUserInController(controller, email);
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
                LoginController loginAction = generateNewInitializedController(LoginController.class);
                loginAction.setLoginUsername(p.getEmail());
                loginAction.setLoginPassword("password");
                loginAction.setServletRequest(getServletPostRequest());
                assertEquals(TdarActionSupport.AUTHENTICATED, loginAction.authenticate());
                Person person = genericService.find(Person.class, p.getId());
                boolean deleteUser = authService.getAuthenticationProvider().deleteUser(person);
                assertTrue("could not delete user", deleteUser);
                genericService.delete(genericService.findAll(AuthenticationToken.class));
                genericService.delete(request);
                genericService.delete(person);
                return null;
            }
        });
    }

    @Test
    @Rollback
    public void testUserCreatedTooFast() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setTimeCheck(System.currentTimeMillis() - 1000);
        String execute = setupValidUserInController(controller);
        assertEquals("expecting result to be 'null' (validate should fail)",null, execute);
        String firstError = getFirstFieldError(controller);
        assertTrue(firstError.equals(UserAccountController.COULD_NOT_AUTHENTICATE_AT_THIS_TIME));
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testUserCreatedTooSlow() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setTimeCheck(System.currentTimeMillis() + 1000 * 61);
        String execute = setupValidUserInController(controller);
        assertEquals("expecting result to be 'null' (validate should fail)",null, execute);
        String firstError = getFirstFieldError(controller);
        assertTrue(firstError.equals(UserAccountController.COULD_NOT_AUTHENTICATE_AT_THIS_TIME));
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testUserCreatedFallsIntoHoneypot() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        controller.setComment("could you help me?  I love your site");
        String execute = setupValidUserInController(controller);
        assertEquals("expecting result to be 'null' (validate should fail)",null, execute);
        String firstError = getFirstFieldError(controller);
        assertTrue(firstError.equals(UserAccountController.COULD_NOT_AUTHENTICATE_AT_THIS_TIME));
        setIgnoreActionErrors(true);
    }

    @Test
    public void testEmailRegistration() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        assertFalse("email should not exist", controller.isUsernameRegistered("testuser@testuser.com"));
        assertFalse("email should exist but not be registered", controller.isUsernameRegistered("tiffany.clark@asu.edu"));
        assertTrue("email should exist and be registered", controller.isUsernameRegistered("admin@tdar.org"));
    }

    @Test
    @Rollback
    public void testPrepareValidate() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.prepare();
        Person p = controller.getPerson();

        p.setEmail("test@tdar.org");
        assertTrue(p.getId().equals(-1L));
        controller.validate();
        assertTrue("expecting user existing", controller.getActionErrors().contains(UserAccountController.ERROR_USERNAME_ALREADY_REGISTERED));
        setIgnoreActionErrors(true);
    }

    private String getFirstFieldError(UserAccountController controller) {
        return controller.getActionErrors().iterator().next();
    }

    @Test
    @Rollback
    public void testPrepareValidate2() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setPerson(p);
        controller.validate();
        assertTrue("expecting password", controller.getActionErrors().contains(UserAccountController.ERROR_CHOOSE_PASSWORD));
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testPrepareValidate25() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        p.setUsername(TESTING_EMAIL);
        controller.setPassword("password");
        controller.validate();
        assertTrue("expecting confirm email", controller.getActionErrors().contains(UserAccountController.ERROR_CONFIRM_EMAIL));
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testPrepareValidate3() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setConfirmEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.validate();
        assertTrue("expecting confirm password", controller.getActionErrors().contains(UserAccountController.ERROR_CONFIRM_PASSWORD));
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testPrepareValidate4() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setConfirmEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.setConfirmPassword("password_");
        controller.validate();
        assertTrue("expecting matching passwords", controller.getActionErrors().contains(UserAccountController.ERROR_PASSWORDS_DONT_MATCH));
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback
    public void testPrepareValidateWithCaseDifferenceInEmail() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        p.setUsername(TESTING_EMAIL);
        p.setFirstName("First");
        p.setLastName("last");
        controller.setTimeCheck(System.currentTimeMillis() - 5000);
        controller.setConfirmEmail(TESTING_EMAIL.toUpperCase());
        controller.setPassword("password");
        controller.setConfirmPassword("password_");
        controller.validate();
        assertTrue("expecting matching passwords", controller.getActionErrors().contains(UserAccountController.ERROR_PASSWORDS_DONT_MATCH));
        assertEquals(1, controller.getActionErrors().size());
        setIgnoreActionErrors(true);
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    @Override
    protected Person getUser() {
        return null;
    }

    @Test
    @Rollback
    // register new account with mixed-case username, and ensure that user can successfully login
    public void testMixedCaseUsername() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        String username = "BobLoblaw";
        String password = "super.secret";

        Person p = newPerson();

        p.setEmail("foo.bar@mailinator.com");
        p.setUsername(username);
        p.setFirstName("Testing auth");
        p.setLastName("User");
        p.setPhone("212 000 0000");
        p.setContributor(true);
        p.setContributorReason(REASON);
        p.setRpaNumber("234");

        // create account, making sure the controller knows we're legit.
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        String accountResponse = setupValidUserInController(controller, p, "super.secret");
        logger.info(accountResponse);
        logger.info("errors: {}", controller.getActionErrors());
        assertEquals("user should have been successfully created", UserAccountController.SUCCESS, accountResponse);

        // okay, now try to "login": mock a POST request with empty session
        LoginController loginAction = generateNewController(LoginController.class);
        loginAction.setLoginUsername(p.getUsername());
        loginAction.setLoginPassword(password);
        loginAction.setServletRequest(httpServletPostRequest);
        loginAction.setSessionData(new SessionData());

        String loginResponse = loginAction.authenticate();
        assertEquals("login should have been successful", LoginController.AUTHENTICATED, loginResponse);
    }

    // return a new person reference. an @after method will try to delete this person from crowd
    private Person newPerson() {
        Person person = new Person();
        crowdPeople.add(person);
        return person;
    }
}

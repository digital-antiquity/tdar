package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.struts.action.resource.DocumentController;
import org.tdar.struts.action.resource.ResourceController;
import org.tdar.utils.MessageHelper;
import org.tdar.web.SessionData;

import com.opensymphony.xwork2.Action;
import com.vividsolutions.jts.util.Assert;

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

    @Autowired
    private SendEmailProcess sendEmailProcess;

    private List<TdarUser> crowdPeople = new ArrayList<TdarUser>();

    @After
    public void deleteCreateUsersFromCrowd() {
        for (TdarUser person : crowdPeople) {
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
        TdarUser p = new TdarUser("Allen","Lee","allen.lee@asu.edu");
        p.setUsername(p.getEmail());
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        String execute = controller.create();
        assertEquals("Expected controller to return an error, email exists", Action.ERROR, execute);
        logger.info(execute + " : " + controller.getActionMessages());
        assertEquals("expecting valid message", MessageHelper.getMessage("userAccountController.error_username_already_registered"), controller
                .getActionErrors().iterator().next());
    }

    
    @Test
    @Rollback
    public void testUnContributorStatus() throws TdarActionException {
        TdarUser person = createAndSaveNewPerson();

        person.setContributor(false);
        ResourceController controller = generateNewInitializedController(ResourceController.class, person);
        Assert.equals(ResourceController.CONTRIBUTOR, controller.execute());
        
        DocumentController controller2 = generateNewInitializedController(DocumentController.class, person);
        controller2.prepare();
        Assert.equals(ResourceController.CONTRIBUTOR, controller2.add());
        person.setContributor(true);
        Assert.equals(ResourceController.SUCCESS, controller2.add());

    }
    
    @Test
    @Rollback
    public void testDuplicateEmail() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        TdarUser p = new TdarUser();
        p.setUsername("allen.lee");
        p.setFirstName("Allen");
        p.setLastName("lee");
        p.setEmail("allen.lee@asu.edu");
        controller.setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        String execute = controller.create();
        assertEquals("Expected controller to return an error, email exists", Action.ERROR, execute);
        logger.info(execute + " : " + controller.getActionMessages());
        assertEquals("expecting valid message", MessageHelper.getMessage("userAccountController.error_duplicate_email"), controller.getActionErrors()
                .iterator().next());
    }

    @Test
    @Rollback
    public void testExistingAuthorWithoutLogin() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        TdarUser p = new TdarUser();
        p.setEmail("tiffany.clark@asu.edu");
        p.setUsername("tiffany.clark@asu.edu");
        p.setFirstName("Tiffany");
        p.setLastName("Clark");

        // cleanup crowd if we need to...
        authService.getAuthenticationProvider().deleteUser(p);
        genericService.synchronize();
        controller.setPassword("password");
        controller.setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        controller.setServletResponse(getServletResponse());
        String execute = controller.create();
        assertEquals(Action.SUCCESS, execute);

        boolean deleteUser = authService.getAuthenticationProvider().deleteUser(p);
        assertTrue("could not delete user", deleteUser);
    }

    @Test
    @Rollback
    public void testExistingDraftUserWithoutLogin() {
        setIgnoreActionErrors(true);
        String email = "tiffany.clark@asu.edu";
        TdarUser p = testCreatePerson(email, Status.ACTIVE, Action.SUCCESS);
        assertEquals(Status.ACTIVE, p.getStatus());

        p = testCreatePerson(email, Status.DRAFT, Action.SUCCESS);
        assertEquals(Status.ACTIVE, p.getStatus());

        p = testCreatePerson(email, Status.DELETED, Action.ERROR);
        assertEquals(Status.DELETED, p.getStatus());

        p = testCreatePerson(email, Status.FLAGGED, Action.ERROR);
        assertEquals(Status.FLAGGED, p.getStatus());
    }

    private TdarUser testCreatePerson(String email, Status status, String success) {
        TdarUser p = new TdarUser();
        p.setEmail(email);
        p.setUsername(email);
        p.setFirstName("Tiffany");
        p.setLastName("Clark");

        TdarUser findByEmail = entityService.findUserByEmail(email);
        if (findByEmail != null) {
            findByEmail.setStatus(status);
            findByEmail.setUsername(null);
    
            genericService.saveOrUpdate(findByEmail);
        }
        evictCache();
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

        findByEmail = (TdarUser)entityService.findByEmail(email);
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
        TdarUser p = controller.getPerson();
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
        setIgnoreActionErrors(true);
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
        sendEmailProcess.execute();
        ArrayList<SimpleMailMessage> messages = mockMailSender.getMessages();
        // we assume that the message sent was the registration one. If it wasn't we will soon find out...
        assertTrue("Registration email was not sent - " + messages.size(), messages.size() == 1);
        String messageText = messages.get(0).getText();
        assertTrue(StringUtils.isNotBlank(messageText));
    }

    @Test
    @Rollback(false)
    public void testEmailWithPlusSign() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        String email = "test++++++user@gmail.com";
        Person findByEmail = entityService.findByEmail(email);
        if (findByEmail != null) { // this should rarely happen, but it'll clear out the test before we run it if the last time it failed...
            genericService.delete(findByEmail);
        }
        evictCache();
        String execute = setupValidUserInController(controller, email);
        final TdarUser p = controller.getPerson();
        final AuthenticationToken token = controller.getSessionData().getAuthenticationToken();
        assertEquals(p, token.getPerson());
        assertEquals("expecting result to be 'success'", "success", execute);
        assertNotNull("person id should not be null", p.getId());
        assertNotNull("person should have set insitution", p.getInstitution());
        assertEquals("insitution should match", p.getInstitution().getName(), TESTING_AUTH_INSTIUTION);
        assertTrue("person should be registered", p.isRegistered());
        assertTrue("no errors expected", controller.getActionErrors().isEmpty());
        assertTrue("email should contain plus sign", p.getEmail().contains("+"));
        setVerifyTransactionCallback(new TransactionCallback<Person>() {
            @Override
            public TdarUser doInTransaction(TransactionStatus status) {
                LoginController loginAction = generateNewInitializedController(LoginController.class);
                loginAction.setLoginUsername(p.getEmail());
                loginAction.setLoginPassword("password");
                loginAction.setServletRequest(getServletPostRequest());
                assertEquals(TdarActionSupport.AUTHENTICATED, loginAction.authenticate());
                TdarUser person = genericService.find(TdarUser.class, p.getId());
                boolean deleteUser = authService.getAuthenticationProvider().deleteUser(person);
                assertTrue("could not delete user", deleteUser);
                genericService.delete(genericService.findAll(AuthenticationToken.class));
                genericService.delete(person);
                return null;
            }
        });
    }

    @Test
    @Rollback
    public void testUserCreatedTooFast() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setTimeCheck(System.currentTimeMillis() - 1000);
        String execute = setupValidUserInController(controller);
        assertEquals("expecting result to be 'null' (validate should fail)", null, execute);
        String firstError = getFirstFieldError(controller);
        assertTrue(firstError.equals(MessageHelper.getMessage("userAccountController.could_not_authenticate_at_this_time")));
    }

    @Test
    @Rollback
    public void testUserCreatedTooSlow() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setTimeCheck(System.currentTimeMillis() + (1000 * 61));
        String execute = setupValidUserInController(controller);
        assertEquals("expecting result to be 'null' (validate should fail)", null, execute);
        String firstError = getFirstFieldError(controller);
        assertTrue(firstError.equals(MessageHelper.getMessage("userAccountController.could_not_authenticate_at_this_time")));
    }

    @Test
    @Rollback
    public void testUserCreatedFallsIntoHoneypot() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        controller.setComment("could you help me?  I love your site");
        String execute = setupValidUserInController(controller);
        assertEquals("expecting result to be 'null' (validate should fail)", null, execute);
        String firstError = getFirstFieldError(controller);
        assertTrue(firstError.equals(MessageHelper.getMessage("userAccountController.could_not_authenticate_at_this_time")));
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
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.prepare();
        Person p = controller.getPerson();

        p.setEmail("test@tdar.org");
        assertTrue(p.getId().equals(-1L));
        controller.validate();
        assertTrue("expecting user existing",
                controller.getActionErrors().contains(MessageHelper.getMessage("userAccountController.error_username_already_registered")));
    }

    private String getFirstFieldError(UserAccountController controller) {
        return controller.getActionErrors().iterator().next();
    }

    @Test
    @Rollback
    public void testPrepareValidate2() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.prepare();
        TdarUser p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setPerson(p);
        controller.validate();
        assertTrue("expecting password", controller.getActionErrors().contains(MessageHelper.getMessage("userAccountController.error_choose_password")));
    }

    @Test
    @Rollback
    public void testPrepareValidate25() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        TdarUser p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        p.setUsername(TESTING_EMAIL);
        controller.setPassword("password");
        controller.validate();
        assertTrue("expecting confirm email", controller.getActionErrors().contains(MessageHelper.getMessage("userAccountController.error_confirm_email")));
    }

    @Test
    @Rollback
    public void testPrepareValidate3() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setConfirmEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.validate();
        assertTrue("expecting confirm password", controller.getActionErrors()
                .contains(MessageHelper.getMessage("userAccountController.error_confirm_password")));
    }

    @Test
    @Rollback
    public void testPrepareValidate4() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        Person p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.setConfirmEmail(TESTING_EMAIL);
        controller.setPassword("password");
        controller.setConfirmPassword("password_");
        controller.validate();
        assertTrue("expecting matching passwords",
                controller.getActionErrors().contains(MessageHelper.getMessage("userAccountController.error_passwords_dont_match")));
    }

    @Test
    @Rollback
    public void testPrepareValidateWithCaseDifferenceInEmail() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        controller.prepare();
        TdarUser p = controller.getPerson();
        p.setEmail(TESTING_EMAIL);
        p.setUsername(TESTING_EMAIL);
        p.setFirstName("First");
        p.setLastName("last");
        controller.setTimeCheck(System.currentTimeMillis() - 5000);
        controller.setConfirmEmail(TESTING_EMAIL.toUpperCase());
        controller.setPassword("password");
        controller.setConfirmPassword("password_");
        controller.validate();
        assertTrue("expecting matching passwords",
                controller.getActionErrors().contains(MessageHelper.getMessage("userAccountController.error_passwords_dont_match")));
        assertEquals(1, controller.getActionErrors().size());
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    @Override
    protected TdarUser getUser() {
        return null;
    }

    @Test
    @Rollback
    // register new account with mixed-case username, and ensure that user can successfully login
    public void testMixedCaseUsername() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        String username = "BobLoblaw";
        String password = "super.secret";

        TdarUser p = newPerson();

        p.setEmail("foo.bar@mailinator.com");
        p.setUsername(username);
        p.setFirstName("Testing auth");
        p.setLastName("User");
        p.setPhone("212 000 0000");
        
        controller.setRequestingContributorAccess(true);
        controller.setContributorReason(REASON);
        p.setRpaNumber("234");

        // create account, making sure the controller knows we're legit.
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        String accountResponse = setupValidUserInController(controller, p, "super.secret");
        logger.info(accountResponse);
        logger.info("errors: {}", controller.getActionErrors());
        assertEquals("user should have been successfully created", Action.SUCCESS, accountResponse);

        // okay, now try to "login": mock a POST request with empty session
        LoginController loginAction = generateNewController(LoginController.class);
        loginAction.setLoginUsername(p.getUsername());
        loginAction.setLoginPassword(password);
        loginAction.setServletRequest(httpServletPostRequest);
        loginAction.setSessionData(new SessionData());

        String loginResponse = loginAction.authenticate();
        assertEquals("login should have been successful", TdarActionSupport.AUTHENTICATED, loginResponse);
    }

    // return a new person reference. an @after method will try to delete this person from crowd
    private TdarUser newPerson() {
        TdarUser person = new TdarUser();
        crowdPeople.add(person);
        return person;
    }
}

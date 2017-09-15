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
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.struts.action.account.UserAccountController;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.login.LoginController;
import org.tdar.struts.action.resource.ResourceController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.Action;
import com.vividsolutions.jts.util.Assert;

/**
 * $Id$
 * 
 * Tests AccountController's action methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 * @param <E>
 */
public class UserRegistrationITCase extends AbstractControllerITCase implements TestUserAccountHelper {

    private static final String PASSWORD = "password";
    static final String REASON = "because";
    private static final String TESTING_EMAIL = "test2asd@test2.com";
    static final String TESTING_AUTH_INSTIUTION = "testing auth instiution";

    @Autowired
    AuthenticationService authService;

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
        TdarUser p = new TdarUser("Allen", "Lee", "allen.lee@dsu.edu");
        p.setUsername("allen.lee");
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.getRegistration().setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        String execute = controller.create();
        assertEquals("Expected controller to return an error, email exists", Action.INPUT, execute);
        logger.info(execute + " : " + controller.getActionMessages());
        assertEquals("expecting valid message", MessageHelper.getMessage("userAccountController.error_username_already_registered"), controller
                .getActionErrors().iterator().next());
    }

    @Test
    @Rollback
    public void testUnContributorStatus() throws TdarActionException {
        TdarUser person = createAndSaveNewUser();

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
        p.setUsername("allen.lee@dsu.edu");
        p.setFirstName("Allen");
        p.setLastName("lee");
        p.setEmail("allen.lee@dsu.edu");
        controller.getRegistration().setConfirmEmail(p.getEmail());
        controller.getRegistration().setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        String execute = controller.create();
        assertEquals("Expected controller to return an error, email exists", Action.INPUT, execute);
        logger.info(" messages: {}", controller.getActionMessages());
        logger.info(" errors  : {}", controller.getActionErrors());
        logger.info("field err: {}", controller.getFieldErrors());
        assertEquals("expecting valid message", MessageHelper.getMessage("userAccountController.error_duplicate_email"), controller.getActionErrors()
                .iterator().next());
    }

    @Test
    @Rollback
    public void testEmailWithSpace() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        TdarUser p = new TdarUser();
        p.setUsername("roseannt62");
        p.setFirstName("Allen");
        p.setLastName("lee");

        p.setEmail("roseannde groot58@de.salazza.com");
        controller.getRegistration().setConfirmEmail(p.getEmail());
        controller.getRegistration().setPassword(p.getEmail());
        controller.getRegistration().setConfirmPassword(p.getEmail());
        controller.getRegistration().setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        controller.getRegistration().setAcceptTermsOfUse(true);
        controller.getRegistration().getH().setTimeCheck(System.currentTimeMillis() - 5000);
        controller.validate();
        // String execute = controller.create();
        // assertEquals("Expected controller to return an error, email exists", Action.INPUT, execute);
        logger.info(" messages: {}", controller.getActionMessages());
        logger.info(" errors  : {}", controller.getActionErrors());
        logger.info("field err: {}", controller.getFieldErrors());
        assertEquals(1, controller.getFieldErrors().size());
        assertTrue(controller.getActionErrors().size() > 0);
    }

    @Test
    @Rollback
    public void testSpammerContributor() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        TdarUser p = new TdarUser();
        p.setUsername("roseannt62");
        p.setFirstName("Allen");
        p.setLastName("lee");

        p.setEmail("roseanndegroot58@de.szzzzza.com");
        controller.getRegistration().setConfirmEmail(p.getEmail());
        controller.getRegistration().setPassword(p.getEmail());
        controller.getRegistration().setConfirmPassword(p.getEmail());
        controller.getRegistration().setPerson(p);
        controller.getRegistration().setContributorReason("1");

        controller.setServletRequest(getServletPostRequest());
        controller.getRegistration().setAcceptTermsOfUse(true);
        controller.getRegistration().getH().setTimeCheck(System.currentTimeMillis() - 5000);
        controller.validate();
        // String execute = controller.create();
        // assertEquals("Expected controller to return an error, email exists", Action.INPUT, execute);
        logger.info(" messages: {}", controller.getActionMessages());
        logger.info(" errors  : {}", controller.getActionErrors());
        logger.info("field err: {}", controller.getFieldErrors());
        assertEquals(0, controller.getFieldErrors().size());
        assertTrue(controller.getActionErrors().size() > 0);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testExistingAuthorWithoutLogin() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        TdarUser p = new TdarUser();
        p.setEmail("tiffany.clark@dsu.edu");
        p.setUsername("tiffany.clark@dsu.edu");
        p.setFirstName("Tiffany");
        p.setLastName("Clark");

        // cleanup crowd if we need to...
        authService.getAuthenticationProvider().deleteUser(p);
        genericService.synchronize();
        controller.getRegistration().setPassword(PASSWORD);
        controller.getRegistration().setPerson(p);
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
        String email = "tiffany.clark@dsu.edu";
        TdarUser p = testCreatePerson(email, Status.ACTIVE, Action.SUCCESS);
        assertEquals(Status.ACTIVE, p.getStatus());

        p = testCreatePerson(email, Status.DRAFT, Action.SUCCESS);
        assertEquals(Status.ACTIVE, p.getStatus());

        p = testCreatePerson(email, Status.DELETED, Action.INPUT);
        assertEquals(Status.DELETED, p.getStatus());

        p = testCreatePerson(email, Status.FLAGGED, Action.INPUT);
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
            findByEmail.setUsername("abc123");

            genericService.saveOrUpdate(findByEmail);
        }
        evictCache();
        findByEmail = null;
        // cleanup crowd if we need to...
        authService.getAuthenticationProvider().deleteUser(p);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        controller.getRegistration().setPassword(PASSWORD);
        controller.getRegistration().setPerson(p);
        controller.setServletRequest(getServletPostRequest());
        controller.setServletResponse(getServletResponse());
        controller.validate();
        String execute = controller.create();
        assertEquals(success, execute);

        findByEmail = (TdarUser) entityService.findByEmail(email);
        genericService.refresh(findByEmail);

        boolean deleteUser = authService.getAuthenticationProvider().deleteUser(p);
        assertTrue("could not delete user", deleteUser);

        return findByEmail;
    }

    @Test
    @Rollback
    public void testNewUser() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
        String execute = setupValidUserInController(controller);
        TdarUser p = controller.getRegistration().getPerson();
        assertEquals("expecting result to be 'success'", "success", execute);
        assertNotNull("person id should not be null", p.getId());
        assertNotNull("person should have set insitution", p.getInstitution());
        assertEquals("insitution should match", p.getInstitution().getName(), TESTING_AUTH_INSTIUTION);
        assertTrue("person should be registered", p.isRegistered());
        boolean deleteUser = authService.getAuthenticationProvider().deleteUser(p);
        assertTrue("could not delete user", deleteUser);
        assertTrue("no errors expected", controller.getActionErrors().size() == 0);
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testInvalidUsers() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        List<String> emails = Arrays.asList("a", "a b", "adam brin", "abcd1234!", "http://", "!#####/bin/ls");
        for (String email : emails) {
            logger.info("TRYING =======> {}", email);
            controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
            String execute = setupValidUserInController(controller, email);
            // assertFalse("user " + email + " succeeded??", TdarActionSupport.SUCCESS.equals(execute));
            logger.info("errors:{}", controller.getActionErrors());
            assertTrue(controller.getFieldErrors().size() > 0);
        }
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testSpammerWithContributor() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        String email = "sdfdasdf@1234.com";
        logger.info("TRYING =======> {}", email);
        controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
        TdarUser user = new TdarUser("a", "b", email);
        controller.getRegistration().setContributorReason("abasd");
        String execute = setupValidUserInController(controller, user, "test");
        // assertFalse("user " + email + " succeeded??", TdarActionSupport.SUCCESS.equals(execute));
        logger.info("errors:{}", controller.getActionErrors());
        assertTrue(controller.getFieldErrors().size() > 0);
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testValidUsers() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        List<String> emails = Arrays.asList("aaaa-bbbbbb.ccccccc-ddddd@eeeeeee.ffff.hh");

        for (String email : emails) {
            assertTrue(authenticationService.isValidEmail(email));
            assertTrue(authenticationService.isValidUsername(email));
            logger.info("TRYING =======> {}", email);
            controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
            String execute = setupValidUserInController(controller, email);
            // assertFalse("user " + email + " succeeded??", TdarActionSupport.SUCCESS.equals(execute));
            logger.info("errors:{}", controller.getActionErrors());
            assertFalse(controller.getActionErrors().size() > 0);
        }
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testRegistrationEmailSent() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
        setupValidUserInController(controller);
        sendEmailProcess.execute();
        SimpleMailMessage received = checkMailAndGetLatest("Your user name");
    }

    @Test
    @Rollback(false)
    /**
     * NOTE THIS TEST IS FLIMSY AS IT SEEMS TO FAIL WHEN RUN A "SECOND" TIME WITHOUT CLEARNING THE DATABASE UP
     */
    public void testEmailWithPlusSign() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);

        String email = "test+++user@gmail.com";
        TdarUser findByEmail = (TdarUser) entityService.findByEmail(email);
        if (findByEmail != null) { // this should rarely happen, but it'll clear out the test before we run it if the last time it failed...
            genericService.delete(findByEmail);
            authenticationService.getAuthenticationProvider().deleteUser(findByEmail);
        }
        evictCache();
        controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
        String execute = setupValidUserInController(controller, email);
        final TdarUser p = controller.getRegistration().getPerson();
        assertEquals(p.getId(), controller.getSessionData().getTdarUserId());
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
                UserLogin userLogin = loginAction.getUserLogin();
                userLogin.setLoginUsername(p.getEmail());
                userLogin.setLoginPassword(PASSWORD);
                loginAction.setServletRequest(getServletPostRequest());
                assertEquals(TdarActionSupport.SUCCESS, loginAction.authenticate());
                TdarUser person = genericService.find(TdarUser.class, p.getId());
                boolean deleteUser = authService.getAuthenticationProvider().deleteUser(person);
                assertTrue("could not delete user", deleteUser);
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
        controller.getH().setTimeCheck(System.currentTimeMillis() - 1000);
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
        controller.getH().setTimeCheck(System.currentTimeMillis() + (1000 * 61));
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
        controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
        controller.getH().setComment("could you help me?  I love your site");
        String execute = setupValidUserInController(controller);
        assertEquals("expecting result to be 'null' (validate should fail)", null, execute);
        String firstError = getFirstFieldError(controller);
        assertTrue(firstError.equals(MessageHelper.getMessage("userAccountController.could_not_authenticate_at_this_time")));
    }

    @Test
    public void testEmailRegistration() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        assertFalse("email should not exist", controller.isUsernameRegistered("testuser@testuser.com"));
        assertFalse("email should exist but not be registered", controller.isUsernameRegistered("tiffany.clark@dsu.edu"));
        assertTrue("email should exist and be registered", controller.isUsernameRegistered("admin@tdar.org"));
    }

    @Test
    @Rollback
    public void testPrepareValidate() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        TdarUser p = controller.getRegistration().getPerson();

        p.setEmail("test@tdar.org");
        p.setUsername(p.getEmail());
        assertTrue(p.getId().equals(-1L));
        controller.validate();
        assertTrue(
                "expecting user existing",
                controller.getFieldErrors().get("registration.person.username")
                        .contains(MessageHelper.getMessage("userAccountController.error_username_already_registered")));
    }

    private String getFirstFieldError(UserAccountController controller) {
        return controller.getActionErrors().iterator().next();
    }

    @Test
    @Rollback
    public void testPrepareValidate2() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        TdarUser p = controller.getRegistration().getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.getRegistration().setPerson(p);
        controller.validate();
        assertTrue("expecting password",
                controller.getFieldErrors().get("registration.password").contains(MessageHelper.getMessage("userAccountController.error_choose_password")));
    }

    @Test
    @Rollback
    public void testPrepareValidate25() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        TdarUser p = controller.getRegistration().getPerson();
        p.setEmail(TESTING_EMAIL);
        p.setUsername(TESTING_EMAIL);
        controller.getRegistration().setPassword(PASSWORD);
        controller.validate();
        assertTrue("expecting confirm email",
                controller.getFieldErrors().get("registration.confirmEmail").contains(MessageHelper.getMessage("userAccountController.error_confirm_email")));
    }

    @Test
    @Rollback
    public void testPrepareValidate3() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        TdarUser p = controller.getRegistration().getPerson();
        p.setEmail(TESTING_EMAIL);
        p.setFirstName("fn");
        p.setLastName("ln");
        p.setUsername(TESTING_EMAIL);
        controller.getRegistration().setAcceptTermsOfUse(true);
        controller.getRegistration().setConfirmEmail(TESTING_EMAIL);
        controller.getRegistration().setPassword(PASSWORD);
        controller.validate();
        logger.debug("E:{}", controller.getFieldErrors().get("registration.confirmPassword"));
        assertTrue("expecting confirm password", controller.getFieldErrors().get("registration.confirmPassword")
                .contains(MessageHelper.getMessage("userAccountController.error_confirm_password")));
    }

    @Test
    @Rollback
    public void testPrepareValidate4() {
        setIgnoreActionErrors(true);
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.clearErrorsAndMessages();
        Person p = controller.getRegistration().getPerson();
        p.setEmail(TESTING_EMAIL);
        controller.getRegistration().setConfirmEmail(TESTING_EMAIL);
        controller.getRegistration().setPassword(PASSWORD);
        controller.getRegistration().setConfirmPassword("password_");
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
        TdarUser p = controller.getRegistration().getPerson();
        p.setEmail(TESTING_EMAIL);
        p.setUsername(TESTING_EMAIL);
        p.setFirstName("First");
        p.setLastName("last");
        controller.getH().setTimeCheck(System.currentTimeMillis() - 5000);
        controller.getRegistration().setConfirmEmail(TESTING_EMAIL.toUpperCase());
        controller.getRegistration().setPassword(PASSWORD);
        controller.getRegistration().setConfirmPassword("password_");
        controller.validate();
        assertTrue("expecting matching passwords",
                controller.getActionErrors().contains(MessageHelper.getMessage("userAccountController.error_passwords_dont_match")));
        assertEquals(1, controller.getActionErrors().size());
    }

    @Override
    public TdarUser getUser() {
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

        p.setEmail("foo.bar@tdar.net");
        p.setUsername(username);
        p.setFirstName("Testing auth");
        p.setLastName("User");
        p.setPhone("212 000 0000");

        controller.getRegistration().setRequestingContributorAccess(true);
        controller.getRegistration().setContributorReason(REASON);
        p.setRpaNumber("234");

        // create account, making sure the controller knows we're legit.
        controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
        String accountResponse = setupValidUserInController(controller, p, "super.secret");
        logger.info(accountResponse);
        logger.info("errors: {}", controller.getActionErrors());
        assertEquals("user should have been successfully created", Action.SUCCESS, accountResponse);

        // okay, now try to "login": mock a POST request with empty session
        LoginController loginAction = generateNewController(LoginController.class);
        UserLogin userLogin = loginAction.getUserLogin();
        userLogin.setLoginPassword(password);
        userLogin.setLoginUsername(username);
        loginAction.setServletRequest(httpServletPostRequest);
        loginAction.setSessionData(new SessionData());

        String loginResponse = loginAction.authenticate();
        assertEquals("login should have been successful", TdarActionSupport.SUCCESS, loginResponse);
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    // register new account with mixed-case username, and ensure that user can successfully login
    public void testAffiliationAndComment() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        String username = "BobLoblaw";
        String password = "super.secret";

        TdarUser p = newPerson();

        p.setEmail("foo.bar@tdar.net");
        p.setUsername(username);
        p.setFirstName("Testing auth");
        p.setLastName("User");
        p.setPhone("212 000 0000");

        controller.getRegistration().setRequestingContributorAccess(true);
        controller.getRegistration().setContributorReason(REASON);
        controller.getRegistration().setAffiliation(UserAffiliation.CRM_ARCHAEOLOGIST);
        p.setRpaNumber("234");

        // create account, making sure the controller knows we're legit.
        controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
        String accountResponse = setupValidUserInController(controller, p, "super.secret");
        logger.info(accountResponse);
        logger.info("errors: {}", controller.getActionErrors());
        assertEquals("user should have been successfully created", Action.SUCCESS, accountResponse);

        TdarUser user = entityService.findByUsername(username);
        assertNotNull(user);
        assertEquals(UserAffiliation.CRM_ARCHAEOLOGIST, user.getAffiliation());
        assertEquals(REASON, user.getContributorReason());
    }

    @Test
    @Rollback
    // register new account with mixed-case username, and ensure that user can successfully login
    public void testInviteRegister() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setId(8424L);
        String username = "pshackel@dnth.umd.edu";
        controller.setEmail(username);
        controller.execute();
        controller.getRegistration().setRequestingContributorAccess(true);
        controller.getRegistration().setContributorReason(REASON);
        controller.getRegistration().setAffiliation(UserAffiliation.CRM_ARCHAEOLOGIST);
        controller.getReg().getPerson().setUsername(username);
        controller.getReg().setPassword("1234");
        controller.getReg().setConfirmPassword("1234");

        // create account, making sure the controller knows we're legit.
        controller.getH().setTimeCheck(System.currentTimeMillis() - 10000);
        String accountResponse = controller.create();
        logger.info(accountResponse);
        logger.info("errors: {}", controller.getActionErrors());
        assertEquals("user should have been successfully created", Action.SUCCESS, accountResponse);

        TdarUser user = entityService.findByUsername(username);
        assertNotNull(user);
    }

    @Test
    @Rollback
    // register new account with mixed-case username, and ensure that user can successfully login
    public void testInviteRegisterWithoutEmail() {
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);
        controller.setId(8424L);
        String username = "pshackel@dnth.umd.edu";
        controller.execute();
        controller.getRegistration().setRequestingContributorAccess(true);
        controller.getRegistration().setContributorReason(REASON);
        controller.getRegistration().setAffiliation(UserAffiliation.CRM_ARCHAEOLOGIST);
        assertEquals(null, controller.getReg().getConfirmEmail());
        assertEquals(null, controller.getReg().getPerson().getEmail());
        assertEquals(null, controller.getReg().getPerson().getFirstName());
        assertEquals(null, controller.getReg().getPerson().getLastName());
    }

    // return a new person reference. an @after method will try to delete this person from crowd
    private TdarUser newPerson() {
        TdarUser person = new TdarUser();
        crowdPeople.add(person);
        return person;
    }

    @Override
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }
}

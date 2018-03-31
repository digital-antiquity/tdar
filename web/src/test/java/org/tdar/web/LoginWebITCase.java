/**
 * 
 */
package org.tdar.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.core.dao.external.auth.AuthenticationResultType;
import org.tdar.junit.RunWithTdarConfiguration;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleWebTdarConfigurationRunner.class)
public class LoginWebITCase extends AbstractAnonymousWebTestCase {

    private static final String PASSWORD = "test";

    @Before
    public void setup() {
        login("mu@tdar.net", PASSWORD);
    }

    @After
    public void teardown() {
        logout();
    }

    @Test
    public void testAbstractLogin() {
        assertTextPresentInPage("Welcome");
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testLoginWithPrompt() {
        logger.trace(getPageBodyCode());
        assertTextPresent("User Agreements");
        setInput("acceptedAuthNotices", "TOS_AGREEMENT");
        setInput("acceptedAuthNotices", "CONTRIBUTOR_AGREEMENT");
        clickElementWithId("accept");
        assertTextPresentInPage("Welcome");
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testLoginDeclineWithPrompt() {
        logout();
        login("bea@tdar.net", PASSWORD);
        logger.trace(getPageBodyCode());
        assertTextPresent("User Agreements");
        clickElementWithId("decline");
        assertTextPresentInPage("What can you dig up");
        login("bea@tdar.net", PASSWORD);
        clickLinkOnPage("My Profile");
        setInput("contributor", "true");
        submitForm();
    }

    @Test
    public void testSecondLogin() {
        gotoPage("/login");
        assertTextPresentInPage("margeret user's Dashboard");
    }

    @Test
    public void testInvalidLogin() {
        logout();
        login("BAD_USERNAME", "BAD_PASSWORD", true);
        assertTextPresent(AuthenticationResultType.INVALID_PASSWORD.getMessage());
        assertTextNotPresent("Your submitted projects");
    }

    @Test
    public void testInvalidLoginInvalidEmail() {
        logout();
        login("BAD USERNAME", "BAD PASSWORD", true);
        assertTextPresent("Could not authenticate: Authentication failed");
        assertTextNotPresent("Your submitted projects");
    }

    @Test
    public void testLogout() {
        logout();
        assertTextPresentInPage("Log In");
    }
}

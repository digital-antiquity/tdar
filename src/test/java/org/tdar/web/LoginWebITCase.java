/**
 * 
 */
package org.tdar.web;

import org.junit.Test;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.struts.action.UserAccountController;

/**
 * @author Adam Brin
 * 
 */
public class LoginWebITCase extends AbstractAuthenticatedWebTestCase {

    @Test
    public void testAbstractLogin() {
        assertTextPresentInPage("Welcome back,");
    }

    @Test
    public void testSecondLogin() {
        gotoPage("/login");
        assertTextPresentInPage("Featured Content");
    }

    @Test
    public void testInvalidLogin() {
        logout();
        login("BAD_USERNAME", "BAD_PASSWORD", true);
        assertTextPresent(AuthenticationResult.INVALID_PASSWORD.getMessage());
        assertTextNotPresent("Your submitted projects");
    }

    @Test
    public void testInvalidLoginInvalidEmail() {
        logout();
        login("BAD USERNAME", "BAD PASSWORD", true);
        assertTextPresent(AuthenticationAndAuthorizationService.USERNAME_INVALID);
        assertTextNotPresent("Your submitted projects");
    }

    @Test
    public void testLogout() {
        logout();
        assertTextPresentInPage("Log In");
    }
}

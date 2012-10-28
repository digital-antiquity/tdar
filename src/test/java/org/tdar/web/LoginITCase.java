/**
 * 
 */
package org.tdar.web;

import org.junit.Test;
import org.tdar.core.dao.external.auth.AuthenticationResult;

/**
 * @author Adam Brin
 *
 */
public class LoginITCase extends AbstractAuthenticatedWebTestCase {
	
	@Test
	public void testAbstractLogin() {
		assertTextPresentInPage("Welcome back,");	
	}
	
	@Test
	public void testSecondLogin() {
		gotoPage("/login");
		assertTextPresentInPage("Search");
	}

	
	@Test
	public void testInvalidLogin() {
		logout();
		login("BAD USERNAME","BAD PASSWORD",true);
		assertTextPresent(AuthenticationResult.INVALID_PASSWORD.getMessage());
		assertTextNotPresent("Your submitted projects");
	}
	
	@Test
	public void testLogout() {
		logout();
		assertTextPresentInPage("Login");
	}
}

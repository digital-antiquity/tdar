package org.tdar.web.functional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tdar.core.dao.external.auth.AuthenticationResult;

/**
 * This is a rewrite of org.tdar.web.LoginWebITCase.  
 * @author jimdevos
 *
 */
public class LoginFunctionalITCase extends FunctionalWebTestCase{

    @Before
    public void setup() {
        login();
    }
    
    @After
    public void teardown() {
        logout();
    }
    
    @Test
    public void testAbstractLogin() {
        assertTrue(textContains("Welcome back,"));   
    }
    
    @Test
    public void testSecondLogin() {
        gotoPage("/login");
        assertTrue(textContains("Featured Content"));
    }

    @Test
    public void testInvalidLogin() {
        logout();
        login("BAD USERNAME","BAD PASSWORD");
        assertTrue(textContains(AuthenticationResult.INVALID_PASSWORD.getMessage()));
        assertFalse(textContains("Your submitted projects"));
    }
    
    @Test
    public void testLogout() {
        logout();
        assertTrue(textContains("Log In"));
    }

}

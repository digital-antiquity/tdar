package org.tdar.web.functional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import org.hamcrest.text.StringContainsInOrder;

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
        assertThat(getText(), containsString("Welcome back,"));   
    }
    
    @Test
    public void testSecondLogin() {
        gotoPage("/login");
        assertThat(getText(), containsString("Featured Content"));
    }

    @Test
    public void testInvalidLogin() {
        logout();
        login("BAD USERNAME","BAD PASSWORD");
        assertThat(getText(), containsString(AuthenticationResult.INVALID_PASSWORD.getMessage()));
        assertThat(getText(), not(containsString("Your submitted projects")));
    }
    
    @Test
    public void testLogout() {
        logout();
        assertThat(getText(), containsString("Log In"));
    }

}

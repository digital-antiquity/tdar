package org.tdar.functional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.dao.external.auth.AuthenticationResultType;

/**
 * This is a rewrite of org.tdar.web.LoginWebITCase.
 * 
 * @author jimdevos
 * 
 */
public class LoginSeleniumITCase extends AbstractSeleniumWebITCase {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void setup() throws IOException {
        super.beforeTest();
        login();
    }

    @Test
    public void testAbstractLogin() {
        assertTrue(getText().contains("Welcome"));
    }

    @Test
    public void testSecondLogin() {
        gotoPage("/login");
        assertTrue(getText().contains("My Resources"));
    }

    @Test
    public void testInvalidLogin() {
        logout();
        login("BADUSERNAME", "BADPASSWORD");
        logger.debug(getDom());
        assertTrue(getDom().contains(AuthenticationResultType.INVALID_PASSWORD.getMessage()));
        assertFalse(getText().contains("Your submitted projects"));
    }

    @Test
    public void testLogout() {
        logout();
        assertTrue(getText().contains("Log In"));
    }

}

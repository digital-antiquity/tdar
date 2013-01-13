package org.tdar.web;

import static org.tdar.TestConstants.ADMIN_PASSWORD;
import static org.tdar.TestConstants.ADMIN_USERNAME;
import static org.tdar.TestConstants.PASSWORD;
import static org.tdar.TestConstants.USERNAME;

import org.junit.After;
import org.junit.Before;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractAuthenticatedWebTestCase extends AbstractWebTestCase {

    @Before
    public void setUp() {
        // Manual invocation of super.prepare() is unnecessary, see the multiple @Befores section in
        // http://ibiblio.org/java/slides/sdbestpractices2005/junit4/What's_New_in_JUnit_4.html
        // the relevant part is that for multiple @Before annotations, order is
        // unspecified within a class, but superclass @Befores are guaranteed to
        // always execute in front of subtype @Befores
        // super.prepare();
        login();
    }

    public void login() {
        login(USERNAME, PASSWORD);
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    public void loginAdmin() {
        login(getAdminUsername(), getAdminPassword());
    }

    public static String getAdminUsername() {
        return ADMIN_USERNAME;
    }

    public static String getAdminPassword() {
        return ADMIN_PASSWORD;
    }

    @After
    @Override
    public void logout() {
        super.logout();
    }

}

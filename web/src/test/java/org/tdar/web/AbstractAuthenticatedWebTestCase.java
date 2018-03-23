package org.tdar.web;

import org.junit.After;
import org.junit.Before;
import org.tdar.utils.TestConfiguration;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractAuthenticatedWebTestCase extends AbstractWebTestCase {

    protected static final TestConfiguration CONFIG = TestConfiguration.getInstance();

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
        login(CONFIG.getUsername(), CONFIG.getPassword());
    }

    public static String getUsername() {
        return CONFIG.getUsername();
    }

    public static String getPassword() {
        return CONFIG.getPassword();
    }

    public void loginAdmin() {
        login(getAdminUsername(), getAdminPassword());
    }

    public void loginEditor() {
        login(CONFIG.getEditorUsername(), CONFIG.getEditorPassword());
    }

    public static String getAdminUsername() {
        return CONFIG.getAdminUsername();
    }

    public static String getAdminPassword() {
        return CONFIG.getAdminPassword();
    }

    @After
    @Override
    public void logout() {
        super.logout();
    }

}

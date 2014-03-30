package org.tdar.web.functional;

import org.junit.After;
import org.junit.Before;

public class AbstractAdminSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Before
    public void beforeTest() {
        super.reindexOnce();
        loginAdmin();
    }

    @After
    public void afterTest() {
        logout();
    }

}

package org.tdar.functional;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

public class AbstractAdminSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Before
    public void beforeTest() throws IOException {
        super.beforeTest();
        super.reindexOnce();
        loginAdmin();
    }

    @After
    public void afterTest() {
        logout();
    }

}

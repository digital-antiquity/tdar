package org.tdar.functional;

import org.junit.After;
import org.junit.Before;

public class AbstractEditorSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Before
    public void beforeTest() {
        super.reindexOnce();
        loginEditor();
    }

    @After
    public void afterTest() {
        logout();
    }

}

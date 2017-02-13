package org.tdar.functional;

import java.io.IOException;

import org.junit.Before;

public class AbstractEditorSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Before
    public void beforeTest() throws IOException {
        super.beforeTest();
        super.reindexOnce();
        loginEditor();
    }

}

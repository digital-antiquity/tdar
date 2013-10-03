package org.tdar.web;

import org.junit.Before;

public abstract class AbstractEditorAuthenticatedWebTestCase extends AbstractAuthenticatedWebTestCase {

    @Before
    public void setUp() {
        loginEditor();
    }

}

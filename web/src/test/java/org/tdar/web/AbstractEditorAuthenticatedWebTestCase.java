package org.tdar.web;

import org.junit.Before;

public abstract class AbstractEditorAuthenticatedWebTestCase extends AbstractAuthenticatedWebTestCase {

    @Override
    @Before
    public void setUp() {
        loginEditor();
    }

}

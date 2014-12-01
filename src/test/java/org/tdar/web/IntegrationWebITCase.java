package org.tdar.web;

import org.junit.Test;

/**
 * Created by jimdevos on 12/1/14.
 */
public class IntegrationWebITCase extends AbstractAdminAuthenticatedWebTestCase {
    @Test
    public void testWorkspaceAdd() {
        //angular templates will likely break all html validation
        skipHtmlValidation = true;
        gotoPage("/workspace/add-angular");
    }
}

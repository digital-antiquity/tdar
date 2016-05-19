package org.tdar.web.resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Status;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class DraftWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    @Test
    @Rollback
    public void testStatusCodeDraft() {
        gotoPage("/document/" + TestConstants.TEST_DOCUMENT_ID + "/edit");
        setInput("status", Status.DRAFT.name());
        submitForm();
        logout();
        gotoPage("/document/" + TestConstants.TEST_DOCUMENT_ID);
        assertTextNotPresent("2008 New Philadelphia Archaeology Report, Chapter 4, Block 7, Lot 1");
        assertTextPresentIgnoreCase("draft");
        loginAdmin();
        gotoPage("/document/" + TestConstants.TEST_DOCUMENT_ID + "/edit");
        setInput("status", Status.ACTIVE.name());
        submitForm();

    }

}

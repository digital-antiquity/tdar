package org.tdar.web;

import org.junit.Test;
import org.tdar.TestConstants;

public class WorkspaceITCase extends AbstractAdminAuthenticatedWebTestCase {

    // ensure that a 'deleted item no longer shows up in boomarks
    @Test
    public void testDeletedBoomarkedItem() {
        String docTitle = "testing deleted bookmarked items";
        String docDescription = "test";

        // create simple doc, remember name and url
        gotoPage("/document/add");
        setInput(TestConstants.DOCUMENT_FIELD_TITLE, docTitle);
        setInput(TestConstants.DOCUENT_DATE_CREATED, "1923");
        setInput(TestConstants.DOCUMENT_FIELD_DESCRIPTION, docDescription);
        submitForm();
        String viewPage = internalPage.getUrl().getPath().toLowerCase();

        // bookmark it, confirm it's on workspace
        clickLinkOnPage("bookmark");
        gotoPage(viewPage);
        clickLinkOnPage("Workspace");
        assertTextPresentInCode(docTitle);

        // now delete it, and check again: it should be gone.
        gotoPage(viewPage);
        clickLinkOnPage("delete");
        submitForm("delete");
        clickLinkOnPage("Workspace");
        assertTextNotPresent(docTitle);

        // undelete it, should be back on workspace again
        gotoPage(viewPage);
        clickLinkOnPage("edit");
        setInput("status", "ACTIVE");
        submitForm();
        clickLinkOnPage("Workspace");
        assertTextPresentInCode(docTitle);
    }

}

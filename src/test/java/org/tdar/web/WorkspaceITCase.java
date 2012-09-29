package org.tdar.web;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.URLConstants;
import org.tdar.core.bean.resource.Status;

public class WorkspaceITCase extends AbstractAuthenticatedWebTestCase {

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

        // have an admin undelete the resource
        logout();
        loginAdmin();
        gotoPage(viewPage);
        logger.debug("result after trying to go to: '{}': {}", viewPage, getPageText());
        clickLinkOnPage("edit");
        setInput("status", Status.ACTIVE.name());
        submitForm();
        logout();

        // log back in as regular user, we should be able to see the resource in the workspace again
        login();
        gotoPage(URLConstants.WORKSPACE);
        assertTextPresentInCode(docTitle);
    }

    @Test
    public void testDraftResourceInWorkspace() {
        testResourceWithStatus("draft resource in workspace", Status.DRAFT);
    }

    @Test
    public void testFlaggedResourceInWorkspace() {
        testResourceWithStatus("flagged resource in workspace", Status.FLAGGED);
    }

    private void testResourceWithStatus(String docTitle, Status status) {
        String docDescription = "test";

        // create simple doc, remember name and url
        gotoPage("/document/add");
        setInput(TestConstants.DOCUMENT_FIELD_TITLE, docTitle);
        setInput(TestConstants.DOCUENT_DATE_CREATED, "1923");
        setInput(TestConstants.DOCUMENT_FIELD_DESCRIPTION, docDescription);
        if (status != Status.DELETED && status != Status.FLAGGED) {
            setInput("status", status.name());
        }
        submitForm();
        String viewPage = internalPage.getUrl().getPath().toLowerCase();

        // bookmark it, confirm it's on workspace
        clickLinkOnPage("bookmark");
        gotoPage(viewPage);
        clickLinkOnPage("Workspace");
        
        assertTextPresentInCode(docTitle);

        if (status == Status.DELETED || status == Status.FLAGGED) {
            // now delete it, and check again: it should be gone.
            gotoPage(viewPage);
            clickLinkOnPage("edit");
            setInput("status", status.name());
            submitForm();
            clickLinkOnPage("Workspace");
            assertTextNotPresent(docTitle);
        }

        // undelete it, should be back on workspace again
        gotoPage(viewPage);
        logger.debug("result after trying to go to: '{}': {}", viewPage, getPageText());
        clickLinkOnPage("edit");
        setInput("status", Status.ACTIVE.name());
        submitForm();
        clickLinkOnPage("Workspace");
        assertTextPresentInCode(docTitle);
    }

}

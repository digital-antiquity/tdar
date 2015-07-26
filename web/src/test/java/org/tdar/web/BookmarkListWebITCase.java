package org.tdar.web;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.URLConstants;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.html.DomNode;

public class BookmarkListWebITCase extends AbstractAuthenticatedWebTestCase {

    // ensure that a 'deleted item no longer shows up in bookmarks
    @Test
    public void testDeletedBoomarkedItem() {
        String docTitle = "testing deleted bookmarked items";
        String docDescription = "test";

        // create simple doc, remember name and url
        gotoPage("/document/add");
        setDocumentRequiredFields(docTitle, docDescription);
        submitForm();
        String viewPage = internalPage.getUrl().getPath().toLowerCase();

        // bookmark it, confirm it's on workspace
        clickLinkOnPage("bookmark");
        gotoPage(viewPage);
        gotoPage(URLConstants.DASHBOARD);
        assertTextPresentInCode(docTitle);

        // now delete it, and check again: it should be gone.
        gotoPage(viewPage);
        clickLinkOnPage("delete");
        submitForm("delete");
        gotoPage(URLConstants.DASHBOARD);
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
        gotoPage(URLConstants.DASHBOARD);
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
        setDocumentRequiredFields(docTitle, docDescription);

        if ((status != Status.DELETED) && (status != Status.FLAGGED)) {
            setInput("status", status.name());
        }

        submitForm();
        String viewPage = internalPage.getUrl().getPath().toLowerCase();

        // bookmark it, confirm it's on workspace
        clickLinkOnPage("bookmark");
        gotoPage(viewPage);
        gotoPage(URLConstants.DASHBOARD);

        assertTextPresentInCode(docTitle);

        if ((status == Status.DELETED) || (status == Status.FLAGGED)) {
            // now delete it, and check again: it should be gone.
            gotoPage(viewPage);
            clickLinkOnPage("edit");
            setInput("status", status.name());
            submitForm();
            gotoPage(URLConstants.DASHBOARD);
            boolean seen = false;
            for (DomNode element_ : htmlPage.getDocumentElement().querySelectorAll("#bookmarks")) {
                Element el = (Element) element_;
                if (el.toString().contains(docTitle)) {
                    seen = true;
                }
                logger.info(el.toString());
            }
            Assert.assertFalse(seen);
        }

        // undelete it, should be back on workspace again
        gotoPage(viewPage);
        logger.debug("result after trying to go to: '{}': {}", viewPage, getPageText());
        clickLinkOnPage("edit");
        setInput("status", Status.ACTIVE.name());
        submitForm();
        gotoPage(URLConstants.DASHBOARD);
        assertTextPresentInCode(docTitle);
    }

    private void setDocumentRequiredFields(String docTitle, String docDescription) {
        setInput(TestConstants.DOCUMENT_FIELD_TITLE, docTitle);
        setInput(TestConstants.DOCUENT_DATE_CREATED, "1923");
        setInput(TestConstants.DOCUMENT_FIELD_DESCRIPTION, docDescription);
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
    }

}

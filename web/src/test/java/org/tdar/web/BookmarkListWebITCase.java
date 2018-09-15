package org.tdar.web;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.URLConstants;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class BookmarkListWebITCase extends AbstractAuthenticatedWebTestCase {

    // ensure that a 'deleted item no longer shows up in bookmarks
    @Test
    public void testDeletedBoomarkedItem() throws IOException {
        String docTitle = "testing deleted bookmarked items";
        String docDescription = "test";

        // create simple doc, remember name and url
        gotoPage("/document/add");
        setDocumentRequiredFields(docTitle, docDescription);
        submitForm();
        String viewPage = internalPage.getUrl().getPath().toLowerCase();

        // bookmark it, confirm it's on workspace
        String resourceId = querySelectorAll(".bookmark-link").get(0).getAttribute("resource-id");
        bookmark(resourceId);
        gotoPage(viewPage);
        gotoPage(URLConstants.BOOKMARKS);
        assertTextPresentInCode(docTitle);

        // now delete it, and check again: it should be gone.
        gotoPage(viewPage);
        clickLinkOnPage("delete");
        submitForm("delete");
        gotoPage(URLConstants.BOOKMARKS);
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
        gotoPage(URLConstants.BOOKMARKS);
        assertTextPresentInCode(docTitle);
    }

    @Test
    public void testDraftResourceInWorkspace() throws IOException {
        testResourceWithStatus("draft resource in workspace", Status.DRAFT);
    }

    @Test
    public void testFlaggedResourceInWorkspace() throws IOException {
        testResourceWithStatus("flagged resource in workspace", Status.FLAGGED);
    }

    private void testResourceWithStatus(String docTitle, Status status) throws IOException {
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
        // clickBookmarkLink();
        String resourceId = querySelectorAll(".bookmark-link").get(0).getAttribute("resource-id");
        bookmark(resourceId);
        gotoPage(viewPage);
        gotoPage(URLConstants.BOOKMARKS);

        assertTextPresentInCode(docTitle);

        if ((status == Status.DELETED) || (status == Status.FLAGGED)) {
            // now delete it, and check again: it should be gone.
            gotoPage(viewPage);
            clickLinkOnPage("edit");
            setInput("status", status.name());
            submitForm();
            gotoPage(URLConstants.BOOKMARKS);

            // boolean seen = false;
            // for (DomNode element_ : htmlPage.getDocumentElement().querySelectorAll("#bookmarks")) {
            // Element el = (Element) element_;
            // if (el.toString().contains(docTitle)) {
            // seen = true;
            // }
            // logger.info(el.toString());
            // }

            // anyMatch() returns returns true (and terminates scan) if match found; returns false if scan produces no match.
            boolean seen = querySelectorAll("#bookmarks").stream()
                    .anyMatch(el -> el.toString().contains(docTitle));

            Assert.assertFalse("document title should not be found on page:" + docTitle, seen);
        }

        // undelete it, should be back on workspace again
        gotoPage(viewPage);
        logger.debug("result after trying to go to: '{}': {}", viewPage, getPageText());
        clickLinkOnPage("edit");
        setInput("status", Status.ACTIVE.name());
        submitForm();
        gotoPage(URLConstants.BOOKMARKS);
        assertTextPresentInCode(docTitle);
    }

    private void removeBookmark(String resourceId) throws IOException {
        post(getBaseSecureUrl() + "resource/removeBookmark", resourceId);

    }

    private void post(String url, String resourceId) throws FailingHttpStatusCodeException, IOException {
        WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.POST);
        List<NameValuePair> parms = new ArrayList<NameValuePair>();
        parms.add(new NameValuePair("resourceId", resourceId));
        webRequest.setRequestParameters(parms);
        webRequest.setEncodingType(FormEncodingType.MULTIPART);
        Page page = webClient.getPage(webRequest);
        int code = page.getWebResponse().getStatusCode();

    }

    private void bookmark(String resourceId) throws IOException {
        post(getBaseSecureUrl() + "resource/bookmark", resourceId);
    }

    // private void clickBookmarkLink() {
    // try {
    // ((DomElement)htmlPage.getDocumentElement().querySelector(".bookmark-link")).click();
    // } catch (IOException e) {
    // logger.error("{}",e,e);
    // }
    // }

    private void setDocumentRequiredFields(String docTitle, String docDescription) {
        setInput(TestConstants.DOCUMENT_FIELD_TITLE, docTitle);
        setInput(TestConstants.DOCUENT_DATE_CREATED, "1923");
        setInput(TestConstants.DOCUMENT_FIELD_DESCRIPTION, docDescription);
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
    }

}

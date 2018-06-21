package org.tdar.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.configuration.TdarConfiguration;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class CrossActionPostWebITCase extends AbstractAuthenticatedWebTestCase {

    Long documentId = -1L;

    public void setupResource() {
        gotoPage("/document/add");
        assertTextPresentInPage("Create a new Document");
        setInput("document.title", "this is the title ");
        setInput("document.description", "this is the description");
        setInput("document.date", "1934");
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        submitForm();
        documentId = extractTdarIdFromCurrentURL();
        assertNotNull(documentId);
        assertNotSame(-1L, documentId);
    }

    @Test
    public void testDocumentViewGoneAwry() {
        setupResource();
        // load the document and simply test the "view page"
        String docPath = "/document/" + documentId;
        // go to the edit page, change the action, change the title and hit submit
        gotoPage(docPath + "/edit");
        HtmlElement form = getHtmlPage().getElementByName("metadataForm");
        assertNotNull("should have found the resource metadata form", form);
        form.setAttribute("action", docPath);
        String testTitle = "this is another test";
        setInput("document.title", testTitle);
        submitForm();
        gotoPage(docPath);
        logger.debug("TITLE TEXT: " + getHtmlPage().getTitleText());
        assertTextNotPresent(testTitle);
    }

    @Test
    public void testDocumentAuthGoneAwry() {
        setupResource();
        // load the document and simply test the "view page"

        String docPath = "/document/" + documentId;
        String newDocPath = "/document/" + TestConstants.TEST_DOCUMENT_ID;
        // go to the edit page, change the action, change the title and hit submit
        gotoPage(docPath + "/edit");
        HtmlElement form = getHtmlPage().getElementByName("metadataForm");
        assertNotNull("should have found the resource metadata form", form);
        form.setAttribute("action", docPath);

        String testTitle = "this is another test";
        setInput("id", TestConstants.TEST_DOCUMENT_ID);
        setInput("document.title", testTitle);
        submitForm();
        gotoPage(newDocPath);
        logger.debug("TITLE TEXT: " + getHtmlPage().getTitleText());
        assertTextNotPresent(testTitle);
    }

}

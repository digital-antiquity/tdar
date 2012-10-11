/**
 * 
 */
package org.tdar.web;

import static org.tdar.TestConstants.ADMIN_PROJECT_ID;
import static org.tdar.TestConstants.TEST_DOCUMENT;
import static org.tdar.TestConstants.TEST_DOCUMENT_NAME;

import java.net.URL;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.URLConstants;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Adam Brin
 * 
 */
public class FirstRunITCase extends AbstractAuthenticatedWebTestCase {

    public static final String TEST_TITLE = "This is a test title";
    public static final String TEST_ABSTRACT = "This is a test abstract";

    @Test
    public void confirmCreateDocument() {
        String originalLocation = getCurrentUrlPath();
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TEST_DOCUMENT);
        gotoPage(originalLocation);
        // assume that you're at /project/list
        clickLinkWithText("New Document");
        assertTextPresentInPage("Create a new Document");
        setInput("document.title", TEST_TITLE);
        setInput("document.description", TEST_ABSTRACT);
        setInput("document.date", "1934");
        setInput("ticketId", ticketId);
        setInput("projectId", Long.toString(ADMIN_PROJECT_ID));
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        
        addFileProxyFields(0, FileAccessRestriction.PUBLIC, TEST_DOCUMENT_NAME);
        submitForm();
        HtmlPage page = (HtmlPage) internalPage;
        logger.info(page.getUrl().toString());

        // make sure we're on the view page
        assertPageTitleEquals(TEST_TITLE);
        assertTextPresentInPage(TEST_ABSTRACT);
        assertTextPresentInPage(TEST_DOCUMENT_NAME);
        URL url = internalPage.getUrl();

        // change resource to draft, assert you can still see the view page
        // FIXME: I'm able to save document as draft as a non-admin in my local instance, but it fails in the web test. Not sure what's going on.
        clickLinkWithText("edit");
        setInput("status", Status.DRAFT.name());
        submitForm();
        assertPageTitleEquals(TEST_TITLE);

        // now delete it, assert you cannot go to view page anymore
        clickLinkWithText("delete");
        submitForm("delete");
        assertCurrentUrlEquals(getBaseUrl() + URLConstants.DASHBOARD);
        gotoPage(url.toString());
        assertPageTitleEquals("Access Denied");
    }

    @Test
    public void confirmCreateCodingSheet() {
        // assume that you're at /project/list
        clickLinkWithText("New Coding Sheet");

        String codingSheetRules = "1,test,test description\r\n";
        codingSheetRules += "2,test2,\"another description\"";

        assertTextPresentInPage("Create a new Coding Sheet");
        setInput("codingSheet.title", TEST_TITLE);
        setInput("codingSheet.date", "1934");
        setInput("codingSheet.description", TEST_ABSTRACT);
        setInput("fileTextInput", codingSheetRules);
        setInput("projectId", Long.toString(ADMIN_PROJECT_ID));
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        
        submitForm();
        assertPageTitleEquals(TEST_TITLE);
        assertTextPresentInPage(TEST_ABSTRACT);
        assertTextPresentInPage("another description");
        assertTextPresentInPage("test description");
        URL url = internalPage.getUrl();

        clickLinkWithText("delete");
        submitForm("delete");
        assertCurrentUrlEquals(getBaseUrl() + URLConstants.DASHBOARD);

        gotoPage(url.toString());
        assertPageTitleEquals("Access Denied");
    }

}
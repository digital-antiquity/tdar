/**
 * 
 */
package org.tdar.web;

import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.UrlConstants;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.junit.RunWithTdarConfiguration;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleWebTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.FAIMS })
public class FirstRunWebITCase extends AbstractAuthenticatedWebTestCase {

    public static final String TEST_TITLE = "This is a test title";
    public static final String TEST_ABSTRACT = "This is a test abstract";

    @Test
    public void confirmCreateDocument() {
        String originalLocation = getCurrentUrlPath();
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TestConstants.TEST_DOCUMENT);
        gotoPage(originalLocation);
        // assume that you're at /project/list
        // logger.info(getPageText());

        clickLinkWithText("Upload");
        clickLinkWithText(ResourceType.DOCUMENT.getLabel());
        assertTextPresentInPage("Create a new Document");
        setInput("document.title", TEST_TITLE);
        setInput("document.description", TEST_ABSTRACT);
        setInput("document.date", "1934");
        setInput("ticketId", ticketId);
        setInput("projectId", Long.toString(TestConstants.ADMIN_PROJECT_ID));
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }

        addFileProxyFields(0, FileAccessRestriction.PUBLIC, TestConstants.TEST_DOCUMENT_NAME);
        // logger.info(getPageCode());
        submitForm();
        HtmlPage page = (HtmlPage) internalPage;
        logger.info(page.getUrl().toString());

        // make sure we're on the view page
        assertPageTitleContains(TEST_TITLE);
        assertTextPresentInPage(TEST_ABSTRACT);
        assertTextPresentInPage(TestConstants.TEST_DOCUMENT_NAME);
        URL url = internalPage.getUrl();

        // change resource to draft, assert you can still see the view page
        // FIXME: I'm able to save document as draft as a non-admin in my local instance, but it fails in the web test. Not sure what's going on.
        clickLinkWithText("edit");
        setInput("status", Status.DRAFT.name());
        submitForm();
        assertPageTitleContains(TEST_TITLE);

        // now delete it, assert you cannot go to view page anymore
        clickLinkWithText("delete");
        submitForm("delete");
        assertCurrentUrlContains(UrlConstants.DASHBOARD);
        gotoPage(url.toString());
        assertPageTitleContains("Access Denied");
    }

    @Test
    public void confirmCreateCodingSheet() {
        // assume that you're at /project/list
        clickLinkWithText("Upload");
        clickLinkWithText(ResourceType.CODING_SHEET.getLabel());

        String codingSheetRules = "1,test,test description\r\n";
        codingSheetRules += "2,test2,\"another description\"";

        assertTextPresentInPage("Create a new Coding Sheet");
        setInput("codingSheet.title", TEST_TITLE);
        setInput("codingSheet.date", "1934");
        setInput("codingSheet.description", TEST_ABSTRACT);
        setInput("fileTextInput", codingSheetRules);
        setInput("projectId", Long.toString(TestConstants.ADMIN_PROJECT_ID));
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }

        submitForm();
        assertPageTitleContains(TEST_TITLE);
        assertTextPresentInPage(TEST_ABSTRACT);
        assertTextPresentInPage("another description");
        assertTextPresentInPage("test description");
        URL url = internalPage.getUrl();

        clickLinkWithText("delete");
        submitForm("delete");
        assertCurrentUrlContains(UrlConstants.DASHBOARD);

        gotoPage(url.toString());
        assertPageTitleContains("Access Denied");
    }

}
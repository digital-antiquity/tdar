package org.tdar.web;

import java.net.URL;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ViewStatisticsWebITCase extends AbstractAuthenticatedWebTestCase {

    public static final String TEST_TITLE = "This is a test title";
    public static final String TEST_ABSTRACT = "This is a test abstract";

    @Test
    public void testViewStatisticsIncremented() {
        String originalLocation = getCurrentUrlPath();
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TestConstants.TEST_DOCUMENT);
        gotoPage(originalLocation);
        // assume that you're at /project/list
        // logger.info(getPageText());

        clickLinkWithText("UPLOAD");
        clickLinkWithText(ResourceType.DOCUMENT.getLabel());
        assertTextPresentInPage("Create a new Document");
        setInput("document.title", TEST_TITLE);
        setInput("document.description", TEST_ABSTRACT);
        setInput("document.date", "1934");
        setInput("ticketId", ticketId);
        setInput("projectId", Long.toString(TestConstants.ADMIN_PROJECT_ID));
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }

        addFileProxyFields(0, FileAccessRestriction.PUBLIC, TestConstants.TEST_DOCUMENT_NAME);
        // logger.info(getPageCode());
        submitForm();
        HtmlPage page = (HtmlPage) internalPage;
        logger.info(page.getUrl().toString());

        // make sure we're on the view page
        assertPageTitleEquals(TEST_TITLE);
        assertTextPresentInPage(TEST_ABSTRACT);
        assertTextPresentInPage(TestConstants.TEST_DOCUMENT_NAME);
        logger.info(getPageText());
        assertTextPresent("1 time");
        URL url = internalPage.getUrl();
        gotoPage(url.toString());
        assertTextPresent("2 time(s)");


    }

}

/**
 * 
 */
package org.tdar.web;

import static org.junit.Assert.assertTrue;
import static org.tdar.TestConstants.ADMIN_PROJECT_ID;
import static org.tdar.TestConstants.DEFAULT_BASE_URL;
import static org.tdar.TestConstants.TEST_DOCUMENT;
import static org.tdar.TestConstants.TEST_DOCUMENT_NAME;

import java.net.URL;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * @author Adam Brin
 *
 */
public class FirstRunITCase extends AbstractAuthenticatedWebTestCase {

	public static final String TEST_TITLE = "This is a test title";
	public static final String TEST_ABSTRACT = "This is a test abstract";

	@Test
	public void confirmCreateDocument(){
		String originalLocation = getCurrentUrlPath();
		String ticketId = getPersonalFilestoreTicketId();
		uploadFileToPersonalFilestore(ticketId, TEST_DOCUMENT);
		gotoPage(originalLocation);
		// assume that you're at /project/list
		clickLinkWithText("New Document");
		assertTextPresentInPage("Create a Document");
		setInput("document.title", TEST_TITLE);
		setInput("resource.description", TEST_ABSTRACT);
		setInput("ticketId", ticketId);
		setInput("projectId",Integer.toString(ADMIN_PROJECT_ID));
		addFileProxyFields(0, false, TEST_DOCUMENT_NAME);
		submitForm();
		HtmlPage page = (HtmlPage) internalPage;
		logger.info(page.getUrl().toString());
		logger.info(page.asText());
        assertTextPresentInPage(TEST_TITLE);
        assertTextPresentInPage(TEST_ABSTRACT);
        assertTextPresentInPage(TEST_DOCUMENT_NAME);
		URL url = internalPage.getUrl();

		clickLinkWithText("delete");
		submitForm("delete");
		assertTrue(internalPage.getUrl().toString().contains("/project/list"));
		gotoPage(url.toString());
		assertTrue(internalPage.getUrl().toString().equals(DEFAULT_BASE_URL + "/"));		
	}

	@Test
	public void confirmCreateCodingSheet() {
		// assume that you're at /project/list
		clickLinkWithText("New Coding Sheet");
		
		String codingSheetRules = "1,test,test description\r\n";
		codingSheetRules += "2,test2,\"another description\"";
		
		assertTextPresentInPage("Register a New Coding Sheet");
		setInput("codingSheet.title", TEST_TITLE);
		setInput("codingSheet.description", TEST_ABSTRACT);
		setInput("fileTextInput",codingSheetRules);
		setInput("projectId",Integer.toString(ADMIN_PROJECT_ID));
		submitForm();
        assertTextPresentInPage(TEST_TITLE);
        assertTextPresentInPage(TEST_ABSTRACT);
        assertTextPresentInPage("another description");
        assertTextPresentInPage("test description");
		URL url = internalPage.getUrl();

		clickLinkWithText("delete");
		submitForm("delete");
		assertTrue(internalPage.getUrl().toString() + " ? /project/list",internalPage.getUrl().toString().contains("/project/list"));
		gotoPage(url.toString());
		assertTrue(internalPage.getUrl().toString().equals(DEFAULT_BASE_URL + "/"));		
	}
}
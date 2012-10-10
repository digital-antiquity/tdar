/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.ResourceCreatorRoleType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.html.HtmlOption;


public class EditITCase extends AbstractAdminAuthenticatedWebTestCase {

    public static String PROJECT_ID_FIELDNAME = "projectId";
    public static String DOCUMENT_TITLE_FIELDNAME = "document.title";
    public static String DOCUMENT_TYPE_FIELDNAME = "document.documentType";
    public static String AUTHOR_NAME_FIRST_FIELDNAME = "authorFirstNames";
    public static String AUTHOR_NAME_LAST_FIELDNAME = "authorLastNames";
    public static String AUTHOR_EMAIL_FIELDNAME = "authorEmails";
    public static String AUTHOR_INSTITUTION_FIELDNAME = "authorInstitutions";
    public static String AUTHOR_ROLE_FIELDNAME = "authorRoles";
    public static String DESCRIPTION_FIELDNAME = "document.description";

    public static String PROJECT_ID = "3805";
    public static String DOCUMENT_TITLE = "My Sample Document";
    public static String DOCUMENT_TYPE = "BOOK_SECTION";
    public static String AUTHOR_NAME_FIRST = "Jim";
    public static String AUTHOR_NAME_LAST = "deVos";
    public static String AUTHOR_EMAIL = "jim.devos@asu.edu";
    public static String AUTHOR_INSTITUTION = "A wholly new institution name";
    public static String AUTHOR_ROLE = "CONTRIBUTOR";
    public static String DESCRIPTION = "A resource description";
    public static String MALFORMED_DATASET_FILE = TestConstants.TEST_DATA_INTEGRATION_DIR + "malformed_csv_dataset.csv";

    @Test
    public void testOntologyView() {
        super.testOntologyView();
        clickLinkWithText("edit");
        assertTextPresentInPage("Fauna Pathologies - Default Ontology Draft");
        assertButtonPresentWithText("Save");
    }

    @Test
    public void testCodingSheetView() {
        super.testCodingSheetView();
        clickLinkWithText("edit");
        assertButtonPresentWithText("Save");
    }

    @Test
    public void testProjectView() {
        super.testProjectView();
        clickLinkWithText("edit");
        assertButtonPresentWithText("Save");
    }

    @Test
    public void testDocumentView() {
        super.testDocumentView();
        clickLinkWithText("edit");
        logger.trace(getPageText());
        assertButtonPresentWithText("Save");
    }

    @Test
    public void testDatasetView() {
        super.testDatasetView();
        clickLinkWithText("edit");
        assertButtonPresentWithText("Save");
        super.testDatasetView(); // back to view page, then to citations
        // clickLinkWithText("manage citations");
        // assertTextPresentInPage("Managing citation information for");
        assertTextNotPresent("internal error");
        super.testDatasetView(); // back to view page, then to column mapping
        clickLinkWithText(TABLE_METADATA);
        assertTextPresentInPage("Table Metadata for ");
        // TODO: need a resource associated w/ ontologies
        // clickLinkWithText("link ontology");
        // assertTextPresentInPage("Match column values");
    }

    // an malformed file should take you back to save.action and contain an action error
    @Test
    public void testMalformedAttachmentDisplaysError() {
        
        // create a new dataset resource w/o a file
        gotoPage("/dataset/add");
        setInput("projectId", "-1");
        setInput("dataset.title", "testMalformedAttachmentDisplaysError");
        setInput("dataset.date", "2002");
        setInput("dataset.description", "trying to save this with a malformed csv should return action errors ");
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        submitForm();

        // now go to the edit page and try to upload a malformed file
        clickLinkWithText("edit");
        setInput("uploadedFiles", MALFORMED_DATASET_FILE);
        submitFormWithoutErrorCheck("Save");
        // we should still be on the edit page
        assertTextPresentInPage("Editing Dataset");
        // we should have an action error
        assertTextPresent("has more columns");
        assertTextPresentInCode("action-errors");
    }
    
    @Test
    public void testFakeRolesShouldNotAppearInEditSelects() {
    	//FIXME: there's a more automagical way to create this list, yeah?
    	List<String> urls = new ArrayList<String>();
    	for(ResourceType resourceType : ResourceType.values()) {
    	    String url = String.format("/%s/add", resourceType.getUrlNamespace());
    		assertNoFakeRoles(url);
    	}
    }
    
    private void assertNoFakeRoles(String editPage) {
    	gotoPage(editPage);
    	List<Element> selectElements = querySelectorAll(".creator-role-select option");
    	for(Element element : selectElements) {
    		HtmlOption option = (HtmlOption) element;
    		logger.trace("looking for fake roles in {}", option);
			ResourceCreatorRole role = ResourceCreatorRole.valueOf(option.getValueAttribute());
			assertThat("OTHER role should not appear on this page", role.getType(), not(ResourceCreatorRoleType.OTHER));
    	}
    }

}

/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import org.junit.Test;
import org.tdar.TestConstants;

public class EditITCase extends AbstractAdminAuthenticatedWebTestCase {
    
    public static String PROJECT_ID_FIELDNAME = "projectId";
    public static String DOCUMENT_TITLE_FIELDNAME = "document.title";
    public static String DOCUMENT_TYPE_FIELDNAME = "document.documentType";
    public static String AUTHOR_NAME_FIRST_FIELDNAME = "authorFirstNames";
    public static String AUTHOR_NAME_LAST_FIELDNAME = "authorLastNames";
    public static String AUTHOR_EMAIL_FIELDNAME = "authorEmails";
    public static String AUTHOR_INSTITUTION_FIELDNAME = "authorInstitutions";
    public static String AUTHOR_ROLE_FIELDNAME = "authorRoles";
    public static String DESCRIPTION_FIELDNAME = "resource.description";
    
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
		assertTextPresentInPage("TAG Fauna Ontology - Taxon");
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
		assertButtonPresentWithText("Save");
	}

	@Test 
	public void testDatasetView() {
		super.testDatasetView();
		clickLinkWithText("edit");
		assertButtonPresentWithText("Save");
		super.testDatasetView();  //back to view page, then to citations
        clickLinkWithText("manage citations");
        assertTextPresentInPage("Managing citation information for");
        assertTextNotPresent("internal error");
        super.testDatasetView();  //back to view page, then to column mapping
        clickLinkWithText("map columns");
        assertTextPresentInPage("tDAR column metadata registration");
        //TODO: need a resource associated w/ ontologies
        //clickLinkWithText("link ontology");
        //assertTextPresentInPage("Match column values");
	}
	
	
	//an malformed file should take you back to save.action and contain an action error
	@Test
	public void testMalformedAttachmentDisplaysError() {
	    super.testDatasetView();
        clickLinkWithText("edit");
        setInput("uploadedFiles", MALFORMED_DATASET_FILE);
        submitFormWithoutErrorCheck("Save");
        //we should still be on the edit page
        assertTextPresentInPage("Editing Dataset");
        //we should have an action error
        assertTextPresentInCode("action-errors");
	}
	
//	@Test
	public void testMalformedAttachmentDoesntSave() {
	    String resourceTitle  = "malformed upload test";
	    gotoPage("/dataset/add");
	    setInput("dataset.title", "malformed upload test");
	    setInput("dataset.description", "desc");
        setInput("uploadedFiles", MALFORMED_DATASET_FILE);
        submitFormWithoutErrorCheck("Save"); 
        //we should have been booted back to edit page... go to the resource list anyway
        gotoPage("/project/list");
	    assertTextNotPresent(resourceTitle);
	}

}

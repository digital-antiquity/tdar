/**
 *
 * @author Adam Brin
 *
 */

package org.tdar.web.resource;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.ResourceCreatorRoleType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlOption;

@RunWith(MultipleWebTdarConfigurationRunner.class)
public class EditWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String EXACT_LOCATION_CHECKBOX_TEXT = "Reveal location to public users?";
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
    public static String AUTHOR_EMAIL = "jim.devos@dsu.edu";
    public static String AUTHOR_INSTITUTION = "A wholly new institution name";
    public static String AUTHOR_ROLE = "CONTRIBUTOR";
    public static String DESCRIPTION = "A resource description";
    public static String MALFORMED_DATASET_FILE = TestConstants.TEST_DATA_INTEGRATION_DIR + "malformed_csv_dataset.csv";

    @Override
    @Test
    public void testOntologyView() {
        super.testOntologyView();
        clickLinkWithText("edit");
        assertTextPresentInPage("Fauna Pathologies - Default Ontology Draft");
        assertTextNotPresent(EXACT_LOCATION_CHECKBOX_TEXT);
        assertButtonPresentWithText("Save");
    }

    @Override
    @Test
    public void testCodingSheetView() {
        super.testCodingSheetView();
        clickLinkWithText("edit");
        assertButtonPresentWithText("Save");
    }

    @Override
    @Test
    public void testProjectView() {
        reindex();
        super.testProjectView();
        clickLinkWithText("edit");
        assertButtonPresentWithText("Save");
    }

    @Override
    @Test
    public void testDocumentView() {
        super.testDocumentView();
        clickLinkWithText("edit");
        logger.trace(getPageText());
        assertButtonPresentWithText("Save");
    }

    @Override
    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR_DISABLED_OBFUSCATION })
    public void testDatasetView() {
        super.testDatasetView();
        clickLinkWithText("edit");
        // following should not appear on the page when run with the default TDAR property file
        assertTextNotPresent(EXACT_LOCATION_CHECKBOX_TEXT);
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

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testFaimsDatasetView() {
        super.testDatasetView();
        clickLinkWithText("edit");
        // following should appear on the page when run with the FAIMS property file
        assertTextPresent(EXACT_LOCATION_CHECKBOX_TEXT);
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
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        submitForm();
        String url = getCurrentUrlPath();
        String ticketId = getPersonalFilestoreTicketId();
        assertTrue("Expected integer number for ticket - but got: " + ticketId, ticketId.matches("([0-9]*)"));

        uploadFileToPersonalFilestore(ticketId, MALFORMED_DATASET_FILE);

        gotoPage(url);
        // now go to the edit page and try to upload a malformed file
        clickLinkWithText("edit");

        setInput("ticketId", ticketId);
        addFileProxyFields(0, FileAccessRestriction.PUBLIC, MALFORMED_DATASET_FILE);
        submitFormWithoutErrorCheck("Save");
        // we should still be on the edit page
        assertTextPresentInPage("Editing Dataset");
        // we should have an action error
        assertTextPresent("has more columns");
        assertTextPresentInCode("action-errors");
    }

    @Test
    public void testFakeRolesShouldNotAppearInEditSelects() {
        for (ResourceType resourceType : ResourceType.values()) {
            String url = String.format("/%s/add", resourceType.getUrlNamespace());
            assertNoFakeRoles(url);
        }
    }

    @Test
    // failing to fill out all required fields should cause controller to send us back to the edit page (i.e.action should return INPUT)
    public void testDocumentInputPage() {
        gotoPage("/document/add");

        setInput(TestConstants.DOCUMENT_FIELD_TITLE, "");
        setInput(TestConstants.DOCUMENT_FIELD_DESCRIPTION, "");
        setInput("projectId", TestConstants.PARENT_PROJECT_ID);
        submitForm();

        // we should be sent back to the edit page with some error messages at the top (but no exceptions/ stack traces)
        assertNoErrorTextPresent();
    }

    private void assertNoFakeRoles(String editPage) {
        gotoPage(editPage);
        for (DomNode element_ : htmlPage.getDocumentElement().querySelectorAll(".creator-role-select option")) {
            Element element = (Element) element_;
            HtmlOption option = (HtmlOption) element;
            logger.trace("looking for fake roles in {}", option);
            ResourceCreatorRole role = ResourceCreatorRole.valueOf(option.getValueAttribute());
            assertThat("OTHER role should not appear on this page", role.getType(), not(ResourceCreatorRoleType.OTHER));
        }
    }

}

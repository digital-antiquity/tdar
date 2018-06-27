package org.tdar.web.resource;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class ProjectWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String PROJECT_NAME = "changing project test";
    public static String REGEX_DOCUMENT_VIEW = "/document/(\\d+)$";

    @Test
    public void testAddingInformationResourceToProject() {
        String resourceName = "newresource";
        setupDocumentWithProject(resourceName);

        gotoPage("/project/" + TestConstants.PARENT_PROJECT_ID);
        assertTextPresent(resourceName);
    }

    private void setupDocumentWithProject(String resourceName) {
        gotoPage("/project/" + TestConstants.PARENT_PROJECT_ID);

        gotoPage("/document/add");
        setInput("document.title", resourceName);
        setInput("document.description", "hi mom");
        setInput("document.date", "1999");
        setInput("projectId", TestConstants.PARENT_PROJECT_ID.toString());
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        submitForm();
    }

    @Test
    public void testChangingProject() {
        String resourceName = "changing project resource " + System.currentTimeMillis();
        Long projectId = createResourceFromType(ResourceType.PROJECT, PROJECT_NAME);
        setupDocumentWithProject(resourceName);
        String url = getCurrentUrlPath();
        // get the id of the new resource

        gotoPage("/project/" + TestConstants.PARENT_PROJECT_ID);
        assertTextPresent(resourceName);
        gotoPage(url);
        clickLinkWithText("edit");
        setInput("projectId", projectId.toString());
        submitForm();
        gotoPage("/project/" + TestConstants.PARENT_PROJECT_ID);
        assertTextNotPresent(resourceName);
        gotoPage("/project/" + projectId.toString());
        assertTextPresent(resourceName);
        gotoPage(url);
        clickLinkWithText("edit");
        setInput("projectId", "-1");
        submitForm();
        assertTextNotPresent(PROJECT_NAME);
        gotoPage("/project/" + projectId.toString());
        assertTextNotPresent(resourceName);

    }
}

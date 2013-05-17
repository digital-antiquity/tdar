package org.tdar.web;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.configuration.TdarConfiguration;

public class ProjectWebITCase extends AbstractAdminAuthenticatedWebTestCase{

    public static String REGEX_DOCUMENT_VIEW = "/document/(\\d+)$";

    @Test
    public void testAddingInformationResourceToProject() {
        String resourceName = "newresource";
        
        gotoPage("/project/" + TestConstants.PARENT_PROJECT_ID);
        
        gotoPage("/document/add");
        setInput("document.title", resourceName);
        setInput("document.description",  "hi mom");
        setInput("document.date", "1999");
        setInput("projectId", TestConstants.PARENT_PROJECT_ID.toString());
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
//            setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        submitForm();
        //get the id of the new resource
        
        gotoPage("/project/" + TestConstants.PARENT_PROJECT_ID);
        assertTextPresent(resourceName);
    }
}

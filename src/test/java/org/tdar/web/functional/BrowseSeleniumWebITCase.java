package org.tdar.web.functional;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.tdar.TestConstants;

import static  org.junit.Assert.*;

/**
 * User: jimdevos
 * Date: 7/1/13
 */
public class BrowseSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Before
    public void  beforeTest() {
        loginAdmin();
    }


    @After
    public void afterTest() {
        logout();
    }


    private void browseTest(String url) {
        gotoPage(url);
        waitFor("body");
        logger.debug("on page: {}", url);
        find(By.partialLinkText("EDIT")).first().click();
        String editUrl = getDriver().getCurrentUrl();
        assertTrue("url should contain 'edit'. url:"  + editUrl, editUrl.contains("edit"));

        //we could do this implicitly by going to any other page but this makes the test faster
        reportJavascriptErrors();

    }

    @Test
    public void testOntology() {
        browseTest("/ontology/3029");
    }

    @Test
    public void testCodingSheet() {
        browseTest("/coding-sheet/449");
    }

    @Test
    public void testProject() {
        browseTest("/project/3805");
    }

    @Test
    public void testDocument() {
        browseTest("/document/" + TestConstants.TEST_DOCUMENT_ID);
    }

    @Test
    public void testDataset() {
        browseTest("/dataset/3088");
    }

    //TODO: need urls for Image, Sensory Data, Video, Geospatial


}

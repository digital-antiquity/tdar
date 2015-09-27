package org.tdar.functional;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;

/**
 * User: jimdevos
 * Date: 7/1/13
 */
public class BrowseSeleniumWebITCase extends AbstractAdminSeleniumWebITCase {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public BrowseSeleniumWebITCase() {
        // assumption: google quota errors are only expected on pages that show maps
        getJavascriptIgnorePatterns().add(TestConstants.REGEX_GOOGLE_QUOTA_SERVICE_RECORD_EVENT);
    }

    private void browseTest(String url) {
        gotoPage(url);
        waitFor("body");
        logger.debug("on page: {}", url);
        find(By.partialLinkText("EDIT")).first().click();
        String editUrl = getDriver().getCurrentUrl();
        assertTrue("url should contain 'edit'. url:" + editUrl, editUrl.contains("edit"));

        // we could do this implicitly by going to any other page but this makes the test faster
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

    @Test
    public void testSensoryData() {
        browseTest("/sensory-data/4289");
    }

    @Test
    public void testImage() {
        browseTest("/image/4292");
    }

    @Test
    public void testVideo() {
        browseTest("/video/4290");
    }

    @Test
    public void testGeospatial() {
        browseTest("/geospatial/4291");
    }

    @Test
    public void testResourceCollection() {
        browseTest("/collection/1575");
    }

}

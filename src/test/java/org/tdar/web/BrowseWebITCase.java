/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;

public class BrowseWebITCase extends AbstractAnonymousWebTestCase {
    boolean indexed = false;

    public void reindexOnce() {
        if (indexed) {
            return;
        }

        reindexUnauthenticated();
        indexed = true;
    }

    @Test
    @Override
    public void testOntologyView() {
        super.testOntologyView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    @Override
    public void testCodingSheetView() {
        super.testCodingSheetView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    @Override
    public void testProjectView() {
        reindexOnce();
        super.testProjectView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    @Override
    public void testDocumentView() {
        super.testDocumentView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    @Override
    public void testDatasetView() {
        super.testDatasetView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    public void testExploreView() {
        reindexOnce();
        gotoPage("/browse/explore");
    }

    @Test
    public void testExploreGoogleScholar() {
        reindexOnce();
        gotoPage("/year-2012");
    }

    @Test
    public void testMapView() {
        gotoPage("/map");
    }

    @Test
    public void testFeaturedItemView() {
        gotoPage("/featured");
    }

    @Test
    public void testRobots() {
        gotoPage("/robots.txt");
    }

    @Test
    public void testBrowseCreators() {
        reindexOnce();
        gotoPage("/browse/creators/8161");
    }

    @Test
    public void testBrowseCollections() {
        reindexOnce();
        gotoPage("/browse/collections");
    }

    @Test
    public void testViewErrorNotFound() {
        // if you make more than 9999999 test records this test won't work anymore. so don't.
        int statusCode = gotoPageWithoutErrorCheck("/dataset/9999999");
        assertEquals("expecting 404 error", HttpStatus.SC_NOT_FOUND, statusCode);
        assertTextPresent("Sorry, the page you requested cannot be found");
    }

    @Test
    public void testViewErrorBadRequest() {
        // Ideally, this would be a "Bad Request", but it's acceptable that it's a 404 due to the struts type mapping not matching the id as a String
        int statusCode = gotoPageWithoutErrorCheck("/dataset/pay_no_attention_to_this_url");
        logger.debug("STATUS CODE: {}", statusCode);
        // was 400 prior
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, statusCode);
        // assertTextPresent("cannot be found");
        // FIXME: status code will be 200 instead, see http://dev.tdar.org/jira/browse/TDAR-1842 for more details
        // assertEquals("invalid id should 404: ", HttpStatus.SC_NOT_FOUND, statusCode);
        // assertEquals("expecting bad request error", HttpStatus.SC_BAD_REQUEST, statusCode);
    }

    @Test
    public void testViewDeletedResource() {
        int deletedOntologyId = 3479; // FIXME: make this test less brittle by creating a deleted resource.
        int statusCode = gotoPageWithoutErrorCheck("/ontology/" + deletedOntologyId);
        assertPageTitleEquals("Access Denied");
    }

}

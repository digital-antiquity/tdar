/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.Test;

public class BrowseWebITCase extends AbstractAnonymousWebTestCase {
    static boolean indexed = false;

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
        skipHtmlValidation = true;
        gotoPage("/map");
        skipHtmlValidation = false;
    }

    @Test
    public void testFeaturedItemView() {
        skipHtmlValidation = true;
        gotoPage("/featured");
        skipHtmlValidation = false;
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
        int statusCode = gotoPageWithoutErrorCheck("/dataset/pay_no_attention_to_this_url");
        logger.debug("STATUS CODE: {}", statusCode);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, statusCode);
    }

    @Test
    public void testViewDeletedResource() {
        int deletedOntologyId = 3479; // FIXME: make this test less brittle by creating a deleted resource.
        int statusCode = gotoPageWithoutErrorCheck("/ontology/" + deletedOntologyId);
        assertPageTitleEquals("Access Denied");
    }

}

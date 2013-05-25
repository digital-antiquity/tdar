/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class BrowseWebITCase extends AbstractAnonymousWebTestCase {

    @Before
    public void reindexBefore() {
        reindexUnauthenticated();
    }
    
    @Test
    public void testOntologyView() {
        super.testOntologyView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    public void testCodingSheetView() {
        super.testCodingSheetView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    public void testProjectView() {
        super.testProjectView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    public void testDocumentView() {
        super.testDocumentView();
        assertTextNotPresent("edit metadata");
    }

    @Test
    public void testDatasetView() {
        super.testDatasetView();
        assertTextNotPresent("edit metadata");
    }

    
    @Test
    public void testExploreView() {
        gotoPage("/browse/explore");
    }

    @Test
    public void testExploreGoogleScholar() {
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
    public void testBrowseCreators() {
        gotoPage("/browse/creators/1");
    }

    @Test
    public void testBrowseCollections() {
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
        int statusCode = gotoPageWithoutErrorCheck("/dataset/view?id=pay_no_attention_to_this_url");
        assertTextPresent("Sorry, the page you requested cannot be found");
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

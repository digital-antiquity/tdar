/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class BrowseITCase extends AbstractAnonymousWebTestCase {

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
    public void testViewErrorNotFound() {
        // if you make more than 9999999 test records this test won't work anymore. so don't.
        int statusCode = gotoPageWithoutErrorCheck("/dataset/9999999");
        assertEquals("expecting 404 error", HttpStatus.SC_NOT_FOUND, statusCode);
        assertTextPresent("Sorry, the page you requested cannot be found");
    }
	
	@Test
	public void testViewErrorBadRequest() {
	    int statusCode = gotoPageWithoutErrorCheck("/dataset/view?resourceId=pay_no_attention_to_this_url");
        assertEquals("expecting bad request error", HttpStatus.SC_BAD_REQUEST, statusCode);
	}
	
	@Test
	public void testViewDeletedResource() {
	    int deletedOntologyId = 3479;  //FIXME:   make this test less brittle by creating a deleted resource.
        int statusCode = gotoPageWithoutErrorCheck("/ontology/" + deletedOntologyId);
        logger.debug(getPageText());
        assertEquals("expecting bad request error", HttpStatus.SC_GONE, statusCode);
	}
	
}

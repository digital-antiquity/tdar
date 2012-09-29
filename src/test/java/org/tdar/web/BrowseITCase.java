/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import org.junit.Test;


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
	
}

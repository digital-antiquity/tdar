package org.tdar.web.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class WhiteLabelCollectionWebITCase extends AbstractAdminAuthenticatedWebTestCase {

	private static final String BODY_FONT_COLOR_RUST = "{body:font-color:dark-orange;}";
	private static final String ABCDEF = "abcdef";
	private static final String EDIT = "/collection/admin/whitelable/1003/edit";
	private static final String COLLECTION_2580 = "/collection/1003";

	@Before
	public void setupWhiteLabel() {
		gotoPage(COLLECTION_2580);
		if (getPageText().contains("Shared (white label)")) {
			// already a white-label collection
			return;
		} else {
			clickElementWithId("makeWhiteLabelCollection");
		}
	}

	@Test
	public void testSubheader() {
		gotoPage(EDIT);
		setInput("collection.subtitle", ABCDEF);
		submitForm();
		assertTrue("page contains new subtitle",getPageText().contains(ABCDEF));
	}

	@Test
	public void testCustomCss() {
		gotoPage(EDIT);
		setInput("collection.css", BODY_FONT_COLOR_RUST);
		submitForm();
		assertTrue("page contains custom css",getPageCode().contains(BODY_FONT_COLOR_RUST));
	}

	@Test
	public void testWhitelabelBooleanSettings() {
		String[] names = new String[] { "collection.customHeaderEnabled", "collection.customDocumentLogoEnabled",
				"collection.featuredResourcesEnabled", "collection.searchEnabled", "collection.subCollectionsEnabled" };
		for (String name : names) {
			gotoPage(EDIT);
			setInput(name, "true");
			submitForm();
			// implicitly test FTL
			if (name.contains("search")) {
				assertTrue("search form text in page", getPageCode().contains("Search within this collection..."));
			}

			if (name.contains("Document")) {
				gotoPage("/resource/4292");
				//FIXME: need image
				gotoPage(COLLECTION_2580);
			}

			if (name.contains("featured")) {
				assertTrue("page contains featured resource", getPageCode().contains("2008 New Philadelphia Archaeology"));
			}
			
			if (name.contains("subCollections")) {
				assertEquals("page contains subcollection 3 times (title 2x, description 1x)", 3, StringUtils.countMatches(getPageText(), "display_orientationGRID"));
			}

		}
	}
}

package org.tdar.web.collection;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.tdar.web.AbstractEditorAuthenticatedWebTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class CollectionBatchEditWebITCase extends AbstractEditorAuthenticatedWebTestCase {

    private static final String HORP_DESCRIPTION = "horp description";
    private static final String HORP_TITLE = "HORP title";
    private static final String HORP_ID = "1628";



    @Test
    public void testBatchEdit() {
        gotoPage("/collection/admin/batch/1004");
        saveHtml("batch_1004");


        boolean seen = querySelectorAll(".resource-id-field").stream()
                .peek( input -> logger.debug("looking for id in {}", input))
                .anyMatch(input -> StringUtils.equals(HORP_ID, (input.getAttribute("value"))));
        if(!seen) {
            logger.debug(getPageText());
        }
        assertTrue("at least one hidden input should have value:" + HORP_ID, seen);

        //now change some titles (up to five)
        querySelectorAll(".resource-title-field").stream().limit(5)
                .forEach(input ->  input.setAttribute("value", HORP_TITLE));

        // and change some descriptions
        querySelectorAll(".resource-description-field").stream().limit(5)
                .forEach(input -> input.setAttribute("value", HORP_DESCRIPTION));


        submitForm();
        gotoPage("/collection/1004");
        assertTextPresentInPage(HORP_TITLE);
        assertTextPresentInPage(HORP_DESCRIPTION);
        assertTextNotPresent("(HARP)");

        logger.debug(getPageText());
    }
}
        
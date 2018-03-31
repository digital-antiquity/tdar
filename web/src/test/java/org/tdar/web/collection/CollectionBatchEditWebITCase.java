package org.tdar.web.collection;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.tdar.web.AbstractEditorAuthenticatedWebTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class CollectionBatchEditWebITCase extends AbstractEditorAuthenticatedWebTestCase {

    private static final String HORP_DESCRIPTION = "horp description";
    private static final String HORP_TITLE = "HORP title";

    @Test
    public void testBatchEdit() {
        gotoPage("/collection/admin/batch/1004");
        boolean seen = false;
        for (int i = 0; i < 5; i++) {
            try {
                HtmlElement input = getInput("ids[" + i + "]");
                logger.debug("{} --> {} --> {}", input, input.getNodeValue(), input.getAttribute("value"));
                if (StringUtils.equals(input.getAttribute("value"), "1628")) {
                    setInput("titles[" + i + "]", HORP_TITLE);
                    setInput("descriptions[" + i + "]", HORP_DESCRIPTION);
                    // setInput("dates["+i+"]", _12345);
                    seen = true;
                }
            } catch (Exception e) {
            }
        }
        assertTrue("should see HARP ID", seen);
        logger.debug(getPageText());
        submitForm();
        gotoPage("/collection/1004");
        assertTextPresentInPage(HORP_TITLE);
        assertTextPresentInPage(HORP_DESCRIPTION);
        // assertTextPresentInPage(_12345);
        assertTextNotPresent("(HARP)");

        logger.debug(getPageText());
    }
}

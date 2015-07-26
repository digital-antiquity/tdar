package org.tdar.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.tdar.TestConstants;

public class UnApiWebITCase extends AbstractAuthenticatedWebTestCase {

    @Test
    public void testFormatList() {
        int gotoPage = gotoPage("/unapi");
        assertEquals(200, gotoPage);
        logger.debug(getPageBodyCode());
        assertTrue(getPageCode().contains("oai_dc"));
        assertTrue(getPageCode().contains("mods"));
    }

    @Test
    public void testPageContainsUnapiTags() {
        int gotoPage = gotoPage("/document/" + TestConstants.TEST_DOCUMENT_ID);
        assertEquals(200, gotoPage);
        assertTrue(getPageCode().contains("unapi-server"));
        assertTrue(getPageCode().contains("unapi-id"));
    }

    @Test
    public void testModsRequest() {
        int gotoPage = gotoPage("/unapi?format=MODS&id=" + TestConstants.TEST_DOCUMENT_ID);
        assertEquals(200, gotoPage);
        logger.debug(getPageCode());
        assertTrue(getPageCode().contains("titleInfo"));
        assertTrue(getPageCode().contains("/mods/v3"));
        assertTrue(getPageCode().contains("extent"));
    }

    @Test
    public void testDcRequest() {
        int gotoPage = gotoPage("/unapi?format=oai_dc&id=" + TestConstants.TEST_DOCUMENT_ID);
        assertEquals(200, gotoPage);
        logger.debug(getPageCode());
        assertTrue(getPageCode().contains("/OAI/2.0/oai_dc"));
        assertTrue(getPageCode().contains(":dc"));
        assertTrue(getPageCode().contains("type"));
    }
}

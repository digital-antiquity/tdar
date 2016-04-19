package org.tdar.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Test;
import org.tdar.core.service.RssService;

public class XMLCharMatcherTest {

    @Test
    public void test() {
        String tst = "\u0001";
        Matcher matcher = RssService.INVALID_XML_CHARS.matcher(tst);
        assertTrue(matcher.matches());
    }

    @Test
    public void testControl() {
        String tst = "\u0018";
        Matcher matcher = RssService.INVALID_XML_CHARS.matcher(tst);
        assertTrue(matcher.matches());
    }

    @Test
    public void testValidChar() {
        String tst = "abc1";
        Matcher matcher = RssService.INVALID_XML_CHARS.matcher(tst);
        assertFalse(matcher.matches());
        assertEquals(tst, RssService.stripInvalidXMLCharacters(tst));
    }

}

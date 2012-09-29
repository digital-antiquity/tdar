package org.tdar.core.search;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Test;
import org.tdar.struts.action.search.LuceneSearchController;

public class XMLCharMatcherTest {

    @Test
    public void testBadChar() {
        String tst = "\u0001";
        Matcher matcher = LuceneSearchController.INVALID_XML_CHARS.matcher(tst);
        assertTrue(matcher.matches());
    }

    @Test
    public void testValidChar() {
        String tst = "abc1";
        Matcher matcher = LuceneSearchController.INVALID_XML_CHARS.matcher(tst);
        assertFalse(matcher.matches());
        assertEquals(tst,LuceneSearchController.cleanStringForXML(tst));
    }


}

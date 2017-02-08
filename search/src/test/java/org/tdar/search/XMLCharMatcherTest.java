package org.tdar.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Test;
import org.tdar.core.service.RssService;
import org.tdar.utils.XmlEscapeHelper;

public class XMLCharMatcherTest {

    @Test
    public void test() {
        String tst = "\u0001";
        XmlEscapeHelper xse = new XmlEscapeHelper(-1L);
        assertEquals("", xse.stripNonValidXMLCharacters(tst));
    }

    @Test
    public void testControl() {
        String tst = "\u0018";
        XmlEscapeHelper xse = new XmlEscapeHelper(-1L);
        assertEquals("", xse.stripNonValidXMLCharacters(tst));
    }

    @Test
    public void testValidChar() {
        String tst = "abc1";
        XmlEscapeHelper xse = new XmlEscapeHelper(-1L);
        assertEquals("abc1", xse.stripNonValidXMLCharacters(tst));
    }

}

package org.tdar.search;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.search.query.part.PhraseFormatter;


public class PhraseFormatterTest {

    @Test
    public void testEscape() {
        Assert.assertEquals("a\\ b",PhraseFormatter.ESCAPED.format("a b"));
        Assert.assertEquals("",PhraseFormatter.ESCAPED.format(" "));
        Assert.assertEquals("fun'",PhraseFormatter.ESCAPED.format("fun'"));
    }
    
    @Test
    public void testEmbeddedQuotes() {
        String text = PhraseFormatter.ESCAPED_EMBEDDED.format("USGS DEADMAN LAKE SW 7.5' QUAD\"");
        
        Assert.assertEquals("fun\\'",PhraseFormatter.ESCAPED_EMBEDDED.format("fun'"));
        assertEquals("USGS\\ DEADMAN\\ LAKE\\ SW\\ 7.5\\'\\ QUAD\\\"", text);
    }

}

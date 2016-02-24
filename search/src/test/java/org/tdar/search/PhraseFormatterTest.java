package org.tdar.search;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.search.query.part.PhraseFormatter;


public class PhraseFormatterTest {

    @Test
    public void testEscape() {
        Assert.assertEquals("a\\ b",PhraseFormatter.ESCAPED.format("a b"));
        Assert.assertEquals("",PhraseFormatter.ESCAPED.format(" "));
        Assert.assertEquals("fun'",PhraseFormatter.ESCAPED.format("fun'"));
        Assert.assertEquals("fun\\'",PhraseFormatter.ESCAPED_EMBEDDED.format("fun'"));
    }
}

package org.tdar.core.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.search.index.analyzer.SiteCodeTokenizingAnalyzer;

public class SiteNameIndexingITCase {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testRegexp() {
        Pattern compile = SiteCodeTokenizingAnalyzer.pattern;
        assertMatches(compile, "CA-AAA-0000");
        assertMatches(compile, "GA-AA-0000");
        assertMatches(compile, "AZ AA:00:00(XXX)");
        assertMatches(compile, "01-AA-0000");
        assertMatches(compile, "24AA9999");
        assertMatches(compile, "22:22:13-0000");
        assertMatches(compile, "RI-0000");
    }

    @Test
    public void testReader() throws IOException {
        StringReader reader = new StringReader(" CA-AAA-0000 asasd qrqewr 22:22:13-0000 sadas d RI-0000");
        SiteCodeTokenizingAnalyzer tokenizer = new SiteCodeTokenizingAnalyzer();
        TokenStream tokenStream = tokenizer.tokenStream("test", reader);
        while(tokenStream.incrementToken()) {
            String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
            logger.debug(term);
            assertMatches(SiteCodeTokenizingAnalyzer.pattern, term);
        }
        tokenizer.close();
    }
    
    public void assertMatches(Pattern pattern, String text) {
        logger.debug("\"{}\" --> {}", text, pattern.pattern());
        assertTrue("String matches:" + text, pattern.matcher(text).matches());
        Assert.assertFalse(StringUtils.containsIgnoreCase(text,"q"));
    }
}

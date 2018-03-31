package org.tdar.search.index.analyzer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocompleteTestCase {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    @Ignore
    public void testTokenizationByWordSpace() throws IOException {
        // test to test how we handle periods, spaces, etc.
        String text = "U.S. Department of Agriculture";
        logger.debug(text.replaceAll("(\\w)([\\,\\-\\.\\;])", "$1"));
        Map<String, String> args = new HashMap<>();
        args.put("generateWordParts", "0");
        args.put("generateNumberParts", "0");
        args.put("catenateWords", "1");
        args.put("catenateNumbers", "1");
        args.put("preserveOriginal", "1");
        args.put("catenateAll", "0");
        WordDelimiterFilterFactory wdff = new WordDelimiterFilterFactory(args);
        KeywordAnalyzer ka = new KeywordAnalyzer();
        TokenStream tokenStream = ka.tokenStream("test", text);
        TokenFilter tokenFilter = wdff.create(tokenStream);
        tokenFilter.reset();
        while (tokenFilter.incrementToken()) {
            logger.debug("{}", tokenFilter.getAttribute(CharTermAttribute.class));
        }
        ka.close();
    }
}

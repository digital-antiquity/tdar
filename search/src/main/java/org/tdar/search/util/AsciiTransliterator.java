package org.tdar.search.util;

import java.io.IOException;

import javax.persistence.Transient;

import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * For the PDF Service, this uses the Lucene ASCII Filters to transliterate the UTF-8 Text down to ASCII
 * 
 * This class is a kluge to avoid working with UTF-8 fonts in writing PDFs
 * 
 * http://stackoverflow.com/questions/2545200/java-utf-8-to-ascii-conversion-with-supplements
 * http://stackoverflow.com/questions/285228/how-to-convert-utf-8-to-us-ascii-in-java
 */
public class AsciiTransliterator {
    private final KeywordTokenizer keywordTokenizer = new KeywordTokenizer();
    private final ASCIIFoldingFilter asciiFoldingFilter = new ASCIIFoldingFilter(keywordTokenizer);
    private final CharTermAttribute termAttribute = asciiFoldingFilter.getAttribute(CharTermAttribute.class);

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public String process(String line) {
        if (line != null) {
            try {
                keywordTokenizer.reset();
                if (asciiFoldingFilter.incrementToken()) {
                    return new String(termAttribute.buffer());
                }
            } catch (IOException e) {
                logger.warn("Failed to parse: " + line, e);
            }
        }
        return null;
    }
}

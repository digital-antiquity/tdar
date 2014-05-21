package org.tdar.search.index.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.analysis.PatternReplaceFilter;
import org.apache.solr.analysis.PatternTokenizer;
import org.apache.solr.analysis.TrimFilter;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

public final class SiteCodeTokenizingAnalyzer extends Analyzer {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     * 
     * Treats entire field value as a single Keyword Token
     */

    public static final String SEP = "([\\s\\,\\:\\-])";
    public static final String SEP_OPT = SEP + "?";
    public static final String PATTERN_SMITHSONIAN_ALPHA = "([A-Z][A-Z]\\s)?(([A-Z][A-Z])(" + SEP_OPT + "([A-Z]{1,3}))?" + SEP_OPT + "(\\d+))";
    public static final String PATTERN_SMITHSONIAN_ARIZONA = "([A-Z]{2})" + SEP_OPT + "([A-Z]{1,2})?(" + SEP_OPT + "(\\d+))+" + SEP_OPT + "(\\([A-Z]{2,5}\\))?";
    public static final String PATTERN_SMITHSONIAN_NUMERIC = "(\\d{1,2})" + SEP_OPT + "([A-Z0-9]{2,3})" + SEP_OPT + "(\\d+)" + SEP_OPT + "(\\d+)?";
    public static final Pattern pattern = Pattern.compile("(" + PATTERN_SMITHSONIAN_NUMERIC + "|" + PATTERN_SMITHSONIAN_ARIZONA + "|"
            + PATTERN_SMITHSONIAN_ALPHA + ")");
    public static final Pattern sep_pattern = Pattern.compile(SEP);

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        try {
            PatternTokenizer tokenizer = new PatternTokenizer(reader, pattern, 0);
            ASCIIFoldingFilter filter = new ASCIIFoldingFilter(tokenizer);
            TrimFilter trimFilter = new TrimFilter(filter, true);
            // normalizing where possible so that RI-0000 matches RI0000
            PatternReplaceFilter replaceFilter = new PatternReplaceFilter(trimFilter, sep_pattern, "", true);
            return replaceFilter;
        } catch (IOException e) {
            throw new TdarRecoverableRuntimeException();
        }
    }

}

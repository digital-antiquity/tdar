package org.tdar.search.index.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
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
    
    public static final String PATTERN_SMITHSONIAN_ALPHA = "([A-Z][A-Z]\\s)?(([A-Z][A-Z])(([\\s\\:\\-]?)([A-Z]{1,3}))?([\\s\\:\\-]?)(\\d+))";
    public static final String PATTERN_SMITHSONIAN_ARIZONA = "([A-Z]{2})([\\s\\:\\-])?([A-Z]{2})([\\s\\:\\-])?(\\d+)([\\s\\:\\-])?(\\d+)(\\([A-Z]{3}\\))?";
    public static final String PATTERN_SMITHSONIAN_NUMERIC = "(\\d{1,2})([\\s\\:\\-]?)(\\w{2,3})([\\s\\:\\-]?)(\\d+)([\\s\\:\\-]?)(\\d+)?";
    public static final Pattern pattern = Pattern.compile("("+PATTERN_SMITHSONIAN_NUMERIC + "|"+ PATTERN_SMITHSONIAN_ARIZONA+"|"+ PATTERN_SMITHSONIAN_ALPHA+")");
    
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        try {
            PatternTokenizer tokenizer = new PatternTokenizer(reader, pattern, 0);
            ASCIIFoldingFilter filter = new ASCIIFoldingFilter(tokenizer);
            TrimFilter trimFilter = new TrimFilter(filter, true);
            return trimFilter;
        } catch (IOException e) {
            throw new TdarRecoverableRuntimeException();
        }
    }

}

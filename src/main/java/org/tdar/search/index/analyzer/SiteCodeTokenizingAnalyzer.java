package org.tdar.search.index.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.PatternTokenizer;
import org.apache.solr.analysis.PatternTokenizerFactory;
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
    
    public static final String PATTERN_SMITHSONIAN_ALPHA = "(\\w\\w\\s)?((\\w\\w)(([\\s\\:\\-]?)(\\w{1,3}))?([\\s\\:\\-]?)(\\d+))(\\(\\w\\w\\w\\))?";
    public static final String PATTERN_SMITHSONIAN_NUMERIC = "(\\d{1,2})([\\s\\:\\-]?)(\\w{2,3})([\\s\\:\\-]?)(\\d+)([\\s\\:\\-]?)(\\d+)?";
    public static final Pattern pattern = Pattern.compile("("+PATTERN_SMITHSONIAN_NUMERIC + "|"+ PATTERN_SMITHSONIAN_ALPHA+")");
    
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

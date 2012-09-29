package org.tdar.search.index.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.TrimFilter;

public final class NonTokenizingLowercaseKeywordAnalyzer extends Analyzer {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     * 
     * Treats entire field value as a single Keyword Token
     */
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        KeywordTokenizer kt = new KeywordTokenizer(reader);
        LowerCaseFilter stream = new LowerCaseFilter(Version.LUCENE_31,kt);
        ASCIIFoldingFilter filter = new ASCIIFoldingFilter(stream);
        TrimFilter trimFilter = new TrimFilter(filter, true);
        return trimFilter;
    }

}

package org.tdar.search.index.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;

public final class NonTokenizingLowercaseKeywordAnalyzer extends Analyzer {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     * 
     * Treats entire field value as a single Keyword Token
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        KeywordTokenizer kt = new KeywordTokenizer(reader);
        LowerCaseFilter stream = new LowerCaseFilter(kt);
        ASCIIFoldingFilter filter = new ASCIIFoldingFilter(stream);
        TrimFilter trimFilter = new TrimFilter(filter);
        return new TokenStreamComponents(kt, trimFilter);
    }

}

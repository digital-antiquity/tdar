package org.tdar.search.index.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public final class TdarCaseSensitiveStandardAnalyzer extends Analyzer {

    /*
     * Treats each term within the field as it's own token
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer st = new StandardTokenizer();
        TrimFilter trimFilter = new TrimFilter(st);
//        StopFilter stopFilter = new StopFilter(trimFilter, StopFilter.makeStopSet(TdarConfiguration.getInstance().getStopWords()));
        ASCIIFoldingFilter filter = new ASCIIFoldingFilter(trimFilter);
        return new TokenStreamComponents(st, filter);
    }

}

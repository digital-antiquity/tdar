package org.tdar.search.index.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.tdar.core.configuration.TdarConfiguration;

public final class TdarCaseSensitiveStandardAnalyzer extends Analyzer {

    /*
     * Treats each term within the field as it's own token
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        StandardTokenizer st = new StandardTokenizer(reader);
        TrimFilter trimFilter = new TrimFilter(st);
        StopFilter stopFilter = new StopFilter(trimFilter, TdarConfiguration.getInstance().getStopWords());
        ASCIIFoldingFilter filter = new ASCIIFoldingFilter(stopFilter);
        return new TokenStreamComponents(st, filter);
    }

}

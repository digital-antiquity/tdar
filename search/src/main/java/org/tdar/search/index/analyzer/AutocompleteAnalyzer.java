package org.tdar.search.index.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;


public final class AutocompleteAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String name) {
        EdgeNGramTokenizer et = new EdgeNGramTokenizer( 1, 40);
        LowerCaseFilter stream = new LowerCaseFilter(et);
        ASCIIFoldingFilter filter = new ASCIIFoldingFilter(stream);
        return new TokenStreamComponents(et,filter);
    }

}

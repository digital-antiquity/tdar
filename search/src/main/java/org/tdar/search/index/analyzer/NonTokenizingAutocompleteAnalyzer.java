package org.tdar.search.index.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

/**
 * 
 * Perform similar analysis as the autocomplete analyzer, but don't produce any n-grams. Use when performing a search against fields that have
 * been processed by the AutocompleteAnalyzer.
 * 
 * @author Jim deVos
 * 
 */
public class NonTokenizingAutocompleteAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        KeywordTokenizer et = new KeywordTokenizer();
        LowerCaseFilter stream = new LowerCaseFilter(et);
        ASCIIFoldingFilter filter = new ASCIIFoldingFilter(stream);
        return new TokenStreamComponents(et, filter);
    }
}

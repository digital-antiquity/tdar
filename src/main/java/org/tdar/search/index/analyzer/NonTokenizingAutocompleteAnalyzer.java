package org.tdar.search.index.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

/**
 * 
 * Perform similar analysis as the autocomplete analyzer,  but don't produce any n-grams.  Use when performing a search against fields that have
 * been processed by the AutocompleteAnalyzer.
 * 
 * @author Jim deVos
 *
 */
public class NonTokenizingAutocompleteAnalyzer extends Analyzer {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        KeywordTokenizer et = new KeywordTokenizer(reader);
        LowerCaseFilter stream = new LowerCaseFilter(Version.LUCENE_35,et);
        ASCIIFoldingFilter filter = new ASCIIFoldingFilter(stream);
        return filter;
    }
}

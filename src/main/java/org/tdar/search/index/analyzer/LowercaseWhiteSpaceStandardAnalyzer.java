package org.tdar.search.index.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.PatternTokenizer;
import org.apache.solr.analysis.TrimFilter;
import org.apache.solr.analysis.WordDelimiterFilterFactory;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

public final class LowercaseWhiteSpaceStandardAnalyzer extends Analyzer {

    /*
     * Treats each term within the field as it's own token
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     */
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        try {
            // TOKENIZING ON (punctuation?)(space +) (punctuation?)
            Tokenizer st = new PatternTokenizer(reader, Pattern.compile("((^|\\W|\\_)?(\\s+)(\\W|\\_|$)?)"), -1);
            // FIXME: this still lets things like "carp)" through as well as "carp" - it'd be better if the latter was the only thing
            
            // http://wiki.apache.org/solr/AnalyzersTokenizersTokenFilters
            // http://wiki.apache.org/solr/SolrRelevancyCookbook#IntraWordDelimiters
            Map<String, String> params = new HashMap<String, String>();
            params.put("generateWordParts", "1");
            params.put("catenateWords", "1");
            params.put("generateNumberParts", "0");
            params.put("catenateNumbers", "0");
            params.put("catenateAll", "1");
            params.put("preserveOriginal", "1");
            params.put("splitOnNumerics", "0");
            params.put("stemEnglishPossessive", "1");
            params.put("splitOnCaseChange", "0");
            // params.put("types", "wdfftypes.txt"); "% => ALPHA" or "\u002C => DIGIT". Allowable types are: LOWER, UPPER, ALPHA, DIGIT, ALPHANUM,
            // SUBWORD_DELIM.
            // [Solr3.1]
            WordDelimiterFilterFactory wordFilter = new WordDelimiterFilterFactory();
            wordFilter.init(params);
            LowerCaseFilter stream = new LowerCaseFilter(Version.LUCENE_35, wordFilter.create(st));
            TrimFilter trimFilter = new TrimFilter(stream, true);

            StopFilter stopFilter = new StopFilter(Version.LUCENE_35, trimFilter, TdarConfiguration.getInstance().getStopWords());
            ASCIIFoldingFilter filter = new ASCIIFoldingFilter(stopFilter);
            return filter;
        } catch (IOException e) {
            throw new TdarRecoverableRuntimeException("cannot tokenize!");
        }

    }

}

package org.tdar.index;

import java.io.Reader;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter.Side;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;
import org.apache.lucene.util.Version;

public final class AutocompleteAnalyzer extends Analyzer {

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		EdgeNGramTokenizer et = new EdgeNGramTokenizer(reader, Side.FRONT.getLabel(), 1, 30);
		LowerCaseFilter stream = new LowerCaseFilter(Version.LUCENE_31,et);
		ASCIIFoldingFilter filter = new ASCIIFoldingFilter(stream);
		return filter;
	}

}

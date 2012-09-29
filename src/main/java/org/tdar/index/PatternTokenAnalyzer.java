package org.tdar.index;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.PatternTokenizer;
import org.apache.solr.analysis.TrimFilter;

public final class PatternTokenAnalyzer extends Analyzer {

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		Tokenizer t;
		try {
			t = new PatternTokenizer(reader, Pattern.compile("[:\\|,.]"), -1);
			LowerCaseFilter stream = new LowerCaseFilter(Version.LUCENE_31,t);
            TrimFilter trimFilter = new TrimFilter(stream, true);
			ASCIIFoldingFilter filter = new ASCIIFoldingFilter(trimFilter);
			return filter;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

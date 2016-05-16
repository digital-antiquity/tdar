package org.tdar.search.index.analyzer;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Collections;

public final class SiteCodeExtractor {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     * 
     * Treats entire field value as a single Keyword Token
     */
    @SuppressWarnings("unused")
    private static final transient Logger logger = LoggerFactory.getLogger(SiteCodeExtractor.class);

    static final String SEP = "([\\s\\,\\:\\-]0*)";
    static final String SEP_OPT = SEP + "?";
    static final String PATTERN_SMITHSONIAN_ALPHA = "([A-Z][A-Z]\\s)?(([A-Z][A-Z])(" + SEP_OPT + "([A-Z]{1,3}))?" + SEP_OPT + "(\\d+))";
    static final String PATTERN_SMITHSONIAN_ARIZONA = "([A-Z]{2})" + SEP_OPT + "([A-Z]{1,2})?(" + SEP_OPT + "(\\d+))+" + SEP_OPT + "(\\([A-Z]{2,5}\\))?";
    static final String PATTERN_SMITHSONIAN_NUMERIC = "(\\d{1,2})" + SEP_OPT + "([A-Z0-9]{2,3})" + SEP_OPT + "(\\d+)" + SEP_OPT + "(\\d+)?";
    static final Pattern pattern = Pattern.compile("(" + PATTERN_SMITHSONIAN_NUMERIC + "|" + PATTERN_SMITHSONIAN_ARIZONA + "|"
            + PATTERN_SMITHSONIAN_ALPHA + ")");
     static final Pattern sep_pattern = Pattern.compile(SEP);


    @SuppressWarnings("unchecked")
    public static Set<String> extractSiteCodeTokens(String str) {
    	if (str == null) {
    		return Collections.emptySet();
    	}
    	Set<String> toReturn = new HashSet<>();
    	Matcher matcher = pattern.matcher(str);
    	while (matcher.find()) {
    		String group = matcher.group(0);
    		toReturn.add(group.replaceAll("[\\s\\,\\:\\-]", ""));
    	}
    	return toReturn;
    }


	public static boolean matches(String term) {
		return pattern.matcher(term).matches();
	}

}

package org.tdar.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteCodeExtractor {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     * 
     * Treats entire field value as a single Keyword Token
     */
    @SuppressWarnings("unused")
    private static final transient Logger logger = LoggerFactory.getLogger(SiteCodeExtractor.class);

    static final String ONE_THROUGH_FIFTY = "([1-9]|[1-4][0-9]|50)";
    static final String SEP = "([\\s\\,\\:\\-]0*)";
    static final String SEP_OPT = SEP + "?";
    static final String STATE = "(AZ|ME|RI|VT|CT|CA)";
    static final String PATTERN_SMITHSONIAN_ALPHA = "(" + STATE + "\\s)?(([A-Z][A-Z])(" + SEP_OPT + "([A-Z]{1,3}))?" + SEP_OPT + "(\\d+))";
    static final String PATTERN_SMITHSONIAN_ARIZONA = STATE + SEP_OPT + "([A-Z]{1,2})?(" + SEP_OPT + "(\\d+))+" + SEP_OPT + "(\\([A-Z]{2,5}\\))?";
    static final String PATTERN_SMITHSONIAN_NUMERIC = ONE_THROUGH_FIFTY + SEP_OPT + "([A-Z0-9]{2,3})" + SEP_OPT + "(\\d+)" + SEP_OPT + "(\\d+)?";
    private static final Pattern pattern = Pattern.compile("(" + PATTERN_SMITHSONIAN_NUMERIC + "|" + PATTERN_SMITHSONIAN_ARIZONA + "|"
            + PATTERN_SMITHSONIAN_ALPHA + ")");
     static final Pattern sep_pattern = Pattern.compile(SEP);


    @SuppressWarnings("unchecked")
    public static Set<String> extractSiteCodeTokens(String str, boolean mutate) {
        if (str == null) {
            return Collections.emptySet();
        }
        Set<String> toReturn = new HashSet<>();
        Matcher matcher = getPattern().matcher(str);
        while (matcher.find()) {
            String group = matcher.group(0);
            if (mutate) {
                toReturn.add(group.replaceAll(SEP, ""));
            } else {
                toReturn.add(group);
            }
        }
        return toReturn;
    }


    public static boolean matches(String term) {
        return getPattern().matcher(term).matches();
    }


    public static Pattern getPattern() {
        return pattern;
    }

}

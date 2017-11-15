package org.tdar.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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

    static final String ONE_THROUGH_FIFTY = "0?([1-9]|[1-4][0-9]|50)";
    static final String SEP = "([\\s\\:\\-]0*)";
    // AA:1:66
    private static final String CATCHALL = "[A-Z]{1,2}[\\:\\-][A-Z0-9]{1,5}[\\:\\-][0-9]{1,5}";
    static final String SEP_OPT = SEP + "?";
    static final String STATE = "(AZ|ME|RI|VT|CT|CA)";
    static final String PATTERN_SMITHSONIAN_ALPHA = STATE + SEP_OPT + "(([A-Z][A-Z])(" + SEP_OPT + "([A-Z]{1,3}))?" + SEP_OPT + "(\\d+))";
    static final String PATTERN_SMITHSONIAN_ARIZONA = STATE + SEP_OPT + "([A-Z]{1,2})?(" + SEP_OPT + "(\\d+))+" + SEP_OPT
            + "([\\[\\(]\\s?[A-Z]{2,5}\\s?[\\]\\)])?";
    static final String PATTERN_SMITHSONIAN_NUMERIC = ONE_THROUGH_FIFTY + SEP_OPT + "([A-Z0-9]{2,3})" + SEP_OPT + "(\\d+)" + SEP_OPT + "(\\d+)?";
    private static final Pattern PAGE_RANGE_PATTERN = Pattern.compile("^(\\d+)\\-(\\d+)$");
    // start with an expectation of a non-word character at the beginning and END
    private static final Pattern pattern = Pattern.compile("(?<=(^|\b|\\s|\n|\\())(" + PATTERN_SMITHSONIAN_ARIZONA + "|"
            + PATTERN_SMITHSONIAN_NUMERIC + "|" + PATTERN_SMITHSONIAN_ALPHA + "|" + CATCHALL + ")(?=($|\b|\\s|\n|\\)))");
    static final Pattern sep_pattern = Pattern.compile(SEP);

    public static Set<String> extractSiteCodeTokens(String str, boolean mutate) {
        if (str == null) {
            return Collections.emptySet();
        }
        Set<String> toReturn = new HashSet<>();

        for (String part : str.split(",")) {
            Matcher matcher = getPattern().matcher(part);
            while (matcher.find()) {
                String code = matcher.group(0).trim();
                if (code.startsWith("(") && code.endsWith(")")) {
                    code = code.substring(1, code.length() -1).trim();
                }
                // if we're just left with numbers, ignore it, likely a date
                if (StringUtils.isNumeric(code)) {
                    continue;
                }

                if (StringUtils.endsWithAny(code, new String[] { ",", ":", "-" })) {
                    continue;
                }
                
                // trying to deal with cases like: "150 0 0" and "2603 9"
                if (code.contains(" ") && StringUtils.containsNone(code, new char[]{'-',':'})) {
                    continue;
                }
                
                // we're probably a citation part
                if (code.matches("(19|20)\\d\\d:\\d+(\\-\\d+)?")) {
                    continue;
                }
                // if we're left with 150-250, check if sequential, then probably a page range
                Matcher rangeMatcher = PAGE_RANGE_PATTERN.matcher(code);
                if (rangeMatcher.matches()) {
                    // match page ranges
                    if (Integer.parseInt(rangeMatcher.group(1)) < Integer.parseInt(rangeMatcher.group(2))) {
                        continue;
                    }
                }

                // for indexing we mutate to collapse common separators
                if (mutate) {
                    toReturn.add(code.replaceAll(SEP, ""));
                } else {
                    toReturn.add(code);
                }
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

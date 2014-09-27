package org.tdar.core.bean.util;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class UrlUtils{

    private static final Pattern PATTERN_NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern PATTERN_NONWORD = Pattern.compile("[^\\w\\s-]");
    private static final Pattern PATTERN_WHITESPACE = Pattern.compile("[-\\s]+");
    private static final Pattern PATTERN_AFFIX_SLUG = Pattern.compile("(^-)|(-$)");

    /**
     * Slightly faster version of String.replaceAll
     * @param str
     * @param pattern
     * @param replacement
     * @return
     */
    static String replaceAll(String str, Pattern pattern, String replacement) {
        if(StringUtils.isBlank(str)) return str;
        return pattern.matcher(str).replaceAll(replacement);
    }

    /**
     * Convert unicode string into approximated ascii (NFKD normalization)
     * @param utfString
     * @return
     */
    static String normalize(String utfString) {
        String decomp = Normalizer.normalize(utfString, Normalizer.Form.NFKD);
        return replaceAll(decomp, PATTERN_NON_ASCII, "");
    }

    /**
     * convert string to ascii, make lowercase, remove non-word characters, convert space to hyphen, and remove leading/trailing hyphens
     * @param input
     * @return
     */
    public static String slugify(String input) {
        if (StringUtils.isBlank(input)) return input;
        String slug = normalize(input);
        slug = replaceAll(slug, PATTERN_NONWORD, "");
        slug = replaceAll(slug, PATTERN_WHITESPACE, "-");
        slug = replaceAll(slug, PATTERN_AFFIX_SLUG, "");
        return slug.toLowerCase();
    }
}

package org.tdar.core.bean.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlUtils {

    private static final Pattern PATTERN_NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern PATTERN_NONWORD = Pattern.compile("[^\\w\\s-]");
    private static final Pattern PATTERN_WHITESPACE = Pattern.compile("[-\\s]+");
    private static final Pattern PATTERN_AFFIX_SLUG = Pattern.compile("(^-)|(-$)");

    private static Logger logger = LoggerFactory.getLogger(UrlUtils.class);

    /**
     * Slightly faster version of String.replaceAll
     * 
     * @param str
     * @param pattern
     * @param replacement
     * @return
     */
    static String replaceAll(String str, Pattern pattern, String replacement) {
        if (StringUtils.isBlank(str))
            return str;
        return pattern.matcher(str).replaceAll(replacement);
    }

    /**
     * Convert unicode string into approximated ascii (NFKD normalization)
     * 
     * @param utfString
     * @return
     */
    static String normalize(String utfString) {
        String decomp = Normalizer.normalize(utfString, Normalizer.Form.NFKD);
        return replaceAll(decomp, PATTERN_NON_ASCII, "");
    }

    /**
     * convert string to ascii, make lowercase, remove non-word characters, convert space to hyphen, and remove leading/trailing hyphens
     * 
     * @param input
     * @return
     */
    public static String slugify(String input) {
        if (StringUtils.isBlank(input))
            return input;
        String slug = normalize(input);
        slug = replaceAll(slug, PATTERN_NONWORD, "");
        slug = replaceAll(slug, PATTERN_WHITESPACE, "-");
        slug = replaceAll(slug, PATTERN_AFFIX_SLUG, "");
        return slug.toLowerCase();
    }

    /**
     * Enforces "relative url" syntax by stripping out host/scheme/protocol portions from an "untrusted" URL string.
     * 
     * @param untrustedUrl
     *            URL from untrusted source (e.g. http request parameters)
     * @return sanitized copy of untrusted url.
     *
     *         <strong>NOTE</strong>: This method always returns a copy, even if input string is already sanitized.
     */
    public static String sanitizeRelativeUrl(String untrustedUrl) {
        if (untrustedUrl == null)
            return untrustedUrl;
        StringBuilder sb = new StringBuilder();
        try {
            URI uri = new URI(untrustedUrl.trim());
            if (uri.getPath() != null) {
                sb.append(uri.getPath());
            }
            if (uri.getQuery() != null) {
                sb
                        .append("?")
                        .append(uri.getQuery());
            }
            if (uri.getFragment() != null) {
                sb
                        .append("#")
                        .append(uri.getFragment());
            }
        } catch (URISyntaxException e) {
            logger.warn("invalid url:{}  replacing with empty string", untrustedUrl);
        }

        return sb.toString();
    }
    
    
    /**
     * http://stackoverflow.com/questions/4826061/what-is-the-fastest-way-to-get-the-domain-host-name-from-a-url
     * Will take a url such as http://www.stackoverflow.com and return www.stackoverflow.com
     * 
     * @param url
     * @return
     */
    public static String getHost(String url){
        if(url == null || url.length() == 0)
            return "";

        int doubleslash = url.indexOf("//");
        if(doubleslash == -1)
            doubleslash = 0;
        else
            doubleslash += 2;

        int end = url.indexOf('/', doubleslash);
        end = end >= 0 ? end : url.length();

        int port = url.indexOf(':', doubleslash);
        end = (port > 0 && port < end) ? port : end;

        return url.substring(doubleslash, end);
    }


    /**http://stackoverflow.com/questions/4826061/what-is-the-fastest-way-to-get-the-domain-host-name-from-a-url  
     * Based on : http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.3_r1/android/webkit/CookieManager.java#CookieManager.getBaseDomain%28java.lang.String%29
     * Get the base domain for a given host or url. E.g. mail.google.com will return google.com
     * @param host 
     * @return 
     */
    public static String getBaseDomain(String url) {
        String host = getHost(url);

        int startIndex = 0;
        int nextIndex = host.indexOf('.');
        int lastIndex = host.lastIndexOf('.');
        while (nextIndex < lastIndex) {
            startIndex = nextIndex + 1;
            nextIndex = host.indexOf('.', startIndex);
        }
        if (startIndex > 0) {
            return host.substring(startIndex);
        } else {
            return host;
        }
    }
}

package org.tdar.junit;

import static org.junit.Assert.fail;

import java.util.regex.Pattern;

/**
 * An analog of sorts for the junit Assert class, contining assertions that are handy for our tDAR tests. All of the methods are static
 * 
 * @author jimdevos
 * 
 */
public class TdarAssert {

    protected static final String FMT2_FAIL_ASSERT_MATCHES = "Expecting regex match.  Pattern:/%s/ haystack:\"%s\"";

    // prevent instances
    protected TdarAssert() {
    }

    public static void assertMatches(String haystack, String pattern) {
        assertMatches(String.format(FMT2_FAIL_ASSERT_MATCHES, pattern.toString(), haystack), haystack, pattern);
    }

    public static void assertMatches(String message, String haystack, String pattern) {
        Pattern pat = Pattern.compile(pattern);
        if (!pat.matcher(haystack).matches()) {
            fail(message);
        }
    }

}

package org.tdar.search.index.analyzer;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteNameIndexingTestCase {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testRegexp() {
        Pattern compile = SiteCodeExtractor.pattern;
        assertMatches(compile, "CA-AAA-0001");
        assertMatches(compile, "GA-AA-0001");
        assertMatches(compile, "AZ AA:01:01(XXX)");
        assertMatches(compile, "01-AA-0001");
        assertMatches(compile, "24AA9999");
        assertMatches(compile, "22:22:13-0005");
        assertMatches(compile, "RI-0006");
        assertMatches(compile, "AZ U:9:1(ASM)");
        assertMatches(compile, "NA18,009(MNA)");
        assertMatches(compile, "AZ N:16:45 (PC)");
        assertMatches(compile, "AR-03-12-06-193(USFS)");
        assertMatches(compile, "NM-H-46-62 (NN)");
        assertMatches(compile, "LA 9219");
        assertMatches(compile, "AR-03-12-01-1927");
        assertMatches(compile, "38AK933");
        assertMatches(compile, "AZ U:9:1(ASM)");
        assertMatches(compile, "AZ U:9:1 (ASM)");
    }

    @Test
    public void testReader() throws IOException {
        String reader = " CA-AAA-0001 asasd qrqewr 22:22:13-0010 sadas d RI-0090  44:PG:0462";
        Set<String> found = SiteCodeExtractor.extractSiteCodeTokens(reader);
        Iterator<String> iterator = found.iterator();
        while (iterator.hasNext()) {
            String term = iterator.next();
            logger.debug(term);
            assertMatches(SiteCodeExtractor.pattern, term);
        }
    }

    @Test
    public void testReader2() throws IOException {
        String reader = "44PG0462";
        Set<String> found = SiteCodeExtractor.extractSiteCodeTokens(reader);
        Iterator<String> iterator = found.iterator();
        while (iterator.hasNext()) {
            String term = iterator.next();
            logger.debug(term);
            assertMatches(SiteCodeExtractor.pattern, term);
        }
    }

    public void assertMatches(Pattern pattern, String text) {
        logger.debug("\"{}\" --> {}", text, pattern.pattern());
        assertTrue("String matches:" + text, pattern.matcher(text).matches());
        Assert.assertFalse(StringUtils.containsIgnoreCase(text, "q"));
        Assert.assertFalse(StringUtils.containsIgnoreCase(text, "sa"));
    }
}

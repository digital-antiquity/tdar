package org.tdar.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        Pattern compile = SiteCodeExtractor.getPattern();
        assertMatches(compile, "CA-AAA-0001");
        assertMatches(compile, "GA-AA-0001");
        assertMatches(compile, "AZ AA:01:01(XXX)");
        assertMatches(compile, "01-AA-0001");
        assertMatches(compile, "24AA9999");
        assertMatches(compile, "22:22:13-0005");
        assertMatches(compile, "RI-0006");
        assertMatches(compile, "AZ U:9:1(ASM)");
        // assertMatches(compile, "NA18,009(MNA)");
        assertMatches(compile, "AZ N:16:45 (PC)");
        assertMatches(compile, "VT-03-12-06-193(USFS)");
        assertMatches(compile, "CA-H-46-62 (NN)");
        assertMatches(compile, "CA 9219");
        assertMatches(compile, "CT-03-12-01-1927");
        assertMatches(compile, "38AK933");
        assertMatches(compile, "AZ U:9:1(ASM)");
        assertMatches(compile, "AZ U:9:1 (ASM)");
    }

    @Test
    public void testHawaiiSiteExtractor() {
        String title = "POLLEN AND MACROFLORAL ANALYSIS OF SEDIMENT FROM LOKO KAIPUNI FISHPOND (SIHP # 50-80-14-4573), WAIKĪKĪ, O’AHU, HAWAI’I";
        Set<String> extractSiteCodeTokens = SiteCodeExtractor.extractSiteCodeTokens(title, true);
        logger.debug("{}", extractSiteCodeTokens);
        assertTrue("should have one site code", extractSiteCodeTokens.size() == 1);
        assertEquals("5080144573", extractSiteCodeTokens.iterator().next());
    }

    @Test
    public void boundaryTest() {
        String phrase = "GB-235-1D I01";
        Set<String> extractSiteCodeTokens = SiteCodeExtractor.extractSiteCodeTokens(phrase, false);
        logger.debug("{}", extractSiteCodeTokens);
        assertTrue(extractSiteCodeTokens.size() == 0);
    }

    @Test
    public void testAZ() {
        String phrase = "AZ U:15:110(ASM ) Shelltown AA:1:66 Pit structure AZ U:15:110(ASM) Feature 757 All except;  context AZ U:10:6 ; four canals at Las Canopas (AZ U:9:161 [ASM]), located between ;  PRS-01 AZ U:15:110 (ASM) Crushed rock Plain";
        Set<String> extractSiteCodeTokens = SiteCodeExtractor.extractSiteCodeTokens(phrase, false);
        logger.debug("{}", extractSiteCodeTokens);
        assertTrue(extractSiteCodeTokens.contains("AZ U:9:161 [ASM]"));
        assertTrue(extractSiteCodeTokens.contains("AZ U:10:6"));
        assertTrue(extractSiteCodeTokens.contains("AA:1:66"));
        assertTrue(extractSiteCodeTokens.contains("AZ U:15:110 (ASM)"));
        assertTrue(extractSiteCodeTokens.contains("AZ U:15:110(ASM)"));
        assertTrue(extractSiteCodeTokens.contains("AZ U:15:110(ASM )"));
    }

    @Test
    public void testExtractor() {
        Set<String> extractSiteCodeTokens = SiteCodeExtractor.extractSiteCodeTokens("38-AK-500", true);
        logger.debug("{}", extractSiteCodeTokens);
        assertTrue("should have one site code", extractSiteCodeTokens.size() == 1);
    }

    @Test
    public void testExtractorInPhrase() {
        Set<String> extractSiteCodeTokens = SiteCodeExtractor.extractSiteCodeTokens(
                "G- AZ U:15:1, Snaketown, 8D:Strat Test 1, Level 3; H - AZ U:13:1, Snaketown, 8D:Strat Test 1, Level 4; I - AZ AA:1:66,\n", false);
        logger.debug("{}", extractSiteCodeTokens);
        assertTrue("should have one site code", extractSiteCodeTokens.size() == 3);
    }

    @Test
    public void testReader() throws IOException {
        String reader = " CA-AAA-0001 asasd qrqewr 22:22:13-0010 sadas d RI-0090  44:PG:0462";
        Set<String> found = SiteCodeExtractor.extractSiteCodeTokens(reader, true);
        Iterator<String> iterator = found.iterator();
        while (iterator.hasNext()) {
            String term = iterator.next();
            logger.debug(term);
            assertMatches(SiteCodeExtractor.getPattern(), term);
        }
    }

    @Test
    public void testNormalization() throws IOException {
        String reader = " CA-AAA-0001RI-191-0  44:PG:0462";
        Set<String> found = SiteCodeExtractor.extractSiteCodeTokens(reader, true);
        Iterator<String> iterator = found.iterator();
        while (iterator.hasNext()) {
            String term = iterator.next();
            logger.debug(term);
            assertMatches(SiteCodeExtractor.getPattern(), term);
            assertFalse(term.contains("0"));
        }
    }

    @Test
    public void testReader2() throws IOException {
        String reader = "44PG0462";
        Set<String> found = SiteCodeExtractor.extractSiteCodeTokens(reader, true);
        Iterator<String> iterator = found.iterator();
        while (iterator.hasNext()) {
            String term = iterator.next();
            logger.debug(term);
            assertMatches(SiteCodeExtractor.getPattern(), term);
        }
    }

    public void assertMatches(Pattern pattern, String text) {
        logger.debug("\"{}\" --> {}", text, pattern.pattern());
        assertTrue("String matches:" + text, pattern.matcher(text).matches());
        Assert.assertFalse(StringUtils.containsIgnoreCase(text, "q"));
        Assert.assertFalse(StringUtils.containsIgnoreCase(text, "sa"));
    }
}

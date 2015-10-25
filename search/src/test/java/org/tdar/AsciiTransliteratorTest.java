package org.tdar;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.search.util.AsciiTransliterator;

public class AsciiTransliteratorTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    @Ignore
    public void testTransliterator() {
        AsciiTransliterator trans = new AsciiTransliterator();
        String test = "ßTest’s test “Test” – a Æ Ëÿǿ";
        String process = trans.process(test);
        logger.info(process);
        assertEquals("ssTest's test \"Test\" - a AE Eyo".trim(), process.trim());
    }
}

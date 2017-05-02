package org.tdar.balk;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.balk.service.Phases;

public class PhasesTest {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testPhaseCleanup() {
        String filename = "/Client Data/test/Upload to tDAR/Last Name Issue.xlsx";
        String key1 = Phases.createKey(filename);
        logger.debug(key1);
        String filename2 = "/Client Data/test/Last Name Issue.xlsx";
        String key2 = Phases.createKey(filename2);
        logger.debug(key2);
        assertEquals(key1,key2);
    }
}

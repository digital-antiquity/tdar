package org.tdar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.struts.action.AbstractPersistableController;

public class TimeFormattingTestCase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testTimeFormat() {
        String timeString = AbstractPersistableController.formatTime((1000 * 60 * 60 * 1) + (1000 * 60 * 2) + (1000 * 3) + 456);
        logger.debug("time: {}", timeString);
        assertEquals("expecting 1h 2m 3s 456ms", "01:02:03.456", timeString);
    }

}

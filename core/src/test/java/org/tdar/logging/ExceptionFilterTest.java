/**
 * These tests are largely based on RegexFilterTest from Apache Software Foundation.
 */
package org.tdar.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.status.StatusLogger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class ExceptionFilterTest {
    @BeforeClass
    public static void before() {
        StatusLogger.getLogger().setLevel(Level.OFF);
    }

    @Test
    public void testPrefix() {
        Filter filter = ExceptionFilter.createFilter("java.lang", Result.ACCEPT, Result.DENY );
        filter.start();
        assertTrue(filter.isStarted());

        LogEvent rexEvent = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(new RuntimeException()).build();
        assertThat(filter.filter(rexEvent), equalTo(filter.getOnMatch()));

        LogEvent ioEvent = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(new IOException()).build();
        assertThat(filter.filter(ioEvent), equalTo(filter.getOnMismatch()));
    }

    @Test @Ignore
    public void testFullClassname() {
        fail("not implemented");
    }

    @Test @Ignore
    public void testAnonymousException() {
        fail("not implemented");
    }

    @Test @Ignore
    public void testNoThrowable() {
        fail("not implemented");
    }
}
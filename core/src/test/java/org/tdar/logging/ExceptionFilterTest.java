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
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertTrue;
import static org.tdar.logging.ExceptionFilter.createFilter;

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
        Filter filter = createFilter("java.lang", Result.ACCEPT, Result.DENY );
        filter.start();
        assertTrue(filter.isStarted());

        LogEvent rexEvent = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(new RuntimeException()).build();
        assertThat(filter.filter(rexEvent), equalTo(filter.getOnMatch()));

        LogEvent ioEvent = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(new IOException()).build();
        assertThat(filter.filter(ioEvent), equalTo(filter.getOnMismatch()));
    }

    @Test
    public void testDefaults() {
        Filter filter = createFilter("foo", null, null);
        assertThat(filter.getOnMatch(), equalTo(Result.DENY));
        assertThat(filter.getOnMismatch(), equalTo(Result.NEUTRAL));

    }

    /**
     * Some throwables don't have a canonical name.  Make sure it handles as if no exception thrown.
     */
    @Test
    public void testAnonymousException() {
        Filter filter = createFilter("java.lang.RuntimeException", null, null);
        filter.start();

        RuntimeException anonymousException = new RuntimeException() {
            {fillInStackTrace();}
            @Override
            public String getMessage() {
                return "Hello from anonymous class";
            }
        };

        LogEvent event = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(anonymousException).build();
        Result result = filter.filter(event);
        assertThat(event.getThrown().getClass().getCanonicalName(), is( nullValue()));
        assertThat(result, equalTo(filter.getOnMismatch()));
    }

}
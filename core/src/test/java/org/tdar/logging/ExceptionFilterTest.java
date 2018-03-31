/**
 * These tests are largely based on RegexFilterTest from Apache Software Foundation.
 */
package org.tdar.logging;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertTrue;
import static org.tdar.logging.ExceptionFilter.createFilter;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.status.StatusLogger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tdar.logging.ExceptionFilter.Target;

public class ExceptionFilterTest {
    @BeforeClass
    public static void before() {
        StatusLogger.getLogger().setLevel(Level.OFF);
    }

    @Test
    public void testPrefix() {
        Filter filter = createFilter("java.lang", null, Result.ACCEPT, Result.DENY);
        filter.start();
        assertTrue(filter.isStarted());

        LogEvent rexEvent = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(new RuntimeException()).build();
        assertThat(filter.filter(rexEvent), equalTo(filter.getOnMatch()));

        LogEvent ioEvent = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(new IOException()).build();
        assertThat(filter.filter(ioEvent), equalTo(filter.getOnMismatch()));
    }

    @Test
    public void testDefaults() {
        ExceptionFilter filter = createFilter("foo", null, null, null);
        assertThat(filter.getOnMatch(), equalTo(Result.DENY));
        assertThat(filter.getOnMismatch(), equalTo(Result.NEUTRAL));
        assertThat(filter.getTarget(), equalTo(Target.CLASSNAME));
    }

    /**
     * Some throwables don't have a canonical name. If matching on classname, make sure filter treats it as a
     * mismatch.
     */
    @SuppressWarnings("serial")
    @Test
    public void testAnonymousException() {
        Filter filter = createFilter("java.lang.RuntimeException", null, null, null);
        filter.start();

        RuntimeException anonymousException = new RuntimeException() {
            {
                fillInStackTrace();
            }

            @Override
            public String getMessage() {
                return "Hello from anonymous class";
            }
        };

        LogEvent event = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(anonymousException).build();
        Result result = filter.filter(event);
        assertThat(event.getThrown().getClass().getCanonicalName(), is(nullValue()));
        assertThat(result, equalTo(filter.getOnMismatch()));
    }

    @Test
    public void testExceptionMessage() {
        Filter filter = createFilter("Broken pipe", Target.MESSAGE, null, null);
        filter.start();
        LogEvent brokenPipeEvent = Log4jLogEvent.newBuilder().setThrown(new IOException("Broken pipe"))
                .setLevel(Level.ERROR).build();
        Result result = filter.filter(brokenPipeEvent);
        assertThat(result, equalTo(filter.getOnMatch()));

        LogEvent paperEvent = Log4jLogEvent.newBuilder().setThrown(new IOException("Paper jam"))
                .setLevel(Level.ERROR).build();
        result = filter.filter(paperEvent);
        assertThat(result, equalTo(filter.getOnMismatch()));
    }

    /**
     * anonymous throwable should work fine when matching against getMessage()
     */
    @SuppressWarnings("serial")
    @Test
    public void testAnonymousExceptionMessage() {
        Filter filter = createFilter("Something bad happened", Target.MESSAGE, null, null);
        filter.start();

        RuntimeException anonymousException = new RuntimeException() {
            {
                fillInStackTrace();
            }

            @Override
            public String getMessage() {
                return "Something bad happened.";
            }
        };

        LogEvent event = Log4jLogEvent.newBuilder().setLevel(Level.ERROR).setThrown(anonymousException).build();
        Result result = filter.filter(event);
        assertThat(event.getThrown().getClass().getCanonicalName(), is(nullValue()));
        assertThat(result, equalTo(filter.getOnMatch()));
    }

}
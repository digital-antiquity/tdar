package org.tdar.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

/**
 * Filter log messages based upon Exception
 */
@Plugin(name = "ExceptionFilter", category = "Core", elementType = "filter", printObject = true)
public class ExceptionFilter extends AbstractFilter{

    private final String pattern;

    public ExceptionFilter(String pattern, Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
        this.pattern = pattern;
    }

    /**
     * Create a ThresholdFilter.
     * @param pattern The prefix of the FQCN of the exception the filter should match.
     * @param onMatch The action to take on a match.
     * @param onMismatch The action to take on a mismatch.
     * @return The created ThresholdFilter.
     */
    @PluginFactory
    public static ExceptionFilter createFilter(
            @PluginAttribute(value = "pattern") String pattern,
            @PluginAttribute(value = "onMatch", defaultString = "DENY") Result onMatch,
            @PluginAttribute(value = "onMismatch", defaultString = "NEUTRAL") Result onMismatch) {

        return new ExceptionFilter(pattern, onMatch, onMismatch);
    }

    @Override
    public Result filter(LogEvent logEvent) {
        return filter(logEvent.getThrown());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object[] params) {
        return onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return filter(t);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return filter(t);
    }

    public Result filter(Throwable t) {
        if(t == null) {
            return onMismatch;
        }
        String fqcn = "" +  t.getClass().getCanonicalName();
        return fqcn.startsWith(pattern) ? onMatch : onMismatch;
    }

    public static void cout(String format, Object ... vals) {
        System.out.println(String.format(format, vals));
    }


}

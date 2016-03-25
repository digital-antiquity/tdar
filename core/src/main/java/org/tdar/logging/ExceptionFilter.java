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

    public static enum Target {
        CLASSNAME, MESSAGE
    }

    //todo: future use
    enum PatternType {
        PREFIX, SUBSTRING, EXACT, REGEX


    }

    private final String pattern;
    private final Target target;
    private final PatternType patternType = PatternType.PREFIX;

    /**
     * Create a ThresholdFilter.
     * @param pattern The prefix of the FQCN of the exception the filter should match.
     * @param target  Designates which the target value for the filter to match against. If "CLASSNAME", filter
     *                will look for the supplied pattern in the thrown exception's canonical name
     *                (e.g. org.apache.catalina.connector.ClientAbortException), if "MESSAGE", filter
     *                looks for supplied pattern in the thrown exception's message.
     * @param match The action to take on a match. Default: DENY.
     * @param mismatch The action to take on a mismatch. Default: NEUTRAL.
     * @return The created ThresholdFilter.
     */
    @PluginFactory
    public static ExceptionFilter createFilter(
            @PluginAttribute(value = "pattern") String pattern,
            @PluginAttribute(value = "target", defaultString = "CLASSNAME") Target target,
            @PluginAttribute(value = "onMatch", defaultString = "DENY") Result match,
            @PluginAttribute(value = "onMismatch", defaultString = "NEUTRAL") Result mismatch) {
        final Target _target = target == null ? Target.CLASSNAME : target;
        final Result onMatch = match == null ? Result.DENY : match;
        final Result onMismatch = mismatch == null ? Result.NEUTRAL : mismatch;
        return new ExceptionFilter(pattern, _target, onMatch, onMismatch);
    }

    public ExceptionFilter(String pattern, Target target, Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
        this.pattern = pattern;
        this.target = target;
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
        cout("filter called");
        if(t == null) {return onMismatch;}

        String fqcn = "" +  t.getClass().getCanonicalName();
        String msg = "" + t.getMessage();
        String str = fqcn;
        if(target == Target.MESSAGE) {
            str = msg;
        }

        Result result = str.startsWith(pattern) ? onMatch : onMismatch;
        //cout("\n[      exception:%s  target:%S   pattern:%s  result:%s      ]", t.getClass(), target, pattern, result);

        return result;
    }

    public final String getPattern() {
        return pattern;
    }

    public final Target getTarget() {
        return target;
    }

    private static void cout(String fmt, Object ... vals) {
        System.out.println(String.format(fmt, vals));
    }

}

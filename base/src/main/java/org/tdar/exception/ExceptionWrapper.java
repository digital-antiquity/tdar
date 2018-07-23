package org.tdar.exception;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Java exceptions are serializable: in the traditional sense of byte flattening... However, when attempting to transfer them via XML the lack of a no
 * args constructor in the StackTraceElement class causes problems. Hence this wrapper, which wraps exceptions flattened to Strings, thus allowing them to be
 * transferred across process boundaries in an XML format.
 */
@XmlRootElement
public class ExceptionWrapper implements Serializable {

    private static final long serialVersionUID = 2876359066385787018L;

    private String message;
    private boolean fatal = true;
    private String stackTrace;
    private String code;
    private String moreInfoUrlKey;

    private static final String CODE_NULL_STACKTRACE = "0";
    public static final int CODE_MAXLENGTH = 5;

    public static final String SEPARATOR = "::";

    public ExceptionWrapper() {
    }

    public ExceptionWrapper(String string, Throwable e) {
        this.message = string;
        this.stackTrace = ExceptionUtils.getStackTrace(e);
        this.code = ExceptionWrapper.convertExceptionToCode(stackTrace);
    }

    public ExceptionWrapper(String string, String stackTrace) {
        this.message = string;
        this.stackTrace = stackTrace;
        this.code = ExceptionWrapper.convertExceptionToCode(stackTrace);
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", getMessage(), SEPARATOR, getStackTrace());
    }

    public boolean isFatal() {
        return fatal;
    }

    public void setFatal(boolean fatal) {
        this.fatal = fatal;
    }

    public static String convertExceptionToCode(Throwable t) {
        if (t == null) {
            return CODE_NULL_STACKTRACE;
        }
        String trace = ExceptionUtils.getStackTrace(t);
        String code = Integer.toHexString(trace.hashCode());
        return code.toUpperCase();
    }

    public static String convertExceptionToCode(String trace) {
        if (trace == null) {
            return CODE_NULL_STACKTRACE;
        }
        String code = Integer.toHexString(trace.hashCode());
        return code.toUpperCase();
    }

    public String getErrorCode() {
        return code;
    }

    public String getMoreInfoUrlKey() {
        return moreInfoUrlKey;
    }

    public void setMoreInfoUrlKey(String moreInfoUrl) {
        this.moreInfoUrlKey = moreInfoUrl;
    }

}

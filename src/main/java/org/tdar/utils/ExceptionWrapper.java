package org.tdar.utils;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Java exceptions are serializable: in the traditional sense of byte flattening... However, when attempting to transfer them via XML the lack of a no
 * args constructor in the StackTraceElement class causes problems. Hence this wrapper, which wraps exceptions flattened to Strings, thus allowing them to be 
 * transferred across process boundaries in an XML format.
 */
@XmlRootElement
public class ExceptionWrapper implements Serializable {

    private static final long serialVersionUID = 2876359066385787018L;

    
    private String message;
    private boolean fatal;
    private String stackTrace;

    public ExceptionWrapper() {
    }
    
    public ExceptionWrapper(String string, String fullStackTrace) {
        setMessage(string);
        setStackTrace(fullStackTrace);
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
        return String.format("%s :: %s", getMessage() , getStackTrace());
    }

    public boolean isFatal() {
        return fatal;
    }

    public void setFatal(boolean fatal) {
        this.fatal = fatal;
    }
}

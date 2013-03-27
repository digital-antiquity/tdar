package org.tdar.utils;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExceptionWrapper implements Serializable {

    private static final long serialVersionUID = 2876359066385787018L;

    
    private String message;
    
    private String stackTrace;

    public ExceptionWrapper() {}
    
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
}

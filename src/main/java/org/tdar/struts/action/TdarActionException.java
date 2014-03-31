package org.tdar.struts.action;

import org.tdar.core.exception.StatusCode;

public class TdarActionException extends Exception {

    private static final long serialVersionUID = 5625972430936925843L;

    private final StatusCode responseStatusCode;

    public TdarActionException(StatusCode httpStatus, String message) {
        this(httpStatus, message, null);
    }

    public TdarActionException(StatusCode httpStatus, String message, Throwable t) {
        super(message, t);
        this.responseStatusCode = httpStatus;
    }

    public TdarActionException(StatusCode httpStatus, Throwable t) {
        super(t.getMessage(), t);
        this.responseStatusCode = httpStatus;
    }

    public String getResultName() {
        return responseStatusCode.getResultName();
    }

    public int getStatusCode() {
        return responseStatusCode.getHttpStatusCode();
    }

    public StatusCode getResponseStatusCode() {
        return responseStatusCode;
    }
}

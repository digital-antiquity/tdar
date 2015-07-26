package org.tdar.struts.action;

import org.tdar.core.exception.StatusCode;

public class TdarActionException extends Exception {

    private static final long serialVersionUID = 5625972430936925843L;

    private final StatusCode responseStatusCode;
    private final String response;

    public TdarActionException(StatusCode httpStatus, String message) {
        this(httpStatus, null, message, null);
    }

    public TdarActionException(StatusCode httpStatus, String response, String message) {
        this(httpStatus, response, message, null);
    }

    public TdarActionException(StatusCode httpStatus, String response, String message, Throwable t) {
        super(message, t);
        this.response = response;
        this.responseStatusCode = httpStatus;
    }

    public TdarActionException(StatusCode httpStatus, Throwable t) {
        super(t.getMessage(), t);
        this.responseStatusCode = httpStatus;
        this.response = null;
    }

    public int getStatusCode() {
        return getResponseStatusCode().getHttpStatusCode();
    }

    public StatusCode getResponseStatusCode() {
        return responseStatusCode;
    }

    public String getResponse() {
        return response;
    }
}

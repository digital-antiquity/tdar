package org.tdar.core.exception;

import org.apache.http.HttpStatus;
import org.tdar.struts.action.TdarActionSupport;

/**
 * $Id$
 * 
 * Represents an http response that encapsulates a status code, error message, and result name for our web layer {@see SessionSecurityInterceptor} to dispatch
 * appropriately. You can override the default result via withResultName().
 * 
 * FIXME: rename to ResponseStatus and make enum constant names consistent with HttpStatus code names?
 * 
 * @author Adam Brin, <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public enum StatusCode {

    OK(HttpStatus.SC_OK, "SUCCESS", TdarActionSupport.SUCCESS),
    CREATED(HttpStatus.SC_CREATED, "CREATED", "created"),
    GONE(HttpStatus.SC_GONE, "GONE", TdarActionSupport.GONE),
    UPDATED(HttpStatus.SC_ACCEPTED, "UPDATED", "updated"),
    NOT_FOUND(HttpStatus.SC_NOT_FOUND, "NOT FOUND", TdarActionSupport.NOT_FOUND),
    UNAUTHORIZED(HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED", TdarActionSupport.UNAUTHORIZED),
    BAD_REQUEST(HttpStatus.SC_BAD_REQUEST, "BAD REQUEST", "badrequest"),
    UNKNOWN_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "UNKNOWN ERROR", "unknownerror"),
    FORBIDDEN(HttpStatus.SC_FORBIDDEN, "NOT ALLOWED", "notallowed");

    private final int httpStatusCode;
    private final String errorMessage;
    private final ThreadLocal<String> resultNameThreadLocal;

    StatusCode(int httpStatusCode, String errorMessage, final String resultName) {
        this.httpStatusCode = httpStatusCode;
        this.errorMessage = errorMessage;
        this.resultNameThreadLocal = new ThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return resultName;
            }
        };
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getResultName() {
        return resultNameThreadLocal.get();
    }

    public StatusCode withResultName(String resultName) {
        resultNameThreadLocal.set(resultName);
        return this;
    }

    @Override
    public String toString() {
        return String.format("HTTP %d %s (%s)", httpStatusCode, errorMessage, getResultName());
    }
}

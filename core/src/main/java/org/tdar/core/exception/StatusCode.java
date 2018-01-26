package org.tdar.core.exception;

import org.apache.http.HttpStatus;

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

    OK(HttpStatus.SC_OK, "SUCCESS"), CREATED(HttpStatus.SC_CREATED, "CREATED"), GONE(HttpStatus.SC_GONE, "GONE"), UPDATED(HttpStatus.SC_ACCEPTED,
            "UPDATED"), NOT_FOUND(HttpStatus.SC_NOT_FOUND, "NOT FOUND"), UNAUTHORIZED(HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED"), BAD_REQUEST(
                    HttpStatus.SC_BAD_REQUEST, "BAD REQUEST"), UNKNOWN_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "UNKNOWN ERROR"), FORBIDDEN(
                            HttpStatus.SC_FORBIDDEN, "NOT ALLOWED"), NOT_IMPLEMENTED(HttpStatus.SC_NOT_IMPLEMENTED, "NOT IMPLEMENTED");

    private final int httpStatusCode;
    private final String errorMessage;

    StatusCode(int httpStatusCode, String errorMessage) {
        this.httpStatusCode = httpStatusCode;
        this.errorMessage = errorMessage;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return String.format("HTTP %d %s", httpStatusCode, errorMessage);
    }

    public static boolean shouldShowException(int statusCode) {
        switch (statusCode) {
            case HttpStatus.SC_FORBIDDEN:
            case HttpStatus.SC_GONE:
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_OK:
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            case HttpStatus.SC_NOT_FOUND:
                return false;
            default:
                return true;
        }
    }

    public boolean isCritical() {
        switch (this) {
            case CREATED:
            case OK:
            case UPDATED:
                return false;
            default:
                return true;
        }
    }
}

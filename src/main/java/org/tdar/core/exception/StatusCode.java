package org.tdar.core.exception;

public enum StatusCode {
    
    CREATED(201, "CREATED", "created"),
    UPDATED(204, "UPDATED", "updated"),
    NOT_FOUND(404, "NOT FOUND", "notfound"),
    UNAUTHORIZED(401,"UNAUTHORIZED", "unauthorized"),
    BAD_REQUEST(400, "BAD REQUEST", "badrequest"),
    UNKNOWN_ERROR(500,"UNKNOWN ERROR", "unknownerror"),
    NOT_ALLOWED(403, "NOT ALLOWED", "notallowed");

    private final int errorCode;
    private final String errorMessage;
    private final String resultString;

    StatusCode(int errorCode, String errorMessage, String resultString) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.resultString = resultString;
    }

    public int getStatusCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getResultString() {
        return resultString;
    }
}

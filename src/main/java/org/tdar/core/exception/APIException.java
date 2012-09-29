package org.tdar.core.exception;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class APIException extends Exception {

    private static final long serialVersionUID = -6383202970353307213L;

    private ErrorStatusCode code;

    public APIException(String msg, Throwable e, ErrorStatusCode code) {
        super(msg, e);
        this.setCode(code);
    }

    public APIException(String msg, ErrorStatusCode code) {
        super(msg);
        this.setCode(code);
    }

    public void setCode(ErrorStatusCode code) {
        this.code = code;
    }

    public ErrorStatusCode getCode() {
        return code;
    }
}

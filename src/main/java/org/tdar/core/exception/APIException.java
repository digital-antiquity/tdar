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

    private StatusCode code;

    public APIException(String msg, Throwable e, StatusCode code) {
        super(msg, e);
        this.setCode(code);
    }

    public APIException(String msg, StatusCode code) {
        super(msg);
        this.setCode(code);
    }

    public void setCode(StatusCode code) {
        this.code = code;
    }

    public StatusCode getCode() {
        return code;
    }
}

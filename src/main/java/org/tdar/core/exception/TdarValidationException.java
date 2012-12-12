package org.tdar.core.exception;

public class TdarValidationException extends TdarRecoverableRuntimeException {

    private static final long serialVersionUID = 3624836043900396149L;

    public TdarValidationException(String message) {
        super(message);
    }
    
    public TdarValidationException() {
        super();
    }
    
    public TdarValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TdarValidationException(Throwable cause) {
        super(cause);
    }
}

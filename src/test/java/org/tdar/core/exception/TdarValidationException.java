package org.tdar.core.exception;

public class TdarValidationException extends TdarRecoverableRuntimeException {

    private static final long serialVersionUID = 3624836043900396149L;

    public TdarValidationException(String message) {
        super(message);
    }
}

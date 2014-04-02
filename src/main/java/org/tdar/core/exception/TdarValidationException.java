package org.tdar.core.exception;

import java.util.List;

public class TdarValidationException extends TdarRecoverableRuntimeException {

    private static final long serialVersionUID = 3624836043900396149L;

    public TdarValidationException(String message) {
        super(message);
    }

    public TdarValidationException(String key, List<?> values) {
        super(key, values);
    }

    public TdarValidationException() {
        super();
    }

    public TdarValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TdarValidationException(String message, Throwable cause, List<?> values) {
        super(message, cause, values);
    }

    public TdarValidationException(Throwable cause) {
        super(cause);
    }
}

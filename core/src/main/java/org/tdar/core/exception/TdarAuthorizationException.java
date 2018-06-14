package org.tdar.core.exception;

import java.util.List;

import org.tdar.exception.TdarRecoverableRuntimeException;

public class TdarAuthorizationException extends TdarRecoverableRuntimeException {

    private static final long serialVersionUID = 6530578296284063293L;

    public TdarAuthorizationException(String message) {
        super(message);
    }

    public TdarAuthorizationException(String key, List<?> values) {
        super(key, values);
    }

    public TdarAuthorizationException() {
        super();
    }

    public TdarAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TdarAuthorizationException(String message, Throwable cause, List<?> values) {
        super(message, cause, values);
    }

    public TdarAuthorizationException(Throwable cause) {
        super(cause);
    }
}

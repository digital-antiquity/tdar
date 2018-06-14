package org.tdar.core.exception;

import java.util.List;

import org.tdar.exception.TdarRecoverableRuntimeException;

public class NonFatalWorkflowException extends TdarRecoverableRuntimeException {

    public NonFatalWorkflowException(String message, List<?> values) {
        super(message, values);
    }

    public NonFatalWorkflowException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -7747433522748419302L;

}

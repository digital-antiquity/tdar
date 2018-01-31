package org.tdar.core.exception;

import java.util.List;

public class FilestoreLoggingException extends TdarRecoverableRuntimeException {

    private static final long serialVersionUID = 4776284275136688367L;

    public FilestoreLoggingException(String message, List<?> values) {
        super(message, values);
    }

    public FilestoreLoggingException(String message) {
        super(message);
    }

}

package org.tdar.exception;

import java.util.List;

public class ConfigurationFileException extends TdarRecoverableRuntimeException {

    private static final long serialVersionUID = -4378940136523618579L;

    public ConfigurationFileException(String message, List<?> values) {
        super(message, values);
    }

    public ConfigurationFileException(String message) {
        super(message);
    }

}

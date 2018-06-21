package org.tdar.core.exception;

import org.tdar.exception.I18nRuntimeException;

public class PdfCoverPageGenerationException extends I18nRuntimeException {

    private static final long serialVersionUID = 7411723303332607060L;

    public PdfCoverPageGenerationException() {
        super();
    }

    public PdfCoverPageGenerationException(String message) {
        super(message);
    }

    public PdfCoverPageGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

}

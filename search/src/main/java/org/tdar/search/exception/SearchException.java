package org.tdar.search.exception;

import org.tdar.core.exception.I18nException;

public class SearchException extends I18nException {

    private static final long serialVersionUID = -965879718954993589L;

    public SearchException() {
        super();
    }

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Exception e) {
        super(message, e);
    }

}

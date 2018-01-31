package org.tdar.search.exception;

import org.tdar.core.exception.I18nException;

public class SearchIndexException extends I18nException {

    private static final long serialVersionUID = -2457359508886611739L;

    public SearchIndexException() {
        super();
    }

    public SearchIndexException(String message) {
        super(message);
    }

    public SearchIndexException(String message, Exception e) {
        super(message, e);
    }

}

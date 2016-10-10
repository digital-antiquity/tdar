package org.tdar.search.exception;

import org.tdar.core.exception.TdarRecoverableRuntimeException;

public class SearchException extends TdarRecoverableRuntimeException {

    private static final long serialVersionUID = -965879718954993589L;

    public SearchException() {
        super();
    }

    public SearchException(String message) {
        super(message);
    }

}

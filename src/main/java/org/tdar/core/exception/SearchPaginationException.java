package org.tdar.core.exception;

public class SearchPaginationException extends TdarRecoverableRuntimeException {

    private static final long serialVersionUID = 8244347560992978402L;

    public SearchPaginationException() {
        super();
    }
    
    public SearchPaginationException(String message) {
        super(message);
    }


}

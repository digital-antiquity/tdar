package org.tdar.core.dao.external.auth;

import java.io.Serializable;

public enum AuthenticationResult implements Serializable {
    VALID(""),
    INVALID_PASSWORD("Authentication failed.  Please check that your username and password were entered correctly."),
    INACTIVE_ACCOUNT("This account is inactive."),
    ACCOUNT_DOES_NOT_EXIST("This account does not exist"),
    REMOTE_EXCEPTION("The authentication server is currently down.  Please try authenticating again in a few minutes.");
    private final String message;
    private transient ThreadLocal<Throwable> threadLocalThrowable = new ThreadLocal<Throwable>();

    AuthenticationResult(String message) {
        this.message = message;
    }

    public AuthenticationResult exception(Throwable throwable) {
        threadLocalThrowable.set(throwable);
        return this;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        Throwable throwable = threadLocalThrowable.get();
        if (throwable == null) {
            return message;
        }
        return message + " Exception: " + throwable.getLocalizedMessage();
    }

    public boolean isValid() {
        return this == VALID;
    }
}
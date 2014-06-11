package org.tdar.core.dao.external.auth;

import org.tdar.core.bean.entity.TdarUser;

/**
 * FIXME: localize messages
 * 
 */
public enum AuthenticationResult {
    VALID(""),
    INVALID_PASSWORD("Authentication failed.  Please check that your username and password were entered correctly."),
    INACTIVE_ACCOUNT("This account is inactive."),
    ACCOUNT_DOES_NOT_EXIST("This account does not exist"),
    REMOTE_EXCEPTION("The authentication server is currently down.  Please try authenticating again in a few minutes."),
    ACCOUNT_EXISTS("The account already exists");

    private final String message;
    private ThreadLocal<TdarUser> threadLocalPerson = new ThreadLocal<>();
    private transient ThreadLocal<Throwable> threadLocalThrowable = new ThreadLocal<>();

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

    @Override
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

    public TdarUser getPerson() {
        return threadLocalPerson.get();
    }

    public AuthenticationResult withUser(TdarUser person) {
        threadLocalPerson.set(person);
        return this;
    }

}
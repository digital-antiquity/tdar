package org.tdar.core.dao.external.auth;

public enum AuthenticationResultType {
    VALID(""), INVALID_PASSWORD("Authentication failed.  Please check that your username and password were entered correctly."), INACTIVE_ACCOUNT(
            "This account is inactive."), ACCOUNT_DOES_NOT_EXIST("This account does not exist"), REMOTE_EXCEPTION(
                    "The authentication server is currently down.  Please try authenticating again in a few minutes."), ACCOUNT_EXISTS(
                            "The account already exists");

    private final String message;

    AuthenticationResultType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isValid() {
        switch (this) {
            case VALID:
                return true;
            default:
                return false;
        }
    }
}
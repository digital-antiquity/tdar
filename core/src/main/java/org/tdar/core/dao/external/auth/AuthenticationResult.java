package org.tdar.core.dao.external.auth;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.external.AuthenticationServiceImpl.AuthenticationStatus;

public class AuthenticationResult {
    public enum AuthenticationResultType {
        VALID(""),
        INVALID_PASSWORD("Authentication failed.  Please check that your username and password were entered correctly."),
        INACTIVE_ACCOUNT("This account is inactive."),
        ACCOUNT_DOES_NOT_EXIST("This account does not exist"),
        REMOTE_EXCEPTION("The authentication server is currently down.  Please try authenticating again in a few minutes."),
        ACCOUNT_EXISTS("The account already exists");

        private final String message;

        AuthenticationResultType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public boolean isValid() {
            switch(this) {
                case VALID:
                    return true;
                default:
                    return false;
            }
        }
    }

    private TdarUser person;
    private Throwable throwable;
    private AuthenticationResultType type;
    private AuthenticationStatus status;
    private String token;
    private String tokenUsername;

    public AuthenticationResult(AuthenticationResultType type, TdarUser person) {
        this.setType(type);
        this.person = person;
    }

    public AuthenticationResult(AuthenticationResultType type) {
        this.setType(type);
    }

    public AuthenticationResult(AuthenticationResultType type, String token) {
        this.setType(type);
        this.setToken(token);
    }

    public AuthenticationResult(AuthenticationResultType type, Throwable t) {
        this.setType(type);
        this.throwable = t;
    }

    public AuthenticationResult exception(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public String toString() {
        if (throwable == null) {
            return getType().getMessage();
        }
        return getType().getMessage() + " Exception: " + throwable.getLocalizedMessage();
    }

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser person) {
        this.person = person;
    }

    public AuthenticationResultType getType() {
        return type;
    }

    public void setType(AuthenticationResultType type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenUsername() {
        return tokenUsername;
    }

    public void setTokenUsername(String tokenUsername) {
        this.tokenUsername = tokenUsername;
    }

    public AuthenticationStatus getStatus() {
        return status;
    }

    public void setStatus(AuthenticationStatus status) {
        this.status = status;
    }

}
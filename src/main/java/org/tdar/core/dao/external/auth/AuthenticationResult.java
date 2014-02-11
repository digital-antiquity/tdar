package org.tdar.core.dao.external.auth;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.utils.MessageHelper;


public enum AuthenticationResult  {
    VALID(""), 
    INVALID_PASSWORD("Authentication failed.  Please check that your username and password were entered correctly."),
    INACTIVE_ACCOUNT("This account is inactive."),
    ACCOUNT_DOES_NOT_EXIST("This account does not exist"),
    REMOTE_EXCEPTION("The authentication server is currently down.  Please try authenticating again in a few minutes."),
    ACCOUNT_EXISTS(MessageHelper.getMessage("The account already exists"));
    
    private final String message;
    private Person person;
    private ContributorRequest contributorRequest;
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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public ContributorRequest getContributorRequest() {
        return contributorRequest;
    }

    public void setContributorRequest(ContributorRequest contributorRequest) {
        this.contributorRequest = contributorRequest;
    }
}
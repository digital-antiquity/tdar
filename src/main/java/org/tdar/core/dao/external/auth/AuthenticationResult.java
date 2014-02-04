package org.tdar.core.dao.external.auth;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.utils.MessageHelper;


public enum AuthenticationResult  {
    VALID(MessageHelper.getMessage("authenticationResult.valid")),
    INVALID_PASSWORD(MessageHelper.getMessage("authenticationResult.invalid_password")),
    INACTIVE_ACCOUNT(MessageHelper.getMessage("authenticationResult.invalid_account")),
    ACCOUNT_DOES_NOT_EXIST(MessageHelper.getMessage("authenticationResult.account_does_not_exist")),
    REMOTE_EXCEPTION(MessageHelper.getMessage("authenticationResult.remote_exception")), 
    ACCOUNT_EXISTS(MessageHelper.getMessage("authenticationResult.account_already_exists"));
    
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
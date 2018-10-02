package org.tdar.core.service.external.auth;

import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;

public abstract class AbstractWithContributorUserRegistration extends UserRegistration {

    private static final long serialVersionUID = -7908954787663238467L;

    public AbstractWithContributorUserRegistration(AntiSpamHelper h) {
        super(h);
    }

    @Override
    public ErrorTransferObject validate(AuthenticationService authService, String remoteHost) {
  
        ErrorTransferObject validate = super.validate(authService, remoteHost);
        if (!isRequestingContributorAccess()) {
            validate.addFieldError(getPrefix() + "requestingContributorAccess", "userAccountController.require_contributor_agreement");
        }
        return validate;
    }
}

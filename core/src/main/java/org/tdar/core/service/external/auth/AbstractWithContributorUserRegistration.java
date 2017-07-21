package org.tdar.core.service.external.auth;

import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;

public abstract class AbstractWithContributorUserRegistration extends UserRegistration {

    private static final long serialVersionUID = -7908954787663238467L;
    private boolean acceptTermsOfUseAndContributorAgreement;

    public AbstractWithContributorUserRegistration(AntiSpamHelper h) {
        super(h);
    }


    @Override
    public ErrorTransferObject validate(AuthenticationService authService, String remoteHost) {
        if (isAcceptTermsOfUseAndContributorAgreement()) {
            setAcceptTermsOfUse(true);
            setRequestingContributorAccess(true);
        }
        ErrorTransferObject validate = super.validate(authService, remoteHost);
        if (!isRequestingContributorAccess()) {
            validate.addFieldError(getPrefix() + "requestingContributorAccess", "userAccountController.require_contributor_agreement");
        }
        return validate;
    }

    public boolean isAcceptTermsOfUseAndContributorAgreement() {
        return acceptTermsOfUseAndContributorAgreement;
    }

    public void setAcceptTermsOfUseAndContributorAgreement(boolean acceptTermsOfUseAndContributorAgreement) {
        this.acceptTermsOfUseAndContributorAgreement = acceptTermsOfUseAndContributorAgreement;
    }
}

package org.tdar.struts.data;

import java.util.List;

import org.tdar.core.service.external.AuthenticationService;

import com.opensymphony.xwork2.TextProvider;

public class CartUserRegistration extends UserRegistration {

    private static final long serialVersionUID = 6764554347927128792L;

    private boolean acceptTermsOfUseAndContributorAgreement;

    public CartUserRegistration(AntiSpamHelper h) {
        super(h);
    }

    @Override
    public List<String> validate(TextProvider textProvider, AuthenticationService authService) {
        if (isAcceptTermsOfUseAndContributorAgreement()) {
            setAcceptTermsOfUse(true);
            setRequestingContributorAccess(true);
        }
        List<String> validate = super.validate(textProvider, authService);
        if (!isRequestingContributorAccess()) {
            validate.add(textProvider.getText("userAccountController.require_contributor_agreement"));
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

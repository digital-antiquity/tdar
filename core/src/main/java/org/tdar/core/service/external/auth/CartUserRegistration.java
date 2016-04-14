package org.tdar.core.service.external.auth;

public class CartUserRegistration extends UserRegistration {

    private static final long serialVersionUID = 6764554347927128792L;

    public CartUserRegistration(AntiSpamHelper h) {
        super(h);
    }

    @Override
    public String getPrefix() {
        return "registrationInfo.";
    };
}

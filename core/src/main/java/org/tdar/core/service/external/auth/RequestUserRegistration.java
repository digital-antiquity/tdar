package org.tdar.core.service.external.auth;

public class RequestUserRegistration extends AbstractWithContributorUserRegistration {

    private static final long serialVersionUID = -4785580932944910361L;

    public RequestUserRegistration(AntiSpamHelper h) {
        super(h);
    }

    @Override
    public String getPrefix() {
        return "registrationInfo.";
    };

}

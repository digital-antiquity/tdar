package org.tdar.struts.data;

import org.tdar.core.service.external.RecaptchaService;

public class DownloadUserLogin extends UserLogin {

    public DownloadUserLogin(RecaptchaService recaptchaService) {
        super(recaptchaService);
    }

    public DownloadUserLogin() {
        super(null);
    }

    private static final long serialVersionUID = -6157970041213328371L;
}

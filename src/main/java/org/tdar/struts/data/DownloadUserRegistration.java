package org.tdar.struts.data;

import org.tdar.core.service.external.RecaptchaService;

/**
 * Appends file download specific data to typical user registration. 
 */
public class DownloadUserRegistration extends UserRegistration {

    private static final long serialVersionUID = 703786803735892594L;
    
    public DownloadUserRegistration() {
    }

    public DownloadUserRegistration(RecaptchaService recaptchaService) {
        super(recaptchaService);
    }
}

package org.tdar.struts.data;


/**
 * Appends file download specific data to typical user registration. 
 */
public class DownloadUserRegistration extends UserRegistration {

    private static final long serialVersionUID = 703786803735892594L;
    
    public DownloadUserRegistration() {
        super();
    }

    public DownloadUserRegistration(AntiSpamHelper h) {
        super(h);
    }
}

package org.tdar.struts.data;

import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.external.RecaptchaService;

/**
 * Appends file download specific data to typical user registration. 
 */
public class DownloadUserRegistration extends UserRegistration {

    private static final long serialVersionUID = 703786803735892594L;

    private InformationResourceFileVersion version = new InformationResourceFileVersion();
    private Resource  resource = new Resource();
    private String returnUrl;
    private String inputUrl;
    
    public DownloadUserRegistration() {
    }

    public DownloadUserRegistration(RecaptchaService recaptchaService) {
        super(recaptchaService);
    }
    
    public Resource getResource() {
        return resource;
    }

    public void setResource(InformationResource resource) {
        this.resource = resource;
    }

    public InformationResourceFileVersion getVersion() {
        return version;
    }

    public void setVersion(InformationResourceFileVersion version) {
        this.version = version;
    }

    public String getInputUrl() {
        return inputUrl;
    }

    public void setInputUrl(String inputUrl) {
        this.inputUrl = inputUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    
    
}

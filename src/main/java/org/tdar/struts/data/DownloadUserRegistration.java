package org.tdar.struts.data;

import org.tdar.core.service.external.RecaptchaService;

/**
 * Appends file download specific data to typical user registration. 
 */
public class DownloadUserRegistration extends UserRegistration {

    private static final long serialVersionUID = 703786803735892594L;

    private Long informationResourceFileId;
    private Long informationResourceId;
    private String fileName;
    private String download;
    
    public DownloadUserRegistration() {
    }

    public DownloadUserRegistration(RecaptchaService recaptchaService) {
        super(recaptchaService);
    }
    
    public Long getInformationResourceFileId() {
        return informationResourceFileId;
    }
    public void setInformationResourceFileId(Long informationResourceFileId) {
        this.informationResourceFileId = informationResourceFileId;
    }
    public Long getInformationResourceId() {
        return informationResourceId;
    }
    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getDownload() {
        return download;
    }
    public void setDownload(String download) {
        this.download = download;
    }
    
    
    
}

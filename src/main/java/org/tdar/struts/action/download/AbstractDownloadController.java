package org.tdar.struts.action.download;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.DownloadService;
import org.tdar.core.service.PdfService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.data.DownloadUserLogin;
import org.tdar.struts.data.DownloadUserRegistration;

import com.opensymphony.xwork2.Preparable;

public class AbstractDownloadController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = -1831798412944149017L;
    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient PdfService pdfService;

    @Autowired
    private transient InformationResourceFileVersionService informationResourceFileVersionService;

    public static final String GET = "get";
    public static final String DOWNLOAD_ALL_LANDING = "show-download-landing";
    public static final String DOWNLOAD_ALL = "downloadAllAsZip";
    private Long informationResourceFileVersionId;
    private Long informationResourceId;

    public static final String FORBIDDEN = "forbidden";
    private InformationResource informationResource;
    private InformationResourceFileVersion informationResourceFileVersion;

    @Autowired
    private transient RecaptchaService recaptchaService;
    private AntiSpamHelper h = new AntiSpamHelper(recaptchaService);
    private DownloadUserRegistration downloadRegistration = new DownloadUserRegistration(recaptchaService);
    private DownloadUserLogin downloadUserLogin = new DownloadUserLogin(recaptchaService);

    public InformationResource getInformationResource() {
        return informationResource;
    }

    public void setInformationResource(InformationResource informationResource) {
        this.informationResource = informationResource;
    }

    public InformationResourceFileVersion getInformationResourceFileVersion() {
        return informationResourceFileVersion;
    }

    public void setInformationResourceFileVersion(InformationResourceFileVersion informationResourceFileVersion) {
        this.informationResourceFileVersion = informationResourceFileVersion;
    }

    public Long getInformationResourceFileVersionId() {
        return informationResourceFileVersionId;
    }

    public void setInformationResourceFileVersionId(Long informationResourceFileVersionId) {
        this.informationResourceFileVersionId = informationResourceFileVersionId;
    }

    public Long getInformationResourceId() {
        return informationResourceId;
    }

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public DownloadUserRegistration getDownloadRegistration() {
        return downloadRegistration;
    }

    public void setDownloadRegistration(DownloadUserRegistration downloadRegistration) {
        this.downloadRegistration = downloadRegistration;
    }

    public DownloadUserLogin getDownloadUserLogin() {
        return downloadUserLogin;
    }

    public void setDownloadUserLogin(DownloadUserLogin downloadUserLogin) {
        this.downloadUserLogin = downloadUserLogin;
    }

    @Override
    public void prepare() {
        Long irId = getInformationResourceId();
        if (Persistable.Base.isNullOrTransient(irId)) {
            irId = getSessionData().getInformationResourceId();
        }

        Long irfvId = getInformationResourceFileVersionId();
        if (Persistable.Base.isNullOrTransient(irfvId)) {
            irfvId = getSessionData().getInformationResourceFileVersionId();
        }

        if (Persistable.Base.isNullOrTransient(irfvId) &&
                Persistable.Base.isNullOrTransient(irId)) {
            addActionError(getText("downloadController.specify_what_to_download"));
        }
        if (Persistable.Base.isNotNullOrTransient(irId)) {
            setInformationResource(getGenericService().find(InformationResource.class, irId));
        }
        if (Persistable.Base.isNotNullOrTransient(irfvId)) {
            setInformationResourceFileVersion(getGenericService().find(InformationResourceFileVersion.class, irfvId));
        }
    }

}

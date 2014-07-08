package org.tdar.struts.action.download;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.service.DownloadService;
import org.tdar.core.service.PdfService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.struts.data.DownloadUserLogin;
import org.tdar.struts.data.DownloadUserRegistration;

import com.opensymphony.xwork2.Preparable;

public class AbstractDownloadController extends AuthenticationAware.Base implements Preparable, DownloadHandler {

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
    public static final String LOGIN_REGISTER_PROMPT = "../filestore/download-unauthenticated.ftl";
    public static final String DOWNLOAD_SUFFIX = "informationResourceId=${informationResourceId}&informationResourceFileVersionId=${informationResourceFileVersionId}";
    public static final String SUCCESS_REDIRECT_DOWNLOAD = "/filestore/confirm?" + DOWNLOAD_SUFFIX;
    public static final String DOWNLOAD_SINGLE_LANDING = "/filestore/get?" +DOWNLOAD_SUFFIX;
    public static final String FORBIDDEN = "forbidden";
    public static final String SHOW_DOWNLOAD_LANDING = "show-download-landing";
    public static final String DOWNLOAD_ALL_LANDING = "/filestore/show-download-landing?" + DOWNLOAD_SUFFIX;
    public static final String DOWNLOAD_ALL = "downloadAllAsZip";
    private Long informationResourceFileVersionId;
    private Long informationResourceId;
    private transient InputStream inputStream;
    private String contentType;
    private String fileName;
    private Long contentLength;
    private Integer version;
    private boolean coverPageIncluded = true;
    private VersionType type;
    private String dispositionPrefix = "";


    
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
        Long irfvId = getInformationResourceFileVersionId();

        getLogger().debug("IRID: {}, IRFVID: {}", irId, irfvId);
        if (Persistable.Base.isNullOrTransient(irfvId) &&
                Persistable.Base.isNullOrTransient(irId)) {
            addActionError(getText("downloadController.specify_what_to_download"));
        }
        if (Persistable.Base.isNotNullOrTransient(irId)) {
            setInformationResource(getGenericService().find(InformationResource.class, irId));
            //bad, but force onto session until better way found
            getInformationResource().getLatestVersions();
        }
        if (Persistable.Base.isNotNullOrTransient(irfvId)) {
            setInformationResourceFileVersion(getGenericService().find(InformationResourceFileVersion.class, irfvId));
            //bad, but force onto session until better way found
            informationResourceFileVersion.getInformationResourceFile().getLatestThumbnail();
        }
    }

    

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public VersionType getType() {
        return type;
    }

    public void setType(VersionType type) {
        this.type = type;
    }

    public String getDispositionPrefix() {
        return dispositionPrefix;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void setContentType(String mimeType) {
        this.contentType = mimeType;
    }

    @Override
    public void setContentLength(long length) {
        this.contentLength = length;
    }

    @Override
    public void setDispositionPrefix(String string) {
        this.dispositionPrefix = string;
    }

    @Override
    public boolean isCoverPageIncluded() {
        return coverPageIncluded;
    }

    public void setCoverPageIncluded(boolean coverPageIncluded) {
        this.coverPageIncluded = coverPageIncluded;
    }
}

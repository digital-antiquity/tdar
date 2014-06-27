package org.tdar.struts.action;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.service.DownloadService;
import org.tdar.core.service.PdfService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.struts.data.DownloadUserRegistration;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/filestore")
@Results({
        @Result(name = TdarActionSupport.SUCCESS, type = "stream",
                params = {
                        "contentType", "${contentType}",
                        "inputName", "inputStream",
                        "contentDisposition", "${dispositionPrefix}filename=\"${fileName}\"",
                        "contentLength", "${contentLength}"
                }
        ),
        @Result(name = TdarActionSupport.ERROR, type = "httpheader", params = { "error", "404" }),
        @Result(name = TdarActionSupport.FORBIDDEN, type = "httpheader", params = { "error", "403" })

})
@Component
@Scope("prototype")
public class DownloadController extends AuthenticationAware.Base implements DownloadHandler, Preparable {

    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient ResourceService resourceService;

    public static final String GET = "get";
    private static final long serialVersionUID = 7548544212676661097L;
    private static final String DOWNLOAD_ALL_LANDING = "show-download-landing";
    public static final String DOWNLOAD_ALL = "downloadAllAsZip";
    private transient InputStream inputStream;
    private String contentType;
    private String fileName;
    private Long contentLength;
    private Integer version;
    private boolean coverPageIncluded = true;
    private VersionType type;
    private Long informationResourceFileVersionId;
    private Long informationResourceId;

    private String dispositionPrefix = "";

    public static final String FORBIDDEN = "forbidden";

    @Autowired
    private transient RecaptchaService recaptchaService;
    private AntiSpamHelper h = new AntiSpamHelper(recaptchaService);
    private DownloadUserRegistration downloadRegistration = new DownloadUserRegistration();

    @Autowired
    private transient PdfService pdfService;

    @Autowired
    private transient InformationResourceFileVersionService informationResourceFileVersionService;
    private InformationResource informationResource;
    private InformationResourceFileVersion informationResourceFileVersion;

    @Action(value = CONFIRM, results = { @Result(name = CONFIRM, location = "confirm-download.ftl") })
    public String confirm() throws TdarActionException {
        // FIXME: some of the work in execute() is unnecessary as we are only rendering the confirm page.
        String status = execute();
        if (status != SUCCESS) {
            return status;
        }
        return "confirm";
    }

    @Action(value = "download",
            results = {
                    @Result(name = SUCCESS, type = "redirect", location = GET),
                    @Result(name = LOGIN, type = "freemarker", location = "download-unauthenticated.ftl") },
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") })
    public String download() {
        if (getInformationResourceFileVersionId() == null) {
            return ERROR;
        }
        if (isAuthenticated()) {
            return SUCCESS;
        }
        getDownloadRegistration().setVersion(informationResourceFileVersion);
        getDownloadRegistration().setResource(informationResource);
        getDownloadRegistration().setInputUrl(getCurrentUrl());
        getSessionData().setReturnFailureUrl(getCurrentUrl());
        getDownloadRegistration().setReturnUrl(String.format("/download/confirm?informationResourceFileVersionId=%s", getInformationResourceFileVersionId()));
        getSessionData().setReturnUrl(String.format("/download/confirm?informationResourceFileVersionId=%s", getInformationResourceFileVersionId()));
        return LOGIN;
    }

    @Override
    @Action(value = GET)
    public String execute() throws TdarActionException {
        if (informationResourceFileVersion == null) {
            getLogger().debug("no informationResourceFiles associated with this id [{}]", getInformationResourceFileVersionId());
            return ERROR;
        }
        if (!authorizationService.canDownload(informationResourceFileVersion, getAuthenticatedUser())) {
            String msg = String.format("user %s does not have permissions to download %s", getAuthenticatedUser(), informationResourceFileVersion);
            getLogger().warn(msg);
            return FORBIDDEN;
        }
        setInformationResourceId(informationResourceFileVersion.getInformationResourceId());
        getLogger().info("user {} downloaded {}", getAuthenticatedUser(), informationResourceFileVersion);
        downloadService.handleDownload(getAuthenticatedUser(), this, getInformationResourceId(), informationResourceFileVersion);
        return SUCCESS;
    }

    @Actions({
            @Action(value = THUMBNAIL, interceptorRefs = { @InterceptorRef("unauthenticatedStack") }),
            @Action(value = SM, interceptorRefs = { @InterceptorRef("unauthenticatedStack") })
    })
    public String thumbnail() throws TdarActionException {
        if (informationResourceFileVersion == null) {
            getLogger().warn("thumbnail request: no informationResourceFiles associated with this id [{}]", getInformationResourceFileVersionId());
            return ERROR;
        }

        // image must be thumbnail
        if (informationResourceFileVersion.getFileVersionType() != VersionType.WEB_SMALL) {
            getLogger().warn("thumbail request: requested version exists but is not a thumbnail: {}", getInformationResourceFileVersionId());
            return ERROR;
        }

        if (!authorizationService.canDownload(informationResourceFileVersion, getAuthenticatedUser())) {
            getLogger().warn("thumbail request: resource is confidential/embargoed: {}", getInformationResourceFileVersionId());
            return FORBIDDEN;
        }

        downloadService.handleDownload(getAuthenticatedUser(), this, getInformationResourceId(), informationResourceFileVersion);
        return SUCCESS;
    }

    @Action(value = DOWNLOAD_ALL)
    public String downloadZipArchive() throws TdarActionException {
        if (getInformationResourceId() == null) {
            return ERROR;
        }

        List<InformationResourceFileVersion> versions = new ArrayList<>();
        for (InformationResourceFile irf : informationResource.getInformationResourceFiles()) {
            if (irf.isDeleted()) {
                continue;
            }
            if (!authorizationService.canDownload(irf, getAuthenticatedUser())) {
                getLogger().warn("thumbail request: resource is confidential/embargoed: {}", getInformationResourceFileVersionId());
                return FORBIDDEN;
            }
            getLogger().trace("adding: {}", irf.getLatestUploadedVersion());
            versions.add(irf.getLatestUploadedOrArchivalVersion());
        }
        if (CollectionUtils.isEmpty(versions)) {
            return ERROR;
        }

        downloadService.handleDownload(getAuthenticatedUser(), this, getInformationResourceId(), versions.toArray(new InformationResourceFileVersion[0]));
        return SUCCESS;
    }

    @Action(value = DOWNLOAD_ALL_LANDING, results = {
            @Result(name = SUCCESS, type = "freemarker", location = "download-all.ftl") })
    public String showDownloadAllLandingPage() {
        return SUCCESS;
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

    public Long getInformationResourceId() {
        return informationResourceId;
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

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

    @Override
    public boolean isCoverPageIncluded() {
        return coverPageIncluded;
    }

    public void setCoverPageIncluded(boolean coverPageIncluded) {
        this.coverPageIncluded = coverPageIncluded;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public Long getInformationResourceFileVersionId() {
        return informationResourceFileVersionId;
    }

    public void setInformationResourceFileVersionId(Long informationResourceFileVersionId) {
        this.informationResourceFileVersionId = informationResourceFileVersionId;
    }

    @Override
    public void prepare() {
        if (Persistable.Base.isNullOrTransient(getInformationResourceId()) &&
                Persistable.Base.isNullOrTransient(getInformationResourceFileVersionId())) {
            addActionError(getText("downloadController.specify_what_to_download"));
        }
        if (Persistable.Base.isNotNullOrTransient(getInformationResourceId())) {
            setInformationResource(getGenericService().find(InformationResource.class, informationResourceId));
        }
        if (Persistable.Base.isNotNullOrTransient(getInformationResourceFileVersionId())) {
            setInformationResourceFileVersion(getGenericService().find(InformationResourceFileVersion.class, informationResourceFileVersionId));
        }
    }

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

    public DownloadUserRegistration getDownloadRegistration() {
        return downloadRegistration;
    }

    public void setDownloadRegistration(DownloadUserRegistration downloadRegistration) {
        this.downloadRegistration = downloadRegistration;
    }

}

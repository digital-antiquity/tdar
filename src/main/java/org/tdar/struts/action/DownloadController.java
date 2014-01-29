package org.tdar.struts.action;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.service.PdfService;
import org.tdar.struts.data.DownloadHandler;

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
public class DownloadController extends AuthenticationAware.Base implements DownloadHandler {

    public static final String GET = "get";
    private static final long serialVersionUID = 7548544212676661097L;
    private static final String DOWNLOAD_ALL_LANDING = "show-download-landing";
    public static final String DOWNLOAD_ALL = "downloadAllAsZip";
    private transient InputStream inputStream;
    private String contentType;
    private String fileName;
    private Long contentLength;
    private Long informationResourceFileId;
    private Integer version;
    private boolean coverPageIncluded = true;
    private VersionType type;
    private Long informationResourceId;

    private String dispositionPrefix = "";

    public static final String FORBIDDEN = "forbidden";

    @Autowired
    private transient PdfService pdfService;

    @Action(value = CONFIRM, results = { @Result(name = CONFIRM, location = "/WEB-INF/content/confirm-download.ftl") })
    public String confirm() throws TdarActionException {
        // FIXME: some of the work in execute() is unnecessary as we are only rendering the confirm page.
        String status = execute();
        if (status != SUCCESS) {
            return status;
        }
        ;
        return "confirm";
    }

    @Override
    @Action(value = GET)
    public String execute() throws TdarActionException {
        InformationResourceFileVersion irFileVersion = null;
        if (informationResourceFileId == null)
            return ERROR;
        irFileVersion = getInformationResourceFileVersionService().find(informationResourceFileId);
        if (irFileVersion == null) {
            getLogger().debug("no informationResourceFiles associated with this id [" + informationResourceFileId + "]");
            return ERROR;
        }
        if (!getAuthenticationAndAuthorizationService().canDownload(irFileVersion, getSessionData().getPerson())) {
            String msg = String.format("user %s does not have permissions to download %s", getSessionData().getPerson(), irFileVersion);
            getLogger().warn(msg);
            return FORBIDDEN;
        }
        setInformationResourceId(irFileVersion.getInformationResourceId());
        logger.info("user {} downloaded {}",getSessionData().getPerson(), irFileVersion);
        getDownloadService().handleDownload(getAuthenticatedUser(), this, getInformationResourceId(), irFileVersion);
        return SUCCESS;
    }

    @Action(value = THUMBNAIL, interceptorRefs = { @InterceptorRef("unauthenticatedStack") })
    public String thumbnail() throws TdarActionException {
        InformationResourceFileVersion irFileVersion = null;
        if (informationResourceFileId == null)
            return ERROR;
        irFileVersion = getInformationResourceFileVersionService().find(informationResourceFileId);
        if (irFileVersion == null) {
            getLogger().warn("thumbnail request: no informationResourceFiles associated with this id [" + informationResourceFileId + "]");
            return ERROR;
        }

        // image must be thumbnail
        if (irFileVersion.getFileVersionType() != VersionType.WEB_SMALL) {
            getLogger().warn("thumbail request: requested informationResourceFileVersion exists but is not a thumbnail:" + informationResourceFileId);
            return ERROR;
        }

        if (!getAuthenticationAndAuthorizationService().canDownload(irFileVersion, getSessionData().getPerson())) {
            getLogger().warn("thumbail request: resource is confidential/embargoed:" + informationResourceFileId);
            return FORBIDDEN;
        }

        getDownloadService().handleDownload(getAuthenticatedUser(), this, getInformationResourceId(), irFileVersion);
        return SUCCESS;
    }

    @Action(value = DOWNLOAD_ALL)
    public String downloadZipArchive() throws TdarActionException {
        if (getInformationResourceId() == null)
            return ERROR;

        InformationResource ir = (InformationResource) getResourceService().find(getInformationResourceId());
        List<InformationResourceFileVersion> versions = new ArrayList<>();
        for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
            if (!getAuthenticationAndAuthorizationService().canDownload(irf, getSessionData().getPerson())) {
                getLogger().warn("thumbail request: resource is confidential/embargoed:" + informationResourceFileId);
                return FORBIDDEN;
            }
            logger.trace("adding: {}", irf.getLatestUploadedVersion());
            versions.add(irf.getLatestUploadedOrArchivalVersion());
        }
        getDownloadService().handleDownload(getAuthenticatedUser(), this, getInformationResourceId(), versions.toArray(new InformationResourceFileVersion[0]));
        return SUCCESS;
    }

    @Action(value = DOWNLOAD_ALL_LANDING, results = {
            @Result(name = SUCCESS, type = "freemarker", location = "/WEB-INF/content/download-all.ftl") })
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

    public void setInformationResourceFileId(Long informationResourceFileId) {
        this.informationResourceFileId = informationResourceFileId;
    }

    public Long getInformationResourceFileId() {
        return informationResourceFileId;
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

}

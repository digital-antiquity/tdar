package org.tdar.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.service.PdfService;

@ParentPackage("secured")
@Namespace("/filestore/{informationResourceFileId}")
@Results({
        @Result(name = "success", type = "stream",
                params = {
                        "contentType", "${contentType}",
                        "inputName", "inputStream",
                        "contentDisposition", "filename=\"${fileName}\"",
                        "contentLength", "${contentLength}"
                }
        ),
        @Result(name = "error", type = "httpheader", params = { "error", "404" }),
        @Result(name = "forbidden", type = "httpheader", params = { "error", "403" })

})
@Component
@Scope("prototype")
public class DownloadController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 7548544212676661097L;
    private transient InputStream inputStream;
    private String contentType;
    private String fileName;
    private Long contentLength;
    private Long informationResourceFileId;
    private Integer version;
    private VersionType type;
    private Long informationResourceId;

    public static final String FORBIDDEN = "forbidden";

    @Autowired
    private PdfService pdfService;

    @Action(value = "confirm", results = { @Result(name = "confirm", location = "/WEB-INF/content/confirm-download.ftl") })
    public String confirm() {
        String status = execute();
        if (status != SUCCESS) {
            return status;
        }
        ;
        return "confirm";
    }

    @Action(value = "get")
    public String execute() {
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
        informationResourceId = irFileVersion.getInformationResourceId();
        return handleDownload(irFileVersion);
    }

    @Action(value = "thumbnail", interceptorRefs = { @InterceptorRef("unauthenticatedStack") })
    public String thumbnail() {
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

        // (irFileVersion.getInformationResourceFile().isConfidential() || !irFileVersion.getInformationResourceFile().getInformationResource()
        // .isAvailableToPublic()) && !getEntityService().canViewConfidentialInformation(getSessionData().getPerson(),
        // irFileVersion.getInformationResourceFile().getInformationResource())
        // must not be confidential/embargoed
        if (!getAuthenticationAndAuthorizationService().canDownload(irFileVersion, getSessionData().getPerson())) {
            getLogger().warn("thumbail request: resource is confidential/embargoed:" + informationResourceFileId);
            return FORBIDDEN;
        }

        return handleDownload(irFileVersion);
    }

    private String handleDownload(InformationResourceFileVersion irFileVersion) {
        try {

            File resourceFile = irFileVersion.getFile();
            fileName = irFileVersion.getFilename();
            if (resourceFile == null || !resourceFile.exists()) {
                addActionError("File not found");
                return ERROR;
            }

            // If it's a PDF, add the cover page if we can, if we fail, just send the original file
            if (irFileVersion.getExtension().equalsIgnoreCase("PDF")) {
                try {
                    resourceFile = pdfService.mergeCoverPage(getAuthenticatedUser(), irFileVersion);
                    inputStream = new DeleteOnCloseFileInputStream(resourceFile);
                } catch (Exception e) {
                    getLogger().error("Error occured while merging cover page onto " + irFileVersion, e);
                }
            }
            try {
                getLogger().debug("downloading file:" + resourceFile.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            contentLength = resourceFile.length();
            contentType = irFileVersion.getMimeType();
            if (inputStream == null) {
                inputStream = new FileInputStream(resourceFile);
            }
            if (!irFileVersion.isDerivative()) {
                InformationResourceFile irFile = irFileVersion.getInformationResourceFile();
                FileDownloadStatistic stat = new FileDownloadStatistic(new Date(), irFile);
                getInformationResourceService().save(stat);
            }
        } catch (FileNotFoundException e) {
            addActionErrorWithException("File not found", e);
            return ERROR;
        }

        return SUCCESS;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getContentType() {
        return contentType;
    }

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

}

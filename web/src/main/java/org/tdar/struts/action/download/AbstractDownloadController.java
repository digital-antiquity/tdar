package org.tdar.struts.action.download;

import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.download.DownloadTransferObject;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Namespace("/filestore")
@Component
@Scope("prototype")
@Results({
        @Result(name = TdarActionSupport.SUCCESS, type = "stream",
                params = {
                        "contentType", "${downloadTransferObject.mimeType}",
                        "inputName", "downloadTransferObject.inputStream",
                        "contentDisposition", "${downloadTransferObject.dispositionPrefix}filename=\"${downloadTransferObject.fileName}\"",
                        "contentLength", "${downloadTransferObject.contentLength}"
                }
        ),
        @Result(name = TdarActionSupport.ERROR, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.FORBIDDEN, type = TdarActionSupport.HTTPHEADER, params = { "error", "403" })
})
public class AbstractDownloadController extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -1831798412944149018L;
    @Autowired
    private transient AuthorizationService authorizationService;

    private DownloadTransferObject downloadTransferObject;
    private List<UserAffiliation> affiliations = UserAffiliation.getUserSubmittableAffiliations();

    public static final String SUCCESS_DOWNLOAD_ALL = "success-download-all";
    public static final String GET = "get";
    public static final String LOGIN_REGISTER_PROMPT = "../filestore/download-unauthenticated.ftl";
    public static final String DOWNLOAD_SUFFIX = "/${informationResourceId}/${informationResourceFileVersionId}";
    public static final String SUCCESS_REDIRECT_DOWNLOAD = "/filestore/confirm" + DOWNLOAD_SUFFIX;
    public static final String DOWNLOAD_SINGLE_LANDING = "/filestore/get" + DOWNLOAD_SUFFIX;
    public static final String FORBIDDEN = "forbidden";
    public static final String DOWNLOAD_ALL_LANDING = "/filestore/confirm/${informationResourceId}";
    public static final String DOWNLOAD_ALL = "downloadAllAsZip";
    private Long informationResourceFileVersionId;
    private Long informationResourceId;
    private boolean coverPageIncluded = true;

    // the resource being downloaded (or the resource that the file is being downloade from)
    private InformationResource informationResource;
    // the specific version to be downloaded, if just one
    private InformationResourceFileVersion informationResourceFileVersion;

    @Autowired
    private transient RecaptchaService recaptchaService;
    private AntiSpamHelper h = new AntiSpamHelper();

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

    @Override
    public void prepare() {
        Long irId = getInformationResourceId();
        Long irfvId = getInformationResourceFileVersionId();

        getLogger().trace("IRID: {}, IRFVID: {}", irId, irfvId);
        if (PersistableUtils.isNotNullOrTransient(irId)) {
            setInformationResource(getGenericService().find(InformationResource.class, irId));
            // bad, but force onto session until better way found
            authorizationService.applyTransientViewableFlag(informationResource, getAuthenticatedUser());
        }
        if (PersistableUtils.isNotNullOrTransient(irfvId)) {
            setInformationResourceFileVersion(getGenericService().find(InformationResourceFileVersion.class, irfvId));
            // bad, but force onto session until better way found
            if (PersistableUtils.isNotNullOrTransient(getInformationResourceFileVersion())) {
                setInformationResource(informationResourceFileVersion.getInformationResourceFile().getInformationResource());
                informationResourceId = informationResource.getId();
                authorizationService.applyTransientViewableFlag(informationResourceFileVersion, getAuthenticatedUser());
            }
        }
    }

    @Override
    public void validate() {
        Long irId = getInformationResourceId();
        Long irfvId = getInformationResourceFileVersionId();
        if (PersistableUtils.isNullOrTransient(irfvId) &&
                PersistableUtils.isNullOrTransient(irId)) {
            addActionError(getText("downloadController.specify_what_to_download"));
        }
    }

    public boolean isCoverPageIncluded() {
        return coverPageIncluded;
    }

    public void setCoverPageIncluded(boolean coverPageIncluded) {
        this.coverPageIncluded = coverPageIncluded;
    }

    public DownloadTransferObject getDownloadTransferObject() {
        return downloadTransferObject;
    }

    public void setDownloadTransferObject(DownloadTransferObject downloadTransferObject) {
        getLogger().trace("setting download object: {}", downloadTransferObject);
        this.downloadTransferObject = downloadTransferObject;
    }

    public RecaptchaService getRecaptchaService() {
        return recaptchaService;
    }

    public void setRecaptchaService(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

	public List<UserAffiliation> getAffiliations() {
		return affiliations;
	}

	public void setAffiliations(List<UserAffiliation> affiliations) {
		this.affiliations = affiliations;
	}
}

package org.tdar.struts.action.download;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.service.download.DownloadResult;
import org.tdar.core.service.download.DownloadService;
import org.tdar.core.service.download.DownloadTransferObject;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.TdarActionException;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespaces(value={
        @Namespace("/filestore"),
        @Namespace("/files")
})
@Component
@Scope("prototype")
public class DownloadController extends AbstractDownloadController implements Preparable {

    private static final String CONFIRM_DOWNLOAD_FTL = "confirm-download.ftl";
    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private static final long serialVersionUID = 7548544212676661097L;

    private boolean forceAttachment = false;

    @Action(value = CONFIRM, results = { @Result(name = CONFIRM, location = CONFIRM_DOWNLOAD_FTL) })
    public String confirm() throws TdarActionException {
        getSessionData().clearPassthroughParameters();

        DownloadTransferObject dto = downloadService.validateFilterAndSetupDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null,
                isCoverPageIncluded(), this, null);
        setInformationResource(dto.getInformationResource());
        if (dto.getResult() != DownloadResult.SUCCESS) {
            return ERROR;
        }
        return CONFIRM;
    }

    @Action(value = SHOW_DOWNLOAD_LANDING, results = {
            @Result(name = SUCCESS, type = FREEMARKER, location = CONFIRM_DOWNLOAD_FTL) })
    public String showDownloadAllLandingPage() {
        return SUCCESS;
    }

    @Override
    @Actions(value= {
            @Action(value = GET),
            @Action(value = "get/{informationResourceFileVersionId}"),
            @Action(value = "img/md/{informationResourceFileVersionId}"),
            @Action(value = "img/lg/{informationResourceFileVersionId}"),
            @Action(value = "{informationResourceFileVersionId}/get"),
    })
    public String execute() {
        getSessionData().clearPassthroughParameters();
        if (Persistable.Base.isNullOrTransient(getInformationResourceFileVersion())) {
            getLogger().debug("no informationResourceFiles associated with this id [{}]", getInformationResourceFileVersionId());
            return ERROR;
        }
        if (Persistable.Base.isNotNullOrTransient(getInformationResourceId())) {
            setInformationResourceId(getInformationResourceFileVersion().getInformationResourceId());
        }
        setDownloadTransferObject(downloadService.validateFilterAndSetupDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null,
                isCoverPageIncluded(), this, null));
        getDownloadTransferObject().setAttachment(forceAttachment);
        if (getDownloadTransferObject().getResult() != DownloadResult.SUCCESS) {
            return getDownloadTransferObject().getResult().name().toLowerCase();
        }

        return getDownloadTransferObject().getResult().name().toLowerCase();
    }

    @Action(value = DOWNLOAD_ALL)
    public String downloadZipArchive() {
        getSessionData().clearPassthroughParameters();
        if (Persistable.Base.isNullOrTransient(getInformationResource())) {
            return ERROR;
        }
        setDownloadTransferObject(downloadService.validateFilterAndSetupDownload(getAuthenticatedUser(), null, getInformationResource(), isCoverPageIncluded(),
                this, null));
        if (getDownloadTransferObject().getResult() != DownloadResult.SUCCESS) {
            return getDownloadTransferObject().getResult().name().toLowerCase();
        }
        return getDownloadTransferObject().getResult().name().toLowerCase();

    }

    public boolean isForceAttachment() {
        return forceAttachment;
    }

    public void setForceAttachment(boolean forceAttachment) {
        this.forceAttachment = forceAttachment;
    }
}
package org.tdar.struts.action.download;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
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
@Namespace("/filestore")
@Component
@Scope("prototype")
public class DownloadController extends AbstractDownloadController implements Preparable {

    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private static final long serialVersionUID = 7548544212676661097L;

    @Action(value = CONFIRM, results = { @Result(name = CONFIRM, location = "confirm-download.ftl") })
    public String confirm() throws TdarActionException {
        getSessionData().clearPassthroughParameters();

        DownloadTransferObject dto = downloadService.validateFilterAndSetupDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null, isCoverPageIncluded(), this);
        if (dto.getResult() != DownloadResult.SUCCESS) {
            return SUCCESS;
        }
        return CONFIRM;
    }

    @Action(value = SHOW_DOWNLOAD_LANDING, results = {
            @Result(name = SUCCESS, type = FREEMARKER, location = "download-all.ftl") })
    public String showDownloadAllLandingPage() {
        return SUCCESS;
    }

    @Override
    @Action(value = GET)
    public String execute() {
        getSessionData().clearPassthroughParameters();
        if (Persistable.Base.isNullOrTransient(getInformationResourceFileVersion())) {
            getLogger().debug("no informationResourceFiles associated with this id [{}]", getInformationResourceFileVersionId());
            return ERROR;
        }
        if (Persistable.Base.isNotNullOrTransient(getInformationResourceId())) {
            setInformationResourceId(getInformationResourceFileVersion().getInformationResourceId());
        }
        setDownloadTransferObject(downloadService.validateFilterAndSetupDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null, isCoverPageIncluded(), this));
        return getDownloadTransferObject().getResult().name().toLowerCase();
    }

    @Action(value = DOWNLOAD_ALL)
    public String downloadZipArchive() {
        getSessionData().clearPassthroughParameters();
        if (Persistable.Base.isNullOrTransient(getInformationResource())) {
            return ERROR;
        }
        setDownloadTransferObject(downloadService.validateFilterAndSetupDownload(getAuthenticatedUser(), null, getInformationResource(), isCoverPageIncluded(), this));
        return getDownloadTransferObject().getResult().name().toLowerCase();

    }
}
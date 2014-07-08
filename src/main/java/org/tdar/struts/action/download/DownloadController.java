package org.tdar.struts.action.download;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.DownloadService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.DownloadHandler;

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
public class DownloadController extends AbstractDownloadController implements DownloadHandler, Preparable {

    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private static final long serialVersionUID = 7548544212676661097L;

    @Action(value = CONFIRM, results = { @Result(name = CONFIRM, location = "confirm-download.ftl") })
    public String confirm() throws TdarActionException {
        getSessionData().clearPassthroughParameters();

        String status = execute();
        if (status != SUCCESS) {
            return status;
        }
        return CONFIRM;
    }

    @Action(value = SHOW_DOWNLOAD_LANDING, results = {
            @Result(name = SUCCESS, type = "freemarker", location = "download-all.ftl") })
    public String showDownloadAllLandingPage() {
        return SUCCESS;
    }

    @Override
    @Action(value = GET)
    public String execute() throws TdarActionException {
        getSessionData().clearPassthroughParameters();
        if (Persistable.Base.isNullOrTransient(getInformationResourceFileVersion())) {
            getLogger().debug("no informationResourceFiles associated with this id [{}]", getInformationResourceFileVersionId());
            return ERROR;
        }
        if (Persistable.Base.isNotNullOrTransient(getInformationResourceId())) {
            setInformationResourceId(getInformationResourceFileVersion().getInformationResourceId());
        }
        return validateFilterAndSetupDownload();
    }

    @Action(value = DOWNLOAD_ALL)
    public String downloadZipArchive() throws TdarActionException {
        getSessionData().clearPassthroughParameters();
        if (Persistable.Base.isNullOrTransient(getInformationResource())) {
            return ERROR;
        }
        return validateFilterAndSetupDownload();
    }


    public String validateFilterAndSetupDownload() throws TdarActionException {
        List<InformationResourceFileVersion> versionsToDownload = new ArrayList<>();
        if (Persistable.Base.isNotNullOrTransient(getInformationResourceFileVersion())) {
            versionsToDownload.add(getInformationResourceFileVersion());
        }

        if (Persistable.Base.isNotNullOrTransient(getInformationResource())) {
            for (InformationResourceFile irf : getInformationResource().getInformationResourceFiles()) {
                if (irf.isDeleted()) {
                    continue;
                }
                if (!authorizationService.canDownload(irf, getAuthenticatedUser())) {
                    getLogger().warn("thumbail request: resource is confidential/embargoed: {}", getInformationResourceFileVersionId());
                    return FORBIDDEN;
                }
                getLogger().trace("adding: {}", irf.getLatestUploadedVersion());
                versionsToDownload.add(irf.getLatestUploadedOrArchivalVersion());
            }
        }
        if (CollectionUtils.isEmpty(versionsToDownload)) {
            return ERROR;
        }
        getLogger().info("user {} downloaded {} ({})", getAuthenticatedUser(), getInformationResourceFileVersion(), getInformationResource());
        downloadService.handleDownload(getAuthenticatedUser(), this, getInformationResourceId(),
                versionsToDownload.toArray(new InformationResourceFileVersion[0]));
        return SUCCESS;
    }

}
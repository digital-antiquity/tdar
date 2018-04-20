package org.tdar.struts.action.download;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.download.DownloadResult;
import org.tdar.core.service.download.DownloadService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.ResourceCitationFormatter;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/filestore")
@Component
@Scope("prototype")
public class DownloadController extends AbstractDownloadController implements Preparable {

    private static final String CONFIRM_DOWNLOAD_FTL = "confirm-download.ftl";
    @Autowired
    private transient DownloadService downloadService;

    private static final long serialVersionUID = 7548544212676661097L;

    private boolean forceAttachment = false;

    private ResourceCitationFormatter resourceCitation;

    @Actions(value = {
            @Action(value = "confirm/{informationResourceId}/{informationResourceFileVersionId}",
                    results = { @Result(name = CONFIRM, location = CONFIRM_DOWNLOAD_FTL) }),
            @Action(value = "confirm/{informationResourceId}",
                    results = { @Result(name = CONFIRM, location = CONFIRM_DOWNLOAD_FTL) })
    })
    public String confirm() throws TdarActionException {

        if (PersistableUtils.isNotNullOrTransient(getInformationResourceFileVersionId())) {
            setDownloadTransferObject(downloadService.validateDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null,
                    isCoverPageIncluded(), this, null));
        } else {
            setDownloadTransferObject(downloadService.validateDownload(getAuthenticatedUser(), null, getInformationResource(),
                    isCoverPageIncluded(), this, null));
        }
        setInformationResource(getDownloadTransferObject().getInformationResource());
        setResourceCitation(new ResourceCitationFormatter(getInformationResource()));
        if (getDownloadTransferObject().getResult() != DownloadResult.SUCCESS) {
            return ERROR;
        }
        return CONFIRM;
    }

    @Override
    @Actions(value = {
            // @Action(value = "get/{informationResourceFileVersionId}"),
            @Action(value = "img/md/{informationResourceFileVersionId}"),
            @Action(value = "img/lg/{informationResourceFileVersionId}"),
            @Action(value = "get/{informationResourceId}/{informationResourceFileVersionId}"),
    })
    public String execute() {
        if (PersistableUtils.isNullOrTransient(getInformationResourceFileVersion())) {
            getLogger().debug("no informationResourceFiles associated with this id [{}]", getInformationResourceFileVersionId());
            return ERROR;
        }
        if (PersistableUtils.isNotNullOrTransient(getInformationResourceId())) {
            setInformationResourceId(getInformationResourceFileVersion().getInformationResourceId());
        }
        setDownloadTransferObject(downloadService.handleDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null,
                isCoverPageIncluded(), this, null));
        getDownloadTransferObject().setAttachment(forceAttachment);
        if (getDownloadTransferObject().getResult() != DownloadResult.SUCCESS) {
            return getDownloadTransferObject().getResult().name().toLowerCase();
        }

        return getDownloadTransferObject().getResult().name().toLowerCase();
    }

    @Actions(value = {
            @Action(value = "zip/{informationResourceId}"),
            @Action(value = "get/{informationResourceId}")
    })
    public String downloadZipArchive() {
        if (PersistableUtils.isNullOrTransient(getInformationResource())) {
            return ERROR;
        }
        setDownloadTransferObject(downloadService.handleDownload(getAuthenticatedUser(), null, getInformationResource(), isCoverPageIncluded(),
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

    public ResourceCitationFormatter getResourceCitation() {
        return resourceCitation;
    }

    public void setResourceCitation(ResourceCitationFormatter resourceCitation) {
        this.resourceCitation = resourceCitation;
    }
}
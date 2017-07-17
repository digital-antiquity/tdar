package org.tdar.struts.action.download;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.service.download.DownloadResult;
import org.tdar.core.service.download.DownloadService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("default")
@Namespaces(value = {
        @Namespace("/files"), @Namespace("/filestore") })
@Component
@Scope("prototype")
public class ThumbnailDownloadAction extends AbstractDownloadController implements Preparable {

    private static final long serialVersionUID = -1238239968992138611L;

    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Actions(value = {
            @Action(value = "img/thumbnail/{informationResourceFileVersionId}"),
            @Action(value = "img/sm/{informationResourceFileVersionId}"),
            @Action(value = "thumbnail/{informationResourceFileVersionId}"),
            @Action(value = "sm/{informationResourceFileVersionId}")
    })
    public String thumbnail() {
        if (PersistableUtils.isNullOrTransient(getInformationResourceFileVersion())) {
            getLogger().warn("thumbnail request: no informationResourceFiles associated with this id [{}]", getInformationResourceFileVersionId());
            return ERROR;
        }

        // image must be thumbnail
        if (getInformationResourceFileVersion().getFileVersionType() != VersionType.WEB_SMALL) {
            getLogger().warn("thumbail request: requested version exists but is not a thumbnail: {}", getInformationResourceFileVersionId());
            return ERROR;
        }

        if (!authorizationService.canDownload(getAuthenticatedUser(), getInformationResourceFileVersion())) {
            getLogger().warn("thumbail request: resource is confidential/embargoed: {}", getInformationResourceFileVersionId());
            return FORBIDDEN;
        }

        setDownloadTransferObject(downloadService.validateDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null,
                isCoverPageIncluded(), this, null));
        if (getDownloadTransferObject().getResult() != DownloadResult.SUCCESS) {
            return getDownloadTransferObject().getResult().name().toLowerCase();
        }
        return getDownloadTransferObject().getResult().name().toLowerCase();
    }

    @Override
    public void prepare() {
        super.prepare();
    }
}
package org.tdar.struts.action.download;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.service.DownloadResult;
import org.tdar.core.service.DownloadService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("default")
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
        @Result(name = TdarActionSupport.ERROR, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.FORBIDDEN, type = TdarActionSupport.HTTPHEADER, params = { "error", "403" })

})
@Component
@Scope("prototype")
public class UnauthenticatedDownloadController extends AbstractDownloadController implements DownloadHandler, Preparable {

    private static final long serialVersionUID = 3682702108165100228L;
    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Action(value = "download",
            results = {
                    @Result(name = SUCCESS, type = "redirect", location = DOWNLOAD_SINGLE_LANDING),
                    @Result(name = DOWNLOAD_ALL, type = "redirect", location = DOWNLOAD_ALL_LANDING),
                    @Result(name = LOGIN, type = FREEMARKER, location = "download-unauthenticated.ftl") })
    @HttpsOnly
    public String download() {
        // not sure this is really needed, but...
        if (Persistable.Base.isNullOrTransient(getInformationResourceFileVersion()) && Persistable.Base.isNullOrTransient(getInformationResource())) {
            return ERROR;
        }
        if (isAuthenticated()) {
            if (Persistable.Base.isNotNullOrTransient(getInformationResourceFileVersion())) {
                return SUCCESS;
            }
            return DOWNLOAD_ALL;
        }
        return LOGIN;
    }

    /*
     * I believe we'll need something like this for our contract with SRI
     * 
     * public String downloadCustom() {
     * HttpServletRequest request = ServletActionContext.getRequest();
     * String referrer = request.getHeader("referer");
     * if (downloadService.canDownloadUnauthenticated(referrer, getInformationResourceFileVersion())) {
     * downloadService.handleActualDownload(null, this, null, getInformationResourceFileVersion());
     * }
     * return INPUT;
     * }
     */

    @Actions({
            @Action(value = THUMBNAIL),
            @Action(value = SM)
    })
    public String thumbnail() {
        getSessionData().clearPassthroughParameters();
        if (Persistable.Base.isNullOrTransient(getInformationResourceFileVersion())) {
            getLogger().warn("thumbnail request: no informationResourceFiles associated with this id [{}]", getInformationResourceFileVersionId());
            return ERROR;
        }

        // image must be thumbnail
        if (getInformationResourceFileVersion().getFileVersionType() != VersionType.WEB_SMALL) {
            getLogger().warn("thumbail request: requested version exists but is not a thumbnail: {}", getInformationResourceFileVersionId());
            return ERROR;
        }

        if (!authorizationService.canDownload(getInformationResourceFileVersion(), getAuthenticatedUser())) {
            getLogger().warn("thumbail request: resource is confidential/embargoed: {}", getInformationResourceFileVersionId());
            return FORBIDDEN;
        }

        DownloadResult result = downloadService.validateFilterAndSetupDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null, this);
        return result.name().toLowerCase();
    }

    @Override
    public void prepare() {
        super.prepare();
    }
}
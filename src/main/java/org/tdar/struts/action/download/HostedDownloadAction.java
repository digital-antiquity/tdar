package org.tdar.struts.action.download;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.download.DownloadResult;
import org.tdar.core.service.download.DownloadService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

import com.opensymphony.xwork2.Preparable;

/**
 * Created by jimdevos on 9/23/14.
 */
@Namespace("/download/hosted")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpOnlyIfUnauthenticated
public class HostedDownloadAction extends AbstractDownloadController implements Preparable {

    private static final long serialVersionUID = -7618237747050278959L;
    private String apiKey = "";
    @Autowired
    private transient AuthorizationService authorzationService;
    @Autowired
    private transient DownloadService downloadService;

    @Action(
            value = "{informationResourceFileVersionId}/{apiKey}"
            )
            public String execute() {
        setDownloadTransferObject(downloadService.validateFilterAndSetupDownload(getAuthenticatedUser(), getInformationResourceFileVersion(), null,
                isCoverPageIncluded(), this, null, true));
        if (getDownloadTransferObject().getResult() != DownloadResult.SUCCESS) {
            return getDownloadTransferObject().getResult().name().toLowerCase();
        }
        return getDownloadTransferObject().getResult().name().toLowerCase();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void prepare() {
        super.prepare();
        if (StringUtils.isBlank(apiKey)) {
            addActionError("hostedDownloadController.api_key_required");
        }
        try {
            if (!authorzationService.checkValidUnauthenticatedDownload(getInformationResourceFileVersion(), getApiKey(), getServletRequest())) {
                addActionError("hostedDownloadController.invalid_request");
            }
        } catch (Exception e) {
            addActionErrorWithException("hostedDownloadController.error", e);
        }
    }

}

package org.tdar.struts.action.download;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.download.DownloadService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("default")
@Namespace("/filestore")
@Component
@Scope("prototype")
public class UnauthenticatedDownloadAction extends AbstractDownloadController implements Preparable {

    private static final long serialVersionUID = 3682702108165100228L;
    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Actions(value = {
            @Action(value = "download/{informationResourceId}/{informationResourceFileVersionId}",
                    results = {
                            @Result(name = SUCCESS, type = "redirect", location = DOWNLOAD_SINGLE_LANDING),
                            @Result(name = DOWNLOAD_ALL, type = "redirect",
                                    location = "/filestore/zip/${informationResourceId}"),
                            @Result(name = INPUT, type = "httpheader", params = { "error", "400", "errrorMessage", "no file specified" }),
                            @Result(name = LOGIN, type = FREEMARKER, location = "download-unauthenticated.ftl") }),
            @Action(value = "download/{informationResourceId}",
                    results = {
                            @Result(name = SUCCESS, type = "redirect", location = DOWNLOAD_SINGLE_LANDING),
                            @Result(name = DOWNLOAD_ALL, type = "redirect",
                                    location = "/filestore/zip/${informationResourceId}"),
                            @Result(name = INPUT, type = "httpheader", params = { "error", "400", "errrorMessage", "no file specified" }),
                            @Result(name = LOGIN, type = FREEMARKER, location = "download-unauthenticated.ftl") })
    })
    @HttpsOnly
    /**
     * if authenticated and valid, pass through to download, otherwise pass through to login
     * @return
     */
    public String download() {
        if (!isAuthenticated()) {
            if (!isAuthenticated() && StringUtils.isNotBlank(TdarConfiguration.getInstance().getRecaptchaPrivateKey())) {
                getH().generateRecapcha(getRecaptchaService());
            }
            return LOGIN;
        }

        if (PersistableUtils.isNotNullOrTransient(getInformationResourceFileVersion())) {
            return SUCCESS;
        } else {
            return DOWNLOAD_ALL;
        }
    }

    @Override
    public void prepare() {
        super.prepare();
    }
}
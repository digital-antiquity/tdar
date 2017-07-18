package org.tdar.struts.action.download;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.auth.DownloadUserLogin;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@ParentPackage("default")
@Namespace("/filestore")
@Component
@Scope("prototype")
public class DownloadLoginController extends AbstractDownloadController implements Validateable, Preparable {

    private static final long serialVersionUID = 1525006233392261028L;

    private DownloadUserLogin downloadUserLogin = new DownloadUserLogin(getH());

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Action(value = "process-download-login",
            interceptorRefs = { @InterceptorRef("registrationStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = SUCCESS_REDIRECT_DOWNLOAD),
                    @Result(name = SUCCESS_DOWNLOAD_ALL, type = TdarActionSupport.TDAR_REDIRECT, location = DOWNLOAD_ALL_LANDING),
                    @Result(name = INPUT, type = FREEMARKER, location = LOGIN_REGISTER_PROMPT)
            })
    @HttpsOnly
    @WriteableSession
    @PostOnly
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", getDownloadUserLogin().getLoginUsername());

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            AuthenticationResult result = authenticationService.authenticatePerson(getDownloadUserLogin(), getServletRequest(), getServletResponse(),
                    getSessionData());
            status = result.getStatus();
        } catch (Exception e) {
            addActionError(e.getMessage());
            status = AuthenticationStatus.ERROR;
        }

        switch (status) {
            case ERROR:
            case NEW:
                addActionMessage(getText("loginController.user_not_in_local_db"));
                return INPUT;
            default:
                break;
        }
        if (PersistableUtils.isNullOrTransient(getInformationResourceFileVersionId())) {
            return SUCCESS_DOWNLOAD_ALL;
        }
        return SUCCESS;
    }

    @Override
    public void prepare() {
        super.prepare();
    };

    @Override
    public void validate() {
        ErrorTransferObject errors = getDownloadUserLogin().validate(authorizationService, getRecaptchaService(), getServletRequest().getRemoteHost());
        processErrorObject(errors);

        if (errors.isNotEmpty()) {
            getLogger().warn("Returning INPUT because login requested via GET request for user:{}", getDownloadUserLogin().getLoginUsername());
        }
    }

    public DownloadUserLogin getDownloadUserLogin() {
        return downloadUserLogin;
    }

    public void setDownloadUserLogin(DownloadUserLogin downloadUserLogin) {
        this.downloadUserLogin = downloadUserLogin;
    }

}

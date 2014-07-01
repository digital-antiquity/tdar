package org.tdar.struts.action.login;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.data.DownloadUserLogin;
import org.tdar.struts.interceptor.annotation.CacheControl;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Validateable;

/**
 * $Id$
 * 
 * Handles displaying of the login page and logging in.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("default")
@Namespace("/login")
@Component
@Scope("prototype")
@Results({
        @Result(name = TdarActionSupport.AUTHENTICATED, type = TdarActionSupport.REDIRECT, location = URLConstants.DASHBOARD) })
@CacheControl
public class DownloadLoginController extends AuthenticationAware.Base implements Validateable {

    private static final long serialVersionUID = -1219398494032484272L;

    @Autowired
    private RecaptchaService recaptchaService;

    private DownloadUserLogin downloadUserLogin;

    private AntiSpamHelper h = downloadUserLogin.getH();

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Action(value = "process-download-login",
            interceptorRefs = { @InterceptorRef("csrfDefaultStack") },
            results = {
                    @Result(name = SUCCESS, type = REDIRECT, location = "${downloadUserLogin.returnUrl}"),
                    @Result(name = INPUT, type = REDIRECT, location = "${downloadUserLogin.failureUrl}"),
            })
    @HttpsOnly
    @WriteableSession
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", downloadUserLogin.getLoginUsername());

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            status = authenticationService.authenticatePerson(downloadUserLogin, getServletRequest(), getServletResponse(),
                    getSessionData());
        } catch (Exception e) {
            addActionError(e.getMessage());
            return INPUT;
        }

        switch (status) {
            case ERROR:
            case NEW:
                addActionMessage("User is in crowd, but not in local db");
                return INPUT;
            default:
                break;
        }

        return SUCCESS;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public DownloadUserLogin getDownloadUserLogin() {
        return downloadUserLogin;
    }

    public void setDownloadUserLogin(DownloadUserLogin downloadUserLogin) {
        this.downloadUserLogin = downloadUserLogin;
    }

    @Override
    public void validate() {
        List<String> validate = downloadUserLogin.validate(this, authorizationService);
        addActionErrors(validate);

        if (!isPostRequest() || CollectionUtils.isNotEmpty(validate)) {
            getLogger().warn("Returning INPUT because login requested via GET request for user:{}", downloadUserLogin.getLoginUsername());
        }
    }

}

package org.tdar.balk.struts.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.util.UrlUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Validateable;

import main.java.org.tdar.balk.service.SimpleAuthenticationService;

/**
 * $Id$
 * 
 * Handles displaying of the login page and logging in.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("default")
@Namespace("/")
@Component
@Scope("prototype")
@Results({
        @Result(name = TdarActionSupport.AUTHENTICATED, type = TdarActionSupport.TDAR_REDIRECT, location = "/") })
public class LoginAction extends AbstractAuthenticatedAction implements Validateable {

    private static final long serialVersionUID = -1219398494032484272L;

    private String url;
    private String internalReturnUrl;

    @Autowired
    private RecaptchaService recaptchaService;
    private AntiSpamHelper h = new AntiSpamHelper();
    private UserLogin userLogin = new UserLogin(h);

    @Autowired
    private SimpleAuthenticationService authenticationService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    @Action(value = "login", results = {
            @Result(name = TdarActionSupport.SUCCESS, location = "/WEB-INF/content/login.ftl")
    })
    @SkipValidation
    public String execute() {
        if (isAuthenticated()) {
            return TdarActionSupport.AUTHENTICATED;
        }
        return SUCCESS;

    }

    @Action(value = "logout",
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/")
            })
    @PostOnly
    @SkipValidation
    public String logout() {
        // manually handle SSO TOken
        String token = authenticationService.getSsoTokenFromRequest(ServletActionContext.getRequest());
        if (StringUtils.isNotBlank(token) && getTdarConfiguration().ssoEnabled()) {
            getLogger().debug("token:{}", token);
            @SuppressWarnings("unused")
            AuthenticationResult result = authenticationService.checkToken((String) token, getSessionData(), ServletActionContext.getRequest());
        }
        getLogger().debug("is authenticated? {}", getSessionData().isAuthenticated());
        if (getSessionData().isAuthenticated()) {
            authenticationService.logout(getSessionData(), getServletRequest(), getServletResponse());
        }
        return SUCCESS;
    }

    @Actions(
    {
            @Action(value = "login/process",
                    interceptorRefs = { @InterceptorRef("registrationStack") },
                    results = {
                            @Result(name = TDAR_REDIRECT, type = TDAR_REDIRECT, location = "${internalReturnUrl}"),
                            @Result(name = TdarActionSupport.INPUT, location = "/WEB-INF/content/login.ftl"),
                            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/"),
                    })
    })
    @PostOnly
    @WriteableSession
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", userLogin.getLoginUsername());

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            AuthenticationResult result = authenticationService.authenticatePerson(getUserLogin(), getServletRequest(), getServletResponse(), getSessionData());
            status = result.getStatus();
        } catch (Exception e) {
            getLogger().error("{}",e,e);
            addActionError(e.getMessage());
            return INPUT;
        }
        getLogger().debug("{}",status);
        switch (status) {
            case ERROR:
            case NEW:
                addActionMessage(getText("loginController.user_not_in_local_db"));
                return INPUT;
            default:
                break;
        }

        setInternalReturnUrl(parseReturnUrl());
        if (StringUtils.isNotBlank(getInternalReturnUrl())) {
            getSessionData().clearPassthroughParameters();
            return TDAR_REDIRECT;
        }
        return SUCCESS;
    }

    private String parseReturnUrl() {
        String parsedUrl = null;
        getLogger().debug("url: {}, sessionUrl: {}", url, getSessionData().getReturnUrl());
        if ((getSessionData().getReturnUrl() == null) && StringUtils.isEmpty(url)) {
            return null;
        }

        // Favor the session's 'returnUrl' over the querystring 'url'.
        if (StringUtils.isNotBlank(getSessionData().getReturnUrl())) {
            parsedUrl = getSessionData().getReturnUrl();
        } else {
            parsedUrl = UrlUtils.urlDecode(url);
        }

        // enforce valid + relative url
        String normalizedUrl = org.tdar.core.bean.util.UrlUtils.sanitizeRelativeUrl(parsedUrl);
        if (!normalizedUrl.equals(parsedUrl)) {
            getLogger().warn("Return url does not relative. Replacing {} with {}", parsedUrl, normalizedUrl);
        }
        parsedUrl = normalizedUrl;

        getLogger().info("url {} ", parsedUrl);
        if (parsedUrl.contains("filestore/")) {
            getLogger().info("download redirect");
            if (parsedUrl.contains("/get?") || parsedUrl.contains("/get/") || parsedUrl.endsWith("/get")) {
                parsedUrl = parsedUrl.replace("/get", "/confirm");
            } else if (parsedUrl.matches("^(.+)filestore/(\\d+)$")) {
                parsedUrl = parsedUrl + "/confirm";
            }
            getLogger().info(parsedUrl);
        }

        // ignore AJAX/JSON requests
        if (parsedUrl.contains("/lookup") || parsedUrl.contains("/check")
                || parsedUrl.contains("/bookmark")) {
            return null;
        }

        getLogger().debug("Redirecting to return url: " + parsedUrl);
        return parsedUrl;
    }

    public UserLogin getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(UserLogin userLogin) {
        this.userLogin = userLogin;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    @Override
    public void validate() {
        ErrorTransferObject errors = userLogin.validate(authorizationService, recaptchaService, getServletRequest().getRemoteHost());
        processErrorObject(errors);

        if (errors.isNotEmpty()) {
            getLogger().warn("Returning INPUT because login requested via GET request for user:{}", userLogin.getLoginUsername());
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInternalReturnUrl() {
        return internalReturnUrl;
    }

    public void setInternalReturnUrl(String internalReturnUrl) {
        this.internalReturnUrl = internalReturnUrl;
    }

    @Override
    public boolean isBillingManager() {
        return false;
    }

}

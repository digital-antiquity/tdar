package org.tdar.struts.action.login;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.common.util.UrlUtils;
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
import org.tdar.URLConstants;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.data.UserLogin;
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
public class LoginController extends AuthenticationAware.Base implements Validateable {

    private static final long serialVersionUID = -1219398494032484272L;

    private String url;
    private String internalReturnUrl;
    
    @Autowired
    private RecaptchaService recaptchaService;

    private UserLogin userLogin = new UserLogin(recaptchaService);

    private AntiSpamHelper h = userLogin.getH();

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    @HttpsOnly
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
                    @Result(name = SUCCESS, type = "redirect", location = "/")
            })
    @SkipValidation
    public String logout() {
        if (getSessionData().isAuthenticated()) {
            authenticationService.logout(getSessionData(), getServletRequest(), getServletResponse());
        }
        return SUCCESS;
    }

    @Actions(
    {
            @Action(value = "process",
                    interceptorRefs = { @InterceptorRef("csrfDefaultStack") },
                    results = {
                            @Result(name = TdarActionSupport.NEW, type = REDIRECT, location = "/account/new"),
                            @Result(name = REDIRECT, type = REDIRECT, location = "${internalReturnUrl}"),
                            @Result(name = TdarActionSupport.INPUT, location = "/WEB-INF/content/login.ftl"),
                            @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.DASHBOARD),
                    }),
            @Action(value = "process-cart-login",
                    interceptorRefs = { @InterceptorRef("csrfDefaultStack") },
                    results = {
                            @Result(name = SUCCESS, type = REDIRECT, location = "/cart/choose-billing-account"),
                            @Result(name = REDIRECT, type = HTTPHEADER, params = { "error", BAD_REQUEST, "errorMessage",
                                    "returnUrl not expected for login from cart" }),
                            @Result(name = INPUT,
                                    type = "redirectAction", params = { "actionName", "review", "namespace", "/cart" })
                    })
    })
    @HttpsOnly
    @WriteableSession
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", userLogin.getLoginUsername());

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            status = authenticationService.authenticatePerson(userLogin, getServletRequest(), getServletResponse(),
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

        setInternalReturnUrl(parseReturnUrl());
        if (StringUtils.isNotBlank(getInternalReturnUrl())) {
            getSessionData().clearPassthroughParameters();
            return REDIRECT;
        }
        return SUCCESS;
    }

    private String parseReturnUrl() {
        if ((getSessionData().getReturnUrl() == null) && StringUtils.isEmpty(url)) {
            return null;
        }

        String url_ = getSessionData().getReturnUrl();
        if (StringUtils.isBlank(url_)) {
            url_ = UrlUtils.urlDecode(url);
        }

        getLogger().info("url {} ", url_);
        if (url_.contains("filestore/")) {
            getLogger().info("download redirect");
            if (url_.contains("/get?") || url_.endsWith("/get")) {
                url_ = url_.replace("/get", "/confirm");
            } else if (url_.matches("^(.+)filestore/(\\d+)$")) {
                url_ = url_ + "/confirm";
            }
            getLogger().info(url_);
        }

        // ignore AJAX/JSON requests
        if (url_.contains("/lookup") || url_.contains("/check")
                || url_.contains("/bookmark")) {
            return null;
        }

        getLogger().debug("Redirecting to return url: " + url_);
        return url_;
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
        List<String> validate = userLogin.validate(this, authorizationService);
        addActionErrors(validate);

        if (!isPostRequest() || CollectionUtils.isNotEmpty(validate)) {
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

}

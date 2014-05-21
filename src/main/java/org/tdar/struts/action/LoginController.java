package org.tdar.struts.action;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.common.util.UrlUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService.AuthenticationStatus;
import org.tdar.struts.interceptor.annotation.CacheControl;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

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
        @Result(name = TdarActionSupport.AUTHENTICATED, type = TdarActionSupport.REDIRECT, location = URLConstants.DASHBOARD),
        @Result(name = TdarActionSupport.INPUT, location = "/WEB-INF/content/login.ftl") })
@CacheControl
public class LoginController extends AuthenticationAware.Base {

    private static final long serialVersionUID = -1219398494032484272L;

    private String loginUsername;
    private String loginPassword;
    private Person person;
    private boolean userCookieSet;
    private String url;
    private String comment; // for simple spam protection
    private String returnUrl;

    @Override
    @Actions({
            @Action("/login/")
    })
    @HttpsOnly
    public String execute() {
        getLogger().debug("Executing /login/ .");
        if (isAuthenticated()) {
            getLogger().debug("already authenticated, redirecting to project listing.");
            return AUTHENTICATED;
        }
        getLogger().debug("Not authenticated for some reason: " + getSessionData());
        return SUCCESS;
    }

    @Action(value = "process",
            results = {
                    @Result(name = TdarActionSupport.NEW, type = REDIRECT, location = "/account/new"),
                    @Result(name = REDIRECT, type = REDIRECT, location = "${returnUrl}")
            })
    @HttpsOnly
    @WriteableSession
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", getLoginUsername());
        if (StringUtils.isNotBlank(getComment())) {
            getLogger().debug(String.format("we think this user was a spammer: %s  -- %s", getLoginUsername(), getComment()));
            addActionError("Could not authenticate");
            return INPUT;
        }
        if (!isPostRequest()) {
            getLogger().warn("Returning INPUT because login requested via GET request for user:{}", getLoginUsername());
            return INPUT;
        }

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            status = getAuthenticationAndAuthorizationService().authenticatePerson(loginUsername, loginPassword, getServletRequest(), getServletResponse(),
                    getSessionData());
        } catch (Exception e) {
            addActionError(e.getMessage());
            return INPUT;
        }

        if (status != AuthenticationStatus.AUTHENTICATED) {
            if (status.name() == NEW) {
                addActionMessage("User is in crowd, but not in local db");
            }
            return status.name().toLowerCase();
        }

        setReturnUrl(parseReturnUrl());
        if (StringUtils.isNotBlank(getReturnUrl())) {
            getSessionData().setReturnUrl(getReturnUrl());
            return REDIRECT;
        }
        return AUTHENTICATED;
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

    public String getLoginUsername() {
        return loginUsername;
    }

    // FIXME: messages should be localized
    @RequiredStringValidator(type = ValidatorType.FIELD, message = "Please enter your username.", shortCircuit = true)
    public void setLoginUsername(String username) {
        this.loginUsername = username;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    // FIXME: localize message
    @RequiredStringValidator(type = ValidatorType.FIELD, message = "Please enter your password.")
    public void setLoginPassword(String password) {
        this.loginPassword = password;
    }

    /**
     * Returns true if the user wants to have a cookie with their email set.
     * FIXME: currently not implemented.
     * 
     * @return
     */
    public boolean isUserCookieSet() {
        return userCookieSet;
    }

    public void setUserCookieSet(boolean storeUserCookie) {
        this.userCookieSet = storeUserCookie;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    /**
     * @param returnUrl
     *            the returnUrl to set
     */
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    /**
     * @return the returnUrl
     */
    public String getReturnUrl() {
        return returnUrl;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

}

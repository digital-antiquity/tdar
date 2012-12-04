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
import org.tdar.struts.WriteableSession;

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
        @Result(name = "authenticated", type = "redirect", location = URLConstants.DASHBOARD),
        @Result(name = "input", location = "/WEB-INF/content/login.ftl") })
public class LoginController extends AuthenticationAware.Base {

    private static final long serialVersionUID = -1219398494032484272L;

    private static final String NEW = "new";

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
    public String execute() {
        logger.debug("Executing /login/ .");
        if (isAuthenticated()) {
            logger.debug("already authenticated, redirecting to project listing.");
            return AUTHENTICATED;
        }
        logger.debug("Not authenticated for some reason: " + getSessionData());
        return SUCCESS;
    }

    @Action(value = "process",
            results = {
                    @Result(name = NEW, type = "redirect", location = "/account/new"),
                    @Result(name = REDIRECT, type = "redirect", location = "${returnUrl}")
            })
    @WriteableSession
    public String authenticate() {
        logger.debug("Trying to authenticate username:{}", getLoginUsername());
        if (StringUtils.isNotBlank(getComment())) {
            logger.debug(String.format("we think this user was a spammer: %s  -- %s", getLoginUsername(), getComment()));
            addActionError("Could not authenticate");
            return INPUT;
        }
        if (!isPostRequest()) {
            logger.warn("Returning INPUT because login requested via GET request for user:{}", getLoginUsername());
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
            return status.name().toLowerCase();
        }

        if (getSessionData().getReturnUrl() != null || !StringUtils.isEmpty(url)) {
            logger.info("url {} ", url);
            setReturnUrl(UrlUtils.urlDecode(url));
            setReturnUrl(getSessionData().getReturnUrl());
            if (getReturnUrl().contains("filestore/")) {
                if (getReturnUrl().endsWith("/get")) {
                    setReturnUrl(getReturnUrl().replace("/get", "/confirm"));
                    logger.debug(getReturnUrl());
                } else if (getReturnUrl().matches("^(.+)filestore/(\\d+)$")) {
                    setReturnUrl(getReturnUrl() + "/confirm");
                    logger.debug(getReturnUrl());
                }
                logger.info(getReturnUrl());
            }
            if (getReturnUrl().contains("/lookup") || getReturnUrl().contains("/check") || getReturnUrl().contains("/bookmark")) {
                return AUTHENTICATED;
            }

            logger.debug("Redirecting to return url: " + getReturnUrl());
            return REDIRECT;
        }
        getSessionData().setReturnUrl(null);
        return AUTHENTICATED;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    // FIXME: messages should be localized
    @RequiredStringValidator(type = ValidatorType.FIELD, message = "Please enter your login email.", shortCircuit = true)
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

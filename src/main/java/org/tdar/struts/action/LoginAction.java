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
import org.tdar.core.service.external.auth.AuthenticationResult;
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
public class LoginAction extends AuthenticationAware.Base {

    private static final long serialVersionUID = -1219398494032484272L;

    private String loginEmail;
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
                    @Result(name = "new", type = "redirect", location = "/account/new"),
                    @Result(name = "return", type = "redirect", location = "${returnUrl}")
            })
    @WriteableSession
    public String authenticate() {
        logger.debug("Trying to authenticate.");
        if (StringUtils.isNotBlank(getComment())) {
            logger.debug(String.format("we think this user was a spammer: %s  -- %s", getLoginEmail(), getComment()));
            addActionError("Could not authenticate");
            return INPUT;
        }
        AuthenticationResult result = getAuthenticationAndAuthorizationService().getAuthenticationProvider().authenticate(getServletRequest(),
                getServletResponse(), loginEmail, loginPassword);
        if (result.isValid()) {
            person = getEntityService().findByEmail(loginEmail);
            if (person == null) {
                // FIXME: person exists in Crowd but not in tDAR..
                logger.debug("Person successfully authenticated in crowd but not present in tDAR: " + loginEmail);
                person = new Person();
                person.setEmail(loginEmail);
                person.setPassword(loginPassword);
                // how to pass along authentication information..?
                // username was in Crowd but not in tDAR? Redirect them to the account creation page
                return "new";
            }

            // another way to pass a url manually
            if (!StringUtils.isEmpty(url)) {
                logger.info("url {} ", url);
                setReturnUrl(UrlUtils.urlDecode(url));
            }
            logger.debug(loginEmail.toUpperCase() + " logged in from " + getServletRequest().getRemoteAddr() + " using: "
                    + getServletRequest().getHeader("User-Agent"));
            createAuthenticationToken(person);
            getEntityService().registerLogin(person);
            if (getSessionData().getReturnUrl() != null) {
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

                logger.debug("Redirecting to return url: " + getReturnUrl());
                return "return";
            }
            getSessionData().setReturnUrl(null);
            // FIXME: return SUCCESS instead?
            return AUTHENTICATED;
        } else {
            addActionError(result.getMessage());
            getLogger().debug(String.format("Couldn't authenticate %s - (reason: %s)", loginEmail, result));
            return INPUT;
        }
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    // FIXME: messages should be localized
    @RequiredStringValidator(type = ValidatorType.FIELD, message = "Please enter your login email.", shortCircuit = true)
    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
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

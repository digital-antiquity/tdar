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
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.CrowdService.AuthenticationResult;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
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
        @Result(name = "authenticated", type = "redirect", location = "/project/list"),
        @Result(name = "input", location = "/WEB-INF/content/login.ftl") })
public class LoginAction extends AuthenticationAware.Base {

    private static final long serialVersionUID = -1219398494032484272L;

    private String loginEmail;
    private String loginPassword;
    private Person person;
    private boolean userCookieSet;
    private String url;

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
                    @Result(name = "return", type = "redirect", location = "${sessionData.returnUrl}")
    })
    public String authenticate() {
        logger.debug("Trying to authenticate.");
        AuthenticationResult result = getCrowdService().authenticate(getServletRequest(), getServletResponse(), loginEmail, loginPassword);
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
                getSessionData().setReturnUrl(UrlUtils.urlDecode(url));
            }
            logger.debug(loginEmail.toUpperCase() + " logged in from " + getServletRequest().getRemoteAddr() + " using: "
                    + getServletRequest().getHeader("User-Agent"));
            createAuthenticationToken(person);
            if (getSessionData().getReturnUrl() != null) {
                logger.debug("Redirecting to return url: " + getSessionData().getReturnUrl());
                return "return";
            }
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
    @EmailValidator(type = ValidatorType.FIELD, message = "The email address you entered ( ${loginEmail} )  appears to be an invalid format.",
            shortCircuit = true)
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

}

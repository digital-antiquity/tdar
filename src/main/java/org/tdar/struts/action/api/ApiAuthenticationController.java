package org.tdar.struts.action.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.data.UserLogin;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@ParentPackage("default")
@Namespace("/api")
@Component
@HttpsOnly
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@Scope("prototype")
@HttpForbiddenErrorResponseOnly
public class ApiAuthenticationController extends AuthenticationAware.Base implements Validateable, Preparable {

    private static final long serialVersionUID = -7766515866713996249L;

    private AntiSpamHelper h = new AntiSpamHelper();
    private UserLogin userLogin = new UserLogin(h);
    private Map<String, Object> xmlResultObject = new HashMap<>();

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RecaptchaService recaptchaService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private GenericService genericService;

    @Action(value = "login",
            results = {
                    @Result(name = SUCCESS, type = "xmldocument", params = { "statusCode", "200" }),
                    @Result(name = INPUT, type = "xmldocument", params = { "statusCode", "400" })
            })
    @WriteableSession
    @PostOnly
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", getUserLogin().getLoginUsername());

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            AuthenticationResult result = authenticationService.authenticatePerson(getUserLogin(), getServletRequest(), getServletResponse(), getSessionData());
            status = result.getStatus();
            xmlResultObject.put("username", result.getTokenUsername());
            xmlResultObject.put("apiToken", result.getToken());
            xmlResultObject.put("apiTokenKeyName", getTdarConfiguration().getRequestTokenName());
        } catch (Exception e) {
            addActionError(e.getMessage());
            status = AuthenticationStatus.ERROR;
        }

        xmlResultObject.put("status", status.name());

        switch (status) {
            case ERROR:
            case NEW:
                addActionMessage(getText("loginController.user_not_in_local_db"));
                return INPUT;
            default:
                break;
        }
        return SUCCESS;
    }

    @Action(value = "logout",
            results = {
                @Result(name = SUCCESS, type = "xmldocument", params = { "statusCode", "200" })
            })
    @SkipValidation
    public String logout() {
        String token = getServletRequest().getParameter(TdarConfiguration.getInstance().getRequestTokenName());
        if (getSessionData().isAuthenticated() || authenticationService.checkToken(token, getSessionData(), ServletActionContext.getRequest()).getType().isValid()) {
            authenticationService.logout(getSessionData(), getServletRequest(), getServletResponse());
        }
        xmlResultObject.put("status", "success");
        return SUCCESS;
    }

    @Override
    public void validate() {
        ErrorTransferObject errors = getUserLogin().validate(authorizationService, recaptchaService);
        processErrorObject(errors);

        if (!isPostRequest() || errors.isNotEmpty()) {
            getLogger().warn("Returning INPUT because login requested via GET request for user:{}", getUserLogin().getLoginUsername());
        }
    }

    public UserLogin getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(UserLogin userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public void prepare() throws Exception {

    }

    public Map<String, Object> getXmlResultObject() {
        return xmlResultObject;
    }

    public void setXmlResultObject(Map<String, Object> xmlResultObject) {
        this.xmlResultObject = xmlResultObject;
    }

}

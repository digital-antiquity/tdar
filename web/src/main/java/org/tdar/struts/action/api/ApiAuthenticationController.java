package org.tdar.struts.action.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.jaxb.APIParameters;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@ParentPackage("default")
@Namespace("/api")
@Component
@HttpsOnly
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@Scope("prototype")
@HttpForbiddenErrorResponseOnly
public class ApiAuthenticationController extends AbstractAuthenticatableAction implements Validateable, Preparable, APIParameters {

    private static final long serialVersionUID = -7766515866713996249L;

    private AntiSpamHelper h = new AntiSpamHelper();
    private UserLogin userLogin = new UserLogin(h);
    private Map<String, Object> xmlResultObject = new HashMap<>();

    @Autowired
    private transient AuthenticationService authenticationService;
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient EntityService entityService;

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
            TdarUser user = entityService.findByUsername(getUserLogin().getLoginUsername());
            getLogger().debug("user:{}", user);
            if (!authorizationService.isMember(user, TdarGroup.TDAR_API_USER)) {
                addActionError(getText("apiAuthenticationController.invalid_user"));

            } else {
                AuthenticationResult result = authenticationService.authenticatePerson(getUserLogin(), getServletRequest(), getServletResponse(),
                        getSessionData());
                status = result.getStatus();
                xmlResultObject.put(USERNAME, result.getTokenUsername());
                xmlResultObject.put(API_TOKEN, result.getToken());
                xmlResultObject.put(API_TOKEN_KEY_NAME, getTdarConfiguration().getRequestTokenName());
                xmlResultObject.put(MESSAGE, getText("apiAuthenticationController.tos_reminder", Arrays.asList(getTdarConfiguration().getTosUrl())));
            }
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
            interceptorRefs = { @InterceptorRef("authenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "xmldocument", params = { "statusCode", "200" })
            })
    @PostOnly
    @SkipValidation
    public String logout() {
        String token = getServletRequest().getParameter(TdarConfiguration.getInstance().getRequestTokenName());
        getLogger().debug("{} {} {}", getSessionData().isAuthenticated(), getAuthenticatedUser(), token);
        if (getSessionData().isAuthenticated()
                || authenticationService.checkToken(token, getSessionData(), ServletActionContext.getRequest()).getType().isValid()) {
            authenticationService.logout(getSessionData(), getServletRequest(), getServletResponse(), getAuthenticatedUser());
        }
        xmlResultObject.put("status", "success");
        return SUCCESS;
    }

    @Override
    public void validate() {
        ErrorTransferObject errors = getUserLogin().validate(authorizationService, getServletRequest().getRemoteHost());
        processErrorObject(errors);

        if (errors.isNotEmpty()) {
            getLogger().debug("errors: {}", errors);
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

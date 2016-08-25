package org.tdar.struts.action.resource.request;

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
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;
/**
 * Handle login for new users and pass back to request-access-action
 * @author abrin
 *
 */
@ParentPackage("default")
@Namespace("/resource/request")
@Component
@Scope("prototype")
public class RequestAccessLoginController extends AbstractRequestAccessController implements Validateable, Preparable {

    private static final long serialVersionUID = 1525006233392261028L;

    private UserLogin userLogin = new UserLogin(getH());

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Action(value = "process-request-login",
            interceptorRefs = { @InterceptorRef("registrationStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = SUCCESS_REDIRECT_REQUEST_ACCESS),
                    @Result(name = INPUT, type = FREEMARKER, location = "request-access-unauthenticated.ftl")
            })
    @HttpsOnly
    @WriteableSession
    @PostOnly
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", getUserLogin().getLoginUsername());

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            AuthenticationResult result = authenticationService.authenticatePerson(getUserLogin(), getServletRequest(), getServletResponse(),
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
        return SUCCESS;
    }


    @Override
    public void validate() {
        ErrorTransferObject errors = getUserLogin().validate(authorizationService, getRecaptchaService(), getServletRequest().getRemoteHost());
        processErrorObject(errors);

        if (errors.isNotEmpty()) {
            getLogger().warn("Returning INPUT because login requested via GET request for user:{}", getUserLogin().getLoginUsername());
        }
    }

    public UserLogin getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(UserLogin downloadUserLogin) {
        this.userLogin = downloadUserLogin;
    }

}

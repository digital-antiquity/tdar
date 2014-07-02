package org.tdar.struts.action.download;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Validateable;

@ParentPackage("default")
@Namespace("/filestore")
@Component
@Scope("prototype")
public class DownloadLoginController extends AbstractDownloadController implements Validateable {

    private static final long serialVersionUID = 1525006233392261028L;

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private AuthorizationService authorizationService;

    @Action(value = "process-download-login",
            interceptorRefs = { @InterceptorRef("csrfDefaultStack") },
            results = {
                    @Result(name = SUCCESS, type = REDIRECT, location = "${getDownloadUserLogin().returnUrl}"),
                    @Result(name = INPUT, type = "freemarker", location = "../filestore/download-unauthenticated.ftl"),
            })
    @HttpsOnly
    @WriteableSession
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", getDownloadUserLogin().getLoginUsername());

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            status = authenticationService.authenticatePerson(getDownloadUserLogin(), getServletRequest(), getServletResponse(),
                    getSessionData());
        } catch (Exception e) {
            addActionError(e.getMessage());
            status = AuthenticationStatus.ERROR;
        }

        switch (status) {
            case ERROR:
            case NEW:
                addActionMessage("User is in crowd, but not in local db");
                setupLoginRegistrationBeans();
                return INPUT;
            default:
                break;
        }

        return SUCCESS;
    }

    @Override
    public void validate() {
        List<String> validate = getDownloadUserLogin().validate(this, authorizationService);
        addActionErrors(validate);

        if (!isPostRequest() || CollectionUtils.isNotEmpty(validate)) {
            getLogger().warn("Returning INPUT because login requested via GET request for user:{}", getDownloadUserLogin().getLoginUsername());
        }
    }

}

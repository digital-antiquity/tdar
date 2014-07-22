package org.tdar.struts.action.download;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@ParentPackage("default")
@Namespace("/filestore")
@Component
@Scope("prototype")
public class DownloadLoginController extends AbstractDownloadController implements Validateable, Preparable {

    private static final long serialVersionUID = 1525006233392261028L;

    public static final String SUCCESS_DOWNLOAD_ALL = "success-download-all";

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private GenericService genericService;

    @Action(value = "process-download-login",
            interceptorRefs = { @InterceptorRef("csrfDefaultStack") },
            results = {
            @Result(name = SUCCESS, type = TdarActionSupport.REDIRECT, location = SUCCESS_REDIRECT_DOWNLOAD),
            @Result(name = SUCCESS_DOWNLOAD_ALL, type = TdarActionSupport.REDIRECT, location = DOWNLOAD_ALL_LANDING),
                    @Result(name = INPUT, type = FREEMARKER, location = LOGIN_REGISTER_PROMPT)
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
                addActionMessage(getText("loginController.user_not_in_local_db"));
                return INPUT;
            default:
                break;
        }
        if (Persistable.Base.isNotNullOrTransient(getInformationResource())) {
            return SUCCESS_DOWNLOAD_ALL;
        }
        return SUCCESS;
    }
    
    @Override
    public void prepare() {
        super.prepare();
    };

    @Override
    public void validate() {
        List<String> validate = getDownloadUserLogin().validate(this, authorizationService);
        addActionErrors(validate);

        if (!isPostRequest() || CollectionUtils.isNotEmpty(validate)) {
            getLogger().warn("Returning INPUT because login requested via GET request for user:{}", getDownloadUserLogin().getLoginUsername());
        }
    }

}

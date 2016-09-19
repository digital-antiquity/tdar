package org.tdar.struts.action.download;

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
import org.tdar.core.service.external.auth.DownloadUserRegistration;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@ParentPackage("default")
@Namespace("/filestore")
@Component
@Scope("prototype")
public class DownloadRegistrationController extends AbstractDownloadController implements Validateable, Preparable {

    private static final long serialVersionUID = -893535919691607147L;
    private DownloadUserRegistration downloadRegistration = new DownloadUserRegistration(getH());

    @Autowired
    private AuthenticationService authenticationService;

    @Action(value = "process-download-registration",
             interceptorRefs = { @InterceptorRef("registrationStack") },
            results = {
                    @Result(name = INPUT, location = "../filestore/download-unauthenticated.ftl"),
                    @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = SUCCESS_REDIRECT_DOWNLOAD),
                    @Result(name = SUCCESS_DOWNLOAD_ALL, type = TdarActionSupport.TDAR_REDIRECT, location = DOWNLOAD_ALL_LANDING) })
    @HttpsOnly
    @PostOnly
    @WriteableSession
    @DoNotObfuscate(reason = "getPerson() may have not been set on the session before sent to obfuscator, so don't want to wipe email")
    public String create() {
        if (getDownloadRegistration() == null || getDownloadRegistration().getPerson() == null) {
            return INPUT;
        }
        AuthenticationResult result = null;
        try {
            result = authenticationService.addAndAuthenticateUser(
                    getDownloadRegistration(), getServletRequest(), getServletResponse(), getSessionData());
            if (result.getType().isValid()) {
                getDownloadRegistration().setPerson(result.getPerson());
                addActionMessage(getText("userAccountController.successful_registration_message"));
                if (PersistableUtils.isNullOrTransient(getInformationResourceFileVersionId())) {
                    return SUCCESS_DOWNLOAD_ALL;
                }
                return TdarActionSupport.SUCCESS;
            }
        } catch (Throwable e) {
            addActionError(e.getLocalizedMessage());
        }
        return TdarActionSupport.INPUT;
    }

    @Override
    public void validate() {
        getLogger().debug("validating registration request");
        ErrorTransferObject errors = getDownloadRegistration().validate(authenticationService, getRecaptchaService(), getServletRequest().getRemoteHost());
        processErrorObject(errors);
    }

    @Override
    public void prepare() {
        super.prepare();
    }

    public DownloadUserRegistration getDownloadRegistration() {
        return downloadRegistration;
    }

    public void setDownloadRegistration(DownloadUserRegistration downloadRegistration) {
        this.downloadRegistration = downloadRegistration;
    }

}

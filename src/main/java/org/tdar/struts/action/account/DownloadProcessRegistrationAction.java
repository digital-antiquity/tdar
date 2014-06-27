//package org.tdar.struts.action.account;
//
//import static com.opensymphony.xwork2.Action.*;
//
//import java.util.List;
//
//import org.apache.struts2.convention.annotation.Action;
//import org.apache.struts2.convention.annotation.Namespace;
//import org.apache.struts2.convention.annotation.ParentPackage;
//import org.apache.struts2.convention.annotation.Result;
//import org.apache.struts2.convention.annotation.Results;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//import org.tdar.core.dao.external.auth.AuthenticationResult;
//import org.tdar.core.service.external.AuthenticationService;
//import org.tdar.core.service.external.RecaptchaService;
//import org.tdar.struts.action.AuthenticationAware;
//import org.tdar.struts.action.TdarActionSupport;
//import org.tdar.struts.data.AntiSpamHelper;
//import org.tdar.struts.data.DownloadUserRegistration;
//import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
//import org.tdar.struts.interceptor.annotation.PostOnly;
//import org.tdar.struts.interceptor.annotation.WriteableSession;
//
//import com.opensymphony.xwork2.Preparable;
//import com.opensymphony.xwork2.ValidationAware;
//
///**
// * Process the user registration. This action subclasses AbstractCartAction so that it can
// * gracefully render INPUT result, e.g. continue to show invoice detail, owner, subtotal, etc.
// */
//@Component
//@Scope("prototype")
//@Results({
//        @Result(name = INPUT, location = "../filestore/download-unauthenticated.ftl"),
//        // // no need to take user to billing account selection if we no they don't have one
//        // @Result(name = SUCCESS, location = "/cart/process-payment-request", type = "redirect")
//        // route to the billing account selection page for now, even though user has one choice
//        @Result(name = SUCCESS, type = TdarActionSupport.REDIRECT, location = "${sessionData.returnUrl}")
//})
//@Namespace("/account")
//@ParentPackage("default")
//public class DownloadProcessRegistrationAction extends AuthenticationAware.Base implements Preparable, ValidationAware {
//
//    private static final long serialVersionUID = 3853468651096567739L;
//    // private Long informationResourceFileId;
//    // private Long informationResourceId;
//
//    @Autowired
//    private RecaptchaService recaptchaService;
//
//    @Autowired
//    private AuthenticationService authenticationService;
//
//    private DownloadUserRegistration registrationInfo = new DownloadUserRegistration(recaptchaService);
//    private AntiSpamHelper h = registrationInfo.getH();
//
//    @Override
//    public void validate() {
//        getLogger().debug("validating registration request");
//        List<String> errors = registrationInfo.validate(this, authenticationService);
//        getLogger().debug("found errors {}", errors);
//        addActionErrors(errors);
//    }
//
//    @WriteableSession
//    @DoNotObfuscate(reason = "not needed")
//    @Action("process-download-registration2")
//    @PostOnly
//    public String processRegistration() {
//        AuthenticationResult result = authenticationService.addAndAuthenticateUser(
//                registrationInfo, getServletRequest(), getServletResponse(), getSessionData());
//        if (result.getType().isValid()) {
//            registrationInfo.setPerson(result.getPerson());
//            addActionMessage(getText("userAccountController.successful_registration_message"));
//            return TdarActionSupport.SUCCESS;
//        } else {
//            return TdarActionSupport.INPUT;
//        }
//    }
//
//    @Override
//    public void prepare() {
//
//    }
//
//    public DownloadUserRegistration getRegistrationInfo() {
//        return registrationInfo;
//    }
//
//    /**
//     * convenience getter for view-layer
//     * 
//     * @return
//     */
//    public DownloadUserRegistration getReg() {
//        return registrationInfo;
//    }
//
//    public AntiSpamHelper getH() {
//        return h;
//    }
//
//    public void setH(AntiSpamHelper h) {
//        this.h = h;
//    }
//
//}

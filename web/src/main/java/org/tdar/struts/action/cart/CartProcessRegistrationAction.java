package org.tdar.struts.action.cart;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.auth.CartUserRegistration;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

/**
 * Process the user registration. This action subclasses AbstractCartAction so that it can
 * gracefully render INPUT result, e.g. continue to show invoice detail, owner, subtotal, etc.
 */
@Component
@Scope("prototype")
@Results({
        @Result(name = TdarActionSupport.INPUT, location = "review-unauthenticated.ftl"),
        // route to the billing account selection page for now, even though user has one choice
        @Result(name = TdarActionSupport.SUCCESS, location = URLConstants.CART_REVIEW_PURCHASE, type = TdarActionSupport.TDAR_REDIRECT)
})
@Namespace("/cart")
@ParentPackage("default")
public class CartProcessRegistrationAction extends AbstractCartController {

    private static final long serialVersionUID = -191583172083241851L;

    @Autowired
    private AuthenticationService authenticationService;

    // FIXME: relying on order of initialization, possible bug
    private CartUserRegistration registrationInfo = new CartUserRegistration(getH());

    @Override
    public void validate() {
        if (!validateInvoice()) {
            return;
        }
        getLogger().debug("validating registration request");
        // a new user purchasing space is a de facto contributor, therefore they must accept the contributor agreement
        ErrorTransferObject errors = registrationInfo.validate(authenticationService, recaptchaService, getServletRequest().getRemoteHost());
        processErrorObject(errors);
    }

    @WriteableSession
    @DoNotObfuscate(reason = "not needed")
    @Action(value="process-registration",
            interceptorRefs = { @InterceptorRef("registrationStack")} )
    @PostOnly
    public String processRegistration() {
        getLogger().debug("processing registration for person {} {}", registrationInfo.getPerson(), registrationInfo.isRequestingContributorAccess());
        AuthenticationResult result = authenticationService.addAndAuthenticateUser(
                registrationInfo, getServletRequest(), getServletResponse(), getSessionData());
        if (result.getType().isValid()) {
            registrationInfo.setPerson(result.getPerson());
            addActionMessage(getText("userAccountController.successful_registration_message"));
            return TdarActionSupport.SUCCESS;
        } else {
            return TdarActionSupport.INPUT;
        }
    }

    @Override
    public void prepare() {
        super.prepare();
        // the TOU checkbox counds for both TOU and contributer agreement
        if (registrationInfo.isAcceptTermsOfUse()) {
            registrationInfo.setRequestingContributorAccess(true);
        }

    }

    public UserRegistration getRegistrationInfo() {
        return registrationInfo;
    }

    /**
     * convenience getter for view-layer
     * 
     * @return
     */
    public UserRegistration getReg() {
        return registrationInfo;
    }

}

package org.tdar.struts.action.cart;

import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.EntityService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.auth.UserRegistration;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

/**
 * Process the user registration. This action subclasses AbstractCartAction so that it can
 * gracefully render  INPUT result,  e.g.  continue to show invoice detail, owner, subtotal, etc.
 */
@Component
@Scope("prototype")
@Results({
        @Result(name = INPUT, location = "review.ftl"),
        // no need to take user to billing account selection if we no they don't have one
        @Result(name = SUCCESS, location = "/cart/process-payment-request", type = "redirect")
})
@Namespace("/cart")
@ParentPackage("default")
public class CartProcessRegistrationAction extends AbstractCartController {

    @Autowired
    private EntityService entityService;

    private UserRegistration registrationInfo = new UserRegistration();

    @Override
    public void validate() {
        getLogger().debug("validating registration request");
        List<String> errors = registrationInfo.validate(this, getAuthenticationAndAuthorizationService(), entityService);
        getLogger().debug("found errors {}", errors);
        addActionErrors(errors);
    }

    @WriteableSession
    @DoNotObfuscate(reason = "not needed")
    @Action("process-registration")
    @PostOnly
    public String processRegistration() {
        AuthenticationResult result = getAuthenticationAndAuthorizationService().addAndAuthenticateUser(
                registrationInfo.getPerson(), registrationInfo.getPassword(), registrationInfo.getInstitutionName(),
                getServletRequest(), getServletResponse(), getSessionData(), true);
        if (result.getType().isValid()) {
            registrationInfo.setPerson(result.getPerson());
            addActionMessage(getText("userAccountController.successful_registration_message"));
            return TdarActionSupport.SUCCESS;
        } else {
            return TdarActionSupport.INPUT;
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

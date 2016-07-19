package org.tdar.struts.action.cart;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationService.AuthenticationStatus;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.CacheControl;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

/**
 * $Id$
 * 
 * Handles displaying of the login page and logging in.
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@ParentPackage("default")
@Namespace("/cart")
@Component
@Scope("prototype")
@Results({
        @Result(name = TdarActionSupport.AUTHENTICATED, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.DASHBOARD) })
@CacheControl
public class CartLoginController extends AbstractCartController implements Validateable, Preparable {

    private static final long serialVersionUID = 8641969120632289477L;

    @SuppressWarnings("unused")
    @Autowired
    private RecaptchaService recaptchaService;
    @Autowired
    private AuthenticationService authenticationService;

    private AntiSpamHelper h = new AntiSpamHelper();
    private UserLogin userLogin = new UserLogin(h);

    // FIXME: is this still needed? revisit
    @Action(value = "process-cart-login",
            // interceptorRefs = { @InterceptorRef("csrfDefaultStack") },
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = URLConstants.CART_REVIEW_PURCHASE),
                    @Result(name = INPUT, type = HTTPHEADER, params = { "error", BAD_REQUEST, "errorMessage",
                            "returnUrl not expected for login from cart" }),
                    @Result(name = INPUT, type = FREEMARKER, location = "review-unauthenticated.ftl")
            })
    @HttpsOnly
    @WriteableSession
    @PostOnly
    public String authenticate() {
        getLogger().debug("Trying to authenticate username:{}", getUserLogin().getLoginUsername());

        AuthenticationStatus status = AuthenticationStatus.ERROR;
        try {
            AuthenticationResult result = authenticationService.authenticatePerson(getUserLogin(), getServletRequest(), getServletResponse(), getSessionData());
            status = result.getStatus();
        } catch (Exception e) {
            addActionError(e.getMessage());
            return INPUT;
        }

        switch (status) {
            case ERROR:
            case NEW:
                if (!TdarConfiguration.getInstance().isProductionEnvironment()) {
                    addActionMessage(getText("loginController.user_not_in_local_db"));
                }
                return INPUT;
            default:
                break;
        }

        return SUCCESS;
    }

    @Override
    public void prepare() {
        super.prepare();
        // FIXME: this should not be necessary, but is one of the challenges of returning input and not having the session available anymore
        if (getInvoice() != null) {
            getLogger().debug("items:{} ", getInvoice().getItems());
            getLogger().debug("cost:{}", getInvoice().getCalculatedCost());
        }
    }

    public UserLogin getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(UserLogin userLogin) {
        this.userLogin = userLogin;
    }
}

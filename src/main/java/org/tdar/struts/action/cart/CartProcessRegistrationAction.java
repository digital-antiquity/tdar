package org.tdar.struts.action.cart;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.ValidationAware;
import org.apache.struts2.convention.annotation.*;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.struts.action.AbstractRegistrationController;
import org.tdar.struts.action.UserAccountController;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;

import java.util.Map;

/**
 * Created by jimdevos on 6/11/14.
 */
@Component
@Scope("prototype")
@Results({
        @Result(name=INPUT, location="review.ftl"),

        //no need to take user to billing account selection if we no they don't have one
        @Result(name=SUCCESS, location="/cart/process-payment-request", type="redirect")
})
@Namespace("/cart")
@ParentPackage("default")
public class CartProcessRegistrationAction extends AbstractRegistrationController implements SessionAware, Preparable {

    private Invoice invoice;
    Map<String, Object> session;

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public void prepare() {
        Long invoiceId = (Long) session.get(UnauthenticatedCartController.PENDING_INVOICE_ID_KEY);
        invoice =  getGenericService().find(Invoice.class, invoiceId);
    }

    @Override
    public void validate() {
        getLogger().debug("validate{}", 1);
        super.validate();
    }

    @WriteableSession
    @DoNotObfuscate(reason = "not needed")
    @Action("process-registration")
    public String processRegistration() {
        getPerson().setAffiliation(getAffilliation());
        AuthenticationResult result = getAuthenticationAndAuthorizationService().addAndAuthenticateUser(getPerson(), getPassword(), getInstitutionName(),
                getServletRequest(), getServletResponse(), getSessionData(), true);
        if (result.getType().isValid()) {
            setPerson(result.getPerson());
            addActionMessage(getText("userAccountController.successful_registration_message"));
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public Invoice getInvoice() {
        return invoice;
    }

}

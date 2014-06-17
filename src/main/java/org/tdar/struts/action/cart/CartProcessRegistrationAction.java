package org.tdar.struts.action.cart;

import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.convention.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.service.external.RegistrationControllerService;
import org.tdar.struts.action.auth.RegistrationInfo;
import org.tdar.struts.action.auth.RegistrationInfoProvider;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
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
public class CartProcessRegistrationAction extends AbstractCartController implements RegistrationInfoProvider{

    @Autowired
    private RegistrationControllerService controllerService;

    private RegistrationInfo registrationInfo = new RegistrationInfo();

    @Override
    public void validate() {
        controllerService.validateAction(this);
    }

    @WriteableSession
    @DoNotObfuscate(reason = "not needed")
    @Action("process-registration")
    public String processRegistration() {
        return controllerService.executeAction(this);
    }

    @Override
    public RegistrationInfo getRegistrationInfo() {
        return registrationInfo;
    }

    /**
     * convenience getter for view-layer
     * @return
     */
    public RegistrationInfo getReg() {
        return registrationInfo;
    }
}

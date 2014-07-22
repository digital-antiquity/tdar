package org.tdar.struts.action.cart;

import org.apache.struts2.convention.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.struts.action.TdarActionException;

import java.io.IOException;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import static org.tdar.core.bean.Persistable.Base.isTransient;
import static org.tdar.core.dao.external.auth.InternalTdarRights.EDIT_BILLING_INFO;
import static org.tdar.struts.action.TdarActionSupport.JSONRESULT;

/**
 * Created by jimdevos on 7/22/14.
 */
@Component
@Scope("prototype")
@Namespace("/cart")
@ParentPackage("secured")
public class CartApiPollingAction extends CartApiController {
    private static final long serialVersionUID = 0xDEAD_BEEF;

    @Override
    public void validate() {
        if (isTransient(getAuthenticatedUser())) {
            addActionError("cart.must_be_logged_in");
        }

        if(getInvoice() == null) {
            addActionError("cart.invoice_required");
        }

        if (userCannot(EDIT_BILLING_INFO)) {
            if (!getAuthenticatedUser().equals(getInvoice().getOwner())) {
                addActionError("cart.invoice_lookup_not_authorized");
            }
        }

    }

    @Action("polling-check")
    /**
     * Simply return the invoice associated with the user's session.  The client will continue polling until the invoice status changes from pending_transaction to success (or failed)
     */
    public String pollingCheck() throws TdarActionException, IOException {
        setResultJson(getInvoice());
        return SUCCESS;
    }


}

package org.tdar.struts.action.cart;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.action.TdarActionException;

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
        if (Persistable.Base.isTransient(getAuthenticatedUser())) {
            addActionError(getText("cartApiPollingController.must_be_logged_in"));
        }

        if (getInvoice() == null) {
            addActionError(getText("cartApiPollingController.invoice_required"));
        }

        if (userCannot(InternalTdarRights.EDIT_BILLING_INFO)) {
            if (!getAuthenticatedUser().equals(getInvoice().getOwner())) {
                addActionError(getText("cartApiPollingController.invoice_lookup_not_authorized"));
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

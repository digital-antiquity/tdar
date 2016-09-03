package org.tdar.struts.action.cart;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import static org.tdar.struts_base.action.TdarActionSupport.JSONRESULT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.SerializationService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

/**
 * Created by jimdevos on 7/22/14.
 */
@Component
@Scope("prototype")
@Namespace("/cart")
@ParentPackage("secured")
@Results({
        @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }),
        @Result(name = INPUT, type = JSONRESULT, params = { "streamhttp", "resultJson", "status", "400" }),
        @Result(name = ERROR, type = JSONRESULT, params = { "streamhttp", "resultJson", "status", "500" }),
})
@HttpForbiddenErrorResponseOnly
public class CartApiPollingAction extends AbstractCartController {

    private static final long serialVersionUID = 3205742744549028287L;

    private InputStream resultJson;
    private String callback;

    @Autowired
    private transient SerializationService serializationService;

    @Override
    public void validate() {
        if (PersistableUtils.isTransient(getAuthenticatedUser())) {
            addActionError(getText("cartApiPollingController.must_be_logged_in"));
        }

        if (getInvoice() == null) {
            addActionError(getText("cartApiPollingController.invoice_required"));
        }

        if (userCannot(InternalTdarRights.EDIT_BILLING_INFO)) {
            if (!Objects.equals(getAuthenticatedUser(),getInvoice().getOwner())) {
                addActionError(getText("cartApiPollingController.invoice_lookup_not_authorized"));
            }
        }

    }

    @Action("{id}/polling-check")
    /**
     * Simply return the invoice associated with the user's session.  The client will continue polling until the invoice status changes from pending_transaction to success (or failed)
     */
    @PostOnly
    public String pollingCheck() throws TdarActionException, IOException {
        setResultJson(getInvoice());
        return SUCCESS;
    }

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

    public void setResultJson(Object resultObject) {
        setResultJson(new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(resultObject, JsonLookupFilter.class, getCallback())
                .getBytes()));
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

}

package org.tdar.struts.action.cart;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import static org.tdar.core.bean.Persistable.Base.isTransient;
import static org.tdar.core.dao.external.auth.InternalTdarRights.EDIT_BILLING_INFO;
import static org.tdar.struts.action.TdarActionSupport.JSONRESULT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.InvoiceService;
import org.tdar.core.service.XmlService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.PricingOption;


/**
 * Implementation of the cart-related REST API. These endpoints are primarily used by tDAR's client-side pages,  with the exception of the "
 */
@Component
@Scope("prototype")
@Results({
        @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }),
        @Result(name = INPUT, type = JSONRESULT, params = { "stream", "resultJson", "status", "400" }),
        @Result(name = ERROR, type = JSONRESULT, params = { "stream", "resultJson", "status", "500" }),
})
@Namespace("/cart")
public class CartApiController extends AbstractCartController {

    private static final long serialVersionUID = -1870193105271895297L;
    private Long lookupMBCount = 0L;
    private Long lookupFileCount = 0L;
    private List<PricingOption> pricingOptions = new ArrayList<>();
    private InputStream resultJson;
    private String callback;

    //indicates that validation phase should also verify that action is authorized
    private boolean authorizationRequired = false;

    @Autowired
    XmlService xmlService;

    @Autowired
    private transient InvoiceService cartService;

    @Override
    public void prepare() {
        super.prepare();
    }


    @Override
    public void validate() {
        super.validate();

        // this is roughly equivalent to implementing isEditable() and making the following checkValidRequest call
        // checkValidRequest(AbstractPersistableController.RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        //fixme: consider breaking out polling-check into it's own action class
        //authorized actions require additional validation steps
        if(authorizationRequired) {
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

    }

    /**
     * calculate estimated price when user specifies custom files/mb
     * @return
     */
    @Action("api")
    public String api() {
        if (isNotNullOrZero(lookupFileCount) || isNotNullOrZero(lookupMBCount)) {
            addPricingOption(cartService.getCheapestActivityByFiles(lookupFileCount, lookupMBCount, false));
            addPricingOption(cartService.getCheapestActivityByFiles(lookupFileCount, lookupMBCount, true));
            addPricingOption(cartService.getCheapestActivityBySpace(lookupFileCount, lookupMBCount));
        }
        setResultJson(getPricingOptions());
        return SUCCESS;
    }


    @Action("polling-check")
    public String pollingCheck() throws TdarActionException, IOException {
        setResultJson(getInvoice());
        return SUCCESS;
    }


    void addPricingOption(PricingOption incoming) {
        if (incoming == null) {
            return;
        }
        boolean add = true;

        for (PricingOption option : pricingOptions) {
            if ((option == null) || option.sameAs(incoming)) {
                add = false;
            }
        }
        if (add) {
            pricingOptions.add(incoming);
        }
    }

    boolean isNotNullOrZero(Long num) {
        if ((num == null) || (num < 1)) {
            return false;
        }
        return true;
    }

    public Long getLookupMBCount() {
        return lookupMBCount;
    }

    public void setLookupMBCount(Long lookupMBCount) {
        this.lookupMBCount = lookupMBCount;
    }

    public Long getLookupFileCount() {
        return lookupFileCount;
    }

    public void setLookupFileCount(Long lookupFileCount) {
        this.lookupFileCount = lookupFileCount;
    }

    public List<PricingOption> getPricingOptions() {
        return pricingOptions;
    }

    public void setPricingOptions(List<PricingOption> pricingOptions) {
        this.pricingOptions = pricingOptions;
    }

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

    public void setResultJson(Object resultObject) {
        setResultJson(new ByteArrayInputStream(xmlService.convertFilteredJsonForStream(resultObject, null, getCallback()).getBytes()   ));
    }


    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}

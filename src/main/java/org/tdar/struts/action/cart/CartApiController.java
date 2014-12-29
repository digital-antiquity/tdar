package org.tdar.struts.action.cart;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import static org.tdar.struts.action.TdarActionSupport.JSONRESULT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.InvoiceService;
import org.tdar.core.service.billing.PricingOption;

import com.opensymphony.xwork2.Preparable;

/**
 * Implementation of the cart-related REST API. These endpoints are primarily used by tDAR's client-side pages, with the exception of the "
 */
@Component
@Scope("prototype")
@Results({
        @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }),
        @Result(name = INPUT, type = JSONRESULT, params = { "streamhttp", "resultJson", "status", "400" }),
        @Result(name = ERROR, type = JSONRESULT, params = { "streamhttp", "resultJson", "status", "500" }),
})
@ParentPackage("default")
@Namespace("/cart")
public class CartApiController extends AbstractCartController implements Preparable {

    private static final long serialVersionUID = -1870193105271895297L;
    private Long lookupMBCount = 0L;
    private Long lookupFileCount = 0L;
    private List<PricingOption> pricingOptions = new ArrayList<>();
    private InputStream resultJson;
    private String callback;

    @Autowired
    SerializationService serializationService;

    @Autowired
    private transient InvoiceService invoiceService;

    /**
     * calculate estimated price when user specifies custom files/mb
     * 
     * @return
     */
    @Action("api")
    public String api() {
        if (isNotNullOrZero(lookupFileCount) || isNotNullOrZero(lookupMBCount)) {
            addPricingOption(invoiceService.getCheapestActivityByFiles(lookupFileCount, lookupMBCount, false));
            addPricingOption(invoiceService.getCheapestActivityByFiles(lookupFileCount, lookupMBCount, true));
            addPricingOption(invoiceService.getCheapestActivityBySpace(lookupFileCount, lookupMBCount));
        }
        setResultJson(getPricingOptions());
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
        setResultJson(new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(resultObject, null, getCallback()).getBytes()));
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}

package org.tdar.struts.action.cart;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ParameterAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.dao.external.payment.nelnet.TransactionResponse;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.billing.AccountService;
import org.tdar.core.service.billing.InvoiceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

/**
 * Created by jimdevos on 7/7/14.
 */
@Component
@Scope("prototype")
@Namespace("/cart")
// @HttpsOnly
public class CartExternalPaymentResponseAction extends AuthenticationAware.Base implements Preparable, ParameterAware {

    private static final long serialVersionUID = 5114065112304206226L;
    public static final String PROCESS_EXTERNAL_PAYMENT_RESPONSE = "process-external-payment-response";
    public static final String NELNET_RESPONSE_SUCCESS = "success";
    public static final String NELNET_RESPONSE_FAILURE = "failure";

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient AccountService cartService;

    @Autowired
    private transient InvoiceService invoiceService;

    private Invoice invoice;
    private TransactionResponse response;

    // nelnet can send us a whole slew of name/value pairs. It would be too cumbersome to write controller getter/setter methods so we just stuff them here
    private Map<String, String[]> extraParameters;

    @Override
    public void prepare() {
        response = paymentTransactionProcessor.setupTransactionResponse(extraParameters);
        invoice = paymentTransactionProcessor.locateInvoice(response);
    }

    @Override
    public void validate() {
        // TODO:check to see if invoice is modifiable
        if (invoice == null) {
            addActionError(getText("cartExternalPaymentResponseAction.invoice_not_found"));
        }
    }

    @WriteableSession
    @PostOnly
    @Action(value = PROCESS_EXTERNAL_PAYMENT_RESPONSE, results = {
            @Result(name = SUCCESS, type = "stream", params = { "contentType", "text/text", "inputName", "inputStream" }),
            @Result(name = INPUT, type = "streamhttp", params = { "contentType", "text/text", "inputName", "errorInputStream", "status", "400" }),
            // fixme: jtd I'm not sure if the following mapping is safe. Can an exception occur when the action object is undefined? And if so, will struts
            // still attempt to populate the result with action object properties?
            @Result(name = "exception", type = "streamhttp", params = { "contentType", "text/text", "inputName", "errorInputStream", "status", "500" }),
            @Result(name = ERROR, type = "streamhttp", params = { "contentType", "text/text", "inputName", "errorInputStream", "status", "400" })
    })
    public String processExternalPayment() {
        getLogger().trace("PROCESS RESPONSE {}", extraParameters);

        try {
            invoiceService.processTransactionResponse(response, paymentTransactionProcessor);
        } catch (IOException | TdarRecoverableRuntimeException e) {
            getLogger().error("IO error occured when processing nelnet response", e);
            addActionError(e.getMessage());
            return ERROR;
        }
        return SUCCESS;
    }


    public InputStream getInputStream() {
        return toStream(NELNET_RESPONSE_SUCCESS);
    }

    public InputStream getErrorInputStream() {
        return toStream(NELNET_RESPONSE_FAILURE);
    }

    private InputStream toStream(String str) {
        // Assumpution: nelnet docs do not specify a charset, however, "success" and "failure" have same bytes in utf7, utf8, and western-iso-8859
        return new ReaderInputStream(new StringReader(str), "utf-8");
    }

    @Override
    public void setParameters(Map<String, String[]> parameters) {
        this.extraParameters = parameters;
    }

}

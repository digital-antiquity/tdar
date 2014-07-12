package org.tdar.struts.action.cart;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ParameterAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.dao.external.payment.nelnet.TransactionResponse;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.InvoiceService;
import org.tdar.core.service.UserNotificationService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

/**
 * Created by jimdevos on 7/7/14.
 */
@Component
@Scope("prototype")
@Namespace("/cart")
@HttpsOnly
public class CartExternalPaymentResponseAction extends AuthenticationAware.Base implements Preparable, ParameterAware {
    private static final long serialVersionUID = 0xDEADBEEF;

    private static final String PROCESS_EXTERNAL_PAYMENT_RESPONSE = "process-external-payment-response";

    @Autowired
    UserNotificationService notificationService;

    @Autowired
    PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient InvoiceService cartService;

    private Invoice invoice;
    private InputStream inputStream;
    private TransactionResponse response;

    //nelnet can send us a whole slew of name/value pairs. It would be too cumbersome to write controller getter/setter methods so we just stuff them here
    private Map<String, String[]> extraParameters;

    @Override
    public void prepare() {
        response = paymentTransactionProcessor.setupTransactionResponse(extraParameters);
        invoice = paymentTransactionProcessor.locateInvoice(response);
    }


    @Override
    public void validate() {
        //TODO:check to see if invoice is modifiable
    }

    @WriteableSession
    @PostOnly
    @Action(value  = PROCESS_EXTERNAL_PAYMENT_RESPONSE, results = {
            @Result(name = "success", type = "stream", params = { "contentType", "text/text", "inputName", "inputStream" }),
            @Result(name = "error", type = "stream", params = { "contentType", "text/text", "inputName", "inputStream" })
    })
    public String processExternalPayment() {
        getLogger().trace("PROCESS RESPONSE {}", extraParameters);

        try {
            cartService.processTransactionResponse(response, paymentTransactionProcessor);
        } catch (IOException|TdarRecoverableRuntimeException e) {
            getLogger().error("IO error occured when processing nelnet response", e);
            addActionError(e.getMessage());
            inputStream = new ByteArrayInputStream(ERROR.getBytes());
            return ERROR;
        }
        inputStream = new ByteArrayInputStream("success".getBytes());
        handlePurchaseNotifications();
        return SUCCESS;
    }

    /**
     * Send notifications to a user following a successful transaction
     */
    private void handlePurchaseNotifications() {
        //for now, we only care about sending notification if the transaction was successful
        if(invoice.getTransactionStatus() != Invoice.TransactionStatus.TRANSACTION_SUCCESSFUL) {
            getLogger().info("invoice transaction not successful:{}", invoice);
            return;
        }

        //at the very least, send invoice notification
        TdarUser recipient = invoice.getOwner();

        String notificationKey = "cart.new_invoice_notification";
        getLogger().info("sending notification:{} to:{}", notificationKey, recipient);

        notificationService.info(recipient, notificationKey);

        //if user recently became a contributor by way of this invoice, send an additional notification
        //todo: how to figure this out?  user has only one account and account only has this invoice?
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void setParameters(Map<String, String[]> parameters) {
        this.extraParameters = parameters;
    }


}

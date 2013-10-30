package org.tdar.core.dao.external.payment.nelnet;

import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.tdar.core.bean.billing.Invoice;

public interface PaymentTransactionProcessor {

    abstract void initializeTransaction();

    abstract String getTransactionPostUrl();

    abstract String prepareRequest(Invoice invoice) throws URIException;

    abstract NelNetTransactionResponseTemplate processResponse(Map<String, String[]> parameters);

    abstract boolean validateResponse(TransactionResponse response);

    abstract Invoice locateInvoice(TransactionResponse response);

    abstract void updateInvoiceFromResponse(TransactionResponse response, Invoice invoice);

    abstract TransactionResponse setupTransactionResponse(Map<String, String[]> map);

}
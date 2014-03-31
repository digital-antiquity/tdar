package org.tdar.core.dao.external.payment.nelnet;

import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.tdar.core.bean.billing.Invoice;

public interface PaymentTransactionProcessor {

    void initializeTransaction();

    String getTransactionPostUrl();

    String prepareRequest(Invoice invoice) throws URIException;

    NelNetTransactionResponseTemplate processResponse(Map<String, String[]> parameters);

    boolean validateResponse(TransactionResponse response);

    Invoice locateInvoice(TransactionResponse response);

    void updateInvoiceFromResponse(TransactionResponse response, Invoice invoice);

    TransactionResponse setupTransactionResponse(Map<String, String[]> map);

}
package org.tdar.core.dao.external.payment.nelnet;

import java.net.URL;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.tdar.core.bean.billing.Invoice;

public interface PaymentTransactionProcessor {

    void initializeTransaction();

    String getTransactionPostUrl();

    @Deprecated
    String prepareRequest(Invoice invoice) throws URIException;

    NelNetTransactionResponseTemplate processResponse(Map<String, String[]> parameters);

    boolean validateResponse(TransactionResponse response);

    Invoice locateInvoice(TransactionResponse response);

    void updateInvoiceFromResponse(TransactionResponse response, Invoice invoice);

    TransactionResponse setupTransactionResponse(Map<String, String[]> map);

    URL buildPostUrl(Invoice invoice);

}
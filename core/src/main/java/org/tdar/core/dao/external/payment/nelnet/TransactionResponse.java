package org.tdar.core.dao.external.payment.nelnet;

import java.util.Map;

import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Address;

public interface TransactionResponse {

    Map<String, String[]> getValues();

    Long getInvoiceId();

    Address getAddress();

    void updateInvoiceFromResponse(Invoice invoice);

    boolean validate();

    String getTransactionId();

    String getValuesFor(String key);

    boolean isRefund();

}

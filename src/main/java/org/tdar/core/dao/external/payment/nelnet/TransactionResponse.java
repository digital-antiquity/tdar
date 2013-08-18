package org.tdar.core.dao.external.payment.nelnet;

import java.util.Map;

import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Address;

public interface TransactionResponse {

    public Map<String, String[]> getValues();

    public void setValues(Map<String, String[]> values);

    public Long getInvoiceId();

    public Address getAddress();

    public void updateInvoiceFromResponse(Invoice invoice);

    public boolean validate();

    public String getTransactionId();

    String getValuesFor(String key);

}

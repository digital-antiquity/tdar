package org.tdar.core.dao.external.payment.nelnet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.external.payment.PaymentMethod;

@Service
public class NelNetPaymentDao {

    @Autowired
    GenericDao genericDao;
    
    public void initializeTransaction() {

    }

    public String getSecretWord() {
        return "NOT_IMPLEMENTED";
    }

    public String getCheckoutButtonLabel() {
        return "PAY NOW";
    }

    public boolean isTestingMode() {
        return true;
    }


    public String getTransactionReturnUrl() {
        return "";
    }
    
    public String getTransactionPostUrl() {
        return "https://quikpayasp.com/asu/commerce_manager/payer.do";
    }
    
    public List<PaymentMethod> getSupportedPaymentMethods() {
        return Arrays.asList(PaymentMethod.CREDIT_CARD);
    }

    public String prepareRequest(Invoice invoice) throws URIException {
        genericDao.saveOrUpdate(invoice);
        genericDao.markReadOnly(invoice);
        NelNetTransactionRequestTemplate template = new NelNetTransactionRequestTemplate();
        template.populateHashMapFromInvoice(invoice);
        template.constructHashKey();
        String urlSuffix = template.constructUrlSuffix();
        return getTransactionPostUrl() + "?" + urlSuffix;
    }

    public TransactionStatus processResponse(Invoice invoice, Map<String, String[]> parameters) {
        NelNetTransactionResponseTemplate response = new NelNetTransactionResponseTemplate();
        response.setValues(parameters);
        response.validateHashKey();
        response.updateInvoiceFromResponse(invoice);
        return response.getTransactionStatus();
    }
}

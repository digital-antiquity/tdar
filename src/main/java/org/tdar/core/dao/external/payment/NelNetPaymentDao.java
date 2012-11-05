package org.tdar.core.dao.external.payment;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.dao.GenericDao;

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

    public void processInvoice(Invoice invoice) {
        genericDao.saveOrUpdate(invoice);
        genericDao.markReadOnly(invoice);
        
        
    }
}

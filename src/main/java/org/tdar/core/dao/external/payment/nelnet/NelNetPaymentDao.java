package org.tdar.core.dao.external.payment.nelnet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.external.payment.PaymentMethod;

import freemarker.core.Configurable;

@Service
public class NelNetPaymentDao extends Configurable {

    @Autowired
    GenericDao genericDao;

    private ConfigurationAssistant assistant = new ConfigurationAssistant();
    public final Logger logger = LoggerFactory.getLogger(getClass());

    private String configIssue = "";

    public void initializeTransaction() {

    }

    public NelNetPaymentDao() {
        try {
            assistant.loadProperties("nelnet.properties");
        } catch (Throwable t) {
            configIssue  = t.getMessage();
        }
    }

    public boolean isConfigured() {
        if (StringUtils.isNotBlank(getSecretWord()) && StringUtils.isNotBlank(getTransactionPostUrl()) ) {
            return true;
        }
        logger.debug("a required parameter for the EzidDao was not provided. " + configIssue);
        return false;
    }

    public String getSecretWord() {
        return assistant.getStringProperty("secret.word");
    }

    public String getTransactionPostUrl() {
        return assistant.getStringProperty("post.url");
    }
    public String getOrderType() {
        return assistant.getStringProperty("order.type");
    }
    
    public List<PaymentMethod> getSupportedPaymentMethods() {
        return Arrays.asList(PaymentMethod.CREDIT_CARD);
    }

    public String prepareRequest(Invoice invoice) throws URIException {
        genericDao.saveOrUpdate(invoice);
        genericDao.markReadOnly(invoice);
        NelNetTransactionRequestTemplate template = new NelNetTransactionRequestTemplate(getOrderType(), getSecretWord());
        template.populateHashMapFromInvoice(invoice);
        template.constructHashKey();
        String urlSuffix = template.constructUrlSuffix();
        return getTransactionPostUrl() + "?" + urlSuffix;
    }

    public TransactionStatus processResponse(Invoice invoice, Map<String, String[]> parameters) {
        logger.info("parameters: {}  ", parameters);
        NelNetTransactionResponseTemplate response = new NelNetTransactionResponseTemplate();
        response.setValues(parameters);
        response.validateHashKey();
        response.updateInvoiceFromResponse(invoice);
        return response.getTransactionStatus();
    }
}

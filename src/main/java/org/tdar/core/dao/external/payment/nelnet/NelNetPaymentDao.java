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
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.external.payment.PaymentMethod;

import freemarker.core.Configurable;

@Service
public class NelNetPaymentDao extends Configurable implements PaymentTransactionProcessor {

    @Autowired
    GenericDao genericDao;

    private ConfigurationAssistant assistant = new ConfigurationAssistant();
    public final Logger logger = LoggerFactory.getLogger(getClass());

    private String configIssue = "";

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.external.payment.nelnet.TransactionProcessor#initializeTransaction()
     */
    @Override
    public void initializeTransaction() {

    }

    public NelNetPaymentDao() {
        try {
            assistant.loadProperties("nelnet.properties");
        } catch (Throwable t) {
            configIssue = t.getMessage();
        }
    }

    public boolean isConfigured() {
        if (StringUtils.isNotBlank(getSecretRequestWord()) && StringUtils.isNotBlank(getSecretResponseWord()) && StringUtils.isNotBlank(getTransactionPostUrl())) {
            return true;
        }
        logger.debug("a required parameter for the EzidDao was not provided. " + configIssue);
        return false;
    }

    public String getSecretRequestWord() {
        return assistant.getStringProperty("secret.word.passthrough");
    }

    public String getSecretResponseWord() {
        return assistant.getStringProperty("secret.word.real.time.notification");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.external.payment.nelnet.TransactionProcessor#getTransactionPostUrl()
     */
    @Override
    public String getTransactionPostUrl() {
        return assistant.getStringProperty("post.url");
    }

    public String getOrderType() {
        return assistant.getStringProperty("order.type");
    }

    public List<PaymentMethod> getSupportedPaymentMethods() {
        return Arrays.asList(PaymentMethod.CREDIT_CARD);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.external.payment.nelnet.TransactionProcessor#prepareRequest(org.tdar.core.bean.billing.Invoice)
     */
    @Override
    public String prepareRequest(Invoice invoice) throws URIException {
        genericDao.saveOrUpdate(invoice);
        genericDao.markReadOnly(invoice);
        NelNetTransactionRequestTemplate template = new NelNetTransactionRequestTemplate(getOrderType(), getSecretRequestWord());
        template.populateHashMapFromInvoice(invoice);
        template.constructHashKey();
        String urlSuffix = template.constructUrlSuffix();
        return getTransactionPostUrl() + "?" + urlSuffix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.external.payment.nelnet.TransactionProcessor#processResponse(java.util.Map)
     */
    @Override
    public NelNetTransactionResponseTemplate processResponse(Map<String, String[]> parameters) {
        logger.info("parameters: {}  ", parameters);
        NelNetTransactionResponseTemplate response = new NelNetTransactionResponseTemplate(getSecretResponseWord());
        response.setValues(parameters);

        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.dao.external.payment.nelnet.TransactionProcessor#validateResponse(org.tdar.core.dao.external.payment.nelnet.NelNetTransactionResponseTemplate
     * )
     */
    @Override
    public boolean validateResponse(TransactionResponse response) {
        return response.validate(); // I throw an exception if not working
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.dao.external.payment.nelnet.TransactionProcessor#locateInvoice(org.tdar.core.dao.external.payment.nelnet.NelNetTransactionResponseTemplate)
     */
    @Override
    public Invoice locateInvoice(TransactionResponse response) {
        // Long personId = Long.valueOf(response.getValuesFor(NelnetTransactionItem.USER_CHOICE_2.getUserIdKey()));
        Long invoiceId = response.getInvoiceId();
        Invoice invoice = genericDao.find(Invoice.class, invoiceId);
        return invoice;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.external.payment.nelnet.TransactionProcessor#updateInvoiceFromResponse(org.tdar.core.dao.external.payment.nelnet.
     * NelNetTransactionResponseTemplate, org.tdar.core.bean.billing.Invoice)
     */
    @Override
    public void updateInvoiceFromResponse(TransactionResponse response, Invoice invoice) {
        response.updateInvoiceFromResponse(invoice);
        genericDao.saveOrUpdate(invoice);
    }

    public TransactionResponse setupTransactionResponse(Map<String, String[]> values) {
        NelNetTransactionResponseTemplate response = new NelNetTransactionResponseTemplate(getSecretResponseWord());
        response.setValues(values);
        return response;
    }
}
package org.tdar.core.dao.external.payment.nelnet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.external.payment.PaymentMethod;

@Service
public class NelNetPaymentDao implements PaymentTransactionProcessor {

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
        if (StringUtils.isNotBlank(getSecretRequestWord()) && StringUtils.isNotBlank(getSecretResponseWord())
                && StringUtils.isNotBlank(getTransactionPostUrl())) {
            return true;
        }
        logger.debug("a required parameter for the NelNetDao was not provided. " + configIssue);
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

    /**
     * return a url to the payment processor the client will use to complete payment for the invoice associated with this template
     * 
     * @return a valid url, or null if the template was in a state such that it produced a malformed url.
     */
    @Override
    public URL buildPostUrl(Invoice invoice) {
        URL url = null;
        try {
            NelNetTransactionRequestTemplate template = new NelNetTransactionRequestTemplate(getOrderType(), getSecretRequestWord());
            template.populateHashMapFromInvoice(invoice);
            template.constructHashKey();
            // NOTE: in knap and prior this was 'ASCII'; if we ever put true unicode characters into the request we may need to check for
            // encoding issues with passing this data into the URL
            // FIXME: this is cleaner, but doesn't produce an XHTML friendly url with &amp; encoded urls, so it fails tests
            String query = "?" + URLEncodedUtils.format(template.getNameValuePairs(), Consts.UTF_8);

            url = new URL(getTransactionPostUrl() + query);
        } catch (MalformedURLException e) {
            logger.error("malformed payment url", e);
        }
        return url;
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

    @Override
    public TransactionResponse setupTransactionResponse(Map<String, String[]> values) {
        NelNetTransactionResponseTemplate response = new NelNetTransactionResponseTemplate(getSecretResponseWord(), values);
        return response;
    }
}

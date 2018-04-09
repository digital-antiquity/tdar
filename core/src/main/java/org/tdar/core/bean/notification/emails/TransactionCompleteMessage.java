package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

public class TransactionCompleteMessage extends Email {

    
    /**
     * 
     */
    private static final long serialVersionUID = 5730550728455142447L;

    @Override
    public String createSubjectLine(){
        Invoice invoice = (Invoice) getMap().get("invoice");
        return MessageHelper.getMessage(EmailType.TRANSACTION_COMPLETE_ADMIN.getLocaleKey(), Arrays.asList(invoice.getId(), invoice.getTransactedBy().getProperName()));
    }
    
}

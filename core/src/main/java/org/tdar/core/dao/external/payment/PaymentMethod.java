package org.tdar.core.dao.external.payment;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * 
 * Payment method used to settle the account balance on a given Invoice.
 * 
 * The INVOICE type is used to denote that the Invoice will be paid by manual invoice or purchase order, outside of Nelnet CC processing.
 * 
 */
public enum PaymentMethod implements HasLabel, Localizable {
    CREDIT_CARD("Credit Card"),
    CHECK("Check"),
    INVOICE("Invoice / Customer Work Order"),
    MANUAL("Manual");

    private String label;

    private PaymentMethod(String label) {
        this.setLabel(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    private void setLabel(String label) {
        this.label = label;
    }

    public boolean isCreditCard() {
        return this == CREDIT_CARD;
    }
}

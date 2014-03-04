package org.tdar.core.dao.external.payment;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

public enum PaymentMethod implements HasLabel, Localizable {
    CREDIT_CARD("Credit Card"),
    CHECK("Check"),
    INVOICE("Invoice"),
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
}

package org.tdar.core.dao.external.payment;

import org.tdar.core.bean.HasLabel;

public enum PaymentMethod implements HasLabel {
    CREDIT_CARD("Credit Card"),
    CHECK("Check"),
    INVOICE("Invoice"),
    MANUAL("Manual");

    private String label;

    private PaymentMethod(String label) {
        this.setLabel(label);
    }

    public String getLabel() {
        return label;
    }

    private void setLabel(String label) {
        this.label = label;
    }
}

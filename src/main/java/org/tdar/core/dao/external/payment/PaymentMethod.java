package org.tdar.core.dao.external.payment;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum PaymentMethod implements HasLabel {
    CREDIT_CARD(MessageHelper.getMessage("paymentMethod.credit_card")),
    CHECK(MessageHelper.getMessage("paymentMethod.check")),
    INVOICE(MessageHelper.getMessage("paymentMethod.invoice")),
    MANUAL(MessageHelper.getMessage("paymentMethod.manual"));

    private String label;

    private PaymentMethod(String label) {
        this.setLabel(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    private void setLabel(String label) {
        this.label = label;
    }
}

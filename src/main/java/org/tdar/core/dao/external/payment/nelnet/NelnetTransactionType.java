package org.tdar.core.dao.external.payment.nelnet;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.dao.external.payment.PaymentMethod;

public enum NelnetTransactionType implements HasLabel {

    CREDIT_CARD("Credit Card", 1, true),
    CREDIT_CARD_REFUND("Credit Card Refund", 2, false),
    CHECK("eCheck Payment", 3, true);

    private String label;
    private int ordinalValue;
    private boolean visible;

    private NelnetTransactionType(String label, int ordinalValue, boolean visible) {
        this.setOrdinalValue(ordinalValue);
        this.setLabel(label);
        this.setVisible(visible);
    }

    public int getOrdinalValue() {
        return ordinalValue;
    }

    private void setOrdinalValue(int ordinalValue) {
        this.ordinalValue = ordinalValue;
    }

    public String getLabel() {
        return label;
    }

    private void setLabel(String label) {
        this.label = label;
    }

    public boolean isVisible() {
        return visible;
    }

    private void setVisible(boolean visible) {
        this.visible = visible;
    }

    public static NelnetTransactionType fromOrdinal(int ord) {
        for (NelnetTransactionType type : values()) {
            if (type.getOrdinalValue() == ord) {
                return type;
            }
        }
        return null;
    }

    public static PaymentMethod fromOrdinalToPaymentMethod(int intValue) {
        NelnetTransactionType type = fromOrdinal(intValue);
        switch (type) {
            case CHECK:
                return PaymentMethod.CHECK;
            case CREDIT_CARD:
            case CREDIT_CARD_REFUND:
                return PaymentMethod.CREDIT_CARD;
        }
        return null;
    }
}

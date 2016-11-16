package org.tdar.core.dao.external.payment.nelnet;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.utils.MessageHelper;

public enum NelnetTransactionType implements HasLabel, Localizable {

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

    /**
     * @return payment method that corresponds to this transaction type
     */
    public PaymentMethod getPaymentMethod() {
        // note: we currently only ahve two payment methods. Go back to using switch-statement if we add more.
        return this == CHECK ? PaymentMethod.CHECK : PaymentMethod.CREDIT_CARD;
    }
}

package org.tdar.core.bean.entity;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * Enum for Type of Address
 * 
 * @author abrin
 * 
 */
public enum AddressType implements HasLabel, Localizable {
    MAILING("Mailing Address"), BILLING("Billing Address"), OTHER("Other");

    private String label;

    private AddressType(String label) {
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

    public void setLabel(String label) {
        this.label = label;
    }

}

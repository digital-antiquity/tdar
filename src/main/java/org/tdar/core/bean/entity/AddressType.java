package org.tdar.core.bean.entity;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum AddressType implements HasLabel {
    MAILING(MessageHelper.getMessage("addressType.mailing")),
    BILLING(MessageHelper.getMessage("addressType.billing")),
    OTHER(MessageHelper.getMessage("addressType.other"));

    private String label;

    private AddressType(String label) {
        this.setLabel(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}

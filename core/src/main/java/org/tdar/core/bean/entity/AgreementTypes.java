package org.tdar.core.bean.entity;

import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

public enum AgreementTypes implements Localizable {
    USER_AGREEMENT,
    CONTRIBUTOR_AGREEMENT;

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

}

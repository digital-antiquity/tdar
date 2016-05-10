package org.tdar.core.bean.collection;

import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.utils.MessageHelper;

public enum CollectionType implements Localizable, PluralLocalizable {
    INTERNAL("Internal"), SHARED("Shared"), PUBLIC("Public");

    private String label;

    private CollectionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public String getPluralLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatPluralLocalizableKey(this);
    }
}

package org.tdar.core.bean.collection;

import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.utils.MessageHelper;

public enum CollectionType implements Localizable, PluralLocalizable {
    //RENAME Managed / Unmanaged
     SHARED("Shared"), LIST("Public");

    private String label;

    private CollectionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public static <C extends ResourceCollection> CollectionType getTypeForClass(Class<C> cls) {
        if (cls.isAssignableFrom(ResourceCollection.class)) {
            return SHARED;
        }
        return null;
    }

    @Override
    public String getPluralLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatPluralLocalizableKey(this);
    }

    public Class<? extends ResourceCollection> getClassForType() {
        switch (this) {
            case SHARED:
                return ResourceCollection.class;
        }
        return null;
    }

    public String getUrlNamespace() {
        switch (this) {
            case LIST:
                return "listcollection";
            case SHARED:
                return "collection";
            default:
                return "invalid";
        }
    }
}
